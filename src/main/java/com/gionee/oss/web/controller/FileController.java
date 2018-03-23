package com.gionee.oss.web.controller;

import com.gionee.gnif.file.constant.UploadStep;
import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.biz.service.OssCommonService;
import com.gionee.oss.biz.service.OssCommonUploadService;
import com.gionee.oss.biz.service.OssCoreUploadFileService;
import com.gionee.oss.biz.service.OssDownloadService;
import com.gionee.oss.constant.Info;
import com.gionee.oss.dto.CommonSchema;
import com.gionee.oss.dto.DownloadSchema;
import com.gionee.oss.dto.UploadSchema;
import com.gionee.oss.util.HttpUtil;
import com.gionee.oss.util.UploadUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Created by yeqy on 2017/5/27.
 */
@Controller
public class FileController {

    private final Logger logging = Logger.getLogger(FileController.class);
    @Autowired
    private OssCoreUploadFileService fileService;
    @Autowired
    private OssDownloadService ossDownloadService;
    @Autowired
    private OssCommonUploadService ossCommonUploadService;
    @Autowired
    private OssCommonService ossCommonService;
    @Autowired
    private UploadUtil uploadUtil;


    /**
     * 大文件四步上传接口
     *
     * @param schema 上传规则
     * @return
     */
    @RequestMapping(value = "upload.html", method = {RequestMethod.POST, RequestMethod.GET, RequestMethod.OPTIONS})
    @ResponseBody
    public Callable<Message> upload(UploadSchema schema, HttpServletRequest request) {
        return new Upload(schema, request);
    }

    /**
     * 普通文件上传接口
     *
     * @param schema
     * @return
     */
    @RequestMapping(value = "common_upload.html", method = {RequestMethod.POST, RequestMethod.OPTIONS})
    @ResponseBody
    public Callable<Message> commonUpload(UploadSchema schema, HttpServletRequest request) {
        return new CommonUpload(schema, request);
    }

    /**
     * 下载接口
     *
     * @param schema
     * @return
     */
    @RequestMapping(value = "download.html", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    @ResponseBody
    public Callable<Message> download(DownloadSchema schema, HttpServletResponse response, HttpServletRequest request) {
        return new Download(schema, request, response);
    }

    /**
     * 文件删除接口
     *
     * @param schema
     * @return
     */
    @RequestMapping(value = "file_delete.html", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS})
    @ResponseBody
    public Message fileDelete(CommonSchema schema) {
        try {
            return ossCommonService.fileDelete(schema);
        } catch (Exception e) {
            logging.error(e.getMessage());
            e.printStackTrace();
            return Message.error(Info.SYSTEM_ERROR.getInfo());
        }
    }

    /**
     * 改变文件临时状态接口
     *
     * @param schema
     * @return
     */
    @RequestMapping(value = "change_tmp.html", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    @ResponseBody
    public Message changeTmpFile(CommonSchema schema) {
        try {
            return ossCommonService.changeTmpFile(schema);
        } catch (Exception e) {
            logging.error(e.getMessage());
            e.printStackTrace();
            return Message.error(Info.SYSTEM_ERROR.getInfo());
        }
    }


    private Message stepOne(UploadSchema schema) throws Exception {
        return fileService.fileExist(schema);
    }

    private Message stepTwo(UploadSchema schema) {
        return fileService.chunkFileExist(schema);
    }

    private Message stepThree(UploadSchema schema, HttpServletRequest request) throws Exception {
        return fileService.chunkFileUpload(schema, request);
    }

    private Message stepFour(UploadSchema schema) throws Exception {
        return fileService.mergeChunkFile(schema, uploadUtil.mergeChunk(schema));
    }


    private class Download implements Callable<Message> {
        DownloadSchema schema;
        HttpServletRequest request;
        HttpServletResponse response;

        public Download(DownloadSchema schema, HttpServletRequest request, HttpServletResponse response) {
            this.schema = schema;
            this.request = request;
            this.response = response;
        }

        @Override
        public Message call() throws Exception {
            try {
                return ossDownloadService.download(schema, response, request);
            } catch (IOException e) {
                //取消下载，网络中断等..
                return null;
            } catch (Exception e) {
                logging.error(e.getMessage());
                e.printStackTrace();
                return Message.error(Info.SYSTEM_ERROR.getInfo());
            }
        }
    }


    private class Upload implements Callable<Message> {
        UploadSchema schema;
        HttpServletRequest request;

        public Upload(UploadSchema schema, HttpServletRequest request) {
            this.schema = schema;
            this.request = request;
        }

        @Override
        public Message call() throws Exception {
            Message message;
            try {
                switch (UploadStep.valueOf(schema.getStep())) {
                    case STEP_ONE:
                        message = stepOne(schema);
                        if (!message.getNotRepeat())
                            HttpUtil.post(schema, message);
                        return message;
                    case STEP_TWO:
                        return stepTwo(schema);
                    case STEP_THREE:
                        return stepThree(schema, request);
                    case STEP_FOUR:
                        message = stepFour(schema);
                        break;
                    default:
                        if (schema.getStep() == 0)
                            return new CommonUpload(schema, request).call();
                        else
                            return Message.error(Info.EINVAL.getInfo(), schema.getCallback());
                }
            } catch (MultipartException e) {//取消上传
                return null;
            } catch (Exception e) {
                logging.error(e.getMessage());
                e.printStackTrace();
                return Message.error(Info.SYSTEM_ERROR.getInfo(), schema.getCallback());
            }
            HttpUtil.post(schema, message);
            return message;
        }
    }

    private class CommonUpload implements Callable<Message> {
        UploadSchema schema;
        HttpServletRequest request;

        public CommonUpload(UploadSchema schema, HttpServletRequest request) {
            this.schema = schema;
            this.request = request;
        }

        @Override
        public Message call() throws Exception {
            Message message;
            try {
                if (schema.getStep() != 0) {
                    return new Upload(schema, request).call();
                }
                message = ossCommonUploadService.upload(schema, uploadUtil.commonUpload(schema, request));
            } catch (MultipartException e) {//取消上传
                return null;
            } catch (Exception e) {
                logging.error(e.getMessage());
                e.printStackTrace();
                return Message.error(Info.SYSTEM_ERROR.getInfo(), schema.getCallback());
            }
            HttpUtil.post(schema, message);
            return message;
        }
    }
}
