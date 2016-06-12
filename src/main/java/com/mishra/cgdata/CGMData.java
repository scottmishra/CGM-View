package com.mishra.cgdata;

/**
 * Continuous Glucose Monitor Data Object
 * Created by Scott on 1/20/2016.
 */
public class CGMData {

    private Integer glucose;
    private Double date;
    private String type;
    private String direction;
    private Integer trend;
    private String device;
    private Integer updateNumber;


    public CGMData() {
    }

    public Integer getGlucose() {
        return glucose;
    }

    public void setGlucose(Integer glucose) {
        this.glucose = glucose;
    }

    public Double getDate() {
        return date;
    }

    public void setDate(Double date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Integer getTrend() {
        return trend;
    }

    public void setTrend(Integer trend) {
        this.trend = trend;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public Integer getUpdateNumber() {
        return updateNumber;
    }

    public void setUpdateNumber(Integer updateNumber) {
        this.updateNumber = updateNumber;
    }

    @Override
    public String toString() {
        return "CGMData{" +
                "glucose=" + glucose +
                ", date=" + date +
                ", type='" + type + '\'' +
                ", direction='" + direction + '\'' +
                ", trend=" + trend +
                ", device='" + device + '\'' +
                ", updateNumber=" + updateNumber +
                '}';
    }
}
