package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.model.DownloadConfig;
import org.example.service.YtDlpService;

import java.io.File;

public class FetchItApplication extends Application {

    private final TextField urlField = new TextField();
    private final TextField proxyField = new TextField();
    private final Label outputDirLabel = new Label("Папка: test_output");
    private final Button chooseDirButton = new Button("Папка…");
    private final Button downloadButton = new Button("Скачать");
    private final Button updateButton = new Button("Обновить yt-dlp");
    private final TextArea outputArea = new TextArea();
    private final YtDlpService ytDlpService = new YtDlpService();

    private File selectedDir = new File("test_output");

    @Override
    public void start(Stage stage) {

        urlField.setPromptText("Вставьте ссылку на видео или плейлист");
        HBox.setHgrow(urlField, Priority.ALWAYS);

        proxyField.setPromptText("Прокси (необязательно), напр. socks5://127.0.0.1:10808");
        proxyField.setPrefWidth(300);

        HBox settingsBar = new HBox(10, chooseDirButton, outputDirLabel, new Label("|"), proxyField);
        settingsBar.setAlignment(Pos.CENTER_LEFT);
        settingsBar.setPadding(new Insets(5, 10, 5, 10));

        HBox topBar = new HBox(10, urlField, downloadButton, updateButton);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));

        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        VBox.setVgrow(outputArea, Priority.ALWAYS);

        VBox root = new VBox(topBar, settingsBar, outputArea);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 1000, 650);
        stage.setTitle("FetchIt");
        stage.setScene(scene);
        stage.show();

        chooseDirButton.setOnAction(e -> onChooseDirClick(stage));
        downloadButton.setOnAction(e -> onDownloadClick());
        updateButton.setOnAction(e -> onUpdateClick());
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

        appendOutput("\n=== Начинаю загрузку ===\n");
        appendOutput("URL: " + config.getUrl() + "\n");
        appendOutput("Папка: " + config.getOutputDir() + "\n");
        appendOutput("Прокси: " + (config.hasProxy() ? config.getProxy() : "не используется") + "\n\n");

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                return ytDlpService.download(
                        config,
                        line -> Platform.runLater(() -> appendOutput(line + "\n"))
                );
            }
        };

        task.setOnSucceeded(event -> {
            appendOutput("\n=== Загрузка завершена. Код: " + task.getValue() + " ===\n");
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