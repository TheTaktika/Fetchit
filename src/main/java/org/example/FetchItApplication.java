package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.model.*;
import org.example.service.QueueManager;
import org.example.service.YtDlpService;
import org.example.ui.*;

import java.io.File;

public class FetchItApplication extends Application {
    private BottomPanel bottomPanel;
    private TopBar topBar;
    private SettingsBar settingsBar;
    private final QueueTable queueTable = new QueueTable();
    private QueueContextMenu contextMenu;

    private final LanguageManager lang = LanguageManager.getInstance();
    private QueueManager queueManager;

    private final ObservableList<DownloadItem> downloadQueue = FXCollections.observableArrayList();

    private final YtDlpService ytDlpService = new YtDlpService();
    private File selectedDir = new File("downloads");

    @Override
    public void start(Stage stage) {
        queueManager = new QueueManager(downloadQueue, ytDlpService, text -> bottomPanel.appendLog(text));

        try {
            var iconStream = getClass().getResourceAsStream("/icon.png");
            if (iconStream != null) stage.getIcons().add(new Image(iconStream));
        } catch (Exception e) {
            System.err.println(lang.get("app.icon") + e.getMessage());
        }

        if (!selectedDir.exists()) {
            selectedDir.mkdirs();
        }

        topBar = new TopBar(this::updateTexts);
        settingsBar = new SettingsBar();

        queueTable.setItems(downloadQueue);
        VBox.setVgrow(queueTable, Priority.ALWAYS);
        queueTable.updateTexts();

        contextMenu = new QueueContextMenu(queueTable, downloadQueue, queueManager);
        queueTable.setContextMenu(contextMenu);

        bottomPanel = new BottomPanel(queueTable);
        queueTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            bottomPanel.bindStatsTo(newSelection);
        });

        VBox root = new VBox(topBar, settingsBar, bottomPanel);
        root.setPadding(new Insets(0));

        Scene scene = new Scene(root, 1000, 650);
        stage.setScene(scene);
        stage.show();

        topBar.getDownloadButton().setOnAction(e -> onDownloadClick());
        topBar.getUpdateButton().setOnAction(e -> onUpdateClick());
        settingsBar.getChooseDirButton().setOnAction(e -> onChooseDirClick(stage));
        settingsBar.getSettingsButton().setOnAction(e -> onSettingsClick());
        settingsBar.setOutputDir(selectedDir.getName());

        updateTexts();
    }

    private void onChooseDirClick(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(lang.get("dialog.choose_dir_title"));
        if (selectedDir.exists()) {
            chooser.setInitialDirectory(selectedDir);
        }
        File dir = chooser.showDialog(stage);
        if (dir != null) {
            selectedDir = dir;
            settingsBar.setOutputDir(dir.getAbsolutePath());
        }
    }

    private void onDownloadClick() {
        String url = topBar.getUrlField().getText().trim();
        if (url.isEmpty()) {
            bottomPanel.appendLog(lang.get("error.empty_url") + "\n");
            return;
        }

        boolean isDuplicate = downloadQueue.stream()
                .anyMatch(i -> i.getConfig().url().equals(url) &&
                        i.getStatus() != DownloadStatus.ERROR &&
                        i.getStatus() != DownloadStatus.CANCELLED);
        if (isDuplicate) {
            bottomPanel.appendLog(lang.get("error.duplicate_in_queue") + "\n");
            return;
        }

        Preset selectedPreset = settingsBar.getSelectedPreset();
        if (selectedPreset == null) {
            bottomPanel.appendLog(lang.get("error.no_preset") + "\n");
            return;
        }

        DownloadConfig config = new DownloadConfig(
                url,
                selectedDir.getAbsolutePath(),
                settingsBar.getProxyText(),
                selectedPreset
        );

        DownloadItem item = new DownloadItem(url, config);
        downloadQueue.add(item);
        topBar.getUrlField().clear();
        bottomPanel.appendLog(lang.get("status.added_to_queue") + "\n");
        queueManager.processQueue();
    }

    private void onUpdateClick() {
        topBar.getUpdateButton().setDisable(true);
        topBar.getDownloadButton().setDisable(true);
        bottomPanel.appendLog("\n" + lang.get("status.checking_updates") + "\n");

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                return ytDlpService.update(
                        line -> Platform.runLater(() -> bottomPanel.appendLog(line + "\n"))
                );
            }
        };

        task.setOnSucceeded(event -> {
            bottomPanel.appendLog(lang.get("status.done_code") + task.getValue() + " ===\n");
            topBar.getUpdateButton().setDisable(false);
            topBar.getDownloadButton().setDisable(false);
        });

        task.setOnFailed(event -> {
            bottomPanel.appendLog(lang.get("status.error_prefix") + task.getException().getMessage() + " ===\n");
            topBar.getUpdateButton().setDisable(false);
            topBar.getDownloadButton().setDisable(false);
        });

        new Thread(task).start();
    }

    private void onSettingsClick() {
        PresetEditorDialog dialog = new PresetEditorDialog(
                (Stage) settingsBar.getSettingsButton().getScene().getWindow(),
                settingsBar.getPresetStorage()
        );
        dialog.show();
        settingsBar.loadPresets();
    }

    private void updateTexts() {
        bottomPanel.updateTexts();
        topBar.updateTexts();
        settingsBar.updateTexts();
        queueTable.updateTexts();
        contextMenu.updateTexts();

        settingsBar.setOutputDir(selectedDir.getName());

        if (topBar.getScene() != null) {
            Stage stage = (Stage) topBar.getScene().getWindow();
            stage.setTitle(lang.get("app.title"));
        }
    }
}