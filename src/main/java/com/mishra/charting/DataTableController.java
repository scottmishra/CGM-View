package com.mishra.charting;

import com.mishra.cgdata.CGMData;
import com.mishra.cgdata.CGMStats;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeMap;

/**
 *
 * Created by Scott on 1/24/2016.
 */
public class DataTableController implements Initializable {

    @FXML
    TableView<CGMData> gcmDataTableView;
    @FXML
    TableColumn<CGMData, Integer> glucoseColumn;
    @FXML
    TableColumn<CGMData, Double> dateColumn;
    @FXML
    TableColumn<CGMData, String> deviceColumn;
    @FXML
    TableColumn<CGMData, String> directionColumn;
    @FXML
    AnchorPane graphAnchorPane;
    @FXML
    SplitPane splitPane;

    private ObservableList<CGMData> tableList;

    private final NumberAxis yAxis = new NumberAxis();
    private final CategoryAxis xAxis = new CategoryAxis();
    private final XYChart.Series<String,Number> series = new XYChart.Series<>();
    private LineChart<String, Number> lineChart;

    public void initialize(URL location, ResourceBundle resources) {
        tableList = FXCollections.observableArrayList();
        gcmDataTableView.setItems(tableList);
        setupColumns();
        splitPane.setDividerPosition(0,0);
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.getData().add(series);
    }

    public void createChart(List<CGMData> dataList) {
        VBox graphDataBox = new VBox();
        HBox statsBox = new HBox();
        final Label meanLabel = new Label();
        final Label stdLabel = new Label();
        final Label a1cLabel = new Label();
        final Label highLabel = new Label();
        final Label lowLabel = new Label();

        statsBox.getChildren().addAll(meanLabel, stdLabel, a1cLabel, highLabel, lowLabel);
        statsBox.setPadding(new Insets(10.0,10.0,10.0,10.0));
        xAxis.setLabel("Date of update");
        yAxis.setLabel("Glucose Level");


        graphDataBox.getChildren().addAll(lineChart, statsBox);
        graphAnchorPane.getChildren().add(graphDataBox);
        AnchorPane.setBottomAnchor(graphDataBox, 0.0);
        AnchorPane.setTopAnchor(graphDataBox, 0.0);
        AnchorPane.setLeftAnchor(graphDataBox, 0.0);
        AnchorPane.setRightAnchor(graphDataBox, 0.0);

        StatisticsService service = new StatisticsService(dataList);
        service.stateProperty().addListener((obs, oldState, newState) -> {
            switch (newState) {
                case SUCCEEDED:
                    TreeMap<String, CGMStats> stats = service.getValue();
                    System.out.println("Total days: " + stats.size());
                    DescriptiveStatistics totalStats = new DescriptiveStatistics();
                    for (String keys : stats.keySet()) {
                        totalStats.addValue(stats.get(keys).getMean());
                        XYChart.Data<String,Number> data = new XYChart.Data<>(keys,stats.get(keys).getMean());
                        HoverNode node = new HoverNode(null, stats.get(keys));
                        EventHandler handler = event -> {
                            System.out.println("Clicked A Node");
                        };
                        node.setOnClick(handler);
                        data.setNode(node);
                        series.getData().add(data);
                    }
                    meanLabel.setText(String.format("Mean: %.3f " , totalStats.getMean()));
                    stdLabel.setText(String.format("Std: %.3f " , totalStats.getStandardDeviation()));
                    a1cLabel.setText(String.format("A1C: %.3f " , calcA1C(totalStats)));
                    highLabel.setText(String.format("High: %.3f ", calcHigh(stats)));
                    lowLabel.setText(String.format("Low: %.3f ", calcLow(stats)));

                    break;
            }
        });
        service.start();

    }

    private double calcLow(TreeMap<String, CGMStats> stats) {
        double total = stats.values().size();
        double count = 0;
        for(CGMStats cgmStats : stats.values()){
            if(cgmStats.getPercentHigh() > 0.01){
                count +=cgmStats.getPercentLow();
            }
        }
        return count/total;
    }

    private double calcHigh(TreeMap<String, CGMStats> stats) {
        double total = stats.values().size();
        double count = 0;
        for(CGMStats cgmStats : stats.values()){
            if(cgmStats.getPercentLow() > 0.01){
                count +=cgmStats.getPercentHigh();
            }
        }
        return count/total;
    }

    private double calcA1C(DescriptiveStatistics totalStats) {
        double a1c;
        a1c = (totalStats.getMean() + 46.7)/28.7;
        return a1c;
    }

    private void setupColumns() {
        setUpGlucoseColumn();
        setUpDateColumn();
        setUpDeviceColumn();
        setUpDirectionColumn();
    }

    private void setUpDirectionColumn() {
        directionColumn.setCellValueFactory(new PropertyValueFactory<>("direction"));
        directionColumn.setCellFactory(column -> new TableCell<CGMData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("");
                } else {
                    setText(item);
                }
            }
        });
    }

    private void setUpDeviceColumn() {
        deviceColumn.setCellValueFactory(new PropertyValueFactory<>("device"));
        deviceColumn.setCellFactory(column -> new TableCell<CGMData, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("");
                } else {
                    setText(item);
                }
            }
        });
    }

    private void setUpDateColumn() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setCellFactory(column -> new TableCell<CGMData, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("");
                } else {
                    setText(Instant.ofEpochMilli(item.longValue()).toString());
                }
            }
        });
    }

    private void setUpGlucoseColumn() {
        glucoseColumn.setCellValueFactory(new PropertyValueFactory<>("glucose"));
        glucoseColumn.setCellFactory(column -> new TableCell<CGMData, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("");
                } else {
                    setText(item.toString());
                }
            }
        });
    }

    public void addData(CGMData data) {
        tableList.add(data);
    }


}
