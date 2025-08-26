create table RVC_USER (
    createdDate timestamp(6),
    id          uuid  not null,
    displayName varchar(255) not null,
    email       varchar(255) unique,
    login       varchar(255) not null unique,
    password    varchar(255),
    status      varchar(255) not null,
    type        varchar(255),
    primary key (id)
);

create table RVC_SERVER (
    OWNER_ID uuid,
    id       uuid not null,
    name     varchar(255),
    primary key (id),
    constraint FK_RVC_SERVER_OWNER foreign key (OWNER_ID) references RVC_USER DEFERRABLE
);

create table RVC_SERVER_USER (
    SERVER_ID uuid not null,
    USER_ID   uuid not null,
    primary key (SERVER_ID, USER_ID),
    constraint FK_RVC_SERVER_USER_USER foreign key (USER_ID) references RVC_USER DEFERRABLE,
    constraint FK_RVC_SERVER_USER_SERVER foreign key (SERVER_ID) references RVC_SERVER DEFERRABLE
);

create table RVC_ROOM (
    id        uuid  not null,
    SERVER_ID uuid  not null,
    name      varchar(255) not null,
    type      varchar(255) not null,
    primary key (id),
    constraint FK_RVC_ROOM_SERVER foreign key (SERVER_ID) references RVC_SERVER DEFERRABLE
);

create table RVC_INVATION_LINK (
    APPLIER_ID         uuid,
    SENDER_ID          uuid,
    TARGETED_SERVER_ID uuid,
    id                 uuid not null,
    status             varchar(255),
    type               varchar(255),
    primary key (id),
    constraint FK_RVC_INVATION_LINK_APPLIER foreign key (APPLIER_ID) references RVC_USER DEFERRABLE,
    constraint FK_RVC_INVATION_LINK_SENDER foreign key (SENDER_ID) references RVC_USER DEFERRABLE,
    constraint FK_RVC_INVATION_LINK_TARGETED_SERVER foreign key (TARGETED_SERVER_ID) references RVC_SERVER DEFERRABLE
);

create table RVC_MESSAGE (
    createdDate timestamp(6),
    ROOM_ID     uuid not null,
    USER_ID     uuid not null,
    id          uuid not null,
    text        text,
    primary key (id),
    constraint FK_RVC_MESSAGE_ROOM foreign key (ROOM_ID) references RVC_ROOM DEFERRABLE,
    constraint FK_RVC_MESSAGE_USER foreign key (USER_ID) references RVC_USER DEFERRABLE
);

create table RVC_MEDIA_DATA (
    id     uuid not null,
    name   varchar(255),
    url    varchar(255),
    origin varchar(255),
    status varchar(255),
    type   varchar(255),
    primary key (id)
);

create table RVC_MEASSAGE_MEDIA (
    MEASSAGE_ID uuid not null,
    MEDIA_ID    uuid not null unique,
    primary key (MEASSAGE_ID, MEDIA_ID),
    constraint FK_RVC_MEASSAGE_MEDIA_MEDIA_DATA foreign key (MEDIA_ID) references RVC_MEDIA_DATA DEFERRABLE,
    constraint FK_RVC_MEASSAGE_MEDIA_MESSAGE foreign key (MEASSAGE_ID) references RVC_MESSAGE DEFERRABLE
);
