create table RVC_ROOM
(
    id   uuid not null,
    name varchar(255),
    primary key (id)
);

create table RVC_PRIVATE_MESSAGE_ROOM
(
    id uuid not null,
    primary key (id),
    constraint FKPRIVATE_PRIVATE_MESSAGE_ROOM_ROOM foreign key (ID) references RVC_ROOM DEFERRABLE
);

alter table RVC_SERVER_ROOM
    add constraint FKPRIVATE_SERVER_ROOM_ROOM foreign key (ID) references RVC_ROOM DEFERRABLE;

create table RVC_PRIVATE_MESSAGE_ROOM_USERS
(
    ROOM_ID uuid not null,
    USER_ID uuid not null,
    primary key (ROOM_ID, USER_ID),
    constraint FKPRIVATE_MESSAGE_ROOM_USERS_USER foreign key (USER_ID) references RVC_USER DEFERRABLE,
    constraint FKPRIVATE_MESSAGE_ROOM_USERS_ROOM foreign key (ROOM_ID) references RVC_PRIVATE_MESSAGE_ROOM DEFERRABLE
);
