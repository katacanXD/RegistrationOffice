CREATE INDEX IF NOT EXISTS idx_t_appointments_doctor_slot
    ON t_appointments (f_doctor_id, f_slot_datetime)//

CREATE OR REPLACE FUNCTION enforce_active_appointment_uniqueness()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    new_status_name TEXT;
    lock_key BIGINT;
BEGIN
    IF NEW.f_doctor_id IS NULL OR NEW.f_slot_datetime IS NULL OR NEW.f_status_id IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT s.f_status_name
      INTO new_status_name
      FROM t_dict_status s
     WHERE s.f_status_id = NEW.f_status_id;

    IF new_status_name IS NULL THEN
        RETURN NEW;
    END IF;

    IF new_status_name = 'canceled' THEN
        RETURN NEW;
    END IF;

    -- Serialize writes for the same doctor+slot to prevent race conditions.
    lock_key := (NEW.f_doctor_id << 32) # (EXTRACT(EPOCH FROM NEW.f_slot_datetime)::BIGINT & 4294967295);
    PERFORM pg_advisory_xact_lock(lock_key);

    IF EXISTS (
        SELECT 1
          FROM t_appointments a
          JOIN t_dict_status s ON s.f_status_id = a.f_status_id
         WHERE a.f_doctor_id = NEW.f_doctor_id
           AND a.f_slot_datetime = NEW.f_slot_datetime
           AND s.f_status_name <> 'canceled'
           AND (TG_OP = 'INSERT' OR a.f_appoint_id <> NEW.f_appoint_id)
    ) THEN
        RAISE EXCEPTION USING
            ERRCODE = '23505',
            MESSAGE = 'active appointment for this doctor slot';
    END IF;

    RETURN NEW;
END;
$$;
//

DROP TRIGGER IF EXISTS trg_enforce_active_appointment_uniqueness ON t_appointments//

CREATE TRIGGER trg_enforce_active_appointment_uniqueness
BEFORE INSERT OR UPDATE OF f_doctor_id, f_slot_datetime, f_status_id
ON t_appointments
FOR EACH ROW
EXECUTE FUNCTION enforce_active_appointment_uniqueness()//

