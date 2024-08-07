package bms.player.beatoraja.modmenu.multiplayer;

import java.net.Socket;
import java.util.ArrayList;
import java.io.*;

public class MultiplayerClientHandler implements Runnable{

    // Socket Variables
    public static ArrayList<MultiplayerClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String clientUsername;

    // Lobby Information
    public static ArrayList<String> playerNames = new ArrayList<>();


    public MultiplayerClientHandler(Socket socket){
        try{
            this.socket = socket;
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.clientUsername = dataInputStream.readUTF();
            clientHandlers.add(this);
            broadcastMessage("Server: "+clientUsername+" has entered the chat!");
            playerNames.add(clientUsername);
        }catch(IOException e){
            closeEverything(socket,dataInputStream,dataOutputStream);
        }
    }

    @Override
    public void run(){
        String messageFromClient;

        while(socket.isConnected()){
            try{
                messageFromClient = dataInputStream.readUTF();
                broadcastMessage(messageFromClient);
            }catch(IOException e){
                closeEverything(socket,dataInputStream,dataOutputStream);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.dataOutputStream.writeUTF(messageToSend); //
                clientHandler.dataOutputStream.flush();
                MultiplayerMenu.statusText = String.join(", ", playerNames);
            }catch(IOException e){
                closeEverything(socket,dataInputStream,dataOutputStream);
            }
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("Server: "+clientUsername+" has left the chat!");
        playerNames.remove(clientUsername);
    }

    public void closeEverything(Socket skt,DataInputStream dIn, DataOutputStream dOut){
        removeClientHandler();
        try{
            if(dIn!=null){
                dIn.close();
            }
            if(dOut!=null){
                dOut.close();
            }
            if(skt!=null){
                skt.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}