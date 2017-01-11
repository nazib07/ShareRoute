package shareroute.nazib.com.shareroute;


import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import static android.R.id.list;
import static shareroute.nazib.com.shareroute.CommonUtils.createAlert;

/**
 * Created by nazib on 1/8/17.
 */

public class FileUtils {
    private static Context context;
    FileUtils(){
//        context = ShareRoute.getInstance().getAppContext(); // This should be fixed later // FIXME: 1/8/17
    }

    public static void setContext(Context ctx){
        context = ctx;
    }

    public static void createDir() {

        if(context != null) {
            Log.d("[SHARE_ROUTE]", context.getDir("Test", Context.MODE_PRIVATE).toString());
        }else{
            Log.d("[SHARE_ROUTE]", "context is null");
        }
    }

    private static File createDirInsideExternalAppDir(String dirName){
        Log.d("SHARE_ROUTE", "createDirInsideExternalDataDir");
        return context.getExternalFilesDir(dirName);
    }

    public static File createNewRouteFile(String filename){
        File folder = createDirInsideExternalAppDir("CreatedRoute");
        File file = new File(folder.getAbsolutePath(), filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
           createAlert(context, "Route already exist, choose different name");
        }

        return file;
    }

    public static void deleteCreatedNewRouteFile(String filename){
        ArrayList<File> filelist = getCreatedRouteFileList();
        for(File file : filelist){
            if(file.getName().equals(filename)){
                file.delete();
            }
        }
    }

    public static File getCreatedRouteFileObject(String filename) {
        File folder = createDirInsideExternalAppDir("CreatedRoute");
        return new File(folder.getAbsolutePath(), filename);
    }

    public static ArrayList<File> getCreatedRouteFileList(){
        ArrayList<File> filelist = new ArrayList<>();
        File folder = createDirInsideExternalAppDir("CreatedRoute");

        File[] files = folder.listFiles();

        for (File file : files)
        {
            if(file.getName().endsWith(".geojson"))
            {
                filelist.add(file);
            }
        }
        return  filelist;
    }

    public static ArrayList<String> getCreatedRouteNames(){
        ArrayList<String> namelist = new ArrayList<>();
        String ext = ".geojson";

        ArrayList<File> filelist = getCreatedRouteFileList();
        for(File file : filelist){
            String filename = file.getName();
            String trimmed_filename = filename.substring(0,filename.length() - ext.length());
            Log.d("SHARE_ROUTE", "file name " +trimmed_filename);
            namelist.add(trimmed_filename);
        }

        return namelist;
    }

    ///////////////////////////////////Share Route Dir/////////////////////////////


    public static File createSharedRouteFile(String filename){
        File folder = createDirInsideExternalAppDir("SharedRoute");
        File file = new File(folder.getAbsolutePath(), filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            createAlert(context, "Route already exist, choose different name");
        }

        return file;
    }

    public static void deleteSharedNewRouteFile(String filename){
        ArrayList<File> filelist = getSharedRouteFileList();
        for(File file : filelist){
            if(file.getName().equals(filename)){
                file.delete();
            }
        }
    }

    public static File getSharedRouteFileObject(String filename) {
        File folder = createDirInsideExternalAppDir("SharedRoute");
        return new File(folder.getAbsolutePath(), filename);
    }

    public static ArrayList<File> getSharedRouteFileList(){
        ArrayList<File> filelist = new ArrayList<>();
        File folder = createDirInsideExternalAppDir("SharedRoute");

        File[] files = folder.listFiles();

        for (File file : files)
        {
            if(file.getName().endsWith(".geojson"))
            {
                filelist.add(file);
            }
        }
        return  filelist;
    }

    public static ArrayList<String> getSharedRouteNames(){
        ArrayList<String> namelist = new ArrayList<>();
        String ext = ".geojson";

        ArrayList<File> filelist = getSharedRouteFileList();
        for(File file : filelist){
            String filename = file.getName();
            String trimmed_filename = filename.substring(0,filename.length() - ext.length());
            Log.d("SHARE_ROUTE", "file name " +trimmed_filename);
            namelist.add(trimmed_filename);
        }

        return namelist;
    }


    ///////////////////////////////////////////////////////////////////////////////


    public static void writeToFile(String filepath, String data){
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(new File(filepath));
            stream.write(data.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String readFromFile(String filepath){
        String data = null;
        File file = new File(filepath);
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.read(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        data = new String(bytes);
        return data;
    }
}
