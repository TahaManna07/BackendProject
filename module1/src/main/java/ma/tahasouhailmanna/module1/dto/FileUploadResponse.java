package ma.tahasouhailmanna.module1.dto;

public class FileUploadResponse {
    private String originalFilename;
    private String storedFilename;
    private long size;
    private String message;

    public FileUploadResponse() {}
    public FileUploadResponse(String originalFilename, String storedFilename, long size, String message) {
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.size = size;
        this.message = message;
    }

    // getters/setters
    // ...existing code...
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getStoredFilename() { return storedFilename; }
    public void setStoredFilename(String storedFilename) { this.storedFilename = storedFilename; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}