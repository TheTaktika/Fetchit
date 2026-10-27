package org.example.ui;

import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.model.DownloadItem;

public class BottomPanel extends SplitPane {
    private final TextArea logsArea = new TextArea();
    private final TextArea statsArea = new TextArea();
    private final TabPane tabs = new TabPane();
    private final Tab logsTab = new Tab();
    private final Tab statsTab = new Tab();
    private final LanguageManager lang = LanguageManager.getInstance();

    public BottomPanel(TableView<?> queueTable) {
        logsArea.setEditable(false);
        logsArea.setWrapText(false);
        logsTab.setContent(logsArea);
        logsTab.setClosable(false);

        statsArea.setEditable(false);
        statsArea.setWrapText(true);
        statsArea.setStyle("-fx-font-family: monospace;");
        statsTab.setContent(statsArea);
        statsTab.setClosable(false);

        tabs.getTabs().addAll(logsTab, statsTab);

        this.setOrientation(Orientation.VERTICAL);
        this.getItems().addAll(queueTable, tabs);
        this.setDividerPositions(0.65);
        VBox.setVgrow(this, Priority.ALWAYS);

        updateTexts();
    }

    public void appendLog(String text) {
        if (!text.endsWith("\n")) {
            logsArea.appendText(text + "\n");
        } else {
            logsArea.appendText(text);
        }
    }

    public void bindStatsTo(DownloadItem item) {
        statsArea.textProperty().unbind();
        if (item != null) {
            statsArea.textProperty().bind(item.summaryProperty());
        } else {
            statsArea.textProperty().unbind();
            statsArea.clear();
        }
    }


    public void updateTexts() {
        logsTab.setText(lang.get("tab.logs"));
        statsTab.setText(lang.get("tab.stats"));
    }
}