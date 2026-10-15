package com.example.photography.service;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

final class UploadArchiveValidator {
    static final int MAX_ENTRIES = 500;
    static final Set<String> MIME_TYPES = Set.of(
            "application/zip", "application/x-zip-compressed", "application/octet-stream"
    );

    private static final int HEADER_SIZE = 16;

    private UploadArchiveValidator() {
    }

    static void validate(MultipartFile file, Set<String> allowedExtensions, long maxExpandedSize) {
        try (BufferedInputStream input = new BufferedInputStream(file.getInputStream())) {
            input.mark(4);
            byte[] signature = input.readNBytes(4);
            input.reset();
            if (signature.length != 4 || signature[0] != 'P' || signature[1] != 'K'
                    || signature[2] != 3 || signature[3] != 4) {
                throw new IllegalArgumentException("压缩包文件内容与格式不匹配");
            }

            inspectEntries(input, allowedExtensions, maxExpandedSize);
        } catch (IOException e) {
            throw new IllegalArgumentException("无法读取压缩包文件", e);
        }
    }

    private static void inspectEntries(BufferedInputStream input, Set<String> allowedExtensions,
                                       long maxExpandedSize) throws IOException {
        Set<String> paths = new HashSet<>();
        int entryCount = 0;
        int fileCount = 0;
        long expandedSize = 0;

        try (ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entryCount += 1;
                if (entryCount > MAX_ENTRIES) {
                    throw new IllegalArgumentException("压缩包内文件和目录不能超过500项");
                }

                String path = normalizeAndValidatePath(entry.getName());
                if (!paths.add(path.toLowerCase(Locale.ROOT))) {
                    throw new IllegalArgumentException("压缩包内包含重复路径：" + path);
                }

                if (entry.isDirectory()) {
                    if (zip.read() != -1) {
                        throw new IllegalArgumentException("压缩包目录项内容不合法：" + path);
                    }
                    continue;
                }

                fileCount += 1;
                String extension = StringUtils.getFilenameExtension(path);
                extension = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
                if (!allowedExtensions.contains(extension)) {
                    throw new IllegalArgumentException("压缩包中包含不支持的文件：" + path);
                }

                byte[] header = zip.readNBytes(HEADER_SIZE);
                expandedSize = addExpandedSize(expandedSize, header.length, maxExpandedSize);
                if (!hasValidMediaHeader(extension, header)) {
                    throw new IllegalArgumentException("压缩包中的文件内容与格式不匹配：" + path);
                }

                byte[] buffer = new byte[8192];
                int read;
                while ((read = zip.read(buffer)) != -1) {
                    expandedSize = addExpandedSize(expandedSize, read, maxExpandedSize);
                }
                zip.closeEntry();
            }
        }

        if (fileCount == 0) {
            throw new IllegalArgumentException("压缩包中没有可审核的文件");
        }
    }

    private static long addExpandedSize(long current, int added, long maximum) {
        if (added > maximum - current) {
            throw new IllegalArgumentException("压缩包解压后的文件总大小超出限制");
        }
        return current + added;
    }

    private static String normalizeAndValidatePath(String originalPath) {
        String path = originalPath == null ? "" : originalPath.replace('\\', '/');
        if (!StringUtils.hasText(path) || path.length() > 1024 || path.indexOf('\0') >= 0
                || path.startsWith("/") || path.matches("^[A-Za-z]:.*")) {
            throw new IllegalArgumentException("压缩包中包含非法文件路径");
        }

        String[] segments = path.split("/", -1);
        int lastIndex = path.endsWith("/") ? segments.length - 1 : segments.length;
        for (int index = 0; index < lastIndex; index++) {
            String segment = segments[index];
            if (!StringUtils.hasText(segment) || segment.equals(".") || segment.equals("..")
                    || segment.length() > 255) {
                throw new IllegalArgumentException("压缩包中包含非法文件路径：" + path);
            }
        }
        return path;
    }

    private static boolean hasValidMediaHeader(String extension, byte[] header) {
        return switch (extension) {
            case "jpg", "jpeg" -> header.length >= 3
                    && (header[0] & 0xff) == 0xff && (header[1] & 0xff) == 0xd8 && (header[2] & 0xff) == 0xff;
            case "png" -> header.length >= 8 && (header[0] & 0xff) == 0x89
                    && header[1] == 'P' && header[2] == 'N' && header[3] == 'G';
            case "gif" -> header.length >= 6 && header[0] == 'G' && header[1] == 'I' && header[2] == 'F';
            case "bmp" -> header.length >= 2 && header[0] == 'B' && header[1] == 'M';
            case "webp" -> header.length >= 12 && header[0] == 'R' && header[1] == 'I'
                    && header[2] == 'F' && header[3] == 'F' && header[8] == 'W' && header[9] == 'E'
                    && header[10] == 'B' && header[11] == 'P';
            case "webm" -> header.length >= 4 && (header[0] & 0xff) == 0x1a
                    && (header[1] & 0xff) == 0x45 && (header[2] & 0xff) == 0xdf && (header[3] & 0xff) == 0xa3;
            case "mp4", "mov" -> header.length >= 12 && header[4] == 'f' && header[5] == 't'
                    && header[6] == 'y' && header[7] == 'p';
            default -> false;
        };
    }
}
