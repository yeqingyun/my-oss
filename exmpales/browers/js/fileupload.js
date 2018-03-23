;
var GnifFileUpload = (function () {

    function flashVersion() {
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


    function updateProgress(file, percentage) {
        var $li = $('#' + file.id);

        var d = $('#' + file.id)[0];
        d.getElementsByTagName('b')[0].innerHTML = '<span>' + parseFloat(percentage * 100).toFixed(2) + "%</span>";
        var prog = d.getElementsByTagName('div')[0];
        var progBar = prog.getElementsByTagName('div')[0];
        progBar.style.width = 2 * percentage * 100 + 'px';
        progBar.setAttribute('aria-valuenow', percentage * 100);


    }

    var uploader;
    var $list;
    var filesCount = 0;
    var key = "a15a6146a9bbbc24";
    var uploadUrl;


    function _init(swfUrl, hostUrl, filePickerDiv, fileListDiv, isMultiple, uploadAllFunc) {
        uploadUrl = hostUrl + "common_upload.html";

        if (!WebUploader.Uploader.support('flash') && WebUploader.browser.ie) {
            if (flashVersion()) {
                alert("flash版本过低");
            } else {
                alert("没有安装flash");
            }
            return;
        } else if (!WebUploader.Uploader.support()) {
            alert("不支持该浏览器");
            return;
        }

        WebUploader.Uploader.register({
            "before-send-file": "beforeSendFile",
            "before-send": "beforeSend",
            "after-send-file": "afterSendFile"
        }, {
            //时间点1：所有分块进行上传之前调用此函数
            beforeSendFile: function (file) {
                var ow = this.owner;
                var deferred = WebUploader.Deferred();
                if (file.size > 50 * 1024 * 1024) {//文件大于50M
                    uploadUrl = hostUrl + "upload.html";
                    this.owner.options.server = uploadUrl;

                    (new WebUploader.Uploader()).md5File(file, 0, file.size).progress(function (percentage) {
                        $('#' + file.id).find('b.state').text('文件分析中...');
                    }).then(function (val) {
                        file.md5 = val;

                        $.jsonp({
                            async: true,
                            type: "POST",
                            url: uploadUrl,
                            callbackParameter: "callback",
                            callback: "uploadcallback",
                            data: {
                                code: "workflow",
                                signature: encodeURIComponent(CryptoJS.enc.Base64.stringify(CryptoJS.enc.Utf8.parse(CryptoJS.HmacSHA1("Tue 23 May 2017 00:53:45 GMT" + "\n" + file.md5 + "\n" + file.name + "\n" + file.size, key)))),
                                fileInfo: encodeURIComponent(CryptoJS.enc.Base64.stringify(CryptoJS.enc.Utf8.parse("Tue 23 May 2017 00:53:45 GMT" + "\n" + file.md5 + "\n" + file.name + "\n" + file.size))),
                                step: 1
                            },
                            success: function (response) {
                                if (response.isSuccess) {
                                    if (!response.notRepeat) {
                                        //文件存在，跳过
                                        deferred.reject(response);
                                        uploader.trigger('repeatUpload', response);
                                    } else {
                                        deferred.resolve();
                                    }
                                } else {
                                    alert(JSON.stringify(response));
                                    deferred.reject(response);
                                }
                            },
                            error: function (XMLHttpRequest, textStatus, errorThrown) {
                                uploader.stop(true);
                                deferred.reject();
                                $('#' + file.id).find('b.state').text('上传出错  ' + errorThrown);
                                if (!!uploadAllFunc) {
                                    uploadAllFunc();
                                }
                                alert("连接出错,请刷新页面，重试");
                            }
                        });

                    });
                } else {
                    uploadUrl = hostUrl + "common_upload.html";
                    this.owner.options.server = uploadUrl;
                    (new WebUploader.Uploader()).md5File(file, 0, file.size).progress(function (percentage) {
                        $('#' + file.id).find('b.state').text('文件分析中...');
                    }).then(function (val) {
                        ow.options.formData.fileMd5 = val;
                        deferred.resolve();
                    });

                }
                return deferred.promise();
            },
            //时间点2：如果有分块上传，则每个分块上传之前调用此函数
            beforeSend: function (block) {
                var ow = this.owner;
                var deferred = WebUploader.Deferred();
                var file = block.file;

                if (file.size > 50 * 1024 * 1024) {
                    (ow).md5File(block.file, block.chunk * ow.options.chunkSize, block.chunk * ow.options.chunkSize + parseInt(block.end - block.start)).then(function (val) {
                        $.jsonp({
                            type: "POST",
                            url: uploadUrl,
                            callbackParameter: "callback",
                            callback: "uploadcallback",
                            data: {
                                //文件名称
                                fileName: block.file.name,
                                //文件唯一标记
                                fileMd5: block.file.md5,
                                //文件大小
                                fileSize: block.file.size,
                                //当前分块名称，即起始位置
                                chunkOrder: block.chunk != 0 ? block.chunk : block.chunks,
                                //当前分块大小
                                chunkSize: block.end - block.start,
                                step: 2
                            },
                            success: function (response) {
                                if (response.isSuccess) {
                                    if (!response.notRepeat) {
                                        //文件块存在，跳过
                                        console.log("文件块已存在");
                                        deferred.reject(response);
                                    } else {
                                        //分块不存在或不完整，重新发送该分块内容
                                        deferred.resolve();
                                    }
                                } else {
                                    if (!response.isSuccess) {
                                        //如果是文件出错，则停止上传
                                        uploader.stop(true);
                                    }
                                    deferred.reject();
                                }
                            },
                            error: function (XMLHttpRequest, textStatus, errorThrown) {
                                deferred.reject();
                                uploader.stop(true);
                                $('#' + file.id).find('b.state').text('上传出错  ' + errorThrown);
                                if (!!uploadAllFunc) {
                                    uploadAllFunc();
                                }
                                alert("连接出错,请刷新页面，重试");
                            }
                        });
                        ow.options.formData.fileMd5 = block.file.md5;
                        ow.options.formData.chunk = block.chunk != 0 ? block.chunk : block.chunks;
                        ow.options.formData.chunkSize = block.end - block.start;
                        ow.options.formData.chunkMd5 = val;
                    });
                } else {
                    deferred.resolve();
                }
                return deferred.promise();

            },
            //时间点3：所有分块上传成功后调用此函数
            afterSendFile: function (file) {
                var deferred = WebUploader.Deferred();
                if (file.size > 50 * 1024 * 1024) {
                    //如果分块上传成功，则通知后台合并分块
                    $.jsonp({
                        type: "POST",
                        url: uploadUrl,
                        callbackParameter: "callback",
                        callback: "uploadcallback",
                        data: {
                            //文件名称
                            fileName: file.name,
                            //文件唯一标记
                            fileMd5: file.md5,
                            //文件大小
                            fileSize: file.size,
                            step: 4,
                            code: 'workflow'
                            //call: encodeURIComponent("eyJ1cmwiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvZ25pZi1vc3MvZG93bmxvYWQuaHRtbCIsInBhcmFtIjoicG9saWN5PU0xeHVNRnh1TVE9PSZzaWduYXR1cmU9TldFNFpqSmtZall4WkROak9UbGlaVGd4TkdNMlpXUTFNMlJpWmpkaVl6RTBOakE0WldNME1nPT0mY29kZT13b3JrZmxvdyJ9IA==")
                        },
                        success: function (response) {
                            if (response.isSuccess) {
                                $('#' + file.id).find('b.state').text('上传成功');
                                uploadAllFunc(response);
                            } else {

                                updateProgress(file, 0);
                                $('#' + file.id).find('b.state').text("上传失败，请刷新页面重新上传！！( 错误原因" + (!!response ? response.message : "") + ")");
                            }
                            deferred.resolve();
                            filesCount++;
                            if (filesCount >= uploader.getFiles().length && !!uploadAllFunc) {
                                uploadAllFunc(response);
                            }
                        },
                        error: function (XMLHttpRequest, textStatus, errorThrown) {
                            uploader.stop(true);
                            deferred.reject();
                            $('#' + file.id).find('b.state').text('上传出错  ' + errorThrown);
                            if (!!uploadAllFunc) {
                                uploadAllFunc();
                            }
                            alert("连接出错,请刷新页面，重试");
                        }
                    });
                } else {
                    deferred.resolve();
                }
                return deferred.promise();
            }
        });

        $list = $('#' + fileListDiv);

        uploader = WebUploader.create({
            // swf文件路径
            swf: swfUrl,
            // 文件接收服务端。
            server: uploadUrl,
            //是否要分片处理大文件上传。
            chunked: true,
            // 如果要分片，分多大一片？ 默认大小为10M.
            chunkSize: 50 * 1024 * 1024,
            // 选择文件的按钮。可选。
            // 内部根据当前运行是创建，可能是input元素，也可能是flash.
            pick: {
                id: '#' + filePickerDiv,
                multiple: !!!isMultiple ? isMultiple : true
            },
            // 不压缩image, 默认如果是jpeg，文件上传前会压缩一把再上传！
            resize: false,
            method: 'POST',
            prepareNextFile: true,
            threads: 1,
            compress: false
        });

        //当有文件被添加进队列的时候
        uploader.on('fileQueued', function (file) {
            var sizeText;
            if (file.size / 1024 / 1024 / 1024 > 1) {
                sizeText = parseFloat(file.size / 1024 / 1024 / 1024).toFixed(2) + "Gb";
            } else if (file.size / 1024 / 1024 > 1) {
                sizeText = parseFloat(file.size / 1024 / 1024).toFixed(2) + "Mb";
            } else if (file.size / 1024 > 1) {
                sizeText = parseFloat(file.size / 1024).toFixed(2) + "kb";
            } else {
                sizeText = parseFloat(file.size).toFixed(2) + "b";
            }
            $list.append('<div id="' + file.id + '">' + file.name + ' (' + sizeText + ')<b class="state"></b>'
                + '<table><tr><td><div class="progress"><div class="progress-bar" style="width: 0%"></div></div></td><td class="file-del-btn"><a href="javascript:void(0);">删除</a></td></tr>'
                + '</div>');

            $(".file-del-btn").click(function(){
                $("#"+$(this).parents("div").attr("id")).remove();
                uploader.removeFile($(this).parents("div").attr("id"),true);
            });
        });

        //文件上传过程中创建进度条实时显示。
        uploader.on('uploadProgress', function (file, percentage) {
            updateProgress(file, percentage);
        });

        uploader.on('uploadError', function (file, response) {
            if (response.isSuccess) {
                updateProgress(file, 1);
                $('#' + file.id).find('b.state').text('上传成功');
            } else {
                updateProgress(file, 0);
                $('#' + file.id).find('b.state').text('上传出错  ' + (response.message ? response.message : ""));
            }
            filesCount++;
            if (filesCount >= uploader.getFiles().length && !!uploadAllFunc) {
                uploadAllFunc();
            }
        });


        uploader.on('uploadBeforeSend', function (block, data) {
            // file为分块对应的file对象。
            var file = block.file;
            // 修改data可以控制发送哪些携带数据。
            var chunkMd5 = data.chunkMd5;
            data.code = 'workflow';


            if(file.size > 50*1024*1024){
                data.signature = encodeURIComponent(CryptoJS.enc.Base64.stringify(CryptoJS.enc.Utf8.parse(CryptoJS.HmacSHA1("Tue 23 May 2017 00:53:45 GMT" + "\n" + data.fileMd5 + "\n" + file.name + "\n" + file.size + "\n" + chunkMd5 + "\n" + data.chunkSize, key))));
                data.fileInfo = encodeURIComponent(CryptoJS.enc.Base64.stringify(CryptoJS.enc.Utf8.parse("Tue 23 May 2017 00:53:45 GMT" + "\n" + data.fileMd5 + "\n" + file.name + "\n" + file.size + "\n" + chunkMd5 + "\n" + data.chunkSize)));
                data.step = 3;
            }else{
                data.signature = encodeURIComponent(CryptoJS.enc.Base64.stringify(CryptoJS.enc.Utf8.parse(CryptoJS.HmacSHA1("Tue 23 May 2017 00:53:45 GMT" + "\n" + data.fileMd5 + "\n" + file.name + "\n" + file.size, key))));
                data.fileInfo = encodeURIComponent(CryptoJS.enc.Base64.stringify(CryptoJS.enc.Utf8.parse("Tue 23 May 2017 00:53:45 GMT" + "\n" + data.fileMd5 + "\n" + file.name + "\n" + file.size)));
            }

        });


        uploader.on('uploadAccept', function (object, response) {//上传中
            var deferred = WebUploader.Deferred();
            if (!response.isSuccess) {
                //显示错误信息
                alert(JSON.stringify(response));
                return false;
            } else {
                deferred.resolve();
            }
        });


        uploader.on('uploadSuccess', function (file, response) {//上传成功
            if (file.size <= 50 * 1024 * 1024) {
                $('#' + file.id).find('b.state').text('上传成功');
                uploadAllFunc(response);
            }
        });


        uploader.on('repeatUpload', function (response) {//上传重复
            uploadAllFunc(response);
        });


    }


    function _stop() {
        uploader.stop(true);
    }

    function _start() {
        uploader.upload();
        if (uploader.getFiles().length == 0) {
            alert("请选择文件");
            return;
        }
    }

    function _getFiles() {
        if (!!uploader) {
            return uploader.getFiles();
        }
    }

    function _getUploadedFiles() {
        return filesCount;
    }

    return {
        init: _init,
        start: _start,
        stop: _stop,
        getFiles: _getFiles,
        getUploadedFiles: _getUploadedFiles
    };

})();