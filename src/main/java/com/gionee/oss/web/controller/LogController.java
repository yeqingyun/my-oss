package com.gionee.oss.web.controller;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.gnif.web.response.PageResponse;
import com.gionee.gnif.web.util.ResponseFactory;
import com.gionee.oss.biz.model.OssDateLog;
import com.gionee.oss.biz.service.OssDateLogService;
import com.gionee.oss.biz.service.OssFileService;
import com.gionee.oss.web.response.MachineVolume;
import com.gionee.oss.web.response.OssDateLogResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by yeqy on 2017/6/21.
 */
@Controller
public class LogController {

    @Autowired
    private OssDateLogService ossDateLogService;
    @Autowired
    private OssFileService ossFileService;

    @RequestMapping(value = "getCurrentOssDateLog.html", method = RequestMethod.GET)
    @ResponseBody
    public OssDateLog getCurrentOssDateLog() {
        return ossDateLogService.getOssDateLog(new Date());
    }


    @RequestMapping(value = "getHistoryOssDateLog.html")
    @ResponseBody
    public PageResponse<OssDateLogResponse> getHistoryOssDateLog(QueryMap critera) {
        Map map = critera.getMap();
        try {
            critera.put("pageRow", Integer.parseInt((String) map.get("pageRow")));
        } catch (Exception e) {
            critera.put("pageRow", 10);
        }
        return ResponseFactory.convertPage(ossDateLogService.queryPage(critera), OssDateLogResponse.class);
    }


    @RequestMapping(value = "getMachineVolume.html")
    @ResponseBody
    public PageResponse<MachineVolume> machineVolume() {
        File[] roots = File.listRoots();
        long usableSpace = 0;
        long totalSpace = 0;
        for (int i = 0; i < roots.length; i++) {
            usableSpace += roots[i].getUsableSpace();
            totalSpace += roots[i].getTotalSpace();
        }
        MachineVolume machineVolume = ossFileService.getAllFileInfo();
        machineVolume.setTotalSpace(totalSpace);
        machineVolume.setUsableSpace(usableSpace);
        List list = new LinkedList();
        list.add(machineVolume);
        PageResponse pageResponse = new PageResponse();
        pageResponse.setRows(list);
        pageResponse.setTotal(1);
        return pageResponse;
    }

}
