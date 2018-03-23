package com.gionee.oss.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yeqy on 2017/5/27.
 */
public class StringUtil {
    private static final String fileNoParam = "{fileNo}";
    private static final String sizeParam = "{fileSize}";
    private static final String nameParam = "{fileName}";

    public static boolean isEmpty(String str) {
        return str == null || str.length() <= 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean hasEmpty(String... strs) {
        for (String str : strs) {
            if (isEmpty(str)) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, String> getParamMap(String params, Map<String, Object> attr) {
        Map<String, String> map = new HashMap<>();
        String[] ps = params.split("&");
        if (ps.length == 0)
            return null;
        for (String s : ps) {
            if (s.contains(fileNoParam)) {
                map.put("fileNo", attr.get("fileNo").toString());
            } else if (s.contains(sizeParam)) {
                map.put("fileSize", attr.get("fileSize").toString());
            } else if (s.contains(nameParam)) {
                map.put("fileName", attr.get("fileName").toString());
            } else {
                String[] tmp = s.split("=");
                if (tmp.length == 2)
                    map.put(tmp[0], tmp[1]);
            }
        }
        return map;
    }
}
