package com.mishra.charting;

import com.mishra.cgdata.CGMData;
import com.mishra.cgdata.CGMStats;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

/**
 * A simple service to compute the descriptive statistics of glucose
 * data using the apache commons math library
 * Created by Scott on 1/31/2016.
 */
public class StatisticsService extends Service<TreeMap<String, CGMStats>> {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private List<CGMData> data;

    public StatisticsService(List<CGMData> dataList) {
        data = dataList;
    }

    @Override
    protected Task<TreeMap<String, CGMStats>> createTask() {
        return new Task<TreeMap<String, CGMStats>>() {
            @Override
            protected TreeMap<String, CGMStats> call() throws Exception {
                TreeMap<String, CGMStats> dateBinData = new TreeMap<>();
                for (CGMData item : data) {
                    Date date = new Date(item.getDate().longValue());
                    if (dateBinData.containsKey(sdf.format(date))) {
                        dateBinData.get(sdf.format(date)).addValue(item.getGlucose());
                    } else {
                        CGMStats newStats = new CGMStats();
                        newStats.addValue(item.getGlucose().doubleValue());
                        dateBinData.put(sdf.format(date), newStats);
                    }
                }
                return dateBinData;
            }
        };
    }
}
