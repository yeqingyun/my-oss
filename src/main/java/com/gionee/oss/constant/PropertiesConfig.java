package com.gionee.oss.constant;

import com.gionee.oss.biz.service.OssCoreUploadFileService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

/**
 * 过渡型的配置文件，用于向controller提供service，向service提供一些配置功能，
 * 便于在没有配置的情况下，该组件可以提供默认的配置
 *
 * @author Administrator
 */
@Component
public class PropertiesConfig implements InitializingBean, ApplicationContextAware {

    //文件分割符
    public final static String fileSeparator = File.separator;
    public final static String directorySeparator = "-";
    //默认情况下的本地的属性，用于处理文件传入记录
    private ApplicationContext applicationContext;
    //临时文件目录
    private String rootPath;
    //缺省配置下的默认缓存区的接受大小，超过了写入临时文件
    private Integer sizeThreshold = 26214400;
    //缺省配置下单个文件大小
    private Long fileSizeMax = 52428800l;
    //每个请求的大小
    private String encoding = "UTF-8";
    //系统默认存放路径，所有系统文件均存于此文件夹下
    private String systemRootPath;

    private CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
    private OssCoreUploadFileService fileService;

    public String getSystemRootPath() {
        return systemRootPath;
    }

    public void setSystemRootPath(String systemRootPath) {
        this.systemRootPath = systemRootPath;
    }

    public OssCoreUploadFileService getFileService() {
        return fileService;
    }

    public void setFileService(OssCoreUploadFileService fileService) {
        this.fileService = fileService;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public Integer getSizeThreshold() {
        return sizeThreshold;
    }

    public void setSizeThreshold(Integer sizeThreshold) {
        this.sizeThreshold = sizeThreshold;
    }

    public Long getFileSizeMax() {
        return fileSizeMax;
    }

    public void setFileSizeMax(Long fileSizeMax) {
        this.fileSizeMax = fileSizeMax;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

    public HttpServletRequest getMultipleReuqest(HttpServletRequest request) {
        if (!(request instanceof DefaultMultipartHttpServletRequest)) {
            if (commonsMultipartResolver.isMultipart(request)) {
                return commonsMultipartResolver.resolveMultipart(request);
            }
        }
        return request;
    }


    @Override
    public void afterPropertiesSet() throws IOException {
    }

    public void init() throws IOException {
        File fileDir = new File(rootPath);
        File systemDir = new File(systemRootPath);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        if (!systemDir.exists()) {
            systemDir.mkdirs();
        }
        commonsMultipartResolver.setDefaultEncoding(encoding);
        commonsMultipartResolver.setMaxInMemorySize(sizeThreshold);
        commonsMultipartResolver.setMaxUploadSize(fileSizeMax);
        commonsMultipartResolver.setUploadTempDir(new FileSystemResource(fileDir));
    }


}
