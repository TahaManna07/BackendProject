package ma.tahasouhailmanna.module1.dto;

public class CsvProcessResult {
    private int processedCount;
    private int errorsCount;
    private String message;

    public CsvProcessResult() {}
    public CsvProcessResult(int processedCount, int errorsCount, String message) {
        this.processedCount = processedCount;
        this.errorsCount = errorsCount;
        this.message = message;
    }

    // getters/setters
    public int getProcessedCount() { return processedCount; }
    public void setProcessedCount(int processedCount) { this.processedCount = processedCount; }
    public int getErrorsCount() { return errorsCount; }
    public void setErrorsCount(int errorsCount) { this.errorsCount = errorsCount; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}