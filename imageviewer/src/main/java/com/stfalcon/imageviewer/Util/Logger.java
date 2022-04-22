package com.stfalcon.imageviewer.Util;

import android.os.Environment;
import android.util.Log;


import java.io.File;



public class Logger {
    private final static String TAG = "xuwenle";
    private static String logstr;
    private static int size;

    public Logger() {
        String path = Environment.getExternalStorageDirectory().getPath() + "/Kids/file/mapLog.txt";
        File file = new File(path);
    }

    public static void i(Object str) {
        try {
            if (true) {
                String name = getFunctionName();
                try {
                    if (name != null) {
                        logstr = str.toString();
                        int length = logstr.length();
                        if (length >= 3000) {
                            size = (int) Math.ceil(length / 3000) + 1;
                            Log.i(TAG, name + "length=" + length);
                            Log.i(TAG, name + "size=" + size);

                            for (int i = 0; i < size; i++) {
                                if (i == 0) {
                                    if (logstr.length() >= 3000) {
                                        Log.i(TAG, name + " - " + logstr.substring(0, 3000));
                                    }else{
                                        Log.i(TAG, name + " - " + logstr);
                                    }

                                }

                                if (i > 0 && i < size - 1)
                                    Log.i(TAG, name + logstr.substring(3000 * i + 1, 3000 * (i + 1)));
                                if (i == size - 1)
                                    Log.i(TAG, name + logstr.substring(3000 * i + 1, length));
                            }
                        } else {
                            Log.i(TAG, name + " - " + str);
                        }
                    } else {
                        Log.i(TAG, str.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(Logger.class.getName())) {
                continue;
            }
            return "[ " + Thread.currentThread().getName() + ": "
                    + st.getFileName() + ":" + st.getLineNumber() + " "
                    + st.getMethodName() + " ]";
        }
        return null;
    }

}
