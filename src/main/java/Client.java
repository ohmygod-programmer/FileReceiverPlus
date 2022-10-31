import specialmessages.FinalMessage;
import specialmessages.GreetingMessage;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private static int SIZE_OF_READ_BUFFER = 256; //in bytes;

    public static void main(String[] args) {
        int port;
        String filename;

        if (args.length < 3) {
            System.out.println("Too few arguments. please give ip, port and filename.");
            return;
        }

        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(args[0]);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Problem with 1st arg, please give ip address tot connect");
            return;
        }
        try {
            port = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Problem with 2nd arg, please give port");
            return;
        }
        filename = args[2];

        File file = new File(filename);
        long filelen = file.length();

        try (BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
             Socket socket = new Socket(inetAddress, port);
             BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {

            GreetingMessage greetingMessage = new GreetingMessage(filename, filelen);
            new ObjectOutputStream(socket.getOutputStream()).writeObject(greetingMessage);
            byte[] buf = new byte[SIZE_OF_READ_BUFFER];
            while (fileInputStream.read(buf, 0, SIZE_OF_READ_BUFFER) != -1) {
                out.write(buf);
            }
            out.flush();
            try {
                FinalMessage finalMessage = (FinalMessage) new ObjectInputStream(socket.getInputStream()).readObject();
                if (finalMessage.getStatus() == FinalMessage.ACCEPTED && finalMessage.getBytesaccepted() == filelen) {
                    System.out.println("File is successfully sended and received");
                } else {
                    System.out.println("Error with accepting file by server");
                    if (finalMessage.getMessage() != null) {
                        System.out.println("Message from server: " + finalMessage.getMessage());
                    }
                }
            } catch (Exception e) {
                System.out.println("Error with status message from server. Status in unknown");
            }
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Error. Shutting down...");
            return;
        }





    }
}
