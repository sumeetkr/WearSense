package sumeetkumar.in.wearsense.utils;

import android.content.Intent;

/**
 * Created by sumeet on 3/8/15.
 */
public class NewDataIntent extends Intent {
    public static String NEW_DATA = "NewData";
    public static String MESSAGE = "MESSAGE";
    public static String STATUS = "STATUS";

    public NewDataIntent(String message, String configJson, boolean isOld){
        super(NEW_DATA);
        setAction(Constants.NEW_DATA_INTENT_FILTER);
        addCategory(Intent.CATEGORY_DEFAULT);
        putExtra(MESSAGE, message);
        putExtra(Constants.NEW_DATA, configJson);
        putExtra(STATUS, isOld);
    }
}

