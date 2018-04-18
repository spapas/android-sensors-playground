package gr.serafeim.sensorplayground;

import android.content.Context;
import android.database.DataSetObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.CpuUsageInfo;
import android.os.Handler;
import android.os.HardwarePropertiesManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private final static String TAG = "MainActivity";

    TextView loadAvgTV;
    TextView sensorTV;
    TextView sensorEventTV;
    Spinner sensorsSpinner;
    LineChart chart;
    LineDataSet chartDataSet;
    LineData lineData;
    int chartX = 0;

    private SensorManager mSensorManager;
    private List<Sensor> allSensors;

    public void getAllSensors() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        allSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
    }

    private static String getStringDate(long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.US);
        long timeInMillis = (new Date()).getTime() + (timestamp - System.nanoTime()) / 1000000L;
        cal.setTimeInMillis(timeInMillis);

        String date = DateFormat.format("yyyy-MM-dd hh:mm:ss", cal).toString();
        return date;
    }

    public static JSONObject sensorToJson(Sensor s) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("name", s.getName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                jo.put("id", Integer.toString(s.getId()));
            }
            jo.put("power", s.getPower());
            jo.put("resolution", s.getResolution());
            jo.put("max_delay", s.getMaxDelay());
            jo.put("type", s.getType());
            jo.put("string_type", s.getStringType());
            jo.put("vendor", s.getVendor());
            jo.put("fifo_max_event_cound", s.getFifoMaxEventCount());
            jo.put("fifo_reserved_event_cound", s.getFifoReservedEventCount());
            jo.put("max_range", s.getMaximumRange());
            jo.put("min_delay", s.getMinDelay());
            jo.put("version", s.getVersion());
            jo.put("reporting_mode", s.getReportingMode());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                jo.put("highest_direct_report_rate_level", s.getHighestDirectReportRateLevel());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jo;
    }

    public static String getAccuracyText(int ac) {
        switch(ac) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH: return "SENSOR_STATUS_ACCURACY_HIGH";
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW: return "ACCURACY_LOW";
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM: return "ACCURACY_MEDIUM";
            case SensorManager.SENSOR_STATUS_NO_CONTACT: return "NO_CONTACT";
            case SensorManager.SENSOR_STATUS_UNRELIABLE: return "UNRELIABLE";
            default: return "ERROR";
        }
    }

    public static JSONObject sensorEventToJson(SensorEvent se) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("sensor", sensorToJson(se.sensor));
            jo.put("accuracy", Integer.toString(se.accuracy));
            jo.put("accuracy_text", getAccuracyText(se.accuracy));
            jo.put("timestamp", se.timestamp);
            jo.put("text_timestamp", getStringDate(se.timestamp ));

            JSONArray vj = new JSONArray();
            for(int i=0;i<se.values.length;i++) {
                vj.put(se.values[i]);
            }
            jo.put("values", vj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jo;
    }

    public static JSONObject getLoadAvg() {
        try {
            BufferedReader mounts = new BufferedReader(new FileReader("/proc/loadavg"));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = mounts.readLine()) != null) {
                sb.append(line);
            }
            String []  parts = sb.toString().split("\\s+");
            JSONObject jo = new JSONObject();
            jo.put("1min", parts[0]);
            jo.put("5min", parts[1]);
            jo.put("15min", parts[2]);
            return jo;
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    Runnable getLoadAvgRunnable = new Runnable() {
        public void run() {
            final JSONObject loadAvg = getLoadAvg();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadAvgTV.setText(loadAvg.toString());
                }
            });
            getLoadAvgHandler.postDelayed(getLoadAvgRunnable , 100);
        }
    };
    Handler getLoadAvgHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadAvgTV = findViewById(R.id.loadAvgTV);
        sensorTV = findViewById(R.id.sensorTV);
        sensorEventTV = findViewById(R.id.sensorEventTV);
        sensorsSpinner  = findViewById(R.id.sensorsSpinner);
        chart  = findViewById(R.id.chart);

        XAxis xAxis = chart.getXAxis();

        xAxis.setTextSize(10f);

        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(1000);

        ArrayList<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry(++chartX, 0));
        chartDataSet = new LineDataSet(entries, "");


        lineData = new LineData(chartDataSet );

        chart.setData(lineData);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setContentDescription("");
        chart.invalidate(); // refresh

        JSONObject loadAvg = getLoadAvg();
        //String allSensors = getAllSensors(getApplicationContext());
        loadAvgTV.setText(loadAvg.toString());
        getAllSensors();

        ArrayList<String> spinnerArray = new ArrayList<>();
        for(Sensor s: allSensors) {
            spinnerArray.add(s.getName());
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sensorsSpinner.setAdapter(spinnerArrayAdapter);
        getLoadAvgHandler.postDelayed(getLoadAvgRunnable , 1000);
        sensorsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String)adapterView.getItemAtPosition(i);
                for(Sensor s: allSensors) {
                    if(s.getName().equals(item)) {
                        sensorTV.setText(sensorToJson(s).toString());
                    }
                }
                ArrayList<Entry> entries = new ArrayList<Entry>();
                chartX = 0;
                entries.add(new Entry(++chartX, 0));
                lineData.removeDataSet(0);
                chartDataSet = new LineDataSet(entries, "");
                lineData.addDataSet( chartDataSet);
                lineData.notifyDataChanged(); // let the data know a dataSet changed
                chart.notifyDataSetChanged(); // let the chart know it's data changed
                chart.invalidate();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            HardwarePropertiesManager hardwarePropertiesManager = getApplicationContext().getSystemService(HardwarePropertiesManager.class);
            CpuUsageInfo[] cpuUsages = hardwarePropertiesManager.getCpuUsages();
            float[] fanSpeeds = hardwarePropertiesManager.getFanSpeeds();
            float[] cpuTemperatures = hardwarePropertiesManager.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU, HardwarePropertiesManager.TEMPERATURE_CURRENT);
            float[] gpuTemperatures = hardwarePropertiesManager.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU, HardwarePropertiesManager.TEMPERATURE_CURRENT);
            float[] batteryTemperatures = hardwarePropertiesManager.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_BATTERY, HardwarePropertiesManager.TEMPERATURE_CURRENT);
            float[] skinTemperatures = hardwarePropertiesManager.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN, HardwarePropertiesManager.TEMPERATURE_CURRENT);
            helloTV.setText("FS " + fanSpeeds[0] + "CPU " + cpuTemperatures[0]);
        } else {
            helloTV.setText("Not supported");
        }
        */


    }

    @Override
    public void onSensorChanged(final SensorEvent sensorEvent) {
        mSensorManager.unregisterListener(this);
        for(Sensor s: allSensors) {
            if(s.getName().equals(sensorsSpinner.getSelectedItem().toString())) {
                mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }

        if(sensorEvent.sensor.getName().equals(sensorsSpinner.getSelectedItem().toString())) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jo = sensorEventToJson(sensorEvent);
                    jo.remove("sensor");
                    sensorEventTV.setText(jo.toString());
                    chartDataSet.addEntry(new Entry(++chartX, sensorEvent.values[0]));
                    chart.notifyDataSetChanged();
                    chart.invalidate();

                    if(chartX > 1000) {
                        ArrayList<Entry> entries = new ArrayList<Entry>();
                        chartX = 0;
                        entries.add(new Entry(++chartX, 0));
                        lineData.removeDataSet(0);
                        chartDataSet = new LineDataSet(entries, "Label");
                        lineData.addDataSet( chartDataSet);
                        lineData.notifyDataChanged(); // let the data know a dataSet changed
                        chart.notifyDataSetChanged(); // let the chart know it's data changed
                        chart.invalidate();
                    }
                }
            });
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "ACCURACY CHANGED " + sensorToJson(sensor).toString() + "L " + getAccuracyText(i));

    }

    @Override
    protected void onResume() {
        super.onResume();
        //getAllSensors();
        for(Sensor s: allSensors) {
            if(s.getName().equals(sensorsSpinner.getSelectedItem().toString())) {
                mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
