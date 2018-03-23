package com.gionee.oss.converter;

import com.alibaba.fastjson.JSON;
import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.util.StringUtil;
import org.apache.log4j.Logger;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * JSONP Fastjson 消息转换器
 * Created by yeqy on 2017/6/5.
 */
public class FastJsonHttpMessageConverter extends com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter {
    private final Logger logger = Logger.getLogger(FastJsonHttpMessageConverter.class);

    @Override
    protected void writeInternal(Object obj, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        if (obj instanceof Message) {
            Message message = (Message) obj;
            if (!StringUtil.isEmpty(message.getCallback())) {
                OutputStream out = outputMessage.getBody();
                String text = new StringBuilder(message.getCallback()).append("(").append(JSON.toJSONString(message, getFeatures())).append(")").toString();
                byte[] bytes = text.getBytes(getCharset());
                out.write(bytes);
            } else {
                super.writeInternal(obj, outputMessage);
            }
        } else {
            super.writeInternal(obj, outputMessage);
        }
    }
}