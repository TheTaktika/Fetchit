package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.model.DownloadConfig;
import org.example.model.DownloadStats;
import org.example.model.ProgressUpdate;
import org.example.scanner.DownloaderScanner;
import org.example.service.YtDlpService;

import java.io.File;
import java.util.List;
import java.util.Set;

public class FetchItApplication extends Application {

    private final TextField urlField = new TextField();
    private final TextField proxyField = new TextField();
    private final Label outputDirLabel = new Label("Папка: test_output");
    private final Button chooseDirButton = new Button("Папка…");
    private final Button downloadButton = new Button("Скачать");
    private final Button updateButton = new Button("Обновить yt-dlp");

    private final ProgressBar totalProgressBar = new ProgressBar(0);
    private final Label totalProgressLabel = new Label("0%");
    private final ProgressBar fileProgressBar = new ProgressBar(0);
    private final Label fileProgressLabel = new Label("0%");

    private final Label summaryLabel = new Label("");

    private final TextArea outputArea = new TextArea();

    private final YtDlpService ytDlpService = new YtDlpService();
    private File selectedDir = new File("test_output");

    @Override
    public void start(Stage stage) {
        urlField.setPromptText("Вставьте ссылку на видео или плейлист");
        HBox.setHgrow(urlField, Priority.ALWAYS);
        HBox topBar = new HBox(10, urlField, downloadButton, updateButton);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10, 10, 5, 10));

        proxyField.setPromptText("Прокси (необязательно), напр. socks5://127.0.0.1:10808");
        proxyField.setPrefWidth(300);
        HBox settingsBar = new HBox(10, chooseDirButton, outputDirLabel, new Label("|"), proxyField);
        settingsBar.setAlignment(Pos.CENTER_LEFT);
        settingsBar.setPadding(new Insets(0, 10, 10, 10));

        VBox progressBox = new VBox(8,
                buildProgressRow("Общий прогресс:", totalProgressBar, totalProgressLabel),
                buildProgressRow("Текущий файл:   ", fileProgressBar, fileProgressLabel)
        );
        progressBox.setPadding(new Insets(0, 10, 10, 10));

        summaryLabel.setPadding(new Insets(0, 10, 10, 10));
        summaryLabel.setStyle("-fx-font-family: monospace;");

        outputArea.setEditable(false);
        outputArea.setWrapText(false);
        outputArea.setPrefHeight(300);
        TitledPane logsPane = new TitledPane("Логи (для разработки)", outputArea);
        logsPane.setExpanded(false);
        logsPane.setPadding(new Insets(0, 10, 10, 10));

        VBox root = new VBox(topBar, settingsBar, progressBox, summaryLabel, logsPane);
        root.setPadding(new Insets(0));

        Scene scene = new Scene(root, 1000, 650);
        stage.setTitle("FetchIt");
        stage.setScene(scene);
        stage.show();

        chooseDirButton.setOnAction(e -> onChooseDirClick(stage));
        downloadButton.setOnAction(e -> onDownloadClick());
        updateButton.setOnAction(e -> onUpdateClick());
    }


    private HBox buildProgressRow(String labelText, ProgressBar bar, Label percentLabel) {
        Label nameLabel = new Label(labelText);
        nameLabel.setMinWidth(130);

        bar.setPrefWidth(Double.MAX_VALUE);
        HBox.setHgrow(bar, Priority.ALWAYS);

        percentLabel.setMinWidth(60);
        percentLabel.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(10, nameLabel, bar, percentLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void onChooseDirClick(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Выберите папку для сохранения");
        if (selectedDir.exists()) {
            chooser.setInitialDirectory(selectedDir);
        }
        File dir = chooser.showDialog(stage);
        if (dir != null) {
            selectedDir = dir;
            outputDirLabel.setText("Папка: " + dir.getAbsolutePath());
        }
    }

    private void onDownloadClick() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            appendOutput("Ошибка: введите ссылку.\n");
            return;
        }

        DownloadConfig config = new DownloadConfig(
                url,
                selectedDir.getAbsolutePath(),
                proxyField.getText()
        );

        downloadButton.setDisable(true);
        updateButton.setDisable(true);
        totalProgressBar.setProgress(0);
        fileProgressBar.setProgress(0);
        totalProgressLabel.setText("0%");
        fileProgressLabel.setText("0%");
        summaryLabel.setText("");
        outputArea.clear();

        appendOutput("=== Проверка уже скачанных файлов ===\n");

        Task<DownloadStats> task = new Task<>() {
            @Override
            protected DownloadStats call() {
                DownloaderScanner scanner = new DownloaderScanner();
                Set<String> downloadedIds = scanner.scanDownloadedIds(config.getOutputDir());

                List<String> allIds = ytDlpService.getPlaylistVideoIds(
                        config.getUrl(),
                        line -> Platform.runLater(() -> appendOutput(line + "\n"))
                );

                List<String> toDownload = allIds.stream()
                        .filter(id -> !downloadedIds.contains(id))
                        .toList();

                long alreadyOnDisk = allIds.stream()
                        .filter(downloadedIds::contains)
                        .count();

                Platform.runLater(() -> {
                    appendOutput("Найдено локальных MP3: " + downloadedIds.size() + "\n");
                    appendOutput("Всего в плейлисте: " + allIds.size() + "\n");
                    appendOutput("Уже было на диске: " + alreadyOnDisk + "\n");
                    appendOutput("К загрузке: " + toDownload.size() + "\n\n");
                });

                if (toDownload.isEmpty()) {
                    DownloadStats empty = new DownloadStats();
                    empty.setPlaylistTotal(allIds.size());
                    empty.setAlreadyOnDisk((int) alreadyOnDisk);
                    return empty;
                }

                DownloadStats stats = ytDlpService.downloadByIds(
                        config,
                        toDownload,
                        line -> Platform.runLater(() -> appendOutput(line + "\n")),
                        update -> Platform.runLater(() -> updateProgressBars(update))
                );

                stats.setPlaylistTotal(allIds.size());
                stats.setAlreadyOnDisk((int) alreadyOnDisk);
                return stats;
            }
        };

        task.setOnSucceeded(event -> {
            DownloadStats stats = task.getValue();
            if (stats.getDownloadedCount() == 0 && stats.getErrorsCount() == 0) {
                summaryLabel.setText("Все видео уже скачаны. Ничего загружать не нужно.\n");
            } else {
                summaryLabel.setText(stats.toSummary());
            }
            totalProgressBar.setProgress(1.0);
            fileProgressBar.setProgress(1.0);
            totalProgressLabel.setText("100%");
            fileProgressLabel.setText("100%");
            downloadButton.setDisable(false);
            updateButton.setDisable(false);
        });

        task.setOnFailed(event -> {
            appendOutput("\n=== Ошибка: " + task.getException().getMessage() + " ===\n");
            downloadButton.setDisable(false);
            updateButton.setDisable(false);
        });

        new Thread(task).start();
    }

    private void updateProgressBars(ProgressUpdate update) {
        double filePercent = update.filePercent();
        fileProgressBar.setProgress(filePercent / 100.0);
        fileProgressLabel.setText(String.format("%.0f%%", filePercent));

        if (update.totalItems() > 0) {
            double overall = (double) update.completedItems() / update.totalItems();
            totalProgressBar.setProgress(overall);
            totalProgressLabel.setText(String.format("%d/%d (%.0f%%)",
                    update.completedItems(), update.totalItems(), overall * 100));
        } else {
            totalProgressBar.setProgress(filePercent / 100.0);
            totalProgressLabel.setText(String.format("%.0f%%", filePercent));
        }
    }

    private void onUpdateClick() {
        downloadButton.setDisable(true);
        updateButton.setDisable(true);
        appendOutput("\n=== Проверка обновлений yt-dlp ===\n");

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                return ytDlpService.update(
                        line -> Platform.runLater(() -> appendOutput(line + "\n"))
                );
            }
        };

        task.setOnSucceeded(event -> {
            appendOutput("=== Готово. Код: " + task.getValue() + " ===\n");
            downloadButton.setDisable(false);
            updateButton.setDisable(false);
        });

        task.setOnFailed(event -> {
            appendOutput("=== Ошибка: " + task.getException().getMessage() + " ===\n");
            downloadButton.setDisable(false);
            updateButton.setDisable(false);
        });

        new Thread(task).start();
    }

    private void appendOutput(String text) {
        outputArea.appendText(text);
        outputArea.setScrollTop(Double.MAX_VALUE);
    }
}