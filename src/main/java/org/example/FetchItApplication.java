package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.model.*;
import org.example.service.PresetStorage;
import org.example.service.QueueManager;
import org.example.service.YtDlpService;
import org.example.ui.*;

import java.io.File;

public class FetchItApplication extends Application {
    private final BottomPanel bottomPanel;
    private final TopBar topBar;
    private final SettingsBar settingsBar;
    private final QueueTable queueTable = new QueueTable();
    private final QueueContextMenu contextMenu;
    private final VBox root;

    private final LanguageManager lang = LanguageManager.getInstance();
    private final QueueManager queueManager;
    private final ObservableList<DownloadItem> downloadQueue = FXCollections.observableArrayList();
    private final YtDlpService ytDlpService = new YtDlpService();
    private final PresetStorage presetStorage = new PresetStorage();
    private File selectedDir = new File("downloads");

    public FetchItApplication() {
        this.bottomPanel = new BottomPanel(queueTable);
        this.queueManager = new QueueManager(downloadQueue, ytDlpService, text -> bottomPanel.appendLog(text));
        this.contextMenu = new QueueContextMenu(queueTable, downloadQueue, queueManager);

        this.topBar = new TopBar(this::updateTexts);
        this.settingsBar = new SettingsBar(presetStorage);

        this.root = new VBox(topBar, settingsBar, bottomPanel);
    }

    @Override
    public void start(Stage stage) {
        loadIcon(stage);
        setupSelectedDir();

        queueTable.setItems(downloadQueue);
        VBox.setVgrow(queueTable, Priority.ALWAYS);
        queueTable.updateTexts();
        queueTable.setContextMenu(contextMenu);

        queueTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            bottomPanel.bindStatsTo(newSelection);
        });

        root.setPadding(new Insets(0));
        Scene scene = new Scene(root, 1000, 650);
        stage.setScene(scene);
        stage.show();

        bindActions(stage);
        settingsBar.setOutputDir(selectedDir.getAbsolutePath());
        updateTexts();
    }

    private void loadIcon(Stage stage) {
        try {
            var iconStream = getClass().getResourceAsStream("/icon.png");
            if (iconStream != null) stage.getIcons().add(new Image(iconStream));
        } catch (Exception e) {
            System.err.println(lang.get("app.icon") + e.getMessage());
        }
    }

    private void setupSelectedDir() {
        if (!selectedDir.exists()) {
            selectedDir.mkdirs();
        }
    }

    private void bindActions(Stage stage) {
        topBar.setOnDownloadAction(this::onDownloadClick);
        topBar.setOnUpdateAction(this::onUpdateClick);
        settingsBar.setOnChooseDirAction(() -> onChooseDirClick(stage));
        settingsBar.setOnSettingsAction(() -> onSettingsClick(stage));
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
        String url = topBar.getUrl().trim();
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
        topBar.clearUrl();
        bottomPanel.appendLog(lang.get("status.added_to_queue") + "\n");
        queueManager.processQueue();
    }

    private void onUpdateClick() {
        topBar.setControlsDisabled(true);
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
            topBar.setControlsDisabled(false);
        });

        task.setOnFailed(event -> {
            bottomPanel.appendLog(lang.get("status.error_prefix") + task.getException().getMessage() + " ===\n");
            topBar.setControlsDisabled(false);
        });

        new Thread(task).start();
    }

    private void onSettingsClick(Stage stage) {
        PresetEditorDialog dialog = new PresetEditorDialog(stage, presetStorage);
        dialog.show();
        settingsBar.loadPresets();
    }

    private void updateTexts() {
        bottomPanel.updateTexts();
        topBar.updateTexts();
        settingsBar.updateTexts();
        queueTable.updateTexts();

        if (contextMenu != null) {
            contextMenu.updateTexts();
        }

        settingsBar.setOutputDir(selectedDir.getAbsolutePath());

        if (root.getScene() != null) {
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setTitle(lang.get("app.title"));
        }
    }
}