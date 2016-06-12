package com.mishra.cgdata;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Wrapper for collecting descriptive statistics and
 * other sets of data useful for t1dm studies
 * Created by Scott on 2/27/2016.
 */
public class CGMStats {

    private DescriptiveStatistics descriptiveStatistics;
    private Double percentHigh;
    private Double percentLow;
    private final Double HIGH_BG = 200.0;
    private final Double LOW_BG = 50.0;

    public CGMStats() {
        descriptiveStatistics = new DescriptiveStatistics();
    }

    public CGMStats(DescriptiveStatistics descriptiveStatistics, Double percentLow, Double percentHigh) {
        this.descriptiveStatistics = descriptiveStatistics;
        this.percentLow = percentLow;
        this.percentHigh = percentHigh;
    }

    public DescriptiveStatistics getDescriptiveStatistics() {
        return descriptiveStatistics;
    }

    public void setDescriptiveStatistics(DescriptiveStatistics descriptiveStatistics) {
        this.descriptiveStatistics = descriptiveStatistics;
    }

    public Double getPercentHigh() {
        return percentHigh;
    }

    public Double getPercentLow() {
        return percentLow;
    }

    public void addValue(double value){
        descriptiveStatistics.addValue(value);
        computePercentHigh();
        computePercentLow();
    }

    public double[] getStatData(){
        return descriptiveStatistics.getValues();
    }

    /**
     * Uses descriptive stats to compute the % of the day over
     * HIGH BG Value
     */
    private void computePercentLow() {
        double[] values = descriptiveStatistics.getValues();
        double count = 0;
        double total = values.length;
        for(double value : values){
            if(value < LOW_BG){
                count++;
            }
        }
        percentLow = count/total*100.0;
    }

    /**
     * Uses descriptive stats to compute the % of the day under
     * Low BG  Value
     */
    private void computePercentHigh() {
        double[] values = descriptiveStatistics.getValues();
        double count = 0;
        double total = values.length;
        for(double value : values){
            if(value > HIGH_BG){
                count++;
            }
        }
        percentHigh = count/total*100.0;
    }

    public double getMean(){
        return getDescriptiveStatistics().getMean();
    }
}
