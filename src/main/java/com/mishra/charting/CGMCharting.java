package com.mishra.charting;
/**
 * Test application to inject javascript and d3
 * into the java webview
 * Created by Scott on 1/23/2016.
 */

import com.mishra.util.Driver;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.hibernate.Session;

import java.io.IOException;

public class CGMCharting extends Application {

    Driver driver;

    @Override
    public void init() throws Exception {
        super.init();
        driver = new Driver();
        Session session = driver.openSession();
        if(session != null) {
            driver.getAllData(session);
            System.out.println("Total number of data points: " + driver.getCgData().size());
        }
        else{
            System.out.println("Session was null");
            driver.close();
            System.exit(1);
        }
    }


    @Override
    public void start(Stage primaryStage) throws InterruptedException {
        FXMLLoader loader;
        try {
            loader = new FXMLLoader(getClass().getResource("DataTable.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 600,600);
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(event -> driver.close());
            primaryStage.show();

            DataTableController controller = loader.getController();
            driver.getCgData().forEach(controller::addData);
            controller.createChart(driver.getCgData());
        } catch (IOException e) {
            System.out.println("Couldn't find the DataTable.fxml");
            e.printStackTrace();
            driver.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
