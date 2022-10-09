create sequence if not exists app_user_id_seq;

create table if not exists app_user (
    id bigint not null,
    email varchar(100) not null,
    password varchar(200) not null,
    role varchar(100) not null,
    enabled boolean,
    locked boolean,
    expired boolean,
    constraint pk$app_user primary key (id)
);

create sequence if not exists registration_token_id_seq;

create table if not exists registration_token (
    id bigint not null,
    token text not null,
    creation_date datetime not null,
    expiration_date datetime not null,
    app_user_id bigint not null,
    constraint pk$registration_token primary key (id),
    constraint fk$registration_token$app_user foreign key (app_user_id) references app_user(id)
);

create table if not exists qrtz_blob_triggers (
    sched_name varchar(120) not null,
    trigger_name varchar (200)  not null ,
    trigger_group varchar (200)  not null ,
    blob_data image null
);

create table if not exists qrtz_calendars (
    sched_name varchar(120) not null,
    calendar_name varchar (200)  not null ,
    calendar image not null,
    constraint pk_qrtz_calendars primary key (sched_name, calendar_name)
);

create table if not exists qrtz_fired_triggers (
    sched_name varchar(120) not null,
    entry_id varchar (95)  not null ,
    trigger_name varchar (200)  not null ,
    trigger_group varchar (200)  not null ,
    instance_name varchar (200)  not null ,
    fired_time bigint not null ,
    sched_time bigint not null ,
    priority integer not null ,
    state varchar (16)  not null,
    job_name varchar (200)  null ,
    job_group varchar (200)  null ,
    is_nonconcurrent boolean  null ,
    requests_recovery boolean  null,
    constraint pk_qrtz_fired_triggers primary key (sched_name, entry_id)
);

create table if not exists qrtz_paused_trigger_grps (
    sched_name varchar(120) not null,
    trigger_group varchar (200)  not null,
    constraint pk_qrtz_paused_trigger_grps primary key (sched_name, trigger_group)
);

create table if not exists qrtz_scheduler_state (
    sched_name varchar(120) not null,
    instance_name varchar (200)  not null ,
    last_checkin_time bigint not null ,
    checkin_interval bigint not null,
    constraint pk_qrtz_scheduler_state primary key (sched_name, instance_name)
);

create table if not exists qrtz_locks (
    sched_name varchar(120) not null,
    lock_name varchar (40)  not null,
    constraint pk_qrtz_locks primary key (sched_name, lock_name)
);

create table if not exists qrtz_job_details (
    sched_name varchar(120) not null,
    job_name varchar (200)  not null ,
    job_group varchar (200)  not null ,
    description varchar (250) null ,
    job_class_name varchar (250)  not null ,
    is_durable boolean  not null ,
    is_nonconcurrent boolean  not null ,
    is_update_data boolean  not null ,
    requests_recovery boolean  not null ,
    job_data image null,
    constraint pk_qrtz_job_details primary key (sched_name, job_name, job_group)
);

create table if not exists qrtz_triggers (
    sched_name varchar(120) not null,
    trigger_name varchar (200)  not null ,
    trigger_group varchar (200)  not null ,
    job_name varchar (200)  not null ,
    job_group varchar (200)  not null ,
    description varchar (250) null ,
    next_fire_time bigint null ,
    prev_fire_time bigint null ,
    priority integer null ,
    trigger_state varchar (16)  not null ,
    trigger_type varchar (8)  not null ,
    start_time bigint not null ,
    end_time bigint null ,
    calendar_name varchar (200)  null ,
    misfire_instr smallint null ,
    job_data image null,
    constraint pk_qrtz_triggers primary key (sched_name, trigger_name, trigger_group),
    constraint fk_qrtz_triggers_qrtz_job_details foreign key (sched_name, job_name, job_group) references qrtz_job_details (sched_name, job_name, job_group)
);

create table if not exists qrtz_cron_triggers (
    sched_name varchar(120) not null,
    trigger_name varchar (200)  not null ,
    trigger_group varchar (200)  not null ,
    cron_expression varchar (120)  not null ,
    time_zone_id varchar (80),
    constraint pk_qrtz_cron_triggers primary key (sched_name, trigger_name, trigger_group),
    constraint fk_qrtz_cron_triggers_qrtz_triggers foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers (sched_name, trigger_name, trigger_group) on delete cascade
);

create table if not exists qrtz_simple_triggers (
    sched_name varchar(120) not null,
    trigger_name varchar (200)  not null ,
    trigger_group varchar (200)  not null ,
    repeat_count bigint not null ,
    repeat_interval bigint not null ,
    times_triggered bigint not null,
    constraint pk_qrtz_simple_triggers primary key (sched_name, trigger_name, trigger_group),
    constraint fk_qrtz_simple_triggers_qrtz_triggers foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers (sched_name, trigger_name, trigger_group) on delete cascade
);

create table if not exists qrtz_simprop_triggers (
    sched_name varchar(120) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    str_prop_1 varchar(512) null,
    str_prop_2 varchar(512) null,
    str_prop_3 varchar(512) null,
    int_prop_1 integer null,
    int_prop_2 integer null,
    long_prop_1 bigint null,
    long_prop_2 bigint null,
    dec_prop_1 numeric(13,4) null,
    dec_prop_2 numeric(13,4) null,
    bool_prop_1 boolean null,
    bool_prop_2 boolean null,
    constraint pk_qrtz_simprop_triggers primary key (sched_name, trigger_name, trigger_group),
    constraint fk_qrtz_simprop_triggers_qrtz_triggers foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers (sched_name, trigger_name, trigger_group) on delete cascade
);
