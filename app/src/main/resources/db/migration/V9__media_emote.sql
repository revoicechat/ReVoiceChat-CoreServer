UPDATE RVC_MEDIA_DATA
set origin = 'EMOTE'
where id in (select MEDIA_ID from RVC_EMOTE);