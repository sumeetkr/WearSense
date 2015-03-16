package sumeetkumar.in.wearsense.utils;

import android.os.Bundle;
import android.os.Environment;

import com.google.android.gms.wearable.DataMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Set;

/**
 * Created by sumeet on 3/15/15.
 */
public class FileWriter {
    public static void saveData(String dataJSON, String type) {
        if (!isExternalStorageWritable()) {
            Logger.log("External Storage Not Writable");
            return;
        }
        File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS+ "/WearSense");
        directory.mkdirs();
        File file = null;

        if(Constants.SENSOR_DATA_PATH.compareTo(type)==0 ){
            file = new File(directory, "sensor_data.txt");
        }else if(Constants.MOBILE_AUDIO_DATA_PATH.compareTo(type)==0){
            file = new File(directory, Constants.MOBILE_AUDIO_DATA_PATH);
        }
        else{
            file = new File(directory, "audio_data.txt");
        }

        try {
            FileOutputStream stream = new FileOutputStream(file, true);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            writer.write(dataJSON);
            writer.close();

        } catch (Exception e) {
            Logger.log("Error Saving");
            e.printStackTrace();
        }
    }

    public static JSONObject dataMapAsJSONObject(DataMap data) {
        Bundle bundle = data.toBundle();
        JSONObject json = new JSONObject();
        Set<String> keys = bundle.keySet();
        for (String key : keys) {
            try {
                // json.put(key, bundle.get(key)); see edit below
                json.put(key, JSONObject.wrap(bundle.get(key)));
            } catch(JSONException e) {
                Logger.log(e.getMessage());
            }
        }
        return json;
    }

    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
