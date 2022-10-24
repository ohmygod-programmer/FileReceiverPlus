import java.util.Arrays;

public class FileReceiverPlus {
    public static void main(String[] args) {
        if (args.length < 1){
            System.out.println("You have to choose work mode\n" +
                    "There are two modes: server and client\n" +
                    "Server is receiving files\n" +
                    "Client is send file\n");
            return;
        }
        else{
            if (args[0].compareToIgnoreCase("server") == 0){
                Server.main(Arrays.copyOfRange(args, 1, args.length));
            }
            else if (args[0].compareToIgnoreCase("client") == 0){
                Client.main(Arrays.copyOfRange(args, 1, args.length));
            }
            else {
                System.out.println("Incorrect mode\n");
                return;
            }
        }
    }
}
