package com.gionee.oss.biz.service.impl;

import com.gionee.gnif.dto.QueryMap;
import com.gionee.gnif.file.util.CalcUtil;
import com.gionee.gnif.file.web.message.Message;
import com.gionee.gnif.model.PageResult;
import com.gionee.oss.biz.model.OssDownloadUrl;
import com.gionee.oss.biz.model.OssFile;
import com.gionee.oss.biz.model.OssTripartiteSystem;
import com.gionee.oss.biz.service.OssDownloadUrlService;
import com.gionee.oss.cache.Cache;
import com.gionee.oss.constant.Info;
import com.gionee.oss.integration.dao.OssDownloadUrlDao;
import com.gionee.oss.integration.dao.OssFileDao;
import com.gionee.oss.util.NumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

@Service
public class OssDownloadUrlServiceImpl implements OssDownloadUrlService {

    @Autowired
    private OssDownloadUrlDao ossDownloadUrlDao;
    @Autowired
    private OssFileDao ossFileDao;
    @Autowired
    private Cache<String, OssTripartiteSystem> systemKeyCache;

    @Override
    public void save(OssDownloadUrl ossDownloadUrl) {
        if (ossDownloadUrl.getId() == null) {
            ossDownloadUrlDao.insert(ossDownloadUrl);
        } else {
            ossDownloadUrlDao.update(ossDownloadUrl);
        }
    }

    @Override
    public OssDownloadUrl getOssDownloadUrl(Integer id) {
        return ossDownloadUrlDao.get(id);
    }


    @Override
    public void delete(Integer id) {
        OssDownloadUrl ossDownloadUrl = new OssDownloadUrl();
        ossDownloadUrl.setId(id);
        ossDownloadUrl.setStatus(OssDownloadUrl.STATUS_DELETED);
        ossDownloadUrlDao.update(ossDownloadUrl);
    }

    @Override
    public List<OssDownloadUrl> query(QueryMap critera) {
        return ossDownloadUrlDao.getAll(critera.getMap());
    }

    @Override
    public PageResult<OssDownloadUrl> queryPage(QueryMap critera) {
        PageResult<OssDownloadUrl> result = new PageResult<OssDownloadUrl>();
        result.setRows(ossDownloadUrlDao.getPage(critera.getMap()));
        result.setTotal(ossDownloadUrlDao.getPageCount(critera.getMap()));
        return result;
    }

    @Override
    public OssDownloadUrl getByCodeAndPolicy(String policy, String code) {
        return ossDownloadUrlDao.getByCodeAndPolicy(code, policy);
    }

    @Override
    public Message generateFileLink(Integer id, Long overTime, Long downloadCount, Integer downloadType, HttpServletRequest request) throws UnsupportedEncodingException {
        StringBuilder link = new StringBuilder(request.getRequestURL().toString().replace(request.getServletPath(), ""));
        if (!NumberUtil.hasEmpty(id)) {
            OssFile ossFile = ossFileDao.get(id);
            if (ossFile == null) {
                return Message.error(Info.FILE_NOTEXIST.getInfo());
            }
            if (NumberUtil.hasEmpty(overTime)) {//如果为空，设置为0
                overTime = 0l;
            }
            if (NumberUtil.hasEmpty(downloadCount)) {//如果为空，设置为0
                downloadCount = 0l;
            }
            if (NumberUtil.hasEmpty(downloadType)) {//如果为空，设置为0
                downloadType = 0;
            }
            String key = systemKeyCache.getAndSysnc(ossFile.getSystemCode()).getKey();
            //urlencode(base64(count+'\n'+expire+'\n'+fileNo+'\n'+identify))
            StringBuilder policy = new StringBuilder().append(downloadCount).append('\n').append(overTime).append('\n').append(id).append('\n').append(UUID.randomUUID().toString()).append(new Date().getTime());
            //urlencode(base64(hmac-sha1(key，count+'\n'+expire+'\n'+fileNo+'\n'+identify)))
            StringBuilder signature = new StringBuilder(CalcUtil.hamcsha1(policy.toString(), key));
            //download.html?code=workFlow&signature=vjbyPxybdZaNmGa%2ByT272YEAiv4%3D&policy=vjbyPsdf&2x44g3xybdZaNmGa%
            link.append("/download.html?").append("code=").append(ossFile.getSystemCode()).append("&download=").append(downloadType)
                    .append("&policy=").append(URLEncoder.encode(CalcUtil.getBase64(policy.toString()), "UTF-8"))
                    .append("&signature=").append(URLEncoder.encode(CalcUtil.getBase64(signature.toString()), "UTF-8"));
            Map<String, Object> attribute = new HashMap<>();
            attribute.put("link", link);
            Message message = Message.pass();
            message.setAttributes(attribute);
            return message;
        } else {
            return Message.error(Info.EINVAL.getInfo());
        }
    }

}
