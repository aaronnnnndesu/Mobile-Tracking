import java.io.*;
import java.net.*;
import java.util.Scanner;

public class GETClient {

    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException{
        try{
            System.out.println("Enter the server name and port number (in URL format): ");
            Scanner scanner = new Scanner(System. in);
            String inputString = scanner. nextLine();
            String hostname = inputString.substring(7,inputString.length()-5);
            int port = Integer.parseInt(inputString.substring(inputString.length()-4));

            //establish socket connection to server
            Socket socket = new Socket(hostname, port);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            //send a GET request to the aggregation server
            System.out.println("Sending GET request to Aggregation Server? ");

            String send = scanner.nextLine();
            dos.writeUTF(send); 

            String received = dis.readUTF(); 
            System.out.println(received); 

            socket.close();
            scanner.close(); 
            dis.close(); 
            dos.close(); 
        }catch(Exception e){ 
            e.printStackTrace(); 
        }
    }
}
