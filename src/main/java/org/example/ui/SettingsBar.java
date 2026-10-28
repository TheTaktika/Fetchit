package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.example.model.Preset;
import org.example.service.PresetStorage;

import java.util.List;

public class SettingsBar extends HBox {
    private final Label outputDirLabel = new Label();
    private final Button chooseDirButton = new Button();
    private final TextField proxyField = new TextField();
    private final ComboBox<Preset> presetComboBox = new ComboBox<>();
    private final Button settingsButton = new Button();
    private final PresetStorage presetStorage;
    private final LanguageManager lang = LanguageManager.getInstance();

    public SettingsBar(PresetStorage presetStorage) {
        super(10);
        this.presetStorage = presetStorage;

        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(5, 10, 5, 10));

        proxyField.setPrefWidth(300);
        presetComboBox.setPrefWidth(200);

        presetComboBox.setCellFactory(lv -> createPresetCell());
        presetComboBox.setButtonCell(createPresetCell());

        loadPresets();

        getChildren().addAll(
                chooseDirButton, outputDirLabel,
                new Label("|"),
                proxyField,
                new Label("|"),
                new Label(lang.get("label.preset")), presetComboBox, settingsButton
        );
        updateTexts();
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

    public void loadPresets() {
        List<Preset> presets = presetStorage.load();
        presetComboBox.getItems().setAll(presets);
        if (!presets.isEmpty() && presetComboBox.getValue() == null) {
            presetComboBox.getSelectionModel().selectFirst();
        }
    }

    public void updateTexts() {
        proxyField.setPromptText(lang.get("field.proxy"));
        chooseDirButton.setText(lang.get("button.folder"));
        settingsButton.setText(lang.get("button.settings"));
    }

    public void setOutputDir(String dirName) {
        outputDirLabel.setText(lang.get("label.folder") + " " + dirName);
    }

    public Preset getSelectedPreset() {
        return presetComboBox.getValue();
    }

    public String getProxyText() {
        return proxyField.getText();
    }

    public void setOnChooseDirAction(Runnable action) {
        chooseDirButton.setOnAction(e -> action.run());
    }

    public void setOnSettingsAction(Runnable action) {
        settingsButton.setOnAction(e -> action.run());
    }
}