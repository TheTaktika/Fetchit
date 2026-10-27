package org.example.ui;

import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import org.example.model.DownloadItem;
import org.example.service.QueueManager;

import java.util.Collections;

public class QueueContextMenu extends ContextMenu {
    private final LanguageManager lang = LanguageManager.getInstance();

    private final MenuItem cancelMenu = new MenuItem();
    private final MenuItem removeMenu = new MenuItem();
    private final MenuItem pauseMenu = new MenuItem();
    private final MenuItem resumeMenu = new MenuItem();
    private final MenuItem moveUpMenu = new MenuItem();
    private final MenuItem moveDownMenu = new MenuItem();

    public QueueContextMenu(TableView<DownloadItem> table, ObservableList<DownloadItem> queue, QueueManager queueManager) {
        super();
        getItems().addAll(pauseMenu, resumeMenu, cancelMenu, moveUpMenu, moveDownMenu, removeMenu);
        updateTexts();

        cancelMenu.setOnAction(e -> {
            DownloadItem selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                queueManager.cancelItem(selected);
                table.refresh();
            }
        });

        removeMenu.setOnAction(e -> {
            DownloadItem selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (selected.getStatus() != DownloadItem.Status.DONE &&
                        selected.getStatus() != DownloadItem.Status.ERROR &&
                        selected.getStatus() != DownloadItem.Status.CANCELLED) {
                    queueManager.cancelItem(selected);
                }
                queue.remove(selected);
            }
        });

        pauseMenu.setOnAction(e -> {
            DownloadItem selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                queueManager.pauseItem(selected);
            }
        });

        resumeMenu.setOnAction(e -> {
            DownloadItem selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                queueManager.resumeItem(selected);
            }
        });

        moveUpMenu.setOnAction(e -> {
            int index = table.getSelectionModel().getSelectedIndex();
            if (index > 0) {
                Collections.swap(queue, index, index - 1);
                table.getSelectionModel().select(index - 1);
            }
        });

        moveDownMenu.setOnAction(e -> {
            int index = table.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < queue.size() - 1) {
                Collections.swap(queue, index, index + 1);
                table.getSelectionModel().select(index + 1);
            }
        });

        setOnShowing(e -> {
            DownloadItem selected = table.getSelectionModel().getSelectedItem();
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
    }

    public void updateTexts() {
        pauseMenu.setText(lang.get("menu.pause"));
        resumeMenu.setText(lang.get("menu.resume"));
        cancelMenu.setText(lang.get("menu.cancel"));
        removeMenu.setText(lang.get("menu.remove"));
        moveUpMenu.setText(lang.get("menu.move_up"));
        moveDownMenu.setText(lang.get("menu.move_down"));
    }
}