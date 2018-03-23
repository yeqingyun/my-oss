package com.gionee.oss.web.controller;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.gnif.model.PageResult;
import com.gionee.gnif.web.response.MessageResponse;
import com.gionee.gnif.web.response.PageResponse;
import com.gionee.gnif.web.util.ResponseFactory;
import com.gionee.oss.biz.model.OssTripartiteSystem;
import com.gionee.oss.biz.service.OssTripartiteSystemService;
import com.gionee.oss.constant.Info;
import com.gionee.oss.util.StringUtil;
import com.gionee.oss.web.response.OssTripartiteSystemResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Created by yeqy on 2017/6/23.
 */
@Controller
@RequestMapping("/tripartiteSystem")
public class TripartiteSystemController {

    @Autowired
    private OssTripartiteSystemService tripartiteSystemService;

    @RequestMapping("/list.json")
    @ResponseBody
    public PageResponse<OssTripartiteSystemResponse> list(QueryMap critera) {
        Map map = critera.getMap();
        critera.put("pageRow", Integer.parseInt((String) map.get("pageRow")));
        PageResult<OssTripartiteSystem> results = tripartiteSystemService.queryPage(critera);
        return ResponseFactory.convertPage(results, OssTripartiteSystemResponse.class);
    }

    @RequestMapping("/loadSystem.json")
    @ResponseBody
    public List<OssTripartiteSystemResponse> loadSystem() {
        return ResponseFactory.convertList(tripartiteSystemService.query(new QueryMap()), OssTripartiteSystemResponse.class);
    }

    @RequestMapping("/add.json")
    @ResponseBody
    public MessageResponse add(OssTripartiteSystem ossTripartiteSystem) {
        try {
            return tripartiteSystemService.add(ossTripartiteSystem);
        } catch (Exception e) {
            e.printStackTrace();
            return new MessageResponse(Info.SYSTEM_ERROR.getInfo());
        }

    }

    @RequestMapping("/update.json")
    @ResponseBody
    public MessageResponse update(OssTripartiteSystem ossTripartiteSystem) {
        try {
            return tripartiteSystemService.update(ossTripartiteSystem);
        } catch (Exception e) {
            e.printStackTrace();
            return new MessageResponse(Info.SYSTEM_ERROR.getInfo());
        }
    }

    @RequestMapping("/remove.json")
    @ResponseBody
    public MessageResponse remove(String code) {
        try {
            if (StringUtil.isEmpty(code)) {
                return new MessageResponse(Info.SYSTEM_ERROR.getInfo());
            }
            tripartiteSystemService.delete(code);
            return new MessageResponse();
        } catch (Exception e) {
            e.printStackTrace();
            return new MessageResponse(Info.SYSTEM_ERROR.getInfo());
        }
    }
}
