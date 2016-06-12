package com.mishra.charting;

import com.mishra.cgdata.CGMStats;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Custom Gui that gives hover values
 * Created by Scott on 2/21/2016.
 */
public class HoverNode extends StackPane {

    public HoverNode(CGMStats priorValue, CGMStats value) {
        this(15.0, 15.0, priorValue, value);
    }

    public HoverNode(double prefWidth, double prefHeight, CGMStats priorValue, CGMStats value) {
        super();
        setPrefSize(prefWidth, prefHeight);

        /**
         * Show pane when the
         */
        final Label label = createDataThresholdLabel(value);

        setOnMouseEntered(mouseEvent -> {
            getChildren().setAll(label);
            setCursor(Cursor.NONE);
            toFront();
        });
        setOnMouseExited(mouseEvent -> {
            getChildren().clear();
            setCursor(Cursor.CROSSHAIR);
        });
    }

    private Label createDataThresholdLabel(CGMStats value) {
        final Label label = new Label(String.format("%.2f\n%.2f\n%.2f", value.getMean(), value.getPercentHigh(), value.getPercentLow()));
        label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
        label.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        label.setTextFill(Color.FIREBRICK);

        label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
        return label;
    }

    /**
     * Set the mouse click listener for the hover nodes
     */
    public void setOnClick(EventHandler handler){
        setOnMouseClicked(handler);
    }
}