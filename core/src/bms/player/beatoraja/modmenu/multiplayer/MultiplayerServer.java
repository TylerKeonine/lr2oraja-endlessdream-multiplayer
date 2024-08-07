package bms.player.beatoraja.modmenu.multiplayer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiplayerServer {
    
    private static ServerSocket serverSocket;

    public MultiplayerServer(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void startServer(){
        try{
            while(!serverSocket.isClosed()){      
                Socket socket = serverSocket.accept();
                MultiplayerMenu.statusText = ("A new client has connected!"+socket.toString());
                MultiplayerClientHandler clientHandler = new MultiplayerClientHandler(socket);
                
                Thread thread = new Thread(clientHandler);
                thread.start();
            }

        }catch(IOException e){
            closeServerSocket();
        }

    }

    public static void closeServerSocket(){
        try{
            if(serverSocket!=null){
                serverSocket.close();
                serverSocket = null; // TODO server stays running despite host leaving? unable to join though
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void hostLobby(){
        try {
            ServerSocket serverSocket;
            serverSocket = new ServerSocket(5730,0,InetAddress.getLocalHost()); // manual ip input for host later
            MultiplayerServer server = new MultiplayerServer(serverSocket);
            new Thread(() -> server.startServer()).start();
            Multiplayer.hostIp = serverSocket.getInetAddress().getHostAddress();
        } catch (IOException e) {
            closeServerSocket();
        }
    }
}
