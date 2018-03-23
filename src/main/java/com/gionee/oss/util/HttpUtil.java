package com.gionee.oss.util;

import com.alibaba.fastjson.JSON;
import com.gionee.gnif.file.biz.model.Callback;
import com.gionee.gnif.file.util.CalcUtil;
import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.constant.Info;
import com.gionee.oss.dto.UploadSchema;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yeqy on 2017/6/7.
 */
public class HttpUtil {
    private static final Logger logger = Logger.getLogger(HttpUtil.class);

    public static void post(UploadSchema schema, Message message) {
        if (StringUtil.isEmpty(schema.getCall()))//回调为空
            return;
        Map attr = message.getAttributes();
        CloseableHttpResponse response = null;

        //设置会话超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000).setConnectionRequestTimeout(4000)
                .setSocketTimeout(5000).build();

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            Callback callback = JSON.toJavaObject(JSON.parseObject(CalcUtil.getFromBase64(schema.getCall())), Callback.class);

            if (callback == null || StringUtil.isEmpty(callback.getUrl()) || !message.getIsSuccess())
                return;

            String body = "";


            HttpPost httpPost = new HttpPost(callback.getUrl());

            httpPost.setConfig(requestConfig);

            Map<String, String> map = StringUtil.getParamMap(callback.getParam(), attr);


            //装填参数
            List<NameValuePair> nvps = new ArrayList<>();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), URLEncoder.encode(entry.getValue(), "UTF-8")));
            }
            //设置参数到请求对象中
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

            if(logger.isDebugEnabled())
                logger.debug("回调地址:[" + callback.getUrl() + "]，请求参数：" + nvps.toString());

            httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
            httpPost.setHeader("User-Agent", "gnif-oss");


            response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, "UTF-8");
            }
            EntityUtils.consume(entity);

            attr.put("callbackResult", body);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            attr.put("callbackResult", Info.CALLBACK_ERROR.getInfo());
            message.setIsSuccess(false);
            message.setMessage(Info.CALLBACK_ERROR.getInfo());
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
