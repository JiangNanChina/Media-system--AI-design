package com.example.photography.service;

import com.example.photography.dto.request.LandingContentItemRequest;
import com.example.photography.model.entity.LandingContentItem;
import com.example.photography.model.entity.SiteConfig;
import com.example.photography.model.enums.LandingSectionType;
import com.example.photography.repository.LandingContentItemRepository;
import com.example.photography.util.FileUploadUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LandingServiceTest {
    private LandingContentItemRepository itemRepository;
    private SiteConfigService siteConfigService;
    private FileUploadUtil fileUploadUtil;
    private LandingService service;

    @BeforeEach
    void setUp() {
        itemRepository = mock(LandingContentItemRepository.class);
        siteConfigService = mock(SiteConfigService.class);
        fileUploadUtil = mock(FileUploadUtil.class);
        service = new LandingService(itemRepository, siteConfigService, fileUploadUtil);

        when(siteConfigService.getConfigValue(anyString(), nullable(String.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(siteConfigService.getAllConfigs()).thenReturn(List.of());
        when(itemRepository.findByDeletedFalseOrderBySectionTypeAscSortOrderAsc()).thenReturn(List.of());
        when(itemRepository.findByPublishedTrueAndDeletedFalseOrderBySectionTypeAscSortOrderAsc()).thenReturn(List.of());
        when(itemRepository.save(any(LandingContentItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void clearingSavedMediaSettingPhysicallyDeletesPreviousManagedFile() {
        when(siteConfigService.getConfigValue(eq(SiteConfig.Keys.SITE_LOGO), nullable(String.class)))
                .thenReturn("/uploads/site/old-logo.png", "");

        service.saveSettings(Map.of(SiteConfig.Keys.SITE_LOGO, ""));

        verify(fileUploadUtil).deleteFile("/uploads/site/old-logo.png");
    }

    @Test
    void clearingMediaSettingKeepsFileWhenLandingContentStillReferencesIt() {
        LandingContentItem item = landingItem(1L, "/uploads/site/shared.png");
        when(siteConfigService.getConfigValue(eq(SiteConfig.Keys.LANDING_HERO_MEDIA), nullable(String.class)))
                .thenReturn("/uploads/site/shared.png", "");
        when(itemRepository.findByDeletedFalseOrderBySectionTypeAscSortOrderAsc()).thenReturn(List.of(item));

        service.saveSettings(Map.of(SiteConfig.Keys.LANDING_HERO_MEDIA, ""));

        verify(fileUploadUtil, never()).deleteFile(anyString());
    }

    @Test
    void deletingContentItemPhysicallyDeletesUnreferencedManagedFile() {
        LandingContentItem item = landingItem(7L, "/uploads/site/card.png");
        when(itemRepository.findById(7L)).thenReturn(Optional.of(item));

        service.deleteItem(7L);

        assertThat(item.getDeleted()).isTrue();
        verify(itemRepository).save(item);
        verify(fileUploadUtil).deleteFile("/uploads/site/card.png");
    }

    @Test
    void replacingContentItemMediaDeletesOnlyPreviousManagedFile() {
        LandingContentItem item = landingItem(9L, "/uploads/site/old-card.png");
        when(itemRepository.findById(9L)).thenReturn(Optional.of(item));
        when(itemRepository.findByDeletedFalseOrderBySectionTypeAscSortOrderAsc()).thenReturn(List.of(item));

        LandingContentItemRequest request = new LandingContentItemRequest();
        request.setSectionType(LandingSectionType.CAMPUS_FEATURE);
        request.setTitle("校园现场");
        request.setMediaUrl("/uploads/site/new-card.png");

        service.saveItem(9L, request);

        verify(fileUploadUtil).deleteFile("/uploads/site/old-card.png");
        verify(fileUploadUtil, never()).deleteFile("/uploads/site/new-card.png");
    }

    @Test
    void deleteMediaEndpointIgnoresExternalAndUnsafePaths() {
        service.deleteUnreferencedMedia("https://example.com/uploads/site/not-managed.png");
        service.deleteUnreferencedMedia("/uploads/site/../application.yml");

        verify(fileUploadUtil, never()).deleteFile(anyString());
    }

    private LandingContentItem landingItem(Long id, String mediaUrl) {
        LandingContentItem item = new LandingContentItem();
        item.setId(id);
        item.setSectionType(LandingSectionType.CAMPUS_FEATURE);
        item.setTitle("校园现场");
        item.setMediaUrl(mediaUrl);
        item.setPublished(true);
        item.setDeleted(false);
        item.setSortOrder(1);
        return item;
    }
}
