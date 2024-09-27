package bms.player.beatoraja.modmenu.multiplayer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;

public class MultiplayerServer extends Thread{
    
    protected DatagramSocket socket = null;

    public MultiplayerServer() throws IOException{
        socket = new DatagramSocket(444);
    }

    public void run(){
        while (socket.isClosed()==false){
            try{
                byte[] buf = new byte[256];

                DatagramPacket packet = new DatagramPacket(buf,buf.length);
                socket.receive(packet);

                String msg = "hi from server";

                buf = msg.getBytes();

                InetAddress ipaddr = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf,buf.length,ipaddr,port);

                socket.send(packet);
            }catch(IOException e){
                e.printStackTrace();
                break;
            }
        }
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

    public static void hostLobby() throws IOException{
        // probably dont want to have a main function here. start the server somewhere else
        // should be able to make multiple servers?
        MultiplayerServer server = new MultiplayerServer();
        server.start();
    }
}
