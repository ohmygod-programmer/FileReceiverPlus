package SpecialMessages;

import java.io.Serializable;

/**Sends to client after file accepting*/
public class FinalMessage implements Serializable {

    private int status;
    private long fSize;
    private long bytesaccepted;
    private String message = null;

    public static final int ACCEPTED = 0;
    public static final int NOTACCEPTED = 0;

    public FinalMessage(int status, long fSize, long bytesaccepted, String message) {
        this.status = status;
        this.fSize = fSize;
        this.bytesaccepted = bytesaccepted;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public long getfSize() {
        return fSize;
    }

    public long getBytesaccepted() {
        return bytesaccepted;
    }
}