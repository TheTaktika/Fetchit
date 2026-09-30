package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.service.YtDlpService;

public class FetchItApplication extends Application {

    private final TextField urlField = new TextField();
    private final Button downloadButton = new Button("Скачать");
    private final TextArea outputArea = new TextArea();
    private final YtDlpService ytDlpService = new YtDlpService();

    @Override
    public void start(Stage stage) {
        urlField.setPromptText("Вставьте ссылку на видео или плейлист");
        HBox.setHgrow(urlField, Priority.ALWAYS);

        HBox topBar = new HBox(10, urlField, downloadButton);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10));

        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        VBox.setVgrow(outputArea, Priority.ALWAYS);

        VBox root = new VBox(topBar, outputArea);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("FetchIt");
        stage.setScene(scene);
        stage.show();

        downloadButton.setOnAction(e -> onDownloadClick());
    }

    private void onDownloadClick() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            appendOutput("Ошибка: введите ссылку.\n");
            return;
        }

        downloadButton.setDisable(true);
        appendOutput("Начинаю загрузку: " + url + "\n");

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                return ytDlpService.download(
                        url,
                        "test_output",
                        line -> Platform.runLater(() -> appendOutput(line + "\n"))
                );
            }
        };

        task.setOnSucceeded(event -> {
            int exitCode = task.getValue();
            appendOutput("\n=== Загрузка завершена. Код: " + exitCode + " ===\n");
            downloadButton.setDisable(false);
        });

        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            appendOutput("\n=== Ошибка: " + ex.getMessage() + " ===\n");
            downloadButton.setDisable(false);
        });

        new Thread(task).start();
    }

    private void appendOutput(String text) {
        outputArea.appendText(text);
        outputArea.setScrollTop(Double.MAX_VALUE);
    }
}