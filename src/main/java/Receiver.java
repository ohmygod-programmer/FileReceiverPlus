import SpecialMessages.FinalMessage;
import SpecialMessages.GreetingMessage;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;


public class Receiver implements Runnable{
    private static int STATISTICS_PRINT_PERIOD = 3000; // in milliseconds
    public static final int SOCKET_TIMEOUT = 10000; // in milliseconds
    private static int SIZE_OF_READ_BUFFER = 256;
    Socket socket;
    String path;
    Receiver(Socket s, String writing_directory){
        socket = s;
        path = writing_directory;
    }

    private void closeSocket(){
        try {
            socket.close();
        }
        catch (Exception e1){}
    }

    public void run(){
        try {
            socket.setSoTimeout(SOCKET_TIMEOUT);
        }
        catch (Exception e){
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
        }
        catch (ClassCastException e){
            System.out.println(sessionName + ": Wrong greeting message from client. Receive is cancelling...");
            closeSocket();
            return;
        }
        catch (Exception e){
            System.out.println(sessionName + ": " + e);
            closeSocket();
            return;
        }

        String filename = greetingMessage.getFileName();
        long filesize = greetingMessage.getFileSize();
        path = path + "/" + sessionName + "/" + filename;
        File newFile = new File(path);
        newFile.getParentFile().mkdirs();
        BufferedOutputStream fileStream;
        try {
            newFile.createNewFile();
            fileStream = new BufferedOutputStream(new FileOutputStream(newFile));
        }
        catch (Exception e){
            System.out.println(sessionName + ": Error with creating new file");
            closeSocket();
            return;
        };
        System.out.println(sessionName + ": File " + newFile.getAbsolutePath() + " is created");
        long accbytes_number = 0;
        byte[] buf = new byte[SIZE_OF_READ_BUFFER];

        double avSpeed = 0;
        double curspeed = 0;
        boolean speedisprinted = false;
        try{
            int n;
            long starttime = System.currentTimeMillis();
            long lasttime = starttime;
            long lastprinttime = starttime;
            long curtime = starttime;
            while (accbytes_number < filesize){
                if ((n = in.read(buf, 0, SIZE_OF_READ_BUFFER)) == -1){
                    break;
                }
                long curbytes_accepted = 0;
                if (filesize - accbytes_number >= SIZE_OF_READ_BUFFER){
                    fileStream.write(buf, 0, n);
                    accbytes_number += SIZE_OF_READ_BUFFER;
                    curbytes_accepted = SIZE_OF_READ_BUFFER;
                }
                else {
                    fileStream.write(buf, 0, (int)(filesize - accbytes_number));
                    accbytes_number += filesize - accbytes_number;
                    curbytes_accepted = filesize - accbytes_number;
                }
                curtime = System.currentTimeMillis();

                if (curtime - lastprinttime >= STATISTICS_PRINT_PERIOD){
                    if(curtime - starttime == 0){
                        curtime += 1;
                    }
                    curspeed = curbytes_accepted/((double)(curtime-lasttime)/1000);
                    avSpeed = accbytes_number/((double)(curtime-starttime)/1000);
                    System.out.println(sessionName + ": Working for " + (curtime-starttime)/1000 + " seconds." +
                            " Current speed is " + curspeed + " bytes/s." +
                            " Average speed is " + avSpeed + " bytes/s.");
                    speedisprinted = true;
                    lastprinttime = curtime;
                }
                lasttime = curtime;


            }
            if (!speedisprinted){
                if(curtime - starttime == 0){
                    curtime += 1;
                }
                avSpeed = accbytes_number/((double)(curtime-starttime)/1000);
                System.out.println(sessionName + ": Working for " + (curtime-starttime)/1000 + " seconds." +
                        " Average speed is " + avSpeed + " bytes/s.");
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
        finally {
            try {
                fileStream.close();
            }
            catch (Exception e){}

        }

        FinalMessage finalMessage;
        if (filesize-accbytes_number == 0){
            System.out.println(sessionName + ": File " + filename + " is successfully accepted");
            finalMessage = new FinalMessage(FinalMessage.ACCEPTED, filesize, accbytes_number, null);
        }
        else {
            System.out.println(sessionName + ": File " + filename + " isn't accepted. " +
                    "Accepted " + accbytes_number + " out of " + filesize + " bytes.");
            finalMessage = new FinalMessage(FinalMessage.NOTACCEPTED, filesize, accbytes_number, null);
        }
        try {
            new ObjectOutputStream(socket.getOutputStream()).writeObject(finalMessage);
        }
        catch (Exception e){
            System.out.println("WTF");
        }

        closeSocket();
        return;


    }

}
