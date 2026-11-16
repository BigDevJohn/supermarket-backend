package jv.supermarket.image;

public class ImageDTO {

    private String fileName;
    private String fileType;
    private String downloadUrl;

    public ImageDTO() {
    }

    public ImageDTO(String fileName, String fileType, String downloadUrl) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.downloadUrl = downloadUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

}
