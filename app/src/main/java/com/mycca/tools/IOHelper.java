package com.mycca.tools;

import android.content.Context;
import android.os.AsyncTask;

import com.google.firebase.perf.FirebasePerformance;
import com.mycca.listeners.ReadFileCompletionListener;
import com.mycca.listeners.WriteFileCompletionListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class IOHelper {

    private static com.google.firebase.perf.metrics.Trace mTrace;
    private static IOHelper _instance;
    private String TAG = "iohelper";

    public static final String CIRCLES = "Circle Data";
    public static final String CONTACTS = "Contact Persons";
    public static final String GRIEVANCES = "Grievances";
    public static final String INSPECTIONS = "Inspections";

    private IOHelper() {
        _instance = this;
    }

    public static IOHelper getInstance() {
        if (_instance == null) {
            return new IOHelper();
        } else {
            return _instance;
        }
    }

    public void writeToFile(Context context, Object jsonObject, String filename, String folder, WriteFileCompletionListener writeFileCompletionListener) {
        new WriteFile().execute(context, filename, jsonObject, folder, writeFileCompletionListener);
    }

    public void readFromFile(Context context, String filename, String folder, ReadFileCompletionListener readFileCompletionListener) {
        new ReadFile().execute(context, filename, readFileCompletionListener, folder);
    }

    private static File getFile(Context context, String filename, String folder) {
        File file;
        if (folder != null) {
            File path = new File(context.getFilesDir(), folder);
            path.mkdirs();
            file = new File(path, filename + ".json");
        } else
            file = new File(context.getFilesDir(), filename + ".json");
        return file;
    }

    static class WriteFile extends AsyncTask<Object, Object, Boolean> {

        WriteFileCompletionListener writeFileCompletionListener;

        @Override
        protected Boolean doInBackground(Object... objects) {
            String WRITE_TRACE = "WRITE_trace";
            mTrace = FirebasePerformance.getInstance().newTrace(WRITE_TRACE);
            mTrace.start();

            Context context = (Context) objects[0];
            String filename = (String) objects[1];
            String jsonObject = (String) objects[2];
            String folder = (String) objects[3];
            writeFileCompletionListener = (WriteFileCompletionListener) objects[4];

            File file = getFile(context, filename, folder);
            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(jsonObject.getBytes());
                CustomLogger.getInstance().logDebug("WRITING TO FILE: " + file.getCanonicalPath());
                CustomLogger.getInstance().logDebug("\nContents: " + jsonObject);
                outputStream.close();
            } catch (IOException e) {
                CustomLogger.getInstance().logDebug("Could not write");
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean o) {
            super.onPostExecute(o);
            mTrace.stop();
            writeFileCompletionListener.onFileWrite(o);
        }
    }

    static class ReadFile extends AsyncTask<Object, Object, Object> {

        ReadFileCompletionListener readFileCompletionListener;

        @Override
        protected Object doInBackground(Object... objects) {

            Context context = (Context) objects[0];
            String filename = (String) objects[1];
            readFileCompletionListener = (ReadFileCompletionListener) objects[2];
            String folder = (String) objects[3];

            File file = getFile(context, filename, folder);
            try {
                CustomLogger.getInstance().logDebug("readFromFile: file path = " + file.getPath());
                FileInputStream fin = new FileInputStream(file);
                int size = fin.available();
                byte[] buffer = new byte[size];
                int i = fin.read(buffer);
                CustomLogger.getInstance().logDebug("Bytes read = " + i);
                fin.close();
                return new String(buffer);
            } catch (IOException e) {
                CustomLogger.getInstance().logDebug("could not read");
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            readFileCompletionListener.onFileRead(o);
        }
    }
}
