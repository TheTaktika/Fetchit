package org.example.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import java.util.Locale;

public class TopBar extends HBox {
    private final TextField urlField = new TextField();
    private final Button downloadButton = new Button();
    private final Button updateButton = new Button();
    private final ComboBox<String> langComboBox = new ComboBox<>();
    private final LanguageManager lang = LanguageManager.getInstance();

    public TopBar(Runnable onLanguageChange) {
        super(10);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(10, 10, 5, 10));

        HBox.setHgrow(urlField, Priority.ALWAYS);

        langComboBox.getItems().addAll("RU", "EN");
        langComboBox.setValue(lang.getCurrentLocale().getLanguage().equals("ru") ? "RU" : "EN");
        langComboBox.setPrefWidth(70);

        langComboBox.setOnAction(e -> {
            String selected = langComboBox.getValue();
            Locale newLocale = selected.equals("RU") ? Locale.of("ru") : Locale.of("en");
            lang.setLocale(newLocale);
            updateTexts();
            onLanguageChange.run();
        });

        getChildren().addAll(urlField, downloadButton, updateButton, langComboBox);
        updateTexts();
    }

    public void updateTexts() {
        urlField.setPromptText(lang.get("field.url"));
        downloadButton.setText(lang.get("button.download"));
        updateButton.setText(lang.get("button.update"));
    }

    public TextField getUrlField() { return urlField; }
    public Button getDownloadButton() { return downloadButton; }
    public Button getUpdateButton() { return updateButton; }
}