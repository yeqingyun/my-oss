package com.gionee.oss.util;


import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.biz.model.OssFile;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Properties;

import static com.gionee.oss.constant.DownLoadType.ATTACHMENT;

/**
 * Created by yeqy on 2017/6/7.
 */
public class DownloadUtil {
    private static final Logger logger = Logger.getLogger(DownloadUtil.class);
    private static final String[] IEBrowserSignals = {"MSIE", "TRIDENT", "EDGE"};


    public static void write(BufferedOutputStream out, BufferedInputStream in) throws IOException {
        byte[] bs;
        if (in.available() <= 1048576) {//小于1M
            bs = new byte[1024];
        } else if (in.available() <= 10485760) {//小于10M
            bs = new byte[5120];
        } else {//10M以上
            bs = new byte[51200];
        }
        int i;
        while ((i = in.read(bs)) != -1) {
            out.write(bs, 0, i);
        }
        out.flush();
    }


    /**
     * 根据浏览器类别编码文件名
     *
     * @param request
     * @param srcFileName
     * @return
     */
    public static String encodeFilename(HttpServletRequest request, String srcFileName) {
        String agent = request.getHeader("USER-AGENT");
        String targetFileName;
        try {
            if ((agent != null) && isIe(agent.toUpperCase())) {
                targetFileName = URLEncoder.encode(srcFileName, "UTF-8");
            } else {
                targetFileName = new String(srcFileName.getBytes("UTF-8"), "iso-8859-1");
            }
            return targetFileName;
        } catch (Exception e) {
            e.printStackTrace();
            return srcFileName;
        }
    }

    public static void setOctetStreamHeader(HttpServletResponse response, HttpServletRequest request, OssFile ossFile) {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodeFilename(request, ossFile.getName()) + "\"");
    }


    public static Message download(HttpServletResponse response, HttpServletRequest request, File file, OssFile ossFile, int type) throws IOException, ParseException {
//        long ifModifiedSince;
//        try {
//            ifModifiedSince = request.getDateHeader("If-Modified-Since");
//        } catch (IllegalArgumentException iae) {
//            ifModifiedSince = -1;
//        }
//
//        if (ifModifiedSince >= (ossFile.getUpdateTime().getTime() / 1000 * 1000)) {
//            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
//            return null;
//        }

        //FileChannel bis = null;
        try (FileInputStream fileInputStream = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fileInputStream);
             BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
            //bis = fileInputStream.getChannel();
            if (type == ATTACHMENT.getType()) {
                setOctetStreamHeader(response, request, ossFile);
            } else {
                Properties properties = new Properties();
                properties.load(new ClassPathResource("mime.properties").getInputStream());
                int idx = file.getName().lastIndexOf(".");
                if (idx > 0 && idx + 1 < file.getName().length()) {
                    String suffix = file.getName().substring(idx + 1);
                    String ext = null;
                    if (StringUtil.isNotEmpty(suffix))
                        ext = properties.getProperty(suffix.toLowerCase());
                    if (StringUtil.isNotEmpty(ext)) {
                        response.setContentType(ext);
                    } else {
                        setOctetStreamHeader(response, request, ossFile);
                    }
                } else {
                    setOctetStreamHeader(response, request, ossFile);
                }
            }
            //response.setHeader("Last-Modified", DateUtil.getDateGMTString(ossFile.getUpdateTime()));
            response.setHeader("Content-Length", String.valueOf(file.length()));
            write(bos, bis);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }

        return null;
    }


    private static boolean isIe(String agent) {
        for (String signal : IEBrowserSignals) {
            if (agent.contains(signal))
                return true;
        }
        return false;
    }
}