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
    public static ArrayList<String> playerStates = new ArrayList<>();


    public MultiplayerClientHandler(Socket socket){
        try{
            this.socket = socket;
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.clientUsername = dataInputStream.readUTF();
            clientHandlers.add(this);
            broadcastMessage("Server: "+clientUsername+" has entered the chat!");
            // every list needs to be added to
            playerNames.add(clientUsername);
            playerStates.add("Not Ready");
            // update new player to current info
            sendPlayerNames();
            sendPlayerStates();
        }catch(IOException e){
            closeEverything(socket,dataInputStream,dataOutputStream);
        }
    }

    @Override
    public void run(){
        String messageFromClient;
        Byte msgType;

        while(socket.isConnected()){
            try{
                msgType = dataInputStream.readByte();
                switch(msgType){
                    case(0):
                    messageFromClient = dataInputStream.readUTF();
                    broadcastMessage(messageFromClient);                   
                    break;
                }
            }catch(IOException e){
                closeEverything(socket,dataInputStream,dataOutputStream);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.dataOutputStream.writeByte(0); // note these will be a different set of msgTypes
                clientHandler.dataOutputStream.writeUTF(messageToSend);
                clientHandler.dataOutputStream.flush();
            }catch(IOException e){
                closeEverything(socket,dataInputStream,dataOutputStream);
            }
        }
    }

    public void sendPlayerNames(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.dataOutputStream.writeByte(1);
                int repeats = playerNames.size();
                clientHandler.dataOutputStream.writeInt(repeats);
                for(int i=0;i<repeats;i++){
                    clientHandler.dataOutputStream.writeUTF(playerNames.get(i));
                }
            }catch(IOException e){
                closeEverything(socket,dataInputStream,dataOutputStream);
            }
        }
    }

    public void sendPlayerStates(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.dataOutputStream.writeByte(2);
                int repeats = playerStates.size();
                clientHandler.dataOutputStream.writeInt(repeats);
                for(int i=0;i<repeats;i++){
                    clientHandler.dataOutputStream.writeUTF(playerStates.get(i));
                }
            }catch(IOException e){
                closeEverything(socket,dataInputStream,dataOutputStream);
            }
        }
    }


    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("Server: "+clientUsername+" has left the chat!");
        // every list needs to be updated
        int index = playerNames.indexOf(clientUsername);
        playerNames.remove(index);
        playerStates.remove(index);

        // send info to others
        sendPlayerNames();
        sendPlayerStates();
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