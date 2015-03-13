package sumeetkumar.in.wearsense.utils;

/**
 * Created by sumeet on 3/9/15.
 */
import android.util.Log;

public class Logger {
    public static final String TAG = "DATA_COLLECTOR";
    private static final boolean isInDebugMode = true;

    public static void log(String text){
        try{
            if(text==null || text.isEmpty()){
                text = "no message";
            }
            Log.d(TAG, text);

//            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
//            if(stackTraceElements.length>0){
//                StackTraceElement element = stackTraceElements[0];
//                Log.d(TAG, element.getMethodName());
//            }
        }catch(Exception ex){
            Log.d(TAG, ex.getMessage());
        }
    }

    public static void log(String tag, String text){
        Log.d(tag, text);
    }

    public static void debug( String text){
        if(isInDebugMode){
            try{
                if(text==null || text.isEmpty()){
                    text = "no message";
                }
                Log.d(TAG, text);
            }catch(Exception ex){
                Log.d(TAG, ex.getMessage());
            }
        }
    }
}
