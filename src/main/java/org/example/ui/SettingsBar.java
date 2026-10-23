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
    private final PresetStorage presetStorage = new PresetStorage();
    private final LanguageManager lang = LanguageManager.getInstance();
    private List<Preset> presets;

    public SettingsBar() {
        super(10);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(5, 10, 5, 10));

        proxyField.setPrefWidth(300);
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

    public void loadPresets() {
        presets = presetStorage.load();
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

    public Preset getSelectedPreset() { return presetComboBox.getValue(); }
    public String getProxyText() { return proxyField.getText(); }
    public Button getChooseDirButton() { return chooseDirButton; }
    public Button getSettingsButton() { return settingsButton; }
    public PresetStorage getPresetStorage() { return presetStorage; }
}