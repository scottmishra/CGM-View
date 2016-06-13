package com.mishra.charting;

import com.mishra.cgdata.CGMData;
import com.mishra.cgdata.CGMStats;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.net.URL;
import java.util.*;

/**
 * Controller for loading and setting up the CGM chart
 * Created by Scott on 1/24/2016.
 */
public class DataTableController implements Initializable {

    //    @FXML
//    TableView<CGMData> gcmDataTableView;
//    @FXML
//    TableColumn<CGMData, Integer> glucoseColumn;
//    @FXML
//    TableColumn<CGMData, Double> dateColumn;
//    @FXML
//    TableColumn<CGMData, String> deviceColumn;
//    @FXML
//    TableColumn<CGMData, String> directionColumn;
    @FXML
    VBox graphVBox;
//    @FXML
//    SplitPane splitPane;

    private ObservableList<CGMData> tableList;

    private final NumberAxis yAxis = new NumberAxis();
    private final CategoryAxis xAxis = new CategoryAxis();
    private final XYChart.Series<String, Number> series = new XYChart.Series<>();
    private final XYChart.Series<String, Number> meanSeries = new XYChart.Series<>();
    private LineChart<String, Number> lineChart;

    private final Label meanLabel = new Label();
    private final Label stdLabel = new Label();
    private final Label a1cLabel = new Label();
    private final Label highLabel = new Label();
    private final Label lowLabel = new Label();

    public void initialize(URL location, ResourceBundle resources) {
        tableList = FXCollections.observableArrayList();
//        gcmDataTableView.setItems(tableList);
//        setupColumns();
//        splitPane.setDividerPosition(0,0);
        lineChart = new LineChart<>(xAxis, yAxis);
    }

    void createChart(List<CGMData> dataList) {
        HBox statsBox = new HBox();
        statsBox.getChildren().addAll(meanLabel, stdLabel, a1cLabel, highLabel, lowLabel);
        statsBox.setPadding(new Insets(10.0, 10.0, 10.0, 10.0));
        xAxis.setLabel("Date of update");
        yAxis.setLabel("Glucose Level");
        graphVBox.getChildren().addAll(lineChart, statsBox);
        VBox.setVgrow(lineChart, Priority.ALWAYS);
        statsBox.setAlignment(Pos.CENTER);
        StatisticsService service = new StatisticsService(dataList);
        service.stateProperty().addListener(createServiceSucceededChangeListener(service));
        service.start();
    }

    /**
     * Create a change listener for dealing with the finish of the loading and interp. of all
     * the cgm data
     * @param service service to act on once finished
     * @return change listener
     */
    private ChangeListener<Worker.State> createServiceSucceededChangeListener(final StatisticsService service) {
        return (observable, oldValue, newState) -> {
            switch (newState) {
                case SUCCEEDED:
                    final TreeMap<String, CGMStats> stats = service.getValue();
                    System.out.println("Total days: " + stats.size());
                    DescriptiveStatistics totalStats = new DescriptiveStatistics();
                    Set<String> dates = stats.keySet();
                    List<String> dateArray = new ArrayList<>();
                    dateArray.addAll(dates);
                    String firstDate = dateArray.get(0);
                    String lastDate = dateArray.get(dateArray.size() - 1);
                    setUpChart(stats, totalStats);
                    series.setName("Daily Average Glucose Data");
                    meanLabel.setText(String.format("Mean: %.3f ", totalStats.getMean()));
                    stdLabel.setText(String.format("Std: %.3f ", totalStats.getStandardDeviation()));
                    a1cLabel.setText(String.format("A1C: %.3f ", calcA1C(totalStats)));
                    highLabel.setText(String.format("High: %.3f ", calcHigh(stats)));
                    lowLabel.setText(String.format("Low: %.3f ", calcLow(stats)));
                    meanSeries.getData().add(new XYChart.Data<>(firstDate, totalStats.getMean()));
                    meanSeries.getData().add(new XYChart.Data<>(lastDate, totalStats.getMean()));
                    meanSeries.setName("Total Mean");
                    lineChart.getData().addAll(series, meanSeries);
                    break;
            }
        };
    }

    /**
     * Populate the main chart series
     * @param stats map of dates and related general statistics
     * @param totalStats descriptive stats for all the data
     */
    private void setUpChart(TreeMap<String, CGMStats> stats, DescriptiveStatistics totalStats) {
        for (String keys : stats.keySet()) {
            totalStats.addValue(stats.get(keys).getMean());
            XYChart.Data<String, Number> data = new XYChart.Data<>(keys, stats.get(keys).getMean());
            HoverNode node = new HoverNode(null, stats.get(keys));
            EventHandler handler = event -> {
                System.out.println("Creating a new graph for the hover node data");
                final NumberAxis xAxis = new NumberAxis();
                final NumberAxis yAxis = new NumberAxis();
                xAxis.setLabel("Number of Updates");
                //creating the chart
                final LineChart<Number, Number> lineChart =
                        new LineChart<>(xAxis, yAxis);
                XYChart.Series<Number, Number> daySeries = new XYChart.Series<>();
                XYChart.Series<Number, Number> dayMeanSeries = new XYChart.Series<>();
                dayMeanSeries.setName("Day Mean");
                daySeries.setName("Data for Day: " + keys);
                int count = 0;
                for (double dataPoint : stats.get(keys).getStatData()) {
                    daySeries.getData().add(new XYChart.Data<>(count, dataPoint));
                    count++;
                }
                dayMeanSeries.getData().add(new XYChart.Data<>(0, stats.get(keys).getMean()));
                dayMeanSeries.getData().add(new XYChart.Data<>(count - 1, stats.get(keys).getMean()));
                lineChart.getData().addAll(daySeries, dayMeanSeries);
                Stage dataStage = new Stage();
                Scene scene = new Scene(lineChart, 800, 800);
                dataStage.setScene(scene);
                dataStage.setTitle("Data: " + keys);
                dataStage.show();
            };
            node.setOnClick(handler);
            data.setNode(node);
            series.getData().add(data);
        }
    }


    private double calcLow(TreeMap<String, CGMStats> stats) {
        double total = stats.values().size();
        double count = 0;
        for (CGMStats cgmStats : stats.values()) {
            if (cgmStats.getPercentHigh() > 0.01) {
                count += cgmStats.getPercentLow();
            }
        }
        return count / total;
    }

    private double calcHigh(TreeMap<String, CGMStats> stats) {
        double total = stats.values().size();
        double count = 0;
        for (CGMStats cgmStats : stats.values()) {
            if (cgmStats.getPercentLow() > 0.01) {
                count += cgmStats.getPercentHigh();
            }
        }
        return count / total;
    }

    private double calcA1C(DescriptiveStatistics totalStats) {
        double a1c;
        a1c = (totalStats.getMean() + 46.7) / 28.7;
        return a1c;
    }

//    private void setupColumns() {
//        setUpGlucoseColumn();
//        setUpDateColumn();
//        setUpDeviceColumn();
//        setUpDirectionColumn();
//    }
//
//    private void setUpDirectionColumn() {
//        directionColumn.setCellValueFactory(new PropertyValueFactory<>("direction"));
//        directionColumn.setCellFactory(column -> new TableCell<CGMData, String>() {
//            @Override
//            protected void updateItem(String item, boolean empty) {
//                super.updateItem(item, empty);
//                if (item == null) {
//                    setText("");
//                } else {
//                    setText(item);
//                }
//            }
//        });
//    }
//
//    private void setUpDeviceColumn() {
//        deviceColumn.setCellValueFactory(new PropertyValueFactory<>("device"));
//        deviceColumn.setCellFactory(column -> new TableCell<CGMData, String>() {
//            @Override
//            protected void updateItem(String item, boolean empty) {
//                super.updateItem(item, empty);
//                if (item == null) {
//                    setText("");
//                } else {
//                    setText(item);
//                }
//            }
//        });
//    }
//
//    private void setUpDateColumn() {
//        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
//        dateColumn.setCellFactory(column -> new TableCell<CGMData, Double>() {
//            @Override
//            protected void updateItem(Double item, boolean empty) {
//                super.updateItem(item, empty);
//                if (item == null) {
//                    setText("");
//                } else {
//                    setText(Instant.ofEpochMilli(item.longValue()).toString());
//                }
//            }
//        });
//    }
//
//    private void setUpGlucoseColumn() {
//        glucoseColumn.setCellValueFactory(new PropertyValueFactory<>("glucose"));
//        glucoseColumn.setCellFactory(column -> new TableCell<CGMData, Integer>() {
//            @Override
//            protected void updateItem(Integer item, boolean empty) {
//                super.updateItem(item, empty);
//                if (item == null) {
//                    setText("");
//                } else {
//                    setText(item.toString());
//                }
//            }
//        });
//    }

    /**
     * Add data point to the table list
     * @param data CGMData
     */
    void addData(CGMData data) {
        tableList.add(data);
    }
}
