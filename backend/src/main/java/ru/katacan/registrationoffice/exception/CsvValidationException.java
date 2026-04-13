package ru.katacan.registrationoffice.exception;

public class CsvValidationException extends RuntimeException {
    private final int rowNumber;
    private final String rowData;

    public CsvValidationException(String message, int rowNumber, String rowData) {
        super(message);
        this.rowNumber = rowNumber;
        this.rowData = rowData;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public String getRowData() {
        return rowData;
    }
}
