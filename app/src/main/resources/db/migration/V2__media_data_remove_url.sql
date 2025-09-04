DO
$$
    DECLARE
        rec    RECORD;
        v_text TEXT;
    BEGIN
        -- Loop through all rows where migration is not yet done
        FOR rec IN
            SELECT id, text
            FROM rvc_message
            LOOP
                BEGIN
                    -- Try to fetch from pg_largeobject if OID exists
                    SELECT convert_from(lo_get(rec.text::oid), 'UTF8')
                    INTO v_text
                    WHERE EXISTS (SELECT 1
                                  FROM pg_largeobject_metadata lom
                                  WHERE lom.oid = rec.text::oid);

                    -- If we got something, update
                    IF v_text IS NOT NULL THEN
                        UPDATE rvc_message
                        SET text = v_text
                        WHERE id = rec.id;
                    END IF;

                EXCEPTION
                    WHEN others THEN
                        -- If conversion fails, just skip this row
                        CONTINUE;
                END;
            END LOOP;
    END
$$;