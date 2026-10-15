package com.example.photography.service;

import com.example.photography.model.entity.JoinApplication;
import com.example.photography.model.enums.Gender;
import com.example.photography.repository.JoinApplicationRepository;
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

class JoinApplicationServiceTest {
    private final JoinApplicationRepository repository = mock(JoinApplicationRepository.class);
    private JoinApplicationService service;

    @TempDir
    Path tempDirectory;

    @BeforeEach
    void setUp() throws Exception {
        service = new JoinApplicationService(
                repository,
                mock(UserRepository.class),
                mock(SiteConfigService.class),
                mock(EmailNotificationService.class)
        );
        ReflectionTestUtils.setField(service, "privateUploadPath", tempDirectory.toString());
        service.initializeDirectory();
    }

    @Test
    void submitPersistsTrimmedClassName() {
        when(repository.existsByApplicationNumber(anyString())).thenReturn(false);
        when(repository.save(any(JoinApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.submit(
                "张三", "student@qq.com", "13800138000", Gender.MALE,
                "计算机学院", "软件工程", 2026, " 软件工程2601班 ", "热爱摄影", null
        );

        assertEquals("软件工程2601班", response.getClassName());
        verify(repository).save(argThat(application -> "软件工程2601班".equals(application.getClassName())));
    }

    @Test
    void submitRejectsBlankClassName() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.submit(
                        "张三", "student@qq.com", "13800138000", Gender.MALE,
                        "计算机学院", "软件工程", 2026, "  ", "热爱摄影", null
                )
        );

        assertEquals("班级不能为空且不能超过80字", error.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void deletePermanentlyRemovesWorkAndDatabaseRecord() throws Exception {
        JoinApplication application = application("work.png");
        Path work = tempDirectory.resolve("join-applications/work.png");
        Files.writeString(work, "image-content");
        when(repository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(application));

        service.deletePermanently(1L);

        assertFalse(Files.exists(work));
        verify(repository).delete(application);
        verify(repository).flush();
    }

    @Test
    void deletePermanentlyRemovesApplicationWithoutUploadedWork() {
        JoinApplication application = application(null);
        when(repository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(application));

        service.deletePermanently(1L);

        verify(repository).delete(application);
        verify(repository).flush();
    }

    @Test
    void deletePermanentlyRejectsPathOutsideWorkDirectory() {
        JoinApplication application = application(Path.of("..", "outside.png").toString());
        when(repository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(application));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.deletePermanently(1L)
        );

        assertEquals("非法文件路径", error.getMessage());
        verify(repository, never()).delete(any());
        verify(repository, never()).flush();
    }

    @Test
    void deletePermanentlyNeverTreatsStorageDirectoryAsWork() {
        JoinApplication application = application(".");
        when(repository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(application));

        assertThrows(IllegalArgumentException.class, () -> service.deletePermanently(1L));

        assertTrue(Files.isDirectory(tempDirectory.resolve("join-applications")));
        verify(repository, never()).delete(any());
    }

    @Test
    void deletePermanentlyKeepsRecordWhenWorkCannotBeDeleted() throws Exception {
        JoinApplication application = application("not-a-file.png");
        Path directory = tempDirectory.resolve("join-applications/not-a-file.png");
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("child"), "content");
        when(repository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(application));

        UncheckedIOException error = assertThrows(
                UncheckedIOException.class,
                () -> service.deletePermanently(1L)
        );

        assertEquals("服务器中的入部申请作品删除失败，数据库记录未删除", error.getMessage());
        assertTrue(Files.exists(directory));
        verify(repository, never()).delete(any());
        verify(repository, never()).flush();
    }

    @Test
    void deletePermanentlyRemovesRecordWhenWorkIsAlreadyMissing() {
        JoinApplication application = application("missing.png");
        when(repository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(application));

        service.deletePermanently(1L);

        verify(repository).delete(application);
        verify(repository).flush();
    }

    @Test
    void submitAcceptsValidatedPortfolioArchive() throws Exception {
        when(repository.existsByApplicationNumber(anyString())).thenReturn(false);
        when(repository.save(any(JoinApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MockMultipartFile archive = new MockMultipartFile(
                "work", "join-portfolio.zip", "application/zip",
                archive("portfolio/photo.png", pngHeader(), "portfolio/clip.mp4", mp4Header())
        );

        service.submit(
                "张三", "student@qq.com", "13800138000", Gender.MALE,
                "计算机学院", "软件工程", 2026, "软件工程2601班", "热爱摄影", archive
        );

        verify(repository).save(argThat(application ->
                "join-portfolio.zip".equals(application.getWorkOriginalFilename())
                        && "application/zip".equals(application.getWorkMimeType())
                        && application.getWorkStoredFilename().endsWith(".zip")
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

    private byte[] pngHeader() {
        return new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a};
    }

    private byte[] mp4Header() {
        byte[] header = new byte[16];
        header[4] = 'f';
        header[5] = 't';
        header[6] = 'y';
        header[7] = 'p';
        return header;
    }

    private JoinApplication application(String storedFilename) {
        JoinApplication application = new JoinApplication();
        application.setId(1L);
        application.setApplicationNumber("JOIN-001");
        application.setWorkStoredFilename(storedFilename);
        return application;
    }
}
