package org.example.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.Preset;
import org.example.service.PresetStorage;

public class PresetEditorDialog {
    private final LanguageManager lang = LanguageManager.getInstance();

    private final Stage stage;
    private final PresetStorage storage;
    private final ObservableList<Preset> presets;
    private final ListView<Preset> presetList = new ListView<>();

    private final PresetFormBinder formBinder;

    public PresetEditorDialog(Stage owner, PresetStorage storage) {
        this.storage = storage;
        this.presets = FXCollections.observableArrayList(storage.load());
        PresetFormPane formPane = new PresetFormPane();
        this.formBinder = new PresetFormBinder(formPane);

        this.stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle(lang.get("preset.title"));

        presetList.setItems(presets);
        presetList.setPrefWidth(200);
        presetList.setCellFactory(lv -> createPresetCell());
        presetList.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> formBinder.fill(sel)
        );

        Button createBtn = new Button(lang.get("preset.btn_create"));
        Button deleteBtn = new Button(lang.get("preset.btn_delete"));
        Button saveBtn = new Button(lang.get("preset.btn_save"));
        Button closeBtn = new Button(lang.get("preset.btn_close"));

        createBtn.setOnAction(e -> onCreate());
        deleteBtn.setOnAction(e -> onDelete());
        saveBtn.setOnAction(e -> onSave());
        closeBtn.setOnAction(e -> stage.close());

        HBox buttons = new HBox(10, createBtn, deleteBtn, saveBtn, closeBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(10));

        HBox main = new HBox(10, presetList, formPane);
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

    private ListCell<Preset> createPresetCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Preset item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        };
    }

    private void onCreate() {
        Preset p = Preset.defaultAudioPreset();
        p.setName(lang.get("preset.new_name"));
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
            showAlert(lang.get("preset.alert_select"));
            return;
        }
        formBinder.apply(selected);
        presetList.refresh();
        storage.save(presets);
        showAlert(lang.get("preset.alert_saved"));
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.initOwner(stage);
        alert.showAndWait();
    }

    public void show() {
        stage.showAndWait();
    }
}