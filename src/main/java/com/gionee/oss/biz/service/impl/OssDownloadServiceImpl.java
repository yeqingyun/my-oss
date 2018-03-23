package com.gionee.oss.biz.service.impl;

import com.gionee.gnif.file.util.CalcUtil;
import com.gionee.gnif.file.web.message.Message;
import com.gionee.oss.biz.model.OssDownloadUrl;
import com.gionee.oss.biz.model.OssFile;
import com.gionee.oss.biz.model.OssTripartiteSystem;
import com.gionee.oss.biz.service.OssDateLogService;
import com.gionee.oss.biz.service.OssDownloadService;
import com.gionee.oss.biz.service.OssFileService;
import com.gionee.oss.cache.Cache;
import com.gionee.oss.constant.*;
import com.gionee.oss.dto.DownloadSchema;
import com.gionee.oss.task.OssDateLogTask;
import com.gionee.oss.util.DownloadUtil;
import com.gionee.oss.util.EncryptUtil;
import com.gionee.oss.util.StringUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

/**
 * Created by yeqy on 2017/6/6.
 */
@Service
public class OssDownloadServiceImpl implements OssDownloadService {
    private final Logger logger = Logger.getLogger(OssDownloadServiceImpl.class);
    @Autowired
    private OssDownloadUrlServiceImpl ossDownloadUrlService;
    @Autowired
    private OssFileService ossFileService;
    @Autowired
    private Cache<String, OssTripartiteSystem> systemKeyCache;
    @Autowired
    @Qualifier("threadPool")
    private TaskExecutor taskExecutor;
    @Autowired
    private OssDateLogService ossDateLogService;


    @Override
    public Message download(DownloadSchema schema, HttpServletResponse response, HttpServletRequest request) throws Exception {
        OssDownloadUrl ossDownloadUrl = null;
        OssDownloadUrl url = null;
        int stage = 0;

        try {
            if (StringUtil.hasEmpty(schema.getCode(), schema.getPolicy(), schema.getSignature())) {
                return Message.error(Info.EINVAL.getInfo());
            }

            OssTripartiteSystem ossTripartiteSystem = systemKeyCache.getAndSysnc(schema.getCode());
            //查找三方系统
            if (ossTripartiteSystem == null) {
                return Message.error(Info.PERSMISSION_DENIED.getInfo());
            }

            String policyStr = CalcUtil.getFromBase64(schema.getPolicy());//base64解码

            //对比原信息和信息签名
            if (!CalcUtil.hamcsha1(policyStr, ossTripartiteSystem.getKey()).toLowerCase().equals(CalcUtil.getFromBase64(schema.getSignature()).toLowerCase())) {
                return Message.error(Info.TAMPER_INFO.getInfo());
            }


            Policy policy = EncryptUtil.decodePolicy(policyStr);

            if (policy == null) {
                return Message.error(Info.TAMPER_INFO.getInfo());
            }

            if (policy.getExpire() != 0 && policy.getExpire() < System.currentTimeMillis() - 5000) {//下载时间过期  5秒过渡值
                return Message.error(Info.DOWNLOAD_OVERTIME.getInfo());
            }
            if (policy.getCount() < 0) {//下载次数错误
                return Message.error(Info.DOWNLOAD_COUNTERROR.getInfo());
            }

            OssFile ossFile = ossFileService.getOssFile(policy.getFileNo().intValue());
            if (ossFile == null) {//文件不存在
                return Message.error(Info.FILE_NOTEXIST.getInfo());
            }

            stage = 1;//阶段一

            ossDownloadUrl = ossDownloadUrlService.getByCodeAndPolicy(schema.getPolicy(), schema.getCode());
            if (ossDownloadUrl == null) {//url不存在
                int countLess = policy.getCount() == DownLoadCountLess.LIMIT.getValue() ? DownLoadCountLess.COUNT_LESS.getValue() : DownLoadCountLess.LIMIT.getValue();//是否无限制次数
                url = new OssDownloadUrl(schema.getPolicy(), schema.getCode(), policy.getCount().intValue(), policy.getExpire().intValue(), countLess);

                if (countLess != DownLoadCountLess.COUNT_LESS.getValue()) {//存在次数限制
                    int nowCount = url.getCount() - 1;
                    if (nowCount < 0) {
                        return Message.error(Info.DOWNLOAD_COUNTOVER.getInfo());
                    }
                    url.setCount(nowCount);
                }
                ossDownloadUrlService.save(url);
            } else {//存在
                if (ossDownloadUrl.getCountLess() != DownLoadCountLess.COUNT_LESS.getValue()) {//存在次数限制

                    int nowCount = ossDownloadUrl.getCount() - 1;
                    if (nowCount < 0) {
                        return Message.error(Info.DOWNLOAD_COUNTOVER.getInfo());
                    }
                    ossDownloadUrl.setCount(nowCount);
                    ossDownloadUrlService.save(ossDownloadUrl);
                }
            }

            File file = new File(ossFile.getPath());
            if (!file.exists()) {
                return Message.error(Info.FILE_NOTEXIST.getInfo());
            }


            DownloadUtil.download(response, request, file, ossFile, schema.getDownload());

            taskExecutor.execute(new OssDateLogTask(ossDateLogService, new HandleInfo(HandleType.DOWNLOAD)));

            if (logger.isDebugEnabled())
                logger.debug(request.getRemoteAddr() + "下载了文件[" + ossFile.getName() + "]");
            return null;
        } catch (Exception e) {
            //此方法不添加事务，而采用手动回滚解决方案
            if (stage == 1) {//阶段一
                if (ossDownloadUrl == null) {//表中不存在url
                    if (url != null) {
                        if (url.getCountLess() != DownLoadCountLess.COUNT_LESS.getValue()) {//存在次数限制
                            url.setCount(url.getCount() + 1);
                            ossDownloadUrlService.save(url);
                        }
                    }
                } else {//存在
                    if (ossDownloadUrl.getCountLess() != DownLoadCountLess.COUNT_LESS.getValue()) {//存在次数限制
                        ossDownloadUrl.setCount(ossDownloadUrl.getCount() + 1);
                        ossDownloadUrlService.save(ossDownloadUrl);
                    }
                }

            }

            throw e;
        }
    }
}
