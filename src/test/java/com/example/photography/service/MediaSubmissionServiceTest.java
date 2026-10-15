package com.example.photography.service;

import com.example.photography.model.entity.MediaSubmission;
import com.example.photography.repository.MediaSubmissionRepository;
import com.example.photography.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MediaSubmissionServiceTest {
    private final MediaSubmissionRepository repository = mock(MediaSubmissionRepository.class);
    private MediaSubmissionService service;

    @TempDir
    Path tempDirectory;

    @BeforeEach
    void setUp() throws Exception {
        service = new MediaSubmissionService(
                repository,
                mock(UserRepository.class),
                mock(VerificationCodeService.class),
                mock(EmailDeliveryService.class)
        );
        ReflectionTestUtils.setField(service, "privateUploadPath", tempDirectory.toString());
        service.initializeDirectory();
    }

    @Test
    void deletePermanentlyRemovesVideoAndDatabaseRecord() throws Exception {
        MediaSubmission submission = submission("video.mp4");
        Path video = tempDirectory.resolve("submissions/video.mp4");
        Files.writeString(video, "video-content");
        when(repository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(submission));

        service.deletePermanently(1L);

        assertFalse(Files.exists(video));
        verify(repository).delete(submission);
        verify(repository).flush();
    }

    @Test
    void deletePermanentlyRemovesRecordWhenVideoIsAlreadyMissing() {
        MediaSubmission submission = submission("missing.mp4");
        when(repository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(submission));

        service.deletePermanently(1L);

        verify(repository).delete(submission);
        verify(repository).flush();
    }

    @Test
    void deletePermanentlyRejectsPathOutsideSubmissionDirectory() {
        MediaSubmission submission = submission(Path.of("..", "outside.mp4").toString());
        when(repository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(submission));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.deletePermanently(1L)
        );

        assertEquals("非法文件路径", error.getMessage());
        verify(repository, never()).delete(any());
        verify(repository, never()).flush();
    }

    @Test
    void deletePermanentlyNeverTreatsStorageDirectoryAsVideo() {
        MediaSubmission submission = submission(".");
        when(repository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(submission));

        assertThrows(IllegalArgumentException.class, () -> service.deletePermanently(1L));

        assertTrue(Files.isDirectory(tempDirectory.resolve("submissions")));
        verify(repository, never()).delete(any());
    }

    @Test
    void deletePermanentlyKeepsRecordWhenVideoCannotBeDeleted() throws Exception {
        MediaSubmission submission = submission("not-a-file.mp4");
        Path directory = tempDirectory.resolve("submissions/not-a-file.mp4");
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("child"), "content");
        when(repository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(submission));

        UncheckedIOException error = assertThrows(
                UncheckedIOException.class,
                () -> service.deletePermanently(1L)
        );

        assertEquals("服务器中的投稿视频删除失败，数据库记录未删除", error.getMessage());
        assertTrue(Files.exists(directory));
        verify(repository, never()).delete(any());
        verify(repository, never()).flush();
    }

    @Test
    void deletePermanentlyRejectsMissingRecord() {
        when(repository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.deletePermanently(99L)
        );

        assertEquals("投稿不存在", error.getMessage());
        verify(repository, never()).delete(any());
    }

    @Test
    void submitAcceptsValidatedVideoArchive() throws Exception {
        when(repository.save(any(MediaSubmission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MockMultipartFile archive = new MockMultipartFile(
                "file", "video-submission.zip", "application/zip",
                archive("folder/first.mp4", mp4Header(), "second.webm", webmHeader())
        );

        service.submit(
                "校园活动", "", "张三", "13800138000", "student@qq.com", "融媒体中心", "123456", archive
        );

        verify(repository).save(argThat(submission ->
                "video-submission.zip".equals(submission.getOriginalFilename())
                        && "application/zip".equals(submission.getMimeType())
                        && submission.getStoredFilename().endsWith(".zip")
        ));
    }

    private byte[] archive(Object... values) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output)) {
            for (int index = 0; index < values.length; index += 2) {
                zip.putNextEntry(new ZipEntry((String) values[index]));
                zip.write((byte[]) values[index + 1]);
                zip.closeEntry();
            }
        }
        return output.toByteArray();
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

    private MediaSubmission submission(String storedFilename) {
        MediaSubmission submission = new MediaSubmission();
        submission.setId(1L);
        submission.setSubmissionNumber("SUB-001");
        submission.setStoredFilename(storedFilename);
        return submission;
    }
}
