package com.gionee.oss.web.controller;

import com.gionee.gnif.file.constant.UploadStep;
import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.biz.service.OssCommonService;
import com.gionee.oss.biz.service.OssCoreUploadFileService;
import com.gionee.oss.biz.service.OssDownloadUrlService;
import com.gionee.oss.constant.Info;
import com.gionee.oss.dto.AdminUploadSchema;
import com.gionee.oss.util.UploadUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理员操作文件接口
 * Created by yeqy on 2017/8/29.
 */

@Controller
@RequestMapping("adminOperateFile")
public class AdminOperateFileController {

    private final Logger logging = Logger.getLogger(AdminOperateFileController.class);
    @Autowired
    private OssCoreUploadFileService fileService;
    @Autowired
    private UploadUtil uploadUtil;
    @Autowired
    private OssCommonService ossCommonService;
    @Autowired
    private OssDownloadUrlService ossDownloadUrlService;

    @RequestMapping("/upload.html")
    @ResponseBody
    public Message upload(AdminUploadSchema schema, HttpServletRequest request) {
        try {
            switch (UploadStep.valueOf(schema.getStep())) {
                case STEP_ONE:
                    return stepOne(schema);
                case STEP_TWO:
                    return stepTwo(schema);
                case STEP_THREE:
                    return stepThree(schema, request);
                case STEP_FOUR:
                    return stepFour(schema);
                default:
                    return Message.error(Info.EINVAL.getInfo());
            }
        } catch (Exception e) {
            logging.error(e.getMessage());
            e.printStackTrace();
            return Message.error(Info.SYSTEM_ERROR.getInfo());
        }
    }


    @RequestMapping("/common_upload.html")
    @ResponseBody
    public Message commonUpload(AdminUploadSchema schema, HttpServletRequest request) {
        try {
            return fileService.adminUploadFile(schema, uploadUtil.commonUpload(schema, request));
        } catch (Exception e) {
            logging.error(e.getMessage());
            e.printStackTrace();
            return Message.error(Info.SYSTEM_ERROR.getInfo());
        }
    }


    @RequestMapping("/deleteFile.html")
    @ResponseBody
    public Message deleteFile(Integer id) {
        try {
            return ossCommonService.adminFileDelete(id);
        } catch (Exception e) {
            logging.error(e.getMessage());
            e.printStackTrace();
            return Message.error(Info.SYSTEM_ERROR.getInfo());
        }
    }

    @RequestMapping("/generateFileLink.html")
    @ResponseBody
    public Message generateFileLink(Integer id, Long overTime, Long downloadCount, Integer downloadType, HttpServletRequest request) {
        try {
            return ossDownloadUrlService.generateFileLink(id, overTime, downloadCount, downloadType, request);
        } catch (Exception e) {
            logging.error(e.getMessage());
            e.printStackTrace();
            return Message.error(Info.SYSTEM_ERROR.getInfo());
        }
    }

    private Message stepOne(AdminUploadSchema schema) throws Exception {
        return fileService.fileExistWithOutAuth(schema);
    }

    private Message stepTwo(AdminUploadSchema schema) {
        return fileService.adminUploadChunkFileExist(schema);
    }

    private Message stepThree(AdminUploadSchema schema, HttpServletRequest request) throws Exception {
        return fileService.adminUploadChunkFileUpload(schema, request);
    }

    private Message stepFour(AdminUploadSchema schema) throws Exception {
        return fileService.adminUploadFile(schema, uploadUtil.mergeChunk(schema));
    }
}
