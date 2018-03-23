create database gnif_oss default character set utf8 collate utf8_general_ci;
use gnif_oss;

create table gc_property_ (
  key_ varchar(128),
  value_ varchar(128),
  rev_ integer default 0,
  primary key (key_)
);

insert into gc_property_ (key_, value_) values ('gnif.version', '1.0');
insert into gc_property_ (key_, value_) values ('devtool.version', '1.0');
insert into gc_property_ (key_, value_) values ('auth.version', '1.0');

create table auth_user (
  id integer not null,
  account varchar(128) not null,
  password varchar(128),
  name varchar(128),
  pri_group_id integer,
  org_id integer not null,
  telephone varchar(128),
  mobile varchar(128),
  email varchar(128),
  type integer,
  status integer default 0 not null,
  remark varchar(1024),
  create_by varchar(128),
  create_time date,
  update_by varchar(128),
  update_time date,
  primary key (id)
);

create table auth_group (
  id integer not null,
  code varchar(128),
  org_id integer,
  name varchar(128),
  description varchar(1024),
  scope_ integer,
  status integer default 0 not null,
  remark varchar(1024),
  create_by varchar(128),
  create_time date,
  update_by varchar(128),
  update_time date,
  primary key (id)
);

create table auth_organization (
  id integer not null,
  pid integer default 0 not null,
  name varchar(128),
  full_name varchar(256),
  address varchar(128),
  telephone varchar(128),
  status integer default 0 not null,
  remark varchar(1024),
  create_by varchar(128),
  create_time date,
  update_by varchar(128),
  update_time date,
  primary key (id)
);

create table auth_user_group (
  user_id integer not null,
  group_id integer not null,
  status integer default 0 not null,
  remark varchar(1024),
  create_by varchar(128),
  create_time date,
  update_by varchar(128),
  update_time date,
  primary key (user_id, group_id)
);

create table auth_resource (
  id integer not null,
  pid integer default 0 not null,
  type integer,
  name varchar(128),
  code varchar(128),
  index_ integer,
  action varchar(128),
  url varchar(256),
  icon varchar(128),
  status integer default 0 not null,
  remark varchar(1024),
  create_by varchar(128),
  create_time date,
  update_by varchar(128),
  update_time date,
  primary key (id)
);

create table auth_group_resource (
  group_id integer not null,
  res_id integer not null,
  status integer default 0 not null,
  remark varchar(1024),
  create_by varchar(128),
  create_time date,
  update_by varchar(128),
  update_time date,
  primary key (group_id, res_id)
);

create table auth_pri_group (
  id integer not null,
  name varchar(128) not null unique,
  description varchar(255),
  status integer default 0 not null,
  remark varchar(1024),
  create_by varchar(128),
  create_time date,
  update_by varchar(128),
  update_time date,
  primary key (id)
);

create table auth_pri_define (
  id integer not null,
  pid integer default 0,
  name varchar(128),
  pri_key varchar(128),
  def_value varchar(128),
  model varchar(128),
  pindex integer,
  pri_type integer default 0,
  description varchar(255),
  detail varchar(1024),
  status integer default 0 not null,
  remark varchar(1024),
  create_by varchar(128),
  create_time date,
  update_by varchar(128),
  update_time date,
  primary key (id)
);

create table auth_privilege (
  id integer not null,
  pri_group_id integer not null,
  pri_key varchar(128) not null,
  pri_value varchar(128) not null,
  status integer default 0 not null,
  remark varchar(1024),
  create_by varchar(128),
  create_time date,
  update_by varchar(128),
  update_time date,
  primary key (id)
);

create table dev_data_model_ (
  id_ integer not null,
  name_ varchar(128),
  java_name_ varchar(128),
  db_name_ varchar(128),
  primary key (id_)
);

create table dev_data_field_ (
  id_ integer not null,
  model_id_ integer not null,
  name_ varchar(128),
  java_name_ varchar(128),
  java_type_ varchar(128),
  db_name_ varchar(128),
  db_type_ varchar(128),
  primary key (id_)
);



DROP TABLE IF EXISTS `oss_file`;
CREATE TABLE `oss_file` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `system_code` varchar(200) COMMENT '所属系统编码',
  `name` varchar(200) COMMENT '文件原始名称',
  `md5` varchar(200) COMMENT '文件md5',
  `size` int(10) DEFAULT NULL COMMENT '文件大小',
  `refer` int(10) NOT NULL DEFAULT 1 COMMENT '文件引用数量',
  `path` varchar(2000) NOT NULL DEFAULT NULL COMMENT '存放位置',
  `tmp` int(11) NOT NULL DEFAULT '0'COMMENT '是否临时文件',
  `status` int(11) NOT NULL DEFAULT '0',
  `remark` varchar(1024) DEFAULT NULL,
  `create_by` varchar(128) DEFAULT NULL,
  `create_time` date DEFAULT NULL,
  `update_by` varchar(128) DEFAULT NULL,
  `update_time` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_mad5_size` (`md5`,`size`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 COMMENT='文件信息表';


DROP TABLE IF EXISTS `oss_tripartite_system`;
CREATE TABLE `oss_tripartite_system` (
  `code` varchar(200) COMMENT '系统编码',
  `key_` varchar(200) COMMENT '系统所持加密key',
  `system_rootPath` varchar(200) COMMENT '系统文件根路径',
  `name` varchar(200) COMMENT '系统名称',
  `url` varchar(200) COMMENT '系统地址',
  `status` int(11) NOT NULL DEFAULT '0',
  `remark` varchar(1024) DEFAULT NULL,
  `create_by` varchar(128) DEFAULT NULL,
  `create_time` date DEFAULT NULL,
  `update_by` varchar(128) DEFAULT NULL,
  `update_time` date DEFAULT NULL,
  PRIMARY KEY (`code`),
  KEY `idx_code_key` (`code`,`key_`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='三方系统表';

DROP TABLE IF EXISTS `oss_download_url`;
CREATE TABLE `oss_download_url` (
	`id` int(10) NOT NULL AUTO_INCREMENT,
  `policy` varchar(400) COMMENT 'url策略',
  `code` varchar(200) COMMENT '所属系统编码',
  `count` int(11) DEFAULT 0 COMMENT '已访问次数',
  `countless` int(1) COMMENT '访问是否限制',
  `expire` int(11) NOT NULL DEFAULT 0 COMMENT '过期时间的UNIX格式',
  `remark` varchar(1024) DEFAULT NULL,
	`status` int(11) NOT NULL DEFAULT '0',
  `create_by` varchar(128) DEFAULT NULL,
  `create_time` date DEFAULT NULL,
  `update_by` varchar(128) DEFAULT NULL,
  `update_time` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_code_policy_identify` (`code`,`policy`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='下载url表';

DROP TABLE IF EXISTS `oss_file_log`;
CREATE TABLE `oss_file_log` (
  `file_id` int(10) NOT NULL COMMENT '文件id',
  `download` int(11) DEFAULT 0 COMMENT '下载次数',
  `last_download_date` date DEFAULT NULL COMMENT '最近下载时间',
  `status` int(11) NOT NULL DEFAULT '0',
  `remark` varchar(1024) DEFAULT NULL,
  `create_by` varchar(128) DEFAULT NULL,
  `create_time` date DEFAULT NULL,
  `update_by` varchar(128) DEFAULT NULL,
  `update_time` date DEFAULT NULL,
  PRIMARY KEY (`file_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 COMMENT='文件下载日志表';

DROP TABLE IF EXISTS `oss_date_log`;
CREATE TABLE `oss_date_log` (
  `download` int(11) DEFAULT 0 COMMENT '下载文件数',
  `upload` int(11) DEFAULT 0 COMMENT '上传永久文件数',
  `upload_tmp` int(11) DEFAULT 0 COMMENT '上传临时文件数',
  `delete_` int(11) DEFAULT 0 COMMENT '删除文件数',
  `clear` int(11) DEFAULT 0 COMMENT '清除临时文件数',
  `date_` date NOT NULL COMMENT '时间',
  `status` int(11) NOT NULL DEFAULT '0',
  `remark` varchar(1024) DEFAULT NULL,
  `create_by` varchar(128) DEFAULT NULL,
  `create_time` date DEFAULT NULL,
  `update_by` varchar(128) DEFAULT NULL,
  `update_time` date DEFAULT NULL,
  PRIMARY KEY (`date_`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 COMMENT='文件服务日志表';

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



INSERT INTO `auth_group` VALUES ('1', 'admin', null, 'admin', null, null, '0', null, null, null, null, null);
INSERT INTO `auth_group_resource` VALUES ('1', '1', '0', null, null, null, null, null);
INSERT INTO `auth_group_resource` VALUES ('1', '6', '0', null, null, null, null, null);
INSERT INTO `auth_group_resource` VALUES ('1', '7', '0', null, null, null, null, null);
INSERT INTO `auth_group_resource` VALUES ('1', '9', '0', null, null, null, null, null);
INSERT INTO `auth_group_resource` VALUES ('1', '10', '0', null, null, null, null, null);
INSERT INTO `auth_group_resource` VALUES ('1', '11', '0', null, null, null, null, null);
INSERT INTO `auth_group_resource` VALUES ('1', '12', '0', null, null, null, null, null);
INSERT INTO `auth_group_resource` VALUES ('1', '13', '0', null, null, null, null, null);
INSERT INTO `auth_group_resource` VALUES ('1', '14', '0', null, null, null, null, null);
INSERT INTO `auth_group_resource` VALUES ('1', '15', '0', null, null, null, null, null);
INSERT INTO `auth_group_resource` VALUES ('1', '16', '0', null, null, null, null, null);
INSERT INTO `auth_group_resource` VALUES ('1', '17', '0', null, null, null, null, null);
INSERT INTO `auth_group_resource` VALUES ('1', '18', '0', null, null, null, null, null);
INSERT INTO `auth_organization` VALUES ('1', '0', '开发部', '', '', '', '0', null, 'developer', '2017-06-22', 'developer', '2017-06-22');
INSERT INTO `auth_resource` VALUES ('1', '10', '2', '系统管理', 'system_manager', '1', '/systemManager/systemManager.html', '/systemManager/systemManager.html', '', '0', null, 'developer', '2017-06-22', 'developer', '2017-06-22');
INSERT INTO `auth_resource` VALUES ('6', '1', '3', '新建', null, null, 'create', '/systemManager/create.json', 'icon-add', '0', null, 'developer', '2017-06-23', 'developer', '2017-06-23');
INSERT INTO `auth_resource` VALUES ('7', '1', '3', '编辑', null, null, 'edit', '/systemManager/save.json', 'icon-edit', '0', null, 'developer', '2017-06-23', 'developer', '2017-06-23');
INSERT INTO `auth_resource` VALUES ('9', '1', '3', '删除', null, null, 'remove', '/systemManager/remove.json', 'icon-remove', '0', null, 'developer', '2017-06-23', 'developer', '2017-06-23');
INSERT INTO `auth_resource` VALUES ('10', '0', '1', '系统相关', 'system', null, null, null, null, '0', null, null, null, null, null);
INSERT INTO `auth_resource` VALUES ('11', '0', '1', '文件相关', 'file', null, null, null, null, '0', null, null, null, null, null);
INSERT INTO `auth_resource` VALUES ('12', '11', '2', '文件管理', 'file_manager', '1', '/file/fileManager.html', '/file/fileManager.html', null, '0', null, null, null, null, null);
INSERT INTO `auth_resource` VALUES ('13', '12', '3', '删除', null, '4', 'remove', '/file/fileManager/remove.json', 'icon-remove', '0', null, 'developer', '2017-06-26', 'developer', '2017-06-26');
INSERT INTO `auth_resource` VALUES ('14', '12', '3', '保存', null, '3', 'save', '/file/fileManager/save.json', 'icon-ok', '0', null, 'developer', '2017-06-26', 'developer', '2017-06-26');
INSERT INTO `auth_resource` VALUES ('15', '12', '3', '编辑', null, '2', 'edit', '/fileManager/load.json', 'icon-edit', '0', null, 'developer', '2017-06-26', 'developer', '2017-06-26');
INSERT INTO `auth_resource` VALUES ('18', '12', '3', '上传', null, '5', 'upload', '/fileManager/load.json', 'icon-add', '0', null, 'developer', '2017-06-26', 'developer', '2017-06-26');
INSERT INTO `auth_resource` VALUES ('16', '11', '2', '文件删除日志', 'file_delete_log', '1', '/file/fileDeleteLog.html', '/file/fileManager.html', null, '0', null, null, null, null, null);
INSERT INTO `auth_resource` VALUES ('17', '16', '3', '删除', null, '4', 'remove', '/file/fileDeleteLog/remove.json', 'icon-remove', '0', null, 'developer', '2017-08-28', 'developer', '2017-08-28');
INSERT INTO `auth_user` VALUES ('1', '00002732', '', '叶青云', '1', '1', null, null, null, null, '0', null, null, null, null, null);
INSERT INTO `auth_user` VALUES ('2', '00001415', '', '雷鸣', '1', '1', null, null, null, null, '0', null, null, null, null, null);
INSERT INTO `auth_user_group` VALUES ('1', '1', '0', null, null, null, null, null);
INSERT INTO `auth_user_group` VALUES ('2', '1', '0', null, null, null, null, null);

