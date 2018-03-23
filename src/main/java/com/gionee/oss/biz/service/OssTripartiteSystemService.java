package com.gionee.oss.biz.service;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.gnif.model.PageResult;
import com.gionee.gnif.web.response.MessageResponse;
import com.gionee.oss.biz.model.OssTripartiteSystem;

import java.util.List;

public interface OssTripartiteSystemService {

    MessageResponse add(OssTripartiteSystem ossTripartiteSystem);

    MessageResponse update(OssTripartiteSystem ossTripartiteSystem);

    List<OssTripartiteSystem> query(QueryMap critera);

    PageResult<OssTripartiteSystem> queryPage(QueryMap critera);

    OssTripartiteSystem getByCode(String code);

    void delete(String code);

}
