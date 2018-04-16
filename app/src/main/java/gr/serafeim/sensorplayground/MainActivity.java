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
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private final static String TAG = "MainActivity";

    private SensorManager mSensorManager;
    private List<Sensor> allSensors;

    public void getAllSensors() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        allSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helloTV = findViewById(R.id.helloTV);

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
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d(TAG, "SENSOR CHANGED " + sensorEvent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "ACCURACY CHANGED " + sensor + " " + i);
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
