package sample;

import com.sun.javafx.robot.FXRobot;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main extends Application {

    static Stage stage1;

    static String [] connectionParameters;
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("GUI BD Malyushkin v2.3.1");
        Scene scene = new Scene(root, 900, 575);
        primaryStage.setScene(scene);
        stage1 = primaryStage;
        primaryStage.show();

    }

    @Override
    public void init() {

        try {
            Runtime.getRuntime().exec("C:\\pgsql\\start.bat");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedReader br = new BufferedReader(new FileReader("src/sample/connection_parameters.ini"))) {
            int i = 0;
            connectionParameters = new String[4];
            while (br.ready()) {
                connectionParameters[i] = br.readLine();
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        Runtime.getRuntime().exec("C:\\pgsql\\stop.bat");
    }
    public static void main(String[] args) {
        launch(args);
    }
}
