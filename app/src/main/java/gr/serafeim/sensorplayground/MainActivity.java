package gr.serafeim.sensorplayground;

import android.os.Build;
import android.os.CpuUsageInfo;
import android.os.HardwarePropertiesManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";


    TextView helloTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helloTV = findViewById(R.id.helloTV);
        try {
            BufferedReader mounts = new BufferedReader(new FileReader("/proc/loadavg"));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = mounts.readLine()) != null) {
                sb.append(line);
            }
            helloTV.setText(sb.toString());
        }
        catch (FileNotFoundException e) {
            Log.d(TAG, "Cannot find /proc/stat...");
        }
        catch (IOException e) {
            Log.d(TAG, "Ran into problems reading /proc/stat...");
        }

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



    }
}
