package com.mishra.charting;

import com.google.common.collect.Lists;
import com.mishra.cgdata.CGMData;
import com.mishra.util.Driver;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller
 * Created by Scott on 11/12/2016.
 */
public class DateSelectionController implements Initializable {

    @FXML public Button OkButton;
    @FXML public TextField StartTimeTextField;
    @FXML public TextField EndTimeTextField;
    @FXML public TextField NumberOfSegmentsTextField;

    private Instant startInstant;
    private Instant endInstant;
    private int numberOfSegments;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private Driver _driver;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    /**
     * OK Button has been pressed
     * @param actionEvent
     */
    public void OkButtonPressed(ActionEvent actionEvent) {
        //Grab the text field items
        try {
            startInstant = sdf.parse(StartTimeTextField.getText()).toInstant();
            endInstant = sdf.parse(EndTimeTextField.getText()).toInstant().plusSeconds(60*60*18);
            numberOfSegments = Integer.parseInt(NumberOfSegmentsTextField.getText());

            CreateDataTable();
        }catch (ParseException ignore){

        }
    }

    public void PassInDriver(Driver driver) {_driver = driver;}

    private void CreateDataTable() {
        if(_driver != null) {
            List<CGMData> data = _driver.getCgData();
            List<CGMData> filteredData = filterCGMData(data);

            Map<String,List<CGMData>> cgmDataDateMap = new TreeMap<>();
            parseDataToMap(filteredData, cgmDataDateMap);

            Map<String, List<Double>> segmentAverageMAp = new TreeMap<>();
            createDateAverageMap(cgmDataDateMap, segmentAverageMAp);

            TableView dateView = new TableView();
            //create the tables
            TableColumn<ObservableList<String>, String>[] tableColumns = new TableColumn[numberOfSegments+1];
            tableColumns[0] = new TableColumn<>("Date");
            configureTableColumns(tableColumns);
            dateView.getColumns().addAll(tableColumns);
            MapDataToTable(segmentAverageMAp, dateView);
            //configure Table
            dateView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            dateView.setOnKeyPressed(param ->{
                if(param.isControlDown() && param.getCode() == KeyCode.C) {
                    ObservableList<TablePosition> posList = dateView.getSelectionModel().getSelectedCells();
                    StringBuilder clipboardString = new StringBuilder();
                    for(TablePosition pos : posList){
                        int row = pos.getRow();
                        ObservableList rowData = (ObservableList)dateView.getItems().get(row);
                        rowData.forEach(item -> {
                            clipboardString.append(item.toString()+ " ");
                        });
                        clipboardString.append("\n");
                    }
                    final ClipboardContent content = new ClipboardContent();
                    content.putString(clipboardString.toString());
                    Clipboard.getSystemClipboard().setContent(content);
                }
            });

            Stage tableView = new Stage();
            tableView.setScene(new Scene(dateView));
            tableView.show();
        }
    }

    private void MapDataToTable(Map<String, List<Double>> segmentAverageMAp, TableView dateView) {
        //create observable lists for the columns
        ObservableList<ObservableList> cgmData = FXCollections.observableArrayList();
        for (Map.Entry<String, List<Double>> stringListEntry : segmentAverageMAp.entrySet()) {
            ObservableList row = FXCollections.observableArrayList();
            row.add(stringListEntry.getKey());
            row.addAll(stringListEntry.getValue());
            cgmData.add(row);
        }
        dateView.setItems(cgmData); // finally add data to tableview
    }

    private void configureTableColumns(TableColumn<ObservableList<String>, String>[] tableColumns) {
        for(int i=1; i <= numberOfSegments; i++){
            tableColumns[i] = new TableColumn(""+i);
        }
        for(int i = 0; i < tableColumns.length; i++){
            final int finalIdx = i;
            tableColumns[i].setCellValueFactory(param ->
                    new ReadOnlyObjectWrapper<>(param.getValue().get(finalIdx)));
        }
    }

    private void createDateAverageMap(Map<String, List<CGMData>> cgmDataDateMap, Map<String, List<Double>> segmentAverageMAp) {
        for (Map.Entry<String, List<CGMData>> stringListEntry : cgmDataDateMap.entrySet()) {
            List<List<CGMData>> partitionedData = Lists.partition(stringListEntry.getValue(),stringListEntry.getValue().size()/(numberOfSegments)+1);
            List<Double> averages = new ArrayList<>();
            partitionedData.forEach(dataList -> {
                DescriptiveStatistics stats = new DescriptiveStatistics();
                dataList.forEach(item -> stats.addValue(item.getGlucose()));
                averages.add(stats.getMean());
            });
            segmentAverageMAp.put(stringListEntry.getKey(),averages);
        }
    }

    private void parseDataToMap(List<CGMData> filteredData, Map<String, List<CGMData>> cgmDataDateMap) {
        for (CGMData cgmData : filteredData) {
            Date date = new Date(cgmData.getDate().longValue());
            String dateString = sdf.format(date);
            if(cgmDataDateMap.containsKey(dateString)){
                cgmDataDateMap.get(dateString).add(cgmData);
            }else{
                List<CGMData> groupList = new ArrayList<>();
                groupList.add(cgmData);
                cgmDataDateMap.put(dateString,groupList);
            }
        }
    }

    private List<CGMData> filterCGMData(List<CGMData> data) {
        return data.parallelStream().filter(x ->
                        x.getDate()/1000 > startInstant.getEpochSecond()
                        && x.getDate()/1000 < endInstant.getEpochSecond())
                        .collect(Collectors.toList());
    }
}
