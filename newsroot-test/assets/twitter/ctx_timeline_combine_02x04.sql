DELETE FROM sqlite_sequence;
DELETE FROM timegap where TMG_TWT_OLDEST_ID = 349497246842241000;
DELETE FROM timegap where TMG_TWT_EARLIEST_ID = 349531321246294000;
INSERT INTO timegap('TMG_TWT_EARLIEST_ID', 'TMG_ID', 'TMG_TWT_OLDEST_ID') VALUES(349531321246294000,3,349497246842241000);
INSERT INTO "sqlite_sequence" VALUES('timegap',3);
