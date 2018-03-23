package com.gionee.oss.web.controller;

import com.gionee.auth.security.cas.CasUser;
import com.gionee.gnif.dto.QueryMap;
import com.gionee.gnif.file.web.message.Message;
import com.gionee.gnif.model.PageResult;
import com.gionee.gnif.web.response.MessageResponse;
import com.gionee.gnif.web.response.PageResponse;
import com.gionee.gnif.web.util.ResponseFactory;
import com.gionee.oss.biz.model.OssDeleteFileLog;
import com.gionee.oss.biz.model.OssFile;
import com.gionee.oss.biz.service.OssDateLogService;
import com.gionee.oss.biz.service.OssDeleteFileLogService;
import com.gionee.oss.biz.service.OssFileService;
import com.gionee.oss.constant.DownLoadType;
import com.gionee.oss.constant.HandleInfo;
import com.gionee.oss.constant.HandleType;
import com.gionee.oss.constant.Info;
import com.gionee.oss.task.OssDateLogTask;
import com.gionee.oss.task.OssFileDeleteLogTask;
import com.gionee.oss.util.DownloadUtil;
import com.gionee.oss.web.response.OssFileResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yeqy on 2017/6/26.
 */
@Controller
@RequestMapping("/fileManager")
public class FileManagerController {

    private final Logger logging = Logger.getLogger(FileManagerController.class);

    @Autowired
    private OssFileService fileService;
    @Autowired
    private OssDeleteFileLogService ossDeleteFileLogService;
    @Autowired
    private OssDateLogService ossDateLogService;
    @Autowired
    @Qualifier("threadPool")
    private TaskExecutor taskExecutor;

    @RequestMapping("/list.json")
    @ResponseBody
    public PageResponse<OssFileResponse> list(HttpServletRequest request, QueryMap critera) {
        Map map = critera.getMap();
        if (critera.getMap().get("systemCode") != null)
            critera.put("systemCode", critera.getMap().get("systemCode").toString().split(","));
        critera.put("pageRow", Integer.parseInt((String) map.get("pageRow")));
        PageResult<OssFile> results = fileService.queryPage(critera);
        return ResponseFactory.convertPage(results, OssFileResponse.class);
    }

    @RequestMapping("/save.json")
    @ResponseBody
    public MessageResponse save(OssFile ossFile) {
        try {
            fileService.save(ossFile);
            return new MessageResponse();
        } catch (Exception e) {
            e.printStackTrace();
            logging.error(e.getMessage());
            return new MessageResponse(Info.SAVE_FAIL.getInfo());
        }
    }

    @RequestMapping("/remove.json")
    @ResponseBody
    public MessageResponse remove(Integer id) {
        try {
            OssFile ossFile = fileService.realDelete(id);
            List<OssDeleteFileLog> ossDeleteFileLogs = new ArrayList<>();
            ossDeleteFileLogs.add(new OssDeleteFileLog(ossFile));
            taskExecutor.execute(new OssDateLogTask(ossDateLogService, new HandleInfo(HandleType.DELETE)));
            taskExecutor.execute(new OssFileDeleteLogTask(ossDeleteFileLogService, ossDeleteFileLogs));
            return new MessageResponse();
        } catch (Exception e) {
            e.printStackTrace();
            logging.error(e.getMessage());
            return new MessageResponse(Info.DELETE_FAIL.getInfo());
        }
    }

    @RequestMapping("/load.json")
    @ResponseBody
    public OssFileResponse load(Integer id) {
        return new OssFileResponse(fileService.getOssFile(id));
    }


    @RequestMapping(value = "/download.html", method = RequestMethod.GET)
    @ResponseBody
    public Message download(Integer id, Integer type, HttpServletRequest request, HttpServletResponse response) {
        OssFile ossfile = fileService.getOssFile(id);
        if (ossfile == null) {
            return Message.error(Info.FILE_NOTEXIST.getInfo());
        }
        File file = new File(ossfile.getPath());
        if (!file.exists()) {
            return Message.error(Info.FILE_NOTEXIST.getInfo());
        }

        try {
            taskExecutor.execute(new OssDateLogTask(ossDateLogService, new HandleInfo(HandleType.DOWNLOAD)));
            logging.info(((CasUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getName() + "下载了" + file.getName());
            return DownloadUtil.download(response, request, file, ossfile, type == null ? DownLoadType.ATTACHMENT.getType() : type);
        } catch (Exception e) {
            logging.error(e.getMessage());
            e.printStackTrace();
            return Message.error(Info.SYSTEM_ERROR.getInfo());
        }
    }


}
