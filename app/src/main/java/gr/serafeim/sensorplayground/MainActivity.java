package gr.serafeim.sensorplayground;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.CpuUsageInfo;
import android.os.HardwarePropertiesManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private final static String TAG = "MainActivity";

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

    public static JSONObject sensorToJson(Sensor s) throws JSONException {
        JSONObject jo = new JSONObject();
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

    TextView helloTV;
    TextView lightSensorTV;
    TextView accelerrometerTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helloTV = findViewById(R.id.helloTV);
        lightSensorTV = findViewById(R.id.lightSensorTV);
        accelerrometerTV = findViewById(R.id.accelerrometerTV);

        JSONObject loadAvg = getLoadAvg();
        //String allSensors = getAllSensors(getApplicationContext());
        helloTV.setText(loadAvg.toString());



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
        Log.d(TAG, "SENSOR EVENT " + sensorEventToJson(sensorEvent));
        if(sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lightSensorTV.setText(sensorEventToJson(sensorEvent).toString());
                }
            });
        } else if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    accelerrometerTV.setText(sensorEventToJson(sensorEvent).toString());
                }
            });
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        try {
            Log.d(TAG, "ACCURACY CHANGED " + sensorToJson(sensor).toString() + "L " + getAccuracyText(i));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAllSensors();
        for(Sensor s: allSensors) {
            mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
