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
    private String clientSocket;

    // Lobby Information
    // 3d Arrays might be better? desync between arraylists could be possible
    public static ArrayList<String> socketList = new ArrayList<>();
    public static ArrayList<String> playerNames = new ArrayList<>();
    public static ArrayList<String> playerStates = new ArrayList<>();
    public static ArrayList<Boolean> playerPlaying = new ArrayList<>();
    public static String selectedSong = "";


    public MultiplayerClientHandler(Socket socket){
        try{
            this.socket = socket;
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.clientUsername = dataInputStream.readUTF();
            this.clientSocket = dataInputStream.readUTF();
            clientHandlers.add(this);
            broadcastMessage("Server: "+clientUsername+" has entered the chat!");
            // every list needs to be added to
            socketList.add(clientSocket);  // add client socket
            playerNames.add(clientUsername);
            playerStates.add("Not Ready");
            playerPlaying.add(false);
            // update new player to current info
            sendPlayerNames();
            sendPlayerStates();
            sendSelectedSong();
            sendPlayerPlaying();
        }catch(IOException e){
            closeEverything(socket,dataInputStream,dataOutputStream);
        }
    }

    @Override
    public void run(){
        String messageFromClient;
        Byte msgType;
        int index;
        Boolean bool;

        while(socket.isConnected()){
            try{
                msgType = dataInputStream.readByte();
                switch(msgType){
                    case(0): // test
                        messageFromClient = dataInputStream.readUTF();
                        broadcastMessage(messageFromClient); 
                    break;
                    case(1): // ready
                        messageFromClient = dataInputStream.readUTF();
                        index = socketList.indexOf(messageFromClient);
                        if(playerStates.get(index).equals("Ready")){
                            playerStates.set(index,"Not Ready");
                        }else{
                            playerStates.set(index,"Ready");
                        }
                        sendPlayerStates();
                    break;
                    case(2): // host
                        messageFromClient = dataInputStream.readUTF();
                        playerStates.set(socketList.indexOf(messageFromClient),"Host");
                        sendPlayerStates();                    
                    break;
                    case(3): // start
                        broadcastStart();
                    break;               
                    case(4): // update
                        broadcastUpdate();
                    break;
                    case(5): // select song
                        messageFromClient = dataInputStream.readUTF();
                        selectedSong = messageFromClient;
                        sendSelectedSong();
                    break;
                    case(6): // playing status
                        messageFromClient = dataInputStream.readUTF();
                        index = socketList.indexOf(messageFromClient);
                        bool = dataInputStream.readBoolean();
                        playerPlaying.set(index, bool);
                        sendPlayerPlaying();
                    break;
                    case(7): // force end
                        playerPlaying.replaceAll(e -> false);
                        broadcastEnd();
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

    public void broadcastStart(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.dataOutputStream.writeByte(3);
                clientHandler.dataOutputStream.flush();
            }catch(IOException e){
                closeEverything(socket,dataInputStream,dataOutputStream);
            }
        }
    }

    public void broadcastUpdate(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.dataOutputStream.writeByte(4);
                clientHandler.dataOutputStream.flush();
            }catch(IOException e){
                closeEverything(socket,dataInputStream,dataOutputStream);
            }
        }
    }

    public void sendSelectedSong(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.dataOutputStream.writeByte(5);
                clientHandler.dataOutputStream.writeUTF(selectedSong);
                clientHandler.dataOutputStream.flush();
            }catch(IOException e){
                closeEverything(socket,dataInputStream,dataOutputStream);
            }
        }
    }

    public void sendPlayerPlaying(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.dataOutputStream.writeByte(6);
                int repeats = playerStates.size();
                clientHandler.dataOutputStream.writeInt(repeats);
                for(int i=0;i<repeats;i++){
                    clientHandler.dataOutputStream.writeBoolean(playerPlaying.get(i));
                }
                clientHandler.dataOutputStream.flush();
            }catch(IOException e){
                closeEverything(socket,dataInputStream,dataOutputStream);
            }
        }
    }

    public void broadcastEnd(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.dataOutputStream.writeByte(7);
                clientHandler.dataOutputStream.flush();
            }catch(IOException e){
                closeEverything(socket,dataInputStream,dataOutputStream);
            }
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("Server: "+clientUsername+" has left the chat!");
        // every list needs to be updated
        int index = socketList.indexOf(clientSocket.toString());
        socketList.remove(index);
        playerNames.remove(index);
        playerStates.remove(index);
        playerPlaying.remove(index);

        // send info to others
        sendPlayerNames();
        sendPlayerStates();
        sendPlayerPlaying();
    }

    public void closeEverything(Socket skt,DataInputStream dIn, DataOutputStream dOut){
        Multiplayer.leaveLobby();
        MultiplayerMenu.statusText = "SERVER ERROR";
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