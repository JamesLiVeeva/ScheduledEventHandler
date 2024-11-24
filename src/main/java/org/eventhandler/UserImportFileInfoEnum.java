package org.eventhandler;

public enum UserImportFileInfoEnum {

    WonderDrugs("WonderDrugs", "WonderDrugsHR-", "yyyy-MM-dd", "csv"),
    WonderPharma("WonderPharma", "WonderPharmaHR-", "yyyy-MM-dd", "csv"),
    WonderBio("WonderBio", "HRExported-", "yyyy-MM-dd","txt");

    private final String filePath;
    private final String fileNamePrefix;
    private final String datePattern;
    private final String fileExtension;

    UserImportFileInfoEnum(String filePath, String fileNamePrefix, String datePattern, String fileExtension) {
        this.filePath = filePath;
        this.fileNamePrefix = fileNamePrefix;
        this.datePattern = datePattern;
        this.fileExtension = fileExtension;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public String getFileExtension() {
        return fileExtension;
    }

}
