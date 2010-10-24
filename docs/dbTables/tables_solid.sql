DROP TABLE qrtz2_locks;
DROP TABLE qrtz2_scheduler_state;
DROP TABLE qrtz2_fired_triggers;
DROP TABLE qrtz2_paused_trigger_grps;
DROP TABLE qrtz2_calendars;
DROP TABLE qrtz2_blob_triggers;
DROP TABLE qrtz2_cron_triggers;
DROP TABLE qrtz2_simple_triggers;
DROP TABLE qrtz2_simprop_triggers;
DROP TABLE qrtz2_triggers;
DROP TABLE qrtz2_job_details;

create table qrtz2_job_details (
	job_name varchar(80) not null,
	job_group varchar(80) not null,
	description varchar(120) ,
	job_class_name varchar(128) not null,
	is_durable varchar(5) not null,
    is_nonconcurrent varchar(5) not null,
    is_update_data varchar(5) not null,
	requests_recovery varchar(5) not null,
	job_data long varbinary,
primary key (job_name,job_group)
);

create table qrtz2_triggers(
	trigger_name varchar(80) not null,
	trigger_group varchar(80) not null,
	job_name varchar(80) not null,
	job_group varchar(80) not null,
	description varchar(120) ,
	next_fire_time numeric(13),
	prev_fire_time numeric(13),
	priority integer,
	trigger_state varchar(16) not null,
	trigger_type varchar(8) not null,
	start_time numeric(13) not null,
	end_time numeric(13),
	calendar_name varchar(80),
	misfire_instr smallint,
	job_data long varbinary,
primary key (trigger_name,trigger_group),
foreign key (job_name,job_group) references qrtz2_job_details(job_name,job_group)
);

create table qrtz2_simple_triggers(
	trigger_name varchar(80) not null,
	trigger_group varchar(80) not null,
	repeat_count numeric(13) not null,
	repeat_interval numeric(13) not null,
	times_triggered numeric(13) not null,
primary key (trigger_name,trigger_group),
foreign key (trigger_name,trigger_group) references qrtz2_triggers(trigger_name,trigger_group)
);

CREATE TABLE qrtz2_simprop_triggers
  (          
    trigger_name VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    STR_PROP_1 VARCHAR(512) NULL,
    STR_PROP_2 VARCHAR(512) NULL,
    STR_PROP_3 VARCHAR(512) NULL,
    INT_PROP_1 INTEGER NULL,
    INT_PROP_2 INTEGER NULL,
    LONG_PROP_1 NUMERIC(13) NULL,
    LONG_PROP_2 NUMERIC(13) NULL,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 VARCHAR(5) NULL,
    BOOL_PROP_2 VARCHAR(5) NULL,
PRIMARY KEY (trigger_name,trigger_group),
FOREIGN KEY (trigger_name,trigger_group) REFERENCES qrtz2_triggers(trigger_name,trigger_group)
);


create table qrtz2_cron_triggers(
	trigger_name varchar(80) not null,
	trigger_group varchar(80) not null,
	cron_expression varchar(120) not null,
	time_zone_id varchar(80),
primary key (trigger_name,trigger_group),
foreign key (trigger_name,trigger_group) references qrtz2_triggers(trigger_name,trigger_group)
);

create table qrtz2_blob_triggers(
	trigger_name varchar(80) not null,
	trigger_group varchar(80) not null,
	blob_data long varbinary ,
primary key (trigger_name,trigger_group),
foreign key (trigger_name,trigger_group) references qrtz2_triggers(trigger_name,trigger_group)
);

create table qrtz2_calendars(
	calendar_name varchar(80) not null,
	calendar long varbinary not null,
primary key (calendar_name)
); 

create table qrtz2_paused_trigger_grps
  (
    trigger_group  varchar(80) not null, 
primary key (trigger_group)
);

create table qrtz2_fired_triggers(
	entry_id varchar(95) not null,
	trigger_name varchar(80) not null,
	trigger_group varchar(80) not null,
	instance_name varchar(80) not null,
	fired_time numeric(13) not null,
	priority integer not null,
	state varchar(16) not null,
	job_name varchar(80) null,
	job_group varchar(80) null,
	is_nonconcurrent varchar(5) null,
	requests_recovery varchar(5) null,
primary key (entry_id)
);

create table qrtz2_scheduler_state 
  (
    instance_name varchar(80) not null,
    last_checkin_time numeric(13) not null,
    checkin_interval numeric(13) not null,
primary key (instance_name)
);

create table qrtz2_locks
  (
    lock_name  varchar(40) not null, 
primary key (lock_name)
);

insert into qrtz2_locks values('TRIGGER_ACCESS');
insert into qrtz2_locks values('JOB_ACCESS');
insert into qrtz2_locks values('CALENDAR_ACCESS');
insert into qrtz2_locks values('STATE_ACCESS');
insert into qrtz2_locks values('MISFIRE_ACCESS');

commit work;
