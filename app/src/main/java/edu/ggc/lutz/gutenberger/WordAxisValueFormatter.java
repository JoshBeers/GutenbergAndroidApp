package edu.ggc.lutz.gutenberger;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class WordAxisValueFormatter extends ValueFormatter {

    private final BarLineChartBase<?> chart;

    public WordAxisValueFormatter(BarLineChartBase<?> chart) {
        this.chart = chart;
    }

    @Override
    public String getFormattedValue(float value) {
        return chart.getData()
                .getDataSetByIndex(0)
                .getEntryForIndex((int)value)
                .getData()
                .toString();
    }
}

