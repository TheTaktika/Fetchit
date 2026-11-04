package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FetchItApplication extends Application {

    @Override
    public void start(Stage stage) {
        MainController mainController = new MainController(stage);

        Scene scene = new Scene(mainController.getRoot(), 1000, 650);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        System.exit(0);
    }
}