import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newCachedThreadPool;

public class Server {
    private static final int TERMINATION_TIMEOUT = 1000; //in seconds
    private static final String MESSAGES_BEGIN = "Server: ";
    private static final String WRITING_DIRECTORY = "./uploads/";
    public static final int MAXQUEUESIZE = 100;

    public static void main(String[] args) {

        int port;
        if (args.length < 1) {
            System.out.println("Too few arguments. please give port.");
            return;
        }

        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Problem with 1st arg, please give port");
            return;
        }

        InetAddress serverAddress = null;

        if (args.length > 1) {
            try {
                serverAddress = InetAddress.getByName(args[1]);
                if (NetworkInterface.getByInetAddress(serverAddress) == null) {
                    System.out.println("You can't bind server on this address.");
                    return;
                }
            } catch (Exception e) {
                System.out.println(e);
                System.out.println("Problem with 2nd arg, please give correct address");
                return;
            }
        }


        try (ServerSocket serverSocket = new ServerSocket(port, MAXQUEUESIZE, serverAddress)) {
            Socket connection;
            System.out.println("Server is started on " + serverSocket.getInetAddress());

            ExecutorService executorService = newCachedThreadPool();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Performing some shutdown cleanup...");
                executorService.shutdown();
                try {
                    System.out.println("Waiting for file receiving complete to terminate...");
                    if (!executorService.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.SECONDS)) {
                        System.out.println(MESSAGES_BEGIN + "timeout elapsed.");
                    }
                } catch (InterruptedException e) {
                }
                System.out.println("Done cleaning");
            }));


            try {
                while (true) {
                    connection = serverSocket.accept();
                    Future futureTask = executorService.submit(new Receiver(connection, WRITING_DIRECTORY));
                }
            } catch (Exception e) {
                System.out.println(MESSAGES_BEGIN + e);
                System.out.println("Waiting for file receiving complete to terminate...");
            }


            try {
                executorService.shutdown();
                if (!executorService.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.SECONDS)) {
                    System.out.println(MESSAGES_BEGIN + "timeout elapsed.");
                }
            } catch (Exception e) {
                System.out.println(MESSAGES_BEGIN + e);
                return;
            }

        } catch (Exception e) {
            System.out.println(e);
            return;
        }


    }
}
