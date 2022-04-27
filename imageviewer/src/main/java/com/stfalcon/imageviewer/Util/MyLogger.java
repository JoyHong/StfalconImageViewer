package com.stfalcon.imageviewer.Util;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyLogger {
    private static final boolean DEBUG = true;
    private static final String TAG = "wenle";
    private static String logstr;
    private static int size;

    public static void i(String TAG, String method, String msg) {
        Log.d(TAG, getFunctionName() + msg);
    }

    public static void i(String TAG, String msg) {
        if (DEBUG) {
            Log.d(TAG, getFunctionName() + msg);
        }
    }

    public static void i(String str) {
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
                                    } else {
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
            if (st.getClassName().equals(MyLogger.class.getName())) {
                continue;
            }
            return "[ " + Thread.currentThread().getName() + ": "
                    + st.getFileName() + ":" + st.getLineNumber() + " "
                    + st.getMethodName() + " ]";
        }
        return null;
    }


    public static void e(String msg) {
        if (DEBUG) {
//            Log.e(_FILE_(), getLineMethod() + msg);
            Log.e(TAG, getLineMethod() + msg);
        }
    }

    public static void e(String TAG, String msg) {
        if (DEBUG) {
            Log.e(TAG, getLineMethod() + msg);
        }
    }

    public static String getFileLineMethod() {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        StringBuffer toStringBuffer = new StringBuffer("[")
                .append(traceElement.getFileName()).append(" | ")
                .append(traceElement.getLineNumber()).append(" | ")
                .append(traceElement.getMethodName()).append("]");
        return toStringBuffer.toString();
    }

    public static String getLineMethod() {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        StringBuffer toStringBuffer = new StringBuffer("[")
                .append(traceElement.getLineNumber()).append(" | ")
                .append(traceElement.getMethodName()).append("]");
        return toStringBuffer.toString();
    }

    public static String _FILE_() {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        return traceElement.getFileName();
    }

    public static String _FUNC_() {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
        return traceElement.getMethodName();
    }

    public static int _LINE_() {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
        return traceElement.getLineNumber();
    }

    public static String _TIME_() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(now);
    }

}
