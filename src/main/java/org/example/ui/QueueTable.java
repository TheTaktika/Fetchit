package org.example.ui;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.model.DownloadItem;

public class QueueTable extends TableView<DownloadItem> {
    private final LanguageManager lang = LanguageManager.getInstance();

    private final TableColumn<DownloadItem, Image> thumbnailCol = new TableColumn<>();
    private final TableColumn<DownloadItem, String> titleCol = new TableColumn<>();
    private final TableColumn<DownloadItem, DownloadItem.Status> statusCol = new TableColumn<>();
    private final TableColumn<DownloadItem, Number> progressCol = new TableColumn<>();

    public QueueTable() {
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        thumbnailCol.setCellValueFactory(cell -> cell.getValue().thumbnailProperty());
        thumbnailCol.setPrefWidth(100);
        thumbnailCol.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    imageView.setImage(item);
                    imageView.setFitWidth(100);
                    imageView.setFitHeight(56);
                    imageView.setPreserveRatio(true);
                    setGraphic(imageView);
                }
            }
        });

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
        getColumns().addAll(thumbnailCol, titleCol, statusCol, progressCol);
    }

    public void updateTexts() {
        thumbnailCol.setText(lang.get("column.thumbnail"));
        titleCol.setText(lang.get("column.title"));
        statusCol.setText(lang.get("column.status"));
        progressCol.setText(lang.get("column.progress"));
        setPlaceholder(new Label(lang.get("status.queue_empty")));
        refresh();
    }
}