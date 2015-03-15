package sumeetkumar.in.wearsense.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

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
        }else{
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

    /* Checks if external storage is available for read and write */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
