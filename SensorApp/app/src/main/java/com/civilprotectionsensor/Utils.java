package com.civilprotectionsensor;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class Utils {

    public static List<Sensor> getJsonContent(Context context, String file, boolean loadDefault) {
        InputStream is = null;
        FileInputStream fis = null;
        StringBuilder sb = new StringBuilder();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type sensorListType = new TypeToken<List<Sensor>>() {}.getType();

        if (loadDefault) {
            try {
                is = context.getResources().openRawResource(R.raw.default_sensors);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String text;
                while ((text = br.readLine()) != null) sb.append(text).append("\n");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            try {
                fis = context.openFileInput(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String text;
                while ((text = br.readLine()) != null) sb.append(text).append("\n");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return gson.fromJson(sb.toString(), sensorListType);
    }

    public static void storeJsonContent(Context context, String file, List<Sensor> sensors) {
        FileOutputStream fos = null;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Convert all the sensor objects to JSON format
        StringBuilder jsonContent = new StringBuilder();
        jsonContent.append("[\n");
        for (int i = 0; i < sensors.size(); i++) {
            jsonContent.append(gson.toJson(sensors.get(i)));
            if (i < sensors.size() - 1) jsonContent.append(",");
            jsonContent.append("\n");
        }
        jsonContent.append("]\n");

        // Store JSON string to given file (create/overwrite)
        try {
            fos = context.openFileOutput(file, Context.MODE_PRIVATE);
            fos.write(jsonContent.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}