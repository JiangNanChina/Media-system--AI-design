package com.example.photography.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UploadArchiveValidatorTest {
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "mov", "webm");

    @Test
    void acceptsSupportedFilesInNestedFolders() throws Exception {
        MockMultipartFile archive = archive("folder/raw/clip.mp4", mp4Header(), "folder/final.webm", webmHeader());

        assertDoesNotThrow(() -> UploadArchiveValidator.validate(archive, VIDEO_EXTENSIONS, 1024));
    }

    @Test
    void rejectsPathTraversal() throws Exception {
        MockMultipartFile archive = archive("../outside.mp4", mp4Header());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> UploadArchiveValidator.validate(archive, VIDEO_EXTENSIONS, 1024)
        );

        assertEquals("压缩包中包含非法文件路径：../outside.mp4", error.getMessage());
    }

    @Test
    void rejectsUnsupportedEntryAndMismatchedContent() throws Exception {
        MockMultipartFile unsupported = archive("notes.txt", "text".getBytes());
        MockMultipartFile mismatched = archive("fake.mp4", "not-video".getBytes());

        assertEquals("压缩包中包含不支持的文件：notes.txt", assertThrows(
                IllegalArgumentException.class,
                () -> UploadArchiveValidator.validate(unsupported, VIDEO_EXTENSIONS, 1024)
        ).getMessage());
        assertEquals("压缩包中的文件内容与格式不匹配：fake.mp4", assertThrows(
                IllegalArgumentException.class,
                () -> UploadArchiveValidator.validate(mismatched, VIDEO_EXTENSIONS, 1024)
        ).getMessage());
    }

    @Test
    void rejectsExcessiveExpandedSize() throws Exception {
        MockMultipartFile archive = archive("clip.mp4", mp4Header());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> UploadArchiveValidator.validate(archive, VIDEO_EXTENSIONS, 8)
        );

        assertEquals("压缩包解压后的文件总大小超出限制", error.getMessage());
    }

    @Test
    void rejectsTooManyEntries() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            for (int index = 0; index <= UploadArchiveValidator.MAX_ENTRIES; index++) {
                zip.putNextEntry(new ZipEntry(index + ".mp4"));
                zip.write(mp4Header());
                zip.closeEntry();
            }
        }
        MockMultipartFile archive = new MockMultipartFile("file", "archive.zip", "application/zip", output.toByteArray());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> UploadArchiveValidator.validate(archive, VIDEO_EXTENSIONS, 1024 * 1024)
        );

        assertEquals("压缩包内文件和目录不能超过500项", error.getMessage());
    }

    @Test
    void rejectsFakeZipSignature() {
        MockMultipartFile archive = new MockMultipartFile(
                "file", "archive.zip", "application/zip", "not-a-zip".getBytes()
        );

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> UploadArchiveValidator.validate(archive, VIDEO_EXTENSIONS, 1024)
        );

        assertEquals("压缩包文件内容与格式不匹配", error.getMessage());
    }

    private MockMultipartFile archive(Object... values) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            for (int index = 0; index < values.length; index += 2) {
                byte[] content = (byte[]) values[index + 1];
                CRC32 crc = new CRC32();
                crc.update(content);
                ZipEntry entry = new ZipEntry((String) values[index]);
                entry.setMethod(ZipEntry.STORED);
                entry.setSize(content.length);
                entry.setCompressedSize(content.length);
                entry.setCrc(crc.getValue());
                zip.putNextEntry(entry);
                zip.write(content);
                zip.closeEntry();
            }
        }
        return new MockMultipartFile("file", "archive.zip", "application/zip", output.toByteArray());
    }

    private byte[] mp4Header() {
        byte[] header = new byte[16];
        header[4] = 'f';
        header[5] = 't';
        header[6] = 'y';
        header[7] = 'p';
        return header;
    }

    private byte[] webmHeader() {
        return new byte[]{0x1a, 0x45, (byte) 0xdf, (byte) 0xa3};
    }
}
