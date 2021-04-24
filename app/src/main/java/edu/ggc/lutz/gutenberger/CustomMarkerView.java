package edu.ggc.lutz.gutenberger;

// from : https://github.com/PhilJay/MPAndroidChart/wiki/MarkerView
// and https://stackoverflow.com/questions/33241952/show-value-when-tapped-mpandroidchart

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DecimalFormat;

public class CustomMarkerView extends MarkerView {

    private final TextView tvContent;
    private final ValueFormatter xAxisValueFormatter;

    private final DecimalFormat format;

    public CustomMarkerView(Context context, ValueFormatter xAxisValueFormatter) {
        super(context, R.layout.custom_marker_view_layout);

        this.xAxisValueFormatter = xAxisValueFormatter;
        tvContent = findViewById(R.id.tvContent);
        format = new DecimalFormat("####");
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        tvContent.setText(String.format("%s (%s occurrences)", xAxisValueFormatter.getFormattedValue(e.getX()), format.format(e.getY())));

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}