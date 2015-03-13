package sumeetkumar.in.wearsense;

/**
 * Created by sumeet on 3/9/15.
 */

import android.content.Context;
import android.media.MediaRecorder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sumeetkumar.in.wearsense.utils.Logger;


/**
 * Created by sumeet on 11/10/14.
 */
public class SoundDataCollector  implements ExtAudioRecorder.AudioDataArrivedEventListener {

    private ExtAudioRecorder dataRecorder;
    private List<ExtAudioRecorder.AudioReadResult> dataList;
    private int noOfPointsToCollect = 10000;

    public SoundDataCollector() {

    }

    public void collectData(Context context, boolean toBeWritten){
        try{
            dataRecorder = ExtAudioRecorder.getInstance(false, MediaRecorder.AudioSource.DEFAULT);
            dataList = new ArrayList<ExtAudioRecorder.AudioReadResult>();

            dataRecorder.registerDataListener(this);
            dataRecorder.prepare();
            dataRecorder.start();

        }catch (Exception ex){
            Logger.log(ex.getMessage());

        }
    }

    @Override
    public void onNewDataArrived(ExtAudioRecorder.AudioReadResult data) {
        dataList.add(data);

        Logger.debug("Sound data collection" + Arrays.toString(data.buffer));

        if(dataList.size() > noOfPointsToCollect){
            notifyForDataCollectionFinished();
        }
    }

    public  void notifyForDataCollectionFinished(){
        dataRecorder.stop();
        dataRecorder.release();


//        if(dataRecorder != null){
//            dataRecorder = null;
//        }
    }

}