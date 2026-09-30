package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.service.YtDlpService;

public class FetchItApplication extends Application {

    @Override
    public void start(Stage stage) {
        Label label = new Label("FetchIt — приложение запущено");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("FetchIt");
        stage.setScene(scene);
        stage.show();

        // ВРЕМЕННЫЙ ТЕСТ — потом уберём
        testDownload();
    }

    private void testDownload() {
        YtDlpService service = new YtDlpService();
        service.download(
                "https://youtube.com/playlist?list=PLZbtf2U3P5mC4_51BTLmcq88DXwGzbTCm",
                "test_output",
                System.out::println  // вывод в консоль
        );
    }
}