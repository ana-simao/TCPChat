import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private int port;
    ServerSocket serverSocket;
    List <ServerWorker> synchonizedList;



    public void receivePort(){
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Port: ");
        try {
            port = Integer.parseInt(input.readLine());
            input.close();
        } catch (IOException e) {
            e.getMessage();
        }

    }

    public void startServer(){
        try {
            System.out.println("Binding to port " + port);
            serverSocket = new ServerSocket(port);
            System.out.println("Server started: " + serverSocket.toString());

        } catch (IOException e) {

            e.getMessage();
        }

    }


    public void acceptConnection(){
        LinkedList <ServerWorker> list = new LinkedList<>();

        synchonizedList = Collections.synchronizedList(list);
        ExecutorService service = Executors.newFixedThreadPool(4);

        while (true) {
            try {
                System.out.println("Waiting for a client connection");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection accepted!");
                ServerWorker serverWorker= new ServerWorker(clientSocket);
                synchonizedList.add(serverWorker);
                service.execute(serverWorker);
            } catch (IOException e) {
                e.getMessage();
            }

        }

    }

    public synchronized void sendToAll(String message, ServerWorker serverWorker){
        synchronized (synchonizedList) {
            for (ServerWorker each : synchonizedList) {
                if (each != serverWorker) {
                    each.sendMessage(message);
                }
            }
        }
    }

    public void init(){
        receivePort();
        startServer();
        acceptConnection();
    }

// ==============================ServerWorker========================================================

    private class ServerWorker implements Runnable{

        private Socket clientSocket;
        private BufferedReader in;
        private BufferedWriter out;
        String message;

        public ServerWorker(Socket clientSocket){
            this.clientSocket = clientSocket;
        }


        public void receiveAndForwardMessage(){

            try {
                while(clientSocket.isBound()) {
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    message = in.readLine();
                    if (message == null || message.equals("/quit")) {
                        System.out.println("client removed");
                        in.close();
                        out.close();

                        clientSocket.close();
                        synchonizedList.remove(this);
                        break;

                    } else {
                        sendToAll(message, this);
                    }
                }

            } catch (IOException e) {
                e.getMessage();
            }

        }

        public void sendMessage(String message){
            try{
                out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                out.write(message + "\n");
                out.flush();
            } catch (IOException e) {
                e.getMessage();
            }

        }

        @Override
        public void run() {
            receiveAndForwardMessage();
        }


    }

//================================main============================================

    public static void main(String[] args) {

        ChatServer chatServer = new ChatServer();

        chatServer.init();


    }
}
