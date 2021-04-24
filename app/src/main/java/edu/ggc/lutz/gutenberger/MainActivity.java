package edu.ggc.lutz.gutenberger;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {

    private Button btnAnalyze;
    private Spinner spinner;
    private TextView tvNumWords;
    private TextView tvNumWordsUnique;
    private TextView tvLongestWordSize;
    private TextView tvLongestWordLabel;
    private TextView[] tvMostPopularLabels;
    private TextView[] tvMostPopularValues;
    private BarChart barChart;
    private SeekBar sbXAxis;

    private static final String TAG = "Gutenberger";

    private final RectF onValueSelectedRectF = new RectF();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAnalyze = findViewById(R.id.btnAnalyze);
        btnAnalyze.setOnClickListener(view -> {
            clearFields();
            Context context = getApplicationContext();
            GutenbergTask task = new GutenbergTask(context);
            task.execute(spinner.getSelectedItem().toString());
        });

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.manuscripts, R.layout.spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (barChart.getHighlighted() != null)
                    barChart.highlightValue(null);
                clearFields();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        tvNumWords = findViewById(R.id.tvNumWordsValue);
        tvNumWordsUnique = findViewById(R.id.tvNumWordsUniqueValue);
        tvLongestWordSize = findViewById(R.id.tvLongestWordValue);
        tvLongestWordLabel = findViewById(R.id.tvLongestWordLabel);
        tvMostPopularValues = new TextView[3];
        tvMostPopularValues[0] = findViewById(R.id.tvMostPopularValue);
        tvMostPopularValues[1] = findViewById(R.id.tvSecondPopularValue);
        tvMostPopularValues[2] = findViewById(R.id.tvThirdPopularValue);
        tvMostPopularLabels = new TextView[3];
        tvMostPopularLabels[0] = findViewById(R.id.tvMostPopularLabel);
        tvMostPopularLabels[1] = findViewById(R.id.tvSecondPopularLabel);
        tvMostPopularLabels[2] = findViewById(R.id.tvThirdPopularLabel);

        barChart = findViewById(R.id.barchart);
        ValueFormatter xAxisFormatter = new WordAxisValueFormatter(barChart);
        barChart.setMaxVisibleValueCount(50);
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                    if (e == null) return;
                    RectF bounds = onValueSelectedRectF;
                    barChart.getBarBounds((BarEntry) e, bounds);
                    MPPointF position = barChart.getPosition(e, YAxis.AxisDependency.LEFT);
                    MPPointF.recycleInstance(position);
            }
            @Override public void onNothingSelected() {}
        });

        CustomMarkerView marker = new CustomMarkerView(getApplicationContext(), xAxisFormatter);
        marker.setChartView(barChart); // For bounds control
        barChart.setMarker(marker);
        barChart.setTouchEnabled(true);
        barChart.setScaleXEnabled(true);

        sbXAxis = findViewById(R.id.sbXAxisMax);

        sbXAxis.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float factor =  i/10000.0f;
                barChart.setVisibleXRangeMinimum(factor * barChart.getData().getXMax());
                barChart.setVisibleXRangeMaximum(factor * barChart.getData().getXMax());
                barChart.moveViewToX(0);
                barChart.invalidate();

            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    private void clearFields() {
        tvNumWords.setText("n/a");
        tvNumWordsUnique.setText("n/a");
        tvLongestWordSize.setText("n/a");
        sbXAxis.setProgress(10000);

        for (int j = 0; j < tvMostPopularLabels.length; j++) {
            String current = tvMostPopularLabels[j].getText().toString();
            if (current.indexOf("(") > 0)
                current = current.substring(0,current.indexOf("("));
            tvMostPopularLabels[j].setText(current);
            tvMostPopularValues[j].setText("n/a");
        }

        // sets up dummy, grayed out data
        ArrayList<BarEntry> entries = new ArrayList<>();
        IntStream.rangeClosed(1,95)
                .forEach(i -> entries.add(new BarEntry(i, 90/i, (char)i+32)));
        BarDataSet bardataset = new BarDataSet(entries, "-----");
        BarData data = new BarData(bardataset);
        bardataset.setColor(Color.LTGRAY);
        barChart.setData(data); // set the data and list of labels into chart
        barChart.invalidate();
        barChart.notifyDataSetChanged();
    }

    public void setTvNumWords(int i){
        this.tvNumWords.setText(i);
    }

    class GutenbergTask extends AsyncTask<String, Integer, GutenStats> {

        private Context context;

        public GutenbergTask(Context context) {
            this.context = context;
        }

        @Override
        protected GutenStats doInBackground(String... strings) {

            GutenStats gStats = new GutenStats();
            // TODO add background processing here
            Log.v("task", strings[0]);
            Map<String, Integer> words = new HashMap<>();
            try {
                words = new BufferedReader(new  InputStreamReader(getAssets().open(strings[0]),"UTF-8")).lines()
                        .map(Pattern.compile("[!#\"$%*&()+,./:;<=>?@\\[\\]^_`{|}~”“]")::matcher)
                        .map(matcher -> matcher.replaceAll(""))
                        .flatMap(Pattern.compile("\\s++")::splitAsStream)
                        .filter(s -> s.length() >= 1)
                        .map(s -> s.toLowerCase())
                        .map(s -> {
                            String l = s;
                            if(s.endsWith("'") || s.charAt(0) == '\'')
                                l = s.replace("'","");
                            return l;
                        })
                        .filter(s -> !(s.equals("and") || s.equals("a")|| s.equals("to")|| s.equals("the")|| s.equals("of")|| s.equals("in")|| s.equals("\uFEFFthe")|| s.equals(" ")|| s.equals("")))
                        .filter(s -> !s.startsWith("http"))
                        .collect(Collectors.groupingBy(w -> w,
                                Collectors.summingInt(w -> 1)));
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.v("task",words.toString());

            gStats.setWords(words);

            words.forEach( (key, val) -> {
                gStats.getStats()[0] = gStats.getStats()[0]+val;
            });

            Log.v("task",gStats.getStats().toString());

            return gStats;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(GutenStats gStats) {
            super.onPostExecute(gStats);
            // TODO add onPostExecute processing here
            Log.v("task","onPost "+ gStats.getStats()[0]);
            tvNumWords.setText(""+gStats.getStats()[0]);
        }
    }

}

