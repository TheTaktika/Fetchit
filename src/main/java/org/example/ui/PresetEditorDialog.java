package org.example.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.Preset;
import org.example.service.PresetStorage;

import java.util.List;

public class PresetEditorDialog {

    private final Stage stage;
    private final PresetStorage storage;
    private final ObservableList<Preset> presets;

    private final ListView<Preset> presetList = new ListView<>();
    private final TextField nameField = new TextField();
    private final ComboBox<String> formatBox = new ComboBox<>();
    private final ComboBox<String> audioFormatBox = new ComboBox<>();
    private final Spinner<Integer> audioQualitySpinner = new Spinner<>(0, 10, 0);
    private final ComboBox<String> videoQualityBox = new ComboBox<>();
    private final ComboBox<String> mergeFormatBox = new ComboBox<>();
    private final CheckBox embedThumbnailBox = new CheckBox("Встроить превью");
    private final CheckBox addMetadataBox = new CheckBox("Добавить метаданные");
    private final CheckBox embedChaptersBox = new CheckBox("Встроить главы");
    private final CheckBox writeSubsBox = new CheckBox("Сохранить субтитры");
    private final CheckBox embedSubsBox = new CheckBox("Встроить субтитры");
    private final TextField subLangsField = new TextField();
    private final Spinner<Integer> fragmentsSpinner = new Spinner<>(1, 16, 1);
    private final TextField limitRateField = new TextField();
    private final Spinner<Integer> retriesSpinner = new Spinner<>(0, 50, 10);
    private final TextField sponsorblockRemoveField = new TextField();
    private final CheckBox keepVideoBox = new CheckBox("Сохранять исходное видео");
    private final CheckBox noOverwritesBox = new CheckBox("Не перезаписывать файлы");

    public PresetEditorDialog(Stage owner, PresetStorage storage) {
        this.storage = storage;
        this.presets = FXCollections.observableArrayList(storage.load());

        this.stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Настройки пресетов");

        presetList.setItems(presets);
        presetList.setPrefWidth(200);
        presetList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Preset item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        presetList.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> fillForm(sel)
        );

        buildForm();

        Button createBtn = new Button("Создать");
        Button deleteBtn = new Button("Удалить");
        Button saveBtn = new Button("Сохранить");
        Button closeBtn = new Button("Закрыть");

        createBtn.setOnAction(e -> onCreate());
        deleteBtn.setOnAction(e -> onDelete());
        saveBtn.setOnAction(e -> onSave());
        closeBtn.setOnAction(e -> stage.close());

        HBox buttons = new HBox(10, createBtn, deleteBtn, saveBtn, closeBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(10));

        HBox main = new HBox(10, presetList, buildFormPane());
        HBox.setHgrow(main, Priority.ALWAYS);
        main.setPadding(new Insets(10));

        VBox root = new VBox(main, buttons);
        VBox.setVgrow(main, Priority.ALWAYS);

        Scene scene = new Scene(root, 750, 600);
        stage.setScene(scene);

        if (!presets.isEmpty()) {
            presetList.getSelectionModel().selectFirst();
        }
    }

    private void buildForm() {
        formatBox.setItems(FXCollections.observableArrayList("audio", "video"));
        audioFormatBox.setItems(FXCollections.observableArrayList("mp3", "m4a", "flac", "opus", "wav"));
        videoQualityBox.setItems(FXCollections.observableArrayList("best", "2160p", "1440p", "1080p", "720p", "480p", "360p"));
        mergeFormatBox.setItems(FXCollections.observableArrayList("mp4", "mkv", "webm"));
        audioQualitySpinner.setEditable(true);
        fragmentsSpinner.setEditable(true);
        retriesSpinner.setEditable(true);
    }

    private VBox buildFormPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(160);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        int row = 0;
        grid.add(new Label("Имя:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("Формат:"), 0, row); grid.add(formatBox, 1, row++);
        grid.add(new Label("Аудио формат:"), 0, row); grid.add(audioFormatBox, 1, row++);
        grid.add(new Label("Аудио качество (0–10):"), 0, row); grid.add(audioQualitySpinner, 1, row++);
        grid.add(new Label("Видео качество:"), 0, row); grid.add(videoQualityBox, 1, row++);
        grid.add(new Label("Формат контейнера:"), 0, row); grid.add(mergeFormatBox, 1, row++);
        grid.add(embedThumbnailBox, 0, row++, 2, 1);
        grid.add(addMetadataBox, 0, row++, 2, 1);
        grid.add(embedChaptersBox, 0, row++, 2, 1);
        grid.add(writeSubsBox, 0, row++, 2, 1);
        grid.add(embedSubsBox, 0, row++, 2, 1);
        grid.add(new Label("Языки субтитров:"), 0, row); grid.add(subLangsField, 1, row++);
        subLangsField.setPromptText("например, ru,en");
        grid.add(new Label("Одновременных фрагментов:"), 0, row); grid.add(fragmentsSpinner, 1, row++);
        grid.add(new Label("Ограничение скорости:"), 0, row); grid.add(limitRateField, 1, row++);
        limitRateField.setPromptText("например, 5M (пусто — без лимита)");
        grid.add(new Label("Повторы:"), 0, row); grid.add(retriesSpinner, 1, row++);
        grid.add(new Label("SponsorBlock (удалить):"), 0, row); grid.add(sponsorblockRemoveField, 1, row++);
        sponsorblockRemoveField.setPromptText("sponsor,selfpromo");
        grid.add(keepVideoBox, 0, row++, 2, 1);
        grid.add(noOverwritesBox, 0, row++, 2, 1);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        VBox pane = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        HBox.setHgrow(pane, Priority.ALWAYS);
        return pane;
    }

    private void fillForm(Preset p) {
        if (p == null) return;
        nameField.setText(p.getName());
        formatBox.setValue(p.getFormat());
        audioFormatBox.setValue(p.getAudioFormat());
        audioQualitySpinner.getValueFactory().setValue(p.getAudioQuality());
        videoQualityBox.setValue(p.getVideoQuality());
        mergeFormatBox.setValue(p.getMergeOutputFormat());
        embedThumbnailBox.setSelected(p.isEmbedThumbnail());
        addMetadataBox.setSelected(p.isAddMetadata());
        embedChaptersBox.setSelected(p.isEmbedChapters());
        writeSubsBox.setSelected(p.isWriteSubtitles());
        embedSubsBox.setSelected(p.isEmbedSubtitles());
        subLangsField.setText(p.getSubtitleLanguages() == null ? "" : p.getSubtitleLanguages());
        fragmentsSpinner.getValueFactory().setValue(p.getConcurrentFragments());
        limitRateField.setText(p.getLimitRate() == null ? "" : p.getLimitRate());
        retriesSpinner.getValueFactory().setValue(p.getRetries());
        sponsorblockRemoveField.setText(p.getSponsorblockRemove() == null ? "" : p.getSponsorblockRemove());
        keepVideoBox.setSelected(p.isKeepVideo());
        noOverwritesBox.setSelected(p.isNoOverwrites());
    }

    private void applyForm(Preset p) {
        p.setName(nameField.getText().isBlank() ? "Без имени" : nameField.getText().trim());
        p.setFormat(formatBox.getValue() == null ? "audio" : formatBox.getValue());
        p.setAudioFormat(audioFormatBox.getValue());
        p.setAudioQuality(audioQualitySpinner.getValue());
        p.setVideoQuality(videoQualityBox.getValue());
        p.setMergeOutputFormat(mergeFormatBox.getValue());
        p.setEmbedThumbnail(embedThumbnailBox.isSelected());
        p.setAddMetadata(addMetadataBox.isSelected());
        p.setEmbedChapters(embedChaptersBox.isSelected());
        p.setWriteSubtitles(writeSubsBox.isSelected());
        p.setEmbedSubtitles(embedSubsBox.isSelected());
        p.setSubtitleLanguages(emptyToNull(subLangsField.getText()));
        p.setConcurrentFragments(fragmentsSpinner.getValue());
        p.setLimitRate(emptyToNull(limitRateField.getText()));
        p.setRetries(retriesSpinner.getValue());
        p.setSponsorblockRemove(emptyToNull(sponsorblockRemoveField.getText()));
        p.setKeepVideo(keepVideoBox.isSelected());
        p.setNoOverwrites(noOverwritesBox.isSelected());
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private void onCreate() {
        Preset p = Preset.defaultAudioPreset();
        p.setName("Новый пресет");
        presets.add(p);
        presetList.getSelectionModel().select(p);
    }

    private void onDelete() {
        Preset selected = presetList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        presets.remove(selected);
        storage.save(presets);
    }

    private void onSave() {
        Preset selected = presetList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Выберите пресет или создайте новый.");
            return;
        }
        applyForm(selected);
        presetList.refresh();
        storage.save(presets);
        showAlert("Сохранено.");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.initOwner(stage);
        alert.showAndWait();
    }

    public void show() {
        stage.showAndWait();
    }

    public List<Preset> getPresets() {
        return presets;
    }
}