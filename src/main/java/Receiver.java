import specialmessages.FinalMessage;
import specialmessages.GreetingMessage;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;


public class Receiver implements Runnable {
    private static final int STATISTICS_PRINT_PERIOD = 3000; // in milliseconds
    private static final int SOCKET_TIMEOUT = 10000; // in milliseconds
    private static final int SIZE_OF_READ_BUFFER = 256;
    private static final int MAXIMUM_FILE_NUM = 100;
    private Socket socket;
    private String path;

    Receiver(Socket s, String writing_directory) {
        socket = s;
        path = writing_directory;
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (Exception e1) {
        }
    }

    private File createFile(String path) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        int number = 0;
        while (file.exists() && number < MAXIMUM_FILE_NUM) {
            number++;
            file = new File(file.getParent() + "/" +
                    FilenameUtils.getBaseName(path) + "(" + number + ")" + "." + FilenameUtils.getExtension(path));
        }
        file.createNewFile();
        return file;
    }

    public void run() {
        try {
            socket.setSoTimeout(SOCKET_TIMEOUT);
        } catch (Exception e) {
            System.out.println(e);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        String sessionName = "session" + System.currentTimeMillis();
        System.out.println(sessionName + ": Connection accepted");

        BufferedInputStream in;
        GreetingMessage greetingMessage;


        try {
            greetingMessage = (GreetingMessage) new ObjectInputStream(socket.getInputStream()).readObject();
            in = new BufferedInputStream(socket.getInputStream());
        } catch (ClassCastException e) {
            System.out.println(sessionName + ": Wrong greeting message from client. Receive is cancelling...");
            closeSocket();
            return;
        } catch (Exception e) {
            System.out.println(sessionName + ": " + e);
            closeSocket();
            return;
        }

        String fileName = greetingMessage.getFileName();
        long fileSize = greetingMessage.getFileSize();
        path = path + "/" + sessionName + "/" + fileName;
        File newFile = new File(path);

        BufferedOutputStream fileStream;
        try {
            newFile = createFile(path);
            fileStream = new BufferedOutputStream(new FileOutputStream(newFile));
        } catch (Exception e) {
            System.out.println(sessionName + ": Error with creating new file");
            closeSocket();
            return;
        }

        System.out.println(sessionName + ": File " + newFile.getAbsolutePath() + " is created");
        long accBytesNumber = 0;
        byte[] buf = new byte[SIZE_OF_READ_BUFFER];

        double avSpeed = 0;
        double curSpeed = 0;
        boolean speedIsPrinted = false;
        try {
            int n;
            long startTime = System.currentTimeMillis();
            long lastTime = startTime;
            long lastPrintTime = startTime;
            long curTime = startTime;
            while (accBytesNumber < fileSize) {
                if ((n = in.read(buf, 0, SIZE_OF_READ_BUFFER)) == -1) {
                    break;
                }
                long curBytesAccepted = 0;
                if (fileSize - accBytesNumber >= SIZE_OF_READ_BUFFER) {
                    fileStream.write(buf, 0, n);
                    accBytesNumber += SIZE_OF_READ_BUFFER;
                    curBytesAccepted = SIZE_OF_READ_BUFFER;
                } else {
                    fileStream.write(buf, 0, (int) (fileSize - accBytesNumber));
                    accBytesNumber += fileSize - accBytesNumber;
                    curBytesAccepted = fileSize - accBytesNumber;
                }
                curTime = System.currentTimeMillis();

                if (curTime - lastPrintTime >= STATISTICS_PRINT_PERIOD) {
                    if (curTime - startTime == 0) {
                        curTime += 1;
                    }
                    curSpeed = curBytesAccepted / ((double) (curTime - lastTime) / 1000);
                    avSpeed = accBytesNumber / ((double) (curTime - startTime) / 1000);
                    System.out.println(sessionName + ": Working for " + (curTime - startTime) / 1000 + " seconds." +
                            " Current speed is " + curSpeed + " bytes/s." +
                            " Average speed is " + avSpeed + " bytes/s.");
                    speedIsPrinted = true;
                    lastPrintTime = curTime;
                }
                lastTime = curTime;


            }
            if (!speedIsPrinted) {
                if (curTime - startTime == 0) {
                    curTime += 1;
                }
                avSpeed = accBytesNumber / ((double) (curTime - startTime) / 1000);
                System.out.println(sessionName + ": Working for " + (curTime - startTime) / 1000 + " seconds." +
                        " Average speed is " + avSpeed + " bytes/s.");
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            try {
                fileStream.close();
            } catch (Exception e) {
            }

        }

        FinalMessage finalMessage;
        if (fileSize - accBytesNumber == 0) {
            System.out.println(sessionName + ": File " + fileName + " is successfully accepted");
            finalMessage = new FinalMessage(FinalMessage.ACCEPTED, fileSize, accBytesNumber, null);
        } else {
            System.out.println(sessionName + ": File " + fileName + " isn't accepted. " +
                    "Accepted " + accBytesNumber + " out of " + fileSize + " bytes.");
            finalMessage = new FinalMessage(FinalMessage.NOTACCEPTED, fileSize, accBytesNumber, null);
        }
        try {
            new ObjectOutputStream(socket.getOutputStream()).writeObject(finalMessage);
        } catch (Exception e) {
            System.out.println("WTF");
        }

        closeSocket();
        return;


    }

}
