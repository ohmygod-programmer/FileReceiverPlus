package specialmessages;

import java.io.Serializable;

/**
 * Sends to server before start sending file
 */
public class GreetingMessage implements Serializable {
    private String fName;
    private long fSize;

    public GreetingMessage(String fileName, long fileSize) {
        fName = fileName;
        fSize = fileSize;
    }

    public String getFileName() {
        return fName;
    }

    public long getFileSize() {
        return fSize;
    }
}
