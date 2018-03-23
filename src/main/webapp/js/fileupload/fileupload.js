var GnifFileUpload = function () {

    //判断flash版本
    var flashVersion = function () {
        var version;
        try {
            version = navigator.plugins['Shockwave Flash'];
            version = version.description;
        } catch (ex) {
            try {
                version = new ActiveXObject('ShockwaveFlash.ShockwaveFlash')
                    .GetVariable('$version');
            } catch (ex2) {
                version = '0.0';
            }
        }
        version = version.match(/\d+/g);
        return parseFloat(version[0] + '.' + version[1], 10);
    }

    //进度条变化
    var updateProgress = function ($fileList, file, percentage) {
        var progBar = $fileList.find('.' + file.id);
        var pg = progBar.find(".progress-bar")[0];
        if (pg) {
            progBar.attr('aria-valuenow', percentage * 100);
            pg.style.width = percentage * 100 + "%";
            $fileList.find('.' + file.id + 'file-status').text('正在上传');
        }
    }

    //根据flash显示提示信息
    var hintFlashInfo = function () {
        if (!WebUploader.Uploader.support('flash') && WebUploader.browser.ie) {
            if (flashVersion()) {
                $.messager.alert('提示', "flash版本过低，上传控件无法使用", 'info');
            } else {
                $.messager.alert('提示', "没有安装flash，上传控件无法使用", 'info');
            }
            return;
        } else if (!WebUploader.Uploader.support()) {
            $.messager.alert('提示', "上传控件不支持当前浏览器，请换一个浏览器后重试", 'info');
            return;
        }
    }

    //初始化easyUi文件显示grid
    var initFileGrid = function ($fileListDiv) {
        $fileListDiv.datagrid({
            columns: [[
                {field: 'fileId', title: '文件id', width: 0, hidden: true},
                {field: 'fileName', title: '文件', width: 170},
                {field: 'fileSize', title: '文件大小', width: 120},
                {
                    field: 'fileProgress', title: '上传进度', width: 250, align: 'center', height: 30,
                    formatter: function (value, row) {
                        var returnStr = '<div class="progress ' + row.fileId + '"><div class="progress-bar" style="width: 0%"></div></div>';
                        return returnStr;
                    }
                },
                {
                    field: 'fileStatus', title: '上传状态', width: 105, align: 'center',
                    formatter: function (value, row) {
                        var returnStr = "<label class='" + row.fileId + "file-status'>" + value + "</label>";
                        return returnStr;
                    }
                }
            ]],
            title: '文件列表',
            fitColumns: true,
            rownumbers: true,
            nowrap: true,
            border: true
        });

        $fileListDiv.datagrid('fixRowHeight');
    }


    var createUploader = function (swfUrl, fileListDiv, $fileListDiv, filePickerDiv, $pluginDiv, uploadAllFunc, base) {


        var uploadUrl;
        WebUploader.Uploader.register({
            "before-send-file": "beforeSendFile",
            "before-send": "beforeSend",
            "after-send-file": "afterSendFile"
        }, {
            //时间点1：所有分块进行上传之前调用此函数
            beforeSendFile: function (file) {
                var deferred = WebUploader.Deferred(), ow = this.owner, files = uploader.getFiles(), flag = false;
                for (var i = 0; i < files.length; i++) {
                    if (flag = (files[i].id == file.id)) {
                        break;
                    }
                }
                if (!flag) {
                    return;
                }

                if (file.size > 52428800) {//文件大于50M
                    uploadUrl = base + "adminOperateFile/upload.html";
                    this.owner.options.server = uploadUrl;
                    (new WebUploader.Uploader()).md5File(file, 0, file.size).progress(function (percentage) {
                        $pluginDiv.find('.' + file.id + 'file-status').text('文件分析中');
                    }).then(function (val) {
                        file.md5 = val;
                        ow.options.formData.fileMd5 = val;

                        var data = {
                            code: uploader.code,
                            tmp: uploader.tmp,
                            fileMd5: file.md5,
                            fileName: file.name,
                            fileSize: file.size,
                            step: 1
                        };

                        if (uploader.fileId) {
                            data.fileId = uploader.fileId;
                        } else if (uploader.deleteFileId) {
                            data.deleteFileId = uploader.deleteFileId;
                        }
                        $.ajax({
                            async: true,
                            type: "POST",
                            url: uploadUrl,
                            data: data,
                            success: function (response) {
                                response = JSON.parse(response)
                                if (response.isSuccess) {
                                    if (!response.notRepeat) {
                                        //文件存在，跳过
                                        $pluginDiv.find('.' + file.id + 'file-status').text('上传成功');
                                        deferred.reject(response);
                                    } else {
                                        deferred.resolve();
                                    }
                                } else {
                                    deferred.reject(response);
                                }
                            }

                            ,
                            error: function (XMLHttpRequest, textStatus, errorThrown) {
                                uploader.stop(true);
                                deferred.reject();
                                $pluginDiv.find('.' + file.id + 'file-status').text('上传出错');
                                $.messager.alert('提示', "连接出错,请刷新页面，重试", 'error');
                            }
                        })
                        ;

                    });


                } else {
                    uploadUrl = base + "adminOperateFile/common_upload.html";
                    this.owner.options.server = uploadUrl;
                    (new WebUploader.Uploader()).md5File(file, 0, file.size).progress(function (percentage) {
                        $pluginDiv.find('.' + file.id + 'file-status').text('文件分析中');
                    }).then(function (val) {
                        ow.options.formData.fileMd5 = val;
                        deferred.resolve();
                    });
                }


                return deferred.promise();
            },
            //时间点2：如果有分块上传，则每个分块上传之前调用此函数
            beforeSend: function (block) {
                var deferred = WebUploader.Deferred(), ow = this.owner, file = block.file, flag = false,
                    files = uploader.getFiles();
                for (var i = 0; i < files.length; i++) {
                    if (flag = (files[i].id == file.id)) {
                        break;
                    }
                }
                if (!flag) {
                    return;
                }

                if (file.size > 52428800) {
                    $.ajax({
                        type: "POST",
                        url: uploadUrl,
                        data: {
                            //文件名称
                            fileName: block.file.name,
                            //文件md5
                            fileMd5: ow.options.formData.fileMd5,
                            //文件大小
                            fileSize: block.file.size,
                            //当前分块名称，即起始位置
                            chunkOrder: block.chunk != 0 ? block.chunk : block.chunks,
                            //当前分块大小
                            chunkSize: block.end - block.start,
                            step: 2
                        },
                        success: function (response) {
                            response = JSON.parse(response)
                            if (response.isSuccess) {
                                if (!response.notRepeat) {
                                    //文件块存在，跳过
                                    deferred.reject();
                                } else {
                                    //分块不存在或不完整，重新发送该分块内容
                                    deferred.resolve();
                                }
                            } else {
                                //如果是文件出错，则停止上传
                                uploader.trigger('uploadError', file, response);
                            }
                        },
                        error: function (XMLHttpRequest, textStatus, errorThrown) {
                            deferred.reject();
                            uploader.stop(true);
                            $pluginDiv.find('.' + file.id + 'file-status').text('上传出错');
                            $.messager.alert('提示', "连接出错,请刷新页面，重试", 'error');
                        }
                    });
                    ow.options.formData.chunk = block.chunk != 0 ? block.chunk : block.chunks;
                } else {
                    deferred.resolve();
                }
                return deferred.promise();

            },
            //时间点3：所有分块上传成功后调用此函数
            afterSendFile: function (file) {
                var flag = false, files = uploader.getFiles(), ow = this.owner;
                for (var i = 0; i < files.length; i++) {
                    if (flag = (files[i].id == file.id)) {
                        break;
                    }
                }
                if (!flag) {
                    return;
                }
                var deferred = WebUploader.Deferred();
                if (file.size > 52428800) {
                    //如果分块上传成功，则通知后台合并分块
                    if (uploader.fileId) {

                        var data = {
                            step: 4,
                            fileId: uploader.fileId,
                            fileName: file.name,
                            //文件唯一标记
                            fileMd5: ow.options.formData.fileMd5,
                            //文件大小
                            fileSize: file.size
                        };

                    } else if (uploader.deleteFileId) {

                        var data = {
                            step: 4,
                            deleteFileId: uploader.deleteFileId,
                            fileName: file.name,
                            //文件唯一标记
                            fileMd5: ow.options.formData.fileMd5,
                            //文件大,
                            fileSize: file.size
                        };

                    } else {
                        var data = {
                            //文件名称
                            fileName: file.name,
                            //文件唯一标记
                            fileMd5: ow.options.formData.fileMd5,
                            //文件大小
                            fileSize: file.size,
                            step: 4,
                            code: uploader.code,
                            tmp: uploader.tmp,

                        };
                        if (uploader.savepath) {
                            data.savepath = uploader.savepath;
                        }
                    }


                    $.ajax({
                        type: "POST",
                        url: uploadUrl,
                        data: data,
                        success: function (response) {
                            response = JSON.parse(response);
                            if (response.isSuccess) {
                                $pluginDiv.find('.' + file.id + 'file-status').text('上传成功');
                                if (uploadAllFunc)
                                    uploadAllFunc(response, file);
                                deferred.resolve();
                            } else {
                                updateProgress($pluginDiv, file, 0);
                                uploader.trigger('uploadError', file, response);
                            }
                        },
                        error: function (XMLHttpRequest, textStatus, errorThrown) {
                            uploader.stop(true);
                            deferred.reject();
                            $pluginDiv.find('.' + file.id + 'file-status').text('上传出错');
                            $.messager.alert('提示', "连接出错,请刷新页面，重试", 'error');
                        }
                    });
                } else {
                    deferred.resolve();
                }
                return deferred.promise();
            }
        });

        var uploader = WebUploader.create({
            // swf文件路径
            swf: swfUrl,
            // 文件接收服务端。
            server: '',
            //是否要分片处理大文件上传。
            chunked: true,
            // 如果要分片，分多大一片？ 默认大小为50M.
            chunkSize: 50 * 1024 * 1024,
            // 选择文件的按钮。可选。
            // 内部根据当前运行是创建，可能是input元素，也可能是flash.
            pick: {
                id: '#' + filePickerDiv,
                multiple: false
            },
            // 不压缩image, 默认如果是jpeg，文件上传前会压缩再上传！
            resize: false,
            method: 'POST',
            prepareNextFile: true,
            threads: 3,
            compress: false
        });

        uploader.id = fileListDiv;
        uploader.list = $fileListDiv;

        return uploader;
    }

    var initBtn = function (filePickerDiv, pluginDiv) {
        $('#' + filePickerDiv).unbind();
        $('#' + pluginDiv + ' .webuploader-pick').click(function () {
            $('#' + pluginDiv + " .webuploader-element-invisible").trigger("click");
        });
    }

    var registerWebUploadEvent = function (uploader, $pluginDiv, $fileListDiv, uploadAllFunc) {
        //当有文件被添加进队列的时候
        uploader.on('fileQueued', function (file) {
            var sizeText = (file.size).formatSize();

            $fileListDiv.datagrid('appendRow', {
                fileId: file.id,
                fileName: file.name,
                fileSize: sizeText,
                fileStatus: "等待上传"
            });

            if (!uploader.multiple) {
                //将新增的文件替换掉第一个文件(上传队列中永远只有一个文件)

                var item = $fileListDiv.datagrid('getRows');
                if (item.length > 1) {
                    //从上传队列中移除文件
                    uploader.removeFile(item[0].fileId, true);
                    //删除文件显示
                    $fileListDiv.datagrid('deleteRow', $fileListDiv.datagrid('getRowIndex', item[0]));
                }
            }


            $pluginDiv.find("div[class$='-fileProgress']").attr("style", "height:25px;text-align:center;");
        });

        //文件上传过程中创建进度条实时显示。
        uploader.on('uploadProgress', function (file, percentage) {
            updateProgress($pluginDiv, file, percentage);
        });

        uploader.on('uploadError', function (file, response) {
            if (response && response.isSuccess) {
                updateProgress($pluginDiv, file, 1);
                $pluginDiv.find('.' + file.id + 'file-status').text('上传成功');
                if (uploadAllFunc)
                    uploadAllFunc(response, file);
            } else {
                updateProgress($pluginDiv, file, 0);
                $pluginDiv.find('.' + file.id + 'file-status').text('上传失败');
                if (response && response.message) {
                    $.messager.alert('上传失败', response.message, 'error');
                } else {
                    if (response && response == "abort") {
                        $.messager.alert('上传失败', "未连接到服务器", 'error');
                    }
                }
            }

        });


        uploader.on('uploadBeforeSend', function (block, data) {
            // file为分块对应的file对象。
            var file = block.file;
            // 修改data可以控制发送哪些携带数据。
            if (file.size > 52428800) {
                data.step = 3;
                data.fileSize = file.size;
            } else {
                data.fileId = uploader.fileId;
                data.deleteFileId = uploader.deleteFileId;
                data.code = uploader.code;
                data.tmp = uploader.tmp;
                data.fileName = file.name;
                data.fileSize = file.size;
                if (uploader.savepath) {
                    data.savepath = uploader.savepath;
                }
            }

        });


        uploader.on('uploadAccept', function (object, response) {//上传中
            var deferred = WebUploader.Deferred();
            if (!response.isSuccess) {
                //显示错误信息
                $pluginDiv.find('.' + object.id + 'file-status').text('上传失败');
                $.messager.alert('上传失败', response.message, 'error');
                return false;
            } else {
                deferred.resolve();
            }
        });


        uploader.on('uploadSuccess', function (file, response) {//上传成功
            if (!response.isSuccess) {
                uploader.trigger('uploadError', file, response);
                return;
            }
            if (file.size <= 52428800) {
                $pluginDiv.find('.' + file.id + 'file-status').text('上传成功');
                if (uploadAllFunc)
                    uploadAllFunc(response, file);
            }
        });
    }


    function clearFileList($fileListDiv) {
        $fileListDiv.datagrid('loadData', {total: 0, rows: []});
    }


    //初始化按钮
    function _init(swfUrl, filePickerDiv, fileListDiv, uploadBtn, pluginDiv, uploadAllFunc, base) {

        var $pluginDiv = $("#" + pluginDiv),
            $fileListDiv = $("#" + fileListDiv),
            $pluginDiv = $("#" + pluginDiv),
            uploader;

        hintFlashInfo();

        initFileGrid($fileListDiv);

        //registerWebUploadStep($pluginDiv, uploader, uploadAllFunc);

        uploader = createUploader(swfUrl, fileListDiv, $fileListDiv, filePickerDiv, $pluginDiv, uploadAllFunc, base);

        registerWebUploadEvent(uploader, $pluginDiv, $fileListDiv, uploadAllFunc);

        initBtn(filePickerDiv, pluginDiv);


        //初始化上传按钮事件
        $("#" + uploadBtn).unbind('click').click(function () {
            _start(uploader);
        });

        return uploader;
    }


    function _start(ur) {
        if (ur.getFiles().length == 0) {
            $.messager.alert('提示', "请选择文件", 'info');
            return;
        }
        if (!ur.code && !ur.fileId && !ur.deleteFileId) {
            $.messager.alert('提示', "请选择所属系统", 'info');
            return;
        }
        ur.upload();
    }

    function _getFiles(ur) {
        if (!!ur) {
            return ur.getFiles();
        }
    }

    function uploaderRemove(ur, fileId) {
        ur.removeFile(fileId, true);
    }


    return {
        init: _init,
        start: _start,
        getFiles: _getFiles,
        removeFileFromQueue: uploaderRemove,
        clearFileList: clearFileList
    };

};