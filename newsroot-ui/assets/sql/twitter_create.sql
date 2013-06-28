create table TWT_TWEET (
    TWT_ID bigint not null,
    TWT_CREATED_AT varchar,
    TWT_TEXT varchar,
    TWT_NAME varchar,
    TWT_SCREEN_NAME varchar,
    
    primary key (TWT_ID)
);

create table TML_TIMELINE (
    TML_ID BIGINT not null,
    
    primary key (TML_ID)
);

create table TMG_TIMEGAP (
    TMG_ID integer primary key autoincrement,
    TMG_TWT_EARLIEST_ID bigint,
    TMG_TWT_OLDEST_ID bigint
);

create view if not exists VIEW_TIMELINE as
    select TWT_ID, TWT_CREATED_AT, TWT_TEXT, TWT_NAME, TWT_SCREEN_NAME, NULL, NULL, NULL
    from TWT_TWEET
union all
    select TMG_TWT_OLDEST_ID, NULL, NULL, NULL, NULL, TMG_ID, TMG_TWT_EARLIEST_ID, TMG_TWT_OLDEST_ID
    from TMG_TIMEGAP
order by TWT_ID DESC;
