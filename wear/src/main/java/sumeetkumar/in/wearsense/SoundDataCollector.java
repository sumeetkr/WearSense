package sumeetkumar.in.wearsense;

/**
 * Created by sumeet on 3/9/15.
 */

import android.media.MediaRecorder;

import com.google.android.gms.wearable.DataMap;

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
    private List<String> dataPointsAsString = new ArrayList<String>();
    private int noOfPointsToCollect = 4;
    private DataMap dataMap;

    public SoundDataCollector() {

    }

    public void collectData(DataMap audioDataMap){
        try{
            dataMap = audioDataMap;
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

        Logger.debug("New sound data collection");
        dataPointsAsString.add(Arrays.toString(data.buffer));

        if(dataList.size() > noOfPointsToCollect){
            notifyForDataCollectionFinished();
        }
    }

    public String getCollectedAudio(){
         String audioData = "";
        try{
            if(dataPointsAsString.size()>0){
//                List<Short> shorts = new ArrayList<Short>();
//
//                for(ExtAudioRecorder.AudioReadResult result: dataList){
//                    for(short dataPoint : result.buffer){
//                        shorts.add(dataPoint);
//                    }
//                }
//                audioData = shorts.toString();
                StringBuilder builder = new StringBuilder();
                for(String snippet: dataPointsAsString){
                    builder.append(snippet);
                }

                audioData  = builder.toString();
            }

        }catch (Exception ex){
            Logger.log( ex.getMessage());
        }
        return  audioData;
    }

    public String [] getAudioStringArray(){
        String [] audioDataArray = new String [dataPointsAsString.size()];
        for (int i=0;i<dataPointsAsString.size();i++){
            audioDataArray[i] = dataPointsAsString.get(i);
        }
        return audioDataArray;
    }

    public  void notifyForDataCollectionFinished(){
        if(dataRecorder != null){
            try{
                dataRecorder.stop();
                dataRecorder.release();
                dataRecorder = null;
            }catch (Exception ex){
                Logger.log(ex.getMessage());
            }
        }
    }

    public void clear(){
        dataList.clear();
        dataList = null;

        dataPointsAsString.clear();
        dataPointsAsString = null;
    }

}