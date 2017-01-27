package shareroute.nazib.com.shareroute;

import android.app.Application;
import android.content.Context;

/**
 * Created by nazib on 1/8/17.
 */

public class ShareRoute extends Application {
    private static Context context;
    private static ShareRoute instance;

    public void onCreate() {
        super.onCreate();
        instance = this;
        context = getApplicationContext();
    }

    public static ShareRoute getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return context;
    }
}
