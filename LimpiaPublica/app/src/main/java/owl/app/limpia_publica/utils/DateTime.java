package owl.app.limpia_publica.utils;

/**
 * Created by giusseppe on 13/03/2018.
 */

public class DateTime {

    public static String getDateTime(){
        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(dt);
        return date;
    }
}
