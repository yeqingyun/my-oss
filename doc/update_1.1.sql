use gnif_oss;



INSERT INTO `auth_resource` VALUES ('16', '11', '2', '文件删除日志', 'file_delete_log', '1', '/file/fileDeleteLog.html', '/file/fileDeleteLog.html', null, '0', null, null, null, null, null);
INSERT INTO `auth_resource` VALUES ('17', '16', '3', '删除', null, '4', 'remove', '/file/fileDeleteLog/remove.json', 'icon-remove', '0', null, 'developer', '2017-08-28', 'developer', '2017-08-28');
INSERT INTO `auth_resource` VALUES ('18', '12', '3', '上传', null, '5', 'upload', '/fileManager/load.json', 'icon-add', '0', null, 'developer', '2017-06-26', 'developer', '2017-06-26');
INSERT INTO `auth_group_resource` VALUES ('1', '16', '0', null, null, null, null, null);
INSERT INTO `auth_group_resource` VALUES ('1', '17', '0', null, null, null, null, null);
INSERT INTO `auth_group_resource` VALUES ('1', '18', '0', null, null, null, null, null);

DROP TABLE IF EXISTS `oss_delete_file_log`;
CREATE TABLE `oss_delete_file_log` (
  `id` int(10) NOT NULL,
  `file_id` int(10) NOT NULL COMMENT '被删除的文件Id',
  `file_name` varchar(500) NOT NULL COMMENT '被删除的文件名称',
  `system_code` varchar(500) NOT NULL COMMENT '所属系统编码',
  `file_size` int(11) NOT NULL COMMENT '被删除的文件大小',
  `file_path` varchar(2048) NOT NULL COMMENT '被删除的文件路径',
  `file_md5` varchar(1024) NOT NULL COMMENT '被删除的文件md5',
  `file_refer` varchar(1024) NOT NULL COMMENT '被删除的文件引用数',
  `file_tmp` int(10) NOT NULL DEFAULT 1 COMMENT '被删除的文件状态',
  `delete_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP() NOT NULL COMMENT '删除时间',
  `status` int(11) NOT NULL DEFAULT '0',
  `remark` varchar(1024) DEFAULT NULL,
  `create_by` varchar(128) DEFAULT NULL,
  `create_time` date DEFAULT NULL,
  `update_by` varchar(128) DEFAULT NULL,
  `update_time` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_name_code_delete_time` (`file_name`,`system_code`,`delete_time`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 COMMENT='文件删除日志表';
