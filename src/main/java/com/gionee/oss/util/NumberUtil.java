package com.gionee.oss.util;

/**
 * Created by yeqy on 2017/6/5.
 */
public class NumberUtil {

    public static boolean hasEmpty(Number... numbers) {
        for (Number number : numbers) {
            if (number == null) {
                return true;
            }
            if (number instanceof Integer) {
                if (number.intValue() == 0) {
                    return true;
                }
            } else if (number instanceof Long) {
                if (number.longValue() == 0l) {
                    return true;
                }
            } else if (number instanceof Double) {
                if (number.doubleValue() == 0d) {
                    return true;
                }
            } else if (number instanceof Float) {
                if (number.floatValue() == 0f) {
                    return true;
                }
            }
        }
        return false;
    }
}
