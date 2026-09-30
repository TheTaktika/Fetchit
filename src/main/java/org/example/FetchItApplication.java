package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class FetchItApplication extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("FetchIt — приложение запущено");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("FetchIt");
        stage.setScene(scene);
        stage.show();
    }
}