package com.gionee.oss.util;

import com.gionee.oss.constant.FileInfo;
import com.gionee.oss.constant.Policy;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.Date;
import java.util.Random;

/**
 * Created by yeqy on 2017/5/27.
 */
public class EncryptUtil {

    //policy = urlencode(base64(count+'\n'+expire+'\n'+fileNo+'\n'+identify)) identify可省略
    public static Policy decodePolicy(String policy) throws UnsupportedEncodingException {
        String[] str = policy.split("\n");
        if (str.length < 3) {
            return null;
        }
        if (str.length == 3) {
            Policy pcy = new Policy(Long.valueOf(str[0].trim()),
                    Long.valueOf(str[2].trim()), Long.valueOf(str[1]));
            return pcy;
        }
        if (str.length == 4) {
            Policy pcy = new Policy(Long.valueOf(str[0].trim()),
                    Long.valueOf(str[2].trim()), Long.valueOf(str[1]), str[3]);
            return pcy;
        }
        return null;
    }


    //fileInfo = urlencode(base64(date+'\n'+fileMd5+'\n'+filename+'\n'+filesize+'\n'+chunkMd5))
    public static FileInfo decodeFileInfo(String fileInfo) throws UnsupportedEncodingException, ParseException {
        String[] str = fileInfo.split("\n");
        if (str.length == 2) {
            Date date = DateUtil.getGMTFromString(str[0]);
            String[] tmp = URLDecoder.decode(str[1], "UTF-8").split("\n");
            if (tmp.length == 3) {
                return new FileInfo(date, tmp[0], tmp[1], Integer.valueOf(tmp[2]));
            } else if (tmp.length == 5) {
                return new FileInfo(date, tmp[0], tmp[1], Integer.valueOf(tmp[2]), tmp[3], Integer.valueOf(tmp[4]));
            }
        } else if (str.length == 4) {
            return new FileInfo(DateUtil.getGMTFromString(str[0]), str[1], str[2], Integer.valueOf(str[3]));
        } else if (str.length == 6) {
            return new FileInfo(DateUtil.getGMTFromString(str[0]), str[1], str[2], Integer.valueOf(str[3]), str[4], Integer.valueOf(str[5]));
        }
        return null;

    }

    public static String getKey(int length) {
        final char[] keyRange = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIGKLMNOPQRSTUVWXYZ@!.&^$%-#*()<>/".toCharArray();
        Random random = new Random();
        StringBuilder key = new StringBuilder();
        int r = 0;
        for (int i = 0; i < length; i++, r = random.nextInt(keyRange.length)) {
            key.append(keyRange[r]);
        }
        return key.toString();
    }

}
