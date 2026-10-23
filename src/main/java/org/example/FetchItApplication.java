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
import java.util.List;

public class FetchItApplication extends Application {
    private BottomPanel bottomPanel;
    private TopBar topBar;
    private SettingsBar settingsBar;

    private final LanguageManager lang = LanguageManager.getInstance();
    private QueueManager queueManager;

    private final TableView<DownloadItem> queueTable = new TableView<>();
    private final ObservableList<DownloadItem> downloadQueue = FXCollections.observableArrayList();
    private final TableColumn<DownloadItem, String> titleCol = new TableColumn<>();
    private final TableColumn<DownloadItem, DownloadItem.Status> statusCol = new TableColumn<>();
    private final TableColumn<DownloadItem, Number> progressCol = new TableColumn<>();

    private final YtDlpService ytDlpService = new YtDlpService();
    private File selectedDir = new File("test_output");

    private final MenuItem cancelMenu = new MenuItem();
    private final MenuItem removeMenu = new MenuItem();
    private final MenuItem pauseMenu = new MenuItem();
    private final MenuItem resumeMenu = new MenuItem();
    private final MenuItem moveUpMenu = new MenuItem();
    private final MenuItem moveDownMenu = new MenuItem();

    @Override
    public void start(Stage stage) {
        queueManager = new QueueManager(downloadQueue, ytDlpService, text -> bottomPanel.appendLog(text));

        try {
            var iconStream = getClass().getResourceAsStream("/icon.png");
            if (iconStream != null) stage.getIcons().add(new Image(iconStream));
        } catch (Exception e) {
            System.err.println(lang.get("app.icon") + e.getMessage());
        }

        topBar = new TopBar(this::updateTexts);
        settingsBar = new SettingsBar();

        queueTable.setItems(downloadQueue);
        buildQueueTable();

        ContextMenu contextMenu = buildContextMenu();
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

        Preset selectedPreset = settingsBar.getSelectedPreset();
        if (selectedPreset == null) {
            bottomPanel.appendLog(lang.get("error.empty_url") + "\n");
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
        titleCol.setText(lang.get("column.title"));
        statusCol.setText(lang.get("column.status"));
        progressCol.setText(lang.get("column.progress"));
        queueTable.setPlaceholder(new Label(lang.get("status.queue_empty")));
        queueTable.refresh();

        pauseMenu.setText(lang.get("menu.pause"));
        resumeMenu.setText(lang.get("menu.resume"));
        cancelMenu.setText(lang.get("menu.cancel"));
        removeMenu.setText(lang.get("menu.remove"));
        moveUpMenu.setText(lang.get("menu.move_up"));
        moveDownMenu.setText(lang.get("menu.move_down"));

        bottomPanel.updateTexts();
        topBar.updateTexts();
        settingsBar.updateTexts();

        settingsBar.setOutputDir(selectedDir.getName());

        if (topBar.getScene() != null) {
            Stage stage = (Stage) topBar.getScene().getWindow();
            stage.setTitle(lang.get("app.title"));
        }
    }

    private TableView<DownloadItem> buildQueueTable() {
        queueTable.setItems(downloadQueue);
        VBox.setVgrow(queueTable, Priority.ALWAYS);

        titleCol.setCellValueFactory(cell -> cell.getValue().titleProperty());
        titleCol.setPrefWidth(300);

        statusCol.setCellValueFactory(cell -> cell.getValue().statusProperty());
        statusCol.setPrefWidth(150);
        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(DownloadItem.Status item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(lang.get("status." + item.name().toLowerCase()));
                }
            }
        });

        progressCol.setCellValueFactory(cell -> cell.getValue().progressProperty());
        progressCol.setPrefWidth(200);
        progressCol.setCellFactory(column -> new TableCell<>() {
            private final ProgressBar progressBar = new ProgressBar(0);

            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    progressBar.setProgress(item.doubleValue() / 100.0);
                    setGraphic(progressBar);
                }
            }
        });

        //noinspection unchecked
        queueTable.getColumns().addAll(titleCol, statusCol, progressCol);
        return queueTable;
    }

    @SuppressWarnings("ExtractMethodRecommender")
    private ContextMenu buildContextMenu() {
        cancelMenu.setOnAction(e -> {
            DownloadItem selected = queueTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selected.cancel();
                queueTable.refresh();
            }
        });

        removeMenu.setOnAction(e -> {
            DownloadItem selected = queueTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (selected.getStatus() != DownloadItem.Status.DONE &&
                        selected.getStatus() != DownloadItem.Status.ERROR &&
                        selected.getStatus() != DownloadItem.Status.CANCELLED) {
                    selected.cancel();
                }
                downloadQueue.remove(selected);
            }
        });

        pauseMenu.setOnAction(e -> {
            DownloadItem selected = queueTable.getSelectionModel().getSelectedItem();
            if (selected != null) queueManager.pauseItem(selected);
        });

        resumeMenu.setOnAction(e -> {
            DownloadItem selected = queueTable.getSelectionModel().getSelectedItem();
            if (selected != null) queueManager.resumeItem(selected);
        });

        moveUpMenu.setOnAction(e -> {
            int index = queueTable.getSelectionModel().getSelectedIndex();
            if (index > 0) {
                java.util.Collections.swap(downloadQueue, index, index - 1);
                queueTable.getSelectionModel().select(index - 1);
            }
        });

        moveDownMenu.setOnAction(e -> {
            int index = queueTable.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < downloadQueue.size() - 1) {
                java.util.Collections.swap(downloadQueue, index, index + 1);
                queueTable.getSelectionModel().select(index + 1);
            }
        });

        ContextMenu contextMenu = new ContextMenu(pauseMenu, resumeMenu, cancelMenu, moveUpMenu, moveDownMenu, removeMenu);

        contextMenu.setOnShowing(e -> {
            DownloadItem selected = queueTable.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            boolean isActive = (selected.getStatus() == DownloadItem.Status.FETCHING ||
                    selected.getStatus() == DownloadItem.Status.DOWNLOADING ||
                    selected.getStatus() == DownloadItem.Status.PROCESSING);

            pauseMenu.setDisable(!isActive);
            cancelMenu.setDisable(!isActive && selected.getStatus() != DownloadItem.Status.WAITING);
            resumeMenu.setDisable(selected.getStatus() != DownloadItem.Status.PAUSED &&
                    selected.getStatus() != DownloadItem.Status.CANCELLED &&
                    selected.getStatus() != DownloadItem.Status.ERROR);
        });
        return contextMenu;
    }
}