drop table if exists TWT_TWEET;
drop table if exists USR_USER;

create table USR_USER (
  USR_ID bigint not null,
  USR_NAME varchar,
  USR_SCREEN_NAME varchar,

  primary key (USR_ID)
);

create table TWT_TWEET (
  TWT_ID bigint not null,
  TWT_CREATED_AT bigint not null,
  TWT_TEXT varchar,
  TWT_USR_ID bigint not null,
  foreign key(TWT_USR_ID) references USR_USER(USR_ID),

  primary key (TWT_ID)
);
