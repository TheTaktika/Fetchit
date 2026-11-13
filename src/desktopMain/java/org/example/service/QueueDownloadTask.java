package org.example.service;

import org.example.model.*;
import org.example.scanner.DownloaderScanner;
import org.example.ui.LanguageManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class QueueDownloadTask implements Runnable {
    private final DownloadItem item;
    private final YtDlpService ytDlpService;
    private final DownloaderScanner scanner;
    private final Consumer<String> logCallback;
    private final Consumer<Process> onProcessStartedCallback;

    public QueueDownloadTask(DownloadItem item, YtDlpService ytDlpService, DownloaderScanner scanner,
                             Consumer<String> logCallback, Consumer<Process> onProcessStartedCallback) {
        this.item = item;
        this.ytDlpService = ytDlpService;
        this.scanner = scanner;
        this.logCallback = logCallback;
        this.onProcessStartedCallback = onProcessStartedCallback;
    }

    @Override
    public void run() {
        item.setStatus(DownloadStatus.FETCHING);
        DownloadConfig config = item.getConfig();

        PlaylistFetchResult fetchResult = fetchPlaylist(config);
        if (!fetchResult.isSuccess()) {
            item.setStatus(DownloadStatus.ERROR);
            item.addFailedUrl(item.getUrl());
            return;
        }

        Set<String> downloadedIds = scanner.scanDownloadedIds(config.outputDir());
        List<String> toDownload = filterAlreadyDownloaded(fetchResult.ids(), downloadedIds);

        updatePlaylistMetadata(fetchResult, toDownload.size());

        if (toDownload.isEmpty()) {
            handleAllAlreadyDownloaded(fetchResult);
            return;
        }

        item.setStatus(DownloadStatus.DOWNLOADING);
        DownloadStats finalStats = performDownloadWithFallback(config, toDownload, fetchResult.ids());

        if (isCancelledOrPaused()) {
            return;
        }

        updateFinalStatus(finalStats);
    }

    private PlaylistFetchResult fetchPlaylist(DownloadConfig config) {
        return ytDlpService.getPlaylistVideoIds(
                config,
                line -> logCallback.accept(line + "\n")
        );
    }

    private List<String> filterAlreadyDownloaded(List<String> allIds, Set<String> downloadedIds) {
        return allIds.stream()
                .filter(url -> downloadedIds.stream().noneMatch(url::contains))
                .toList();
    }

    private void updatePlaylistMetadata(PlaylistFetchResult fetchResult, int toDownloadSize) {
        if (fetchResult.ids().size() > 1 && toDownloadSize > 0) {
            updateItemMetadata(item, fetchResult.title(), fetchResult.thumbnail());
        }
    }

    private DownloadStats performDownloadWithFallback(DownloadConfig config, List<String> toDownload, List<String> allIds) {
        DownloadStats stats = ytDlpService.downloadByIds(
                config,
                toDownload,
                new QueueDownloadCallbacks(item, allIds, logCallback, onProcessStartedCallback)
        );

        LanguageManager lang = LanguageManager.getInstance();

        if (!stats.getFailedThumbnailUrls().isEmpty()) {
            logCallback.accept(lang.get("log.retry.thumbnail") + "\n");
            Preset noThumbPreset = copyPresetNoThumbnail(config.preset());
            DownloadConfig fallbackConfig = new DownloadConfig(
                    config.url(), config.outputDir(), config.proxy(), noThumbPreset
            );

            DownloadStats retryThumbStats = ytDlpService.downloadByIds(
                    fallbackConfig,
                    stats.getFailedThumbnailUrls(),
                    new QueueDownloadCallbacks(item, allIds, logCallback, onProcessStartedCallback)
            );
            mergeStats(stats, retryThumbStats);
        }

        return stats;
    }

    private void mergeStats(DownloadStats original, DownloadStats retry) {
        if (retry.getErrorsCount() == 0) {
            original.incrementDownloaded();
            original.incrementCompleted();
        } else {
            original.addError(ErrorType.UNKNOWN, LanguageManager.getInstance().get("error.fallback_failed"));
        }
    }

    private boolean isCancelledOrPaused() {
        return item.getStatus() == DownloadStatus.CANCELLED || item.getStatus() == DownloadStatus.PAUSED;
    }

    private void updateFinalStatus(DownloadStats stats) {
        if (stats.getErrorsCount() > 0) {
            item.setStatus(DownloadStatus.ERROR);
            if (stats.getFailedUrls() != null) {
                for (String failedUrl : stats.getFailedUrls()) {
                    item.addFailedUrl(failedUrl);
                }
            }
        } else {
            item.setStatus(DownloadStatus.DONE);
            item.setProgress(100.0);
        }
        item.setSummary(StatsFormatter.format(stats));
    }

    private void handleAllAlreadyDownloaded(PlaylistFetchResult fetchResult) {
        item.setStatus(DownloadStatus.DONE);
        item.setProgress(100.0);

        updateItemMetadata(item, fetchResult.title(), fetchResult.thumbnail());

        DownloadStats emptyStats = new DownloadStats();
        emptyStats.setPlaylistTotal(fetchResult.ids().size());
        emptyStats.setAlreadyOnDisk(fetchResult.ids().size());
        emptyStats.setTotalItems(0);

        item.setSummary(StatsFormatter.format(emptyStats));
        logCallback.accept(LanguageManager.getInstance().get("status.all_downloaded") + "\n");
    }

    private void updateItemMetadata(DownloadItem item, String title, String thumbUrl) {
        if (title != null && !"NA".equals(title)) {
            item.setTitle(title);
        }
        if (thumbUrl != null && !"NA".equals(thumbUrl)) {
            String safeUrl = thumbUrl.replace("vi_webp", "vi").replace(".webp", ".jpg");

            Thread.startVirtualThread(() -> {
                try {
                    URL url = URI.create(safeUrl).toURL();
                    try (InputStream in = url.openStream();
                         ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                        item.setThumbnailData(out.toByteArray());
                    }
                } catch (Exception ignored) {}
            });
        }
    }

    private Preset copyPresetNoThumbnail(Preset original) {
        Preset p = clonePreset(original);
        p.setEmbedThumbnail(false);
        p.setNoOverwrites(false);
        p.setForceOverwrites(true);
        return p;
    }

    private Preset clonePreset(Preset original) {
        Preset p = new Preset();
        p.setName(original.getName());
        p.setFormat(original.getFormat());
        p.setAudioFormat(original.getAudioFormat());
        p.setAudioQuality(original.getAudioQuality());
        p.setVideoQuality(original.getVideoQuality());
        p.setMergeOutputFormat(original.getMergeOutputFormat());
        p.setRecodeVideo(original.getRecodeVideo());

        p.setEmbedThumbnail(original.isEmbedThumbnail());
        p.setAddMetadata(original.isAddMetadata());

        p.setEmbedSubtitles(original.isEmbedSubtitles());
        p.setWriteSubtitles(original.isWriteSubtitles());
        p.setEmbedChapters(original.isEmbedChapters());
        p.setSubtitleLanguages(original.getSubtitleLanguages());
        p.setConcurrentFragments(original.getConcurrentFragments());
        p.setLimitRate(original.getLimitRate());
        p.setRetries(original.getRetries());
        p.setSponsorblockRemove(original.getSponsorblockRemove());
        p.setSponsorblockMark(original.getSponsorblockMark());
        p.setKeepVideo(original.isKeepVideo());
        p.setNoOverwrites(original.isNoOverwrites());
        p.setCookiesFromBrowser(original.getCookiesFromBrowser());
        p.setSplitChapters(original.isSplitChapters());
        p.setForceOverwrites(original.isForceOverwrites());
        return p;
    }
}