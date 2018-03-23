package com.gionee.oss.web.controller;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.gnif.web.response.MessageResponse;
import com.gionee.gnif.web.response.PageResponse;
import com.gionee.gnif.web.util.ResponseFactory;
import com.gionee.oss.biz.service.OssDeleteFileLogService;
import com.gionee.oss.constant.Info;
import com.gionee.oss.web.response.OssDeleteFileLogResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Created by yeqy on 2017/8/28.
 */
@RequestMapping("/fileDeleteLog")
@Controller
public class FileDeleteLogController {
    private final Logger logging = Logger.getLogger(FileDeleteLogController.class);
    @Autowired
    private OssDeleteFileLogService ossDeleteFileLogService;


    @RequestMapping("/list.json")
    @ResponseBody
    public PageResponse<OssDeleteFileLogResponse> list(QueryMap critera) {
        Map map = critera.getMap();
        try {
            critera.put("pageRow", Integer.parseInt((String) map.get("pageRow")));
        } catch (Exception e) {
            critera.put("pageRow", 10);
        }
        return ResponseFactory.convertPage(ossDeleteFileLogService.queryPage(critera), OssDeleteFileLogResponse.class);
    }

    @RequestMapping("/remove.json")
    @ResponseBody
    public MessageResponse remove(Integer id) {
        try {
            ossDeleteFileLogService.delete(id);
        } catch (Exception e) {
            logging.error(e.getMessage());
            e.printStackTrace();
            return new MessageResponse(Info.DELETE_FAIL.getInfo());
        }
        return new MessageResponse();
    }
}
