package com.yih.javafxwebview;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSObject;
import java.io.*;

@Slf4j
public class FXApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException{

        InputStreamReader inputStreamReader = new InputStreamReader(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties"));
        String ff = Thread.currentThread().getContextClassLoader().getResource("application.properties").getPath();
        BufferedReader reader = new BufferedReader(inputStreamReader);

        String environment = "dev";
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("spring.profiles.active")) {
                log.info(">>>>>>> line =  {}", line);
                environment = line.split("=")[1].trim();
//                break;
            }
        }

        primaryStage.setTitle("JavaFX WebView Example");

        WebView webView = new WebView();

        log.info(">>>>>>>>>> environment = {}", environment);

        if ("prd".equals(environment)) {
            webView.getEngine().load("http://127.0.0.1:30000/webview/#/");
        } else {
            collectLogs(webView);
            webView.getEngine().load("http://localhost:8080/");
        }
        webView.getEngine().setOnAlert(event -> {
            log.info(">>>>> Alert: {}", event.getData());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText(event.getData());
            alert.showAndWait();
        });

        VBox vBox = new VBox(webView);
        Scene scene = new Scene(vBox, 960, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public class JavaBridge {
        public void log(String text)
        {
            log.info(">>>>> Console.log: {}", text);
        }
        public void error(String text)
        {
            log.error(">>>>> Console.error: {}", text);
        }
    }

    //collect console.log into java
    private void collectLogs (final WebView webView) {
        WebEngine engine = webView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) ->
        {
            JSObject window = (JSObject) engine.executeScript("window");
            JavaBridge bridge = new JavaBridge();
            window.setMember("java", bridge);
            engine.executeScript("console.log = (message) => {" +
                    "  java.log(message);" +
                    "};");
            engine.executeScript("console.error = (message) => {" +
                    "  java.error(message);" +
                    "};");
        });
    }


}
