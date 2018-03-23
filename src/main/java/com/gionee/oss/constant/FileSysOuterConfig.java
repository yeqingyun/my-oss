package com.gionee.oss.constant;


import com.gionee.oss.biz.service.OssCoreUploadFileService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class FileSysOuterConfig implements InitializingBean {

    @Autowired
    private PropertiesConfig propertiesConfig;

    public void setFileService(OssCoreUploadFileService fileService) {
        this.propertiesConfig.setFileService(fileService);
    }

    public void setRootPath(String rootPath) {
        this.propertiesConfig.setRootPath(rootPath);
    }

    public void setSizeThreshold(Integer sizeThreshold) {
        this.propertiesConfig.setSizeThreshold(sizeThreshold);
    }

    public void setFileSizeMax(Long fileSizeMax) {
        this.propertiesConfig.setFileSizeMax(fileSizeMax);
    }

    public void setEncoding(String encoding) {
        this.propertiesConfig.setEncoding(encoding);
    }

    public void setSystemRootPath(String path) {
        this.propertiesConfig.setSystemRootPath(path);
    }

    @Override
    public void afterPropertiesSet() throws IOException {
    }

    public void init() throws IOException {
        propertiesConfig.init();
    }

}
