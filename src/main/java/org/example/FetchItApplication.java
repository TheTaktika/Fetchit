package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.example.model.*;
import org.example.service.PresetStorage;
import org.example.service.QueueManager;
import org.example.service.YtDlpService;
import org.example.ui.LanguageManager;
import org.example.ui.PresetEditorDialog;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class FetchItApplication extends Application {
    private final ComboBox<String> langComboBox = new ComboBox<>();
    private final LanguageManager lang = LanguageManager.getInstance();
    private final TitledPane logsPane = new TitledPane();
    private QueueManager queueManager;

    private final TextField urlField = new TextField();
    private final TextField proxyField = new TextField();
    private final Label outputDirLabel = new Label();
    private final Button chooseDirButton = new Button();
    private final Button downloadButton = new Button();
    private final Button updateButton = new Button();

    private final ComboBox<Preset> presetComboBox = new ComboBox<>();
    private final Button settingsButton = new Button();
    private final PresetStorage presetStorage = new PresetStorage();
    private List<Preset> presets;

    private final TableView<DownloadItem> queueTable = new TableView<>();
    private final ObservableList<DownloadItem> downloadQueue = FXCollections.observableArrayList();
    private final TableColumn<DownloadItem, String> titleCol = new TableColumn<>();
    private final TableColumn<DownloadItem, DownloadItem.Status> statusCol = new TableColumn<>();
    private final TableColumn<DownloadItem, Number> progressCol = new TableColumn<>();

    private final TextArea statsArea = new TextArea();
    private final TabPane bottomTabs = new TabPane();
    private final Tab logsTab = new Tab();
    private final Tab statsTab = new Tab();
    private final SplitPane splitPane = new SplitPane();

    private final TextArea outputArea = new TextArea();

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
        queueManager = new QueueManager(downloadQueue, ytDlpService, this::appendOutput);

        try {
            var iconStream = getClass().getResourceAsStream("/icon.png");
            if (iconStream != null) stage.getIcons().add(new Image(iconStream));
        } catch (Exception e) {
            System.err.println(lang.get("app.icon") + e.getMessage());
        }

        HBox topBar = buildTopBar();
        HBox settingsBar = buildSettingsBar();

        TableView<DownloadItem> table = buildQueueTable();
        ContextMenu contextMenu = buildContextMenu();
        table.setContextMenu(contextMenu);
        buildBottomPane();

        VBox root = new VBox(topBar, settingsBar, splitPane);
        root.setPadding(new Insets(0));

        Scene scene = new Scene(root, 1000, 650);
        stage.setScene(scene);
        stage.show();

        chooseDirButton.setOnAction(e -> onChooseDirClick(stage));
        downloadButton.setOnAction(e -> onDownloadClick());
        updateButton.setOnAction(e -> onUpdateClick());
        settingsButton.setOnAction(e -> onSettingsClick());

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
            outputDirLabel.setText(lang.get("label.folder") + " " + dir.getAbsolutePath());
        }
    }

    private void onDownloadClick() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            appendOutput(lang.get("error.empty_url") + "\n");
            return;
        }

        Preset selectedPreset = presetComboBox.getValue();
        if (selectedPreset == null) {
            appendOutput(lang.get("error.no_preset") + "\n");
            return;
        }

        DownloadConfig config = new DownloadConfig(
                url,
                selectedDir.getAbsolutePath(),
                proxyField.getText(),
                selectedPreset
        );

        DownloadItem item = new DownloadItem(url, config);
        downloadQueue.add(item);
        urlField.clear();
        appendOutput(lang.get("status.added_to_queue") + "\n");
        queueManager.processQueue();
    }

    private void onUpdateClick() {
        downloadButton.setDisable(true);
        updateButton.setDisable(true);
        appendOutput("\n" + lang.get("status.checking_updates") + "\n");

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                return ytDlpService.update(
                        line -> Platform.runLater(() -> appendOutput(line + "\n"))
                );
            }
        };

        task.setOnSucceeded(event -> {
            appendOutput(lang.get("status.done_code") + task.getValue() + " ===\n");
            downloadButton.setDisable(false);
            updateButton.setDisable(false);
        });

        task.setOnFailed(event -> {
            appendOutput(lang.get("status.error_prefix") + task.getException().getMessage() + " ===\n");
            downloadButton.setDisable(false);
            updateButton.setDisable(false);
        });

        new Thread(task).start();
    }

    private void appendOutput(String text) {
        outputArea.appendText(text);
    }

    private void onSettingsClick() {
        PresetEditorDialog dialog = new PresetEditorDialog(
                (Stage) settingsButton.getScene().getWindow(),
                presetStorage
        );
        dialog.show();

        presets = dialog.getPresets();
        presetComboBox.setItems(FXCollections.observableArrayList(presets));
        if (!presets.isEmpty()) {
            presetComboBox.getSelectionModel().selectFirst();
        }
    }

    private void updateTexts() {
        urlField.setPromptText(lang.get("field.url"));
        proxyField.setPromptText(lang.get("field.proxy"));
        titleCol.setText(lang.get("column.title"));
        statusCol.setText(lang.get("column.status"));
        progressCol.setText(lang.get("column.progress"));
        queueTable.setPlaceholder(new Label(lang.get("status.queue_empty")));
        queueTable.refresh();

        downloadButton.setText(lang.get("button.download"));
        updateButton.setText(lang.get("button.update"));
        settingsButton.setText(lang.get("button.settings"));
        chooseDirButton.setText(lang.get("button.folder"));

        pauseMenu.setText(lang.get("menu.pause"));
        resumeMenu.setText(lang.get("menu.resume"));
        cancelMenu.setText(lang.get("menu.cancel"));
        removeMenu.setText(lang.get("menu.remove"));
        moveUpMenu.setText(lang.get("menu.move_up"));
        moveDownMenu.setText(lang.get("menu.move_down"));

        logsTab.setText(lang.get("tab.logs"));
        statsTab.setText(lang.get("tab.stats"));

        outputDirLabel.setText(lang.get("label.folder") + " " + selectedDir.getName());
        logsPane.setText(lang.get("pane.logs"));

        if (downloadButton.getScene() != null) {
            Stage stage = (Stage) downloadButton.getScene().getWindow();
            stage.setTitle(lang.get("app.title"));
        }
    }

    private HBox buildTopBar() {
        urlField.setPromptText(lang.get("field.url"));
        HBox.setHgrow(urlField, Priority.ALWAYS);

        langComboBox.getItems().addAll("RU", "EN");
        langComboBox.setValue(lang.getCurrentLocale().getLanguage().equals("ru") ? "RU" : "EN");
        langComboBox.setPrefWidth(70);
        langComboBox.setOnAction(e -> {
            String selected = langComboBox.getValue();
            Locale newLocale = selected.equals("RU") ? Locale.of("ru") : Locale.of("en");
            lang.setLocale(newLocale);
            updateTexts();
        });

        HBox topBar = new HBox(10, urlField, downloadButton, updateButton, langComboBox);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(10, 10, 5, 10));
        return topBar;
    }

    private HBox buildSettingsBar() {
        proxyField.setPromptText(lang.get("field.proxy"));
        proxyField.setPrefWidth(300);

        presets = presetStorage.load();
        ObservableList<Preset> presetItems = FXCollections.observableArrayList(presets);
        presetComboBox.setItems(presetItems);
        presetComboBox.setPrefWidth(200);

        presetComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Preset item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        presetComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Preset item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        if (!presetItems.isEmpty()) {
            presetComboBox.getSelectionModel().selectFirst();
        }

        HBox settingsBar = new HBox(10,
                chooseDirButton, outputDirLabel,
                new Label("|"),
                proxyField,
                new Label("|"),
                new Label(lang.get("label.preset")), presetComboBox, settingsButton
        );
        settingsBar.setAlignment(Pos.CENTER_LEFT);
        settingsBar.setPadding(new Insets(5, 10, 5, 10));
        return settingsBar;
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
        queueTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                statsArea.textProperty().bind(newSelection.summaryProperty());
            } else {
                statsArea.textProperty().unbind();
                statsArea.clear();
            }
        });
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

    private SplitPane buildBottomPane() {
        outputArea.setEditable(false);
        outputArea.setWrapText(false);
        logsTab.setContent(outputArea);
        logsTab.setClosable(false);

        statsArea.setEditable(false);
        statsArea.setWrapText(true);
        statsArea.setStyle("-fx-font-family: monospace;");
        statsTab.setContent(statsArea);
        statsTab.setClosable(false);

        bottomTabs.getTabs().addAll(logsTab, statsTab);

        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.getItems().addAll(queueTable, bottomTabs);
        splitPane.setDividerPositions(0.65);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        return splitPane;
    }
}