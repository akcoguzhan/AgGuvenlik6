package org.uludag.bmb;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

import com.dropbox.core.json.JsonReader.FileLoadException;

import org.uludag.bmb.controller.scene.StartupSceneController;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException, FileLoadException {
        new StartupSceneController().displayScene(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}