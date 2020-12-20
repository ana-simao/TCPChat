import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ChatClient implements Runnable{

    private String hostName;
    private int port;

    private BufferedReader input;
    private Socket clientSocket;

    public void receiveInput(){
        input = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.print("Hostname: ");
            hostName = input.readLine();

            System.out.print("Port: ");
            port = Integer.parseInt(input.readLine());


            System.out.println("Trying to establish a connection, please wait...");
            clientSocket = new Socket(hostName, port);
            System.out.println("Connected to " + clientSocket.toString());



        } catch (IOException e) {
            e.getMessage();
        }
    }
    public void sendMessages(){
        try {

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String messageSent;
            while (clientSocket.isBound()) {
                messageSent = input.readLine();
                out.println(messageSent);
                while ((messageSent = input.readLine()) != null){
                    if(messageSent.equals("/quit")){
                        break;
                    }
                }
                out.close();
                input.close();
                clientSocket.close();
                System.exit(1);

            }

        } catch (UnknownHostException e) {
            e.getMessage();
        } catch (IOException e) {
            e.getMessage();
        }
    }


    public void receiveMessage(){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String message = in.readLine();

            System.out.println(message);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        while (clientSocket.isBound()){
            receiveMessage();
        }

    }


//================================main============================================


    public static void main(String[] args) {

        ChatClient chatClient = new ChatClient();
        chatClient.receiveInput();
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(chatClient);
        chatClient.sendMessages();

    }


}
