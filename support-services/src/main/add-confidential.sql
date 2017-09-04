alter table OL_NOTE add confidential bit(1) NOT NULL;

alter table OL_DOCUMENT add favorite bit(1) NOT NULL;
alter table OL_DOCUMENT add confidential bit(1) NOT NULL;