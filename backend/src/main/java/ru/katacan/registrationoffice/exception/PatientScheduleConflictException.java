package ru.katacan.registrationoffice.exception;

/**
 * У пациента уже есть активная запись на выбранное время.
 */
public class PatientScheduleConflictException extends RuntimeException {

    public PatientScheduleConflictException(String message) {
        super(message);
    }
}
