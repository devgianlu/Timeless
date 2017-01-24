package com.gianlu.timeless;

import com.gianlu.commonutils.CommonUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Utils {

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

    public static class ToastMessages {
        public static final CommonUtils.ToastMessage CANT_CHECK_GRANT = new CommonUtils.ToastMessage("Failed checking permissions!", true);
        public static final CommonUtils.ToastMessage TOKEN_REJECTED = new CommonUtils.ToastMessage("Server rejected your request. Try again or contact me!", false);
    }
}
