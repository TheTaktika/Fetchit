package org.example.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class PresetFormPane extends VBox {
    private final LanguageManager lang = LanguageManager.getInstance();

    public final TextField nameField = new TextField();
    public final ComboBox<String> formatBox = new ComboBox<>();
    public final ComboBox<String> audioFormatBox = new ComboBox<>();
    public final Spinner<Integer> audioQualitySpinner = new Spinner<>(0, 10, 0);
    public final ComboBox<String> videoQualityBox = new ComboBox<>();
    public final ComboBox<String> mergeFormatBox = new ComboBox<>();
    public final CheckBox embedThumbnailBox = new CheckBox();
    public final CheckBox addMetadataBox = new CheckBox();
    public final CheckBox embedChaptersBox = new CheckBox();
    public final CheckBox writeSubsBox = new CheckBox();
    public final CheckBox embedSubsBox = new CheckBox();
    public final TextField subLangsField = new TextField();
    public final Spinner<Integer> fragmentsSpinner = new Spinner<>(1, 16, 1);
    public final TextField limitRateField = new TextField();
    public final Spinner<Integer> retriesSpinner = new Spinner<>(0, 50, 10);
    public final TextField sponsorblockRemoveField = new TextField();
    public final CheckBox keepVideoBox = new CheckBox();
    public final CheckBox noOverwritesBox = new CheckBox();
    public final ComboBox<String> cookiesBox = new ComboBox<>();
    public final CheckBox splitChaptersBox = new CheckBox();

    public PresetFormPane() {
        setupControls();
        applyTooltips();
        buildLayout();
    }

    private void setupControls() {
        formatBox.setItems(FXCollections.observableArrayList("audio", "video"));
        audioFormatBox.setItems(FXCollections.observableArrayList("mp3", "m4a", "flac", "opus", "wav"));
        videoQualityBox.setItems(FXCollections.observableArrayList("best", "2160p", "1440p", "1080p", "720p", "480p", "360p"));
        mergeFormatBox.setItems(FXCollections.observableArrayList("mp4", "mkv", "webm"));
        audioQualitySpinner.setEditable(true);
        fragmentsSpinner.setEditable(true);
        retriesSpinner.setEditable(true);

        embedThumbnailBox.setText(lang.get("preset.embed_thumbnail"));
        addMetadataBox.setText(lang.get("preset.add_metadata"));
        embedChaptersBox.setText(lang.get("preset.embed_chapters"));
        splitChaptersBox.setText(lang.get("preset.split_chapters"));
        writeSubsBox.setText(lang.get("preset.write_subs"));
        embedSubsBox.setText(lang.get("preset.embed_subs"));
        keepVideoBox.setText(lang.get("preset.keep_video"));
        noOverwritesBox.setText(lang.get("preset.no_overwrites"));

        cookiesBox.setItems(FXCollections.observableArrayList("none", "chrome", "firefox", "edge", "opera", "brave"));
        cookiesBox.getSelectionModel().selectFirst();
        cookiesBox.setCellFactory(lv -> createCookiesCell());
        cookiesBox.setButtonCell(createCookiesCell());

        subLangsField.setPromptText(lang.get("preset.prompt_sublangs"));
        limitRateField.setPromptText(lang.get("preset.prompt_limit_rate"));
        sponsorblockRemoveField.setPromptText(lang.get("preset.prompt_sponsorblock"));
    }

    private ListCell<String> createCookiesCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if ("none".equals(item)) {
                    setText(lang.get("preset.cookies_none"));
                } else {
                    setText(item);
                }
            }
        };
    }

    private void applyTooltips() {
        nameField.setTooltip(buildTooltip(lang.get("tooltip.name")));
        formatBox.setTooltip(buildTooltip(lang.get("tooltip.format")));
        audioFormatBox.setTooltip(buildTooltip(lang.get("tooltip.audio_format")));
        audioQualitySpinner.setTooltip(buildTooltip(lang.get("tooltip.audio_quality")));
        videoQualityBox.setTooltip(buildTooltip(lang.get("tooltip.video_quality")));
        mergeFormatBox.setTooltip(buildTooltip(lang.get("tooltip.merge_format")));
        embedThumbnailBox.setTooltip(buildTooltip(lang.get("tooltip.embed_thumbnail")));
        addMetadataBox.setTooltip(buildTooltip(lang.get("tooltip.add_metadata")));
        embedChaptersBox.setTooltip(buildTooltip(lang.get("tooltip.embed_chapters")));
        writeSubsBox.setTooltip(buildTooltip(lang.get("tooltip.write_subs")));
        embedSubsBox.setTooltip(buildTooltip(lang.get("tooltip.embed_subs")));
        subLangsField.setTooltip(buildTooltip(lang.get("tooltip.sub_langs")));
        fragmentsSpinner.setTooltip(buildTooltip(lang.get("tooltip.fragments")));
        limitRateField.setTooltip(buildTooltip(lang.get("tooltip.limit_rate")));
        retriesSpinner.setTooltip(buildTooltip(lang.get("tooltip.retries")));
        sponsorblockRemoveField.setTooltip(buildTooltip(lang.get("tooltip.sponsorblock")));
        keepVideoBox.setTooltip(buildTooltip(lang.get("tooltip.keep_video")));
        noOverwritesBox.setTooltip(buildTooltip(lang.get("tooltip.no_overwrites")));
    }

    private Tooltip buildTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(javafx.util.Duration.millis(300));
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(400);
        return tooltip;
    }

    private void buildLayout() {
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
        addRow(grid, row++, "preset.name", nameField);
        addRow(grid, row++, "preset.format", formatBox);
        addRow(grid, row++, "preset.audio_format", audioFormatBox);
        addRow(grid, row++, "preset.audio_quality", audioQualitySpinner);
        addRow(grid, row++, "preset.video_quality", videoQualityBox);
        addRow(grid, row++, "preset.merge_format", mergeFormatBox);
        addSpanRow(grid, row++, embedThumbnailBox);
        addSpanRow(grid, row++, addMetadataBox);
        addSpanRow(grid, row++, embedChaptersBox);
        addSpanRow(grid, row++, writeSubsBox);
        addSpanRow(grid, row++, embedSubsBox);
        addRow(grid, row++, "preset.sub_langs", subLangsField);
        addRow(grid, row++, "preset.fragments", fragmentsSpinner);
        addRow(grid, row++, "preset.limit_rate", limitRateField);
        addRow(grid, row++, "preset.retries", retriesSpinner);
        addRow(grid, row++, "preset.sponsorblock", sponsorblockRemoveField);
        addSpanRow(grid, row++, keepVideoBox);
        addSpanRow(grid, row++, noOverwritesBox);
        addRow(grid, row++, "preset.cookies", cookiesBox);
        addSpanRow(grid, row, splitChaptersBox);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        HBox.setHgrow(this, Priority.ALWAYS);
        getChildren().add(scroll);
    }

    private void addRow(GridPane grid, int row, String labelKey, Node control) {
        grid.add(new Label(lang.get(labelKey)), 0, row);
        grid.add(control, 1, row);
    }

    private void addSpanRow(GridPane grid, int row, Node control) {
        grid.add(control, 0, row, 2, 1);
    }
}