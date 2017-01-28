package com.gianlu.timeless;

import com.gianlu.commonutils.CommonUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    private static final int[] COLORS = new int[]{R.color.red, R.color.pink, R.color.purple, R.color.deepPurple, R.color.indigo, R.color.blue, R.color.lightBlue, R.color.cyan, R.color.teal, R.color.green, R.color.lightGreen, R.color.lime, R.color.yellow, R.color.amber, R.color.orange, R.color.deepOrange, R.color.brown};

    public static int getColor(int pos) {
        int i = pos;
        while (i > COLORS.length)
            i = i - COLORS.length;

        return COLORS[i];
    }

    public static int[] getColors() {
        shuffleArray(COLORS);
        return COLORS;
    }


    public static long parseWithCallback(SimpleDateFormat parser, String string, long callback) {
        if (string != null) {
            try {
                return parser.parse(string).getTime();
            } catch (ParseException ex) {
                return callback;
            }
        } else {
            return callback;
        }
    }

    public static void shuffleArray(int[] ar) {
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class ToastMessages {
        public static final CommonUtils.ToastMessage CANT_CHECK_GRANT = new CommonUtils.ToastMessage("Failed checking permissions!", true);
        public static final CommonUtils.ToastMessage FAILED_REFRESHING = new CommonUtils.ToastMessage("Failed refreshing data.", true);
        public static final CommonUtils.ToastMessage FAILED_LOADING = new CommonUtils.ToastMessage("Failed loading data.", true);
        public static final CommonUtils.ToastMessage TOKEN_REJECTED = new CommonUtils.ToastMessage("Server rejected your request. Try again or contact me!", false);
        public static final CommonUtils.ToastMessage CANT_REFRESH_TOKEN = new CommonUtils.ToastMessage("Failed refreshing the token!", true);
    }
}
