package bms.player.beatoraja.modmenu.multiplayer;

import java.net.Socket;
import java.util.ArrayList;
import java.io.*;

public class MultiplayerClientHandler implements Runnable{

    // Socket Variables
    public static ArrayList<MultiplayerClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private String clientUsername;
    private String clientSocket;

    // Lobby Information
    // 3d Arrays might be better? desync between arraylists could be possible
    public static ArrayList<String> socketList = new ArrayList<>();
    public static ArrayList<String> playerNames = new ArrayList<>();
    public static ArrayList<String> playerStates = new ArrayList<>();


    public MultiplayerClientHandler(Socket socket){
        try{
            this.socket = socket;
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.clientUsername = objectInputStream.readUTF();
            this.clientSocket = objectInputStream.readUTF();
            clientHandlers.add(this);
            broadcastMessage("Server: "+clientUsername+" has entered the chat!");
            // every list needs to be added to
            socketList.add(clientSocket);  // add client socket
            playerNames.add(clientUsername);
            playerStates.add("Not Ready");
            // update new player to current info
            sendPlayerNames();
            sendPlayerStates();
            broadcastUpdate();
        }catch(IOException e){
            closeEverything(socket,objectInputStream,objectOutputStream);
        }
    }

    @Override
    public void run(){
        String messageFromClient;
        Byte msgType;
        int index;

        while(socket.isConnected()){
            try{
                msgType = objectInputStream.readByte();
                switch(msgType){
                    case(0): // test
                        messageFromClient = objectInputStream.readUTF();
                        broadcastMessage(messageFromClient); 
                    break;
                    case(1): // ready
                        messageFromClient = objectInputStream.readUTF();
                        index = socketList.indexOf(messageFromClient);
                        if(playerStates.get(index).equals("Ready")){
                            playerStates.set(index,"Not Ready");
                        }else{
                            playerStates.set(index,"Ready");
                        }
                        sendPlayerStates();
                    break;
                    case(2): // host
                        messageFromClient = objectInputStream.readUTF();
                        playerStates.set(socketList.indexOf(messageFromClient),"Host");
                        sendPlayerStates();                    
                    break;
                    case(3): // start
                        broadcastStart();
                    break;               
                    case(4): // update
                        broadcastUpdate();
                    break;
                }
                
            }catch(IOException e){
                closeEverything(socket,objectInputStream,objectOutputStream);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.objectOutputStream.writeByte(0); // note these will be a different set of msgTypes
                clientHandler.objectOutputStream.writeUTF(messageToSend);
                clientHandler.objectOutputStream.flush();
            }catch(IOException e){
                closeEverything(socket,objectInputStream,objectOutputStream);
            }
        }
    }

    public void sendPlayerNames(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.objectOutputStream.writeByte(1);
                int repeats = playerNames.size();
                clientHandler.objectOutputStream.writeInt(repeats);
                for(int i=0;i<repeats;i++){
                    clientHandler.objectOutputStream.writeUTF(playerNames.get(i));
                }
            }catch(IOException e){
                closeEverything(socket,objectInputStream,objectOutputStream);
            }
        }
    }

    public void sendPlayerStates(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.objectOutputStream.writeByte(2);
                int repeats = playerStates.size();
                clientHandler.objectOutputStream.writeInt(repeats);
                for(int i=0;i<repeats;i++){
                    clientHandler.objectOutputStream.writeUTF(playerStates.get(i));
                }
            }catch(IOException e){
                closeEverything(socket,objectInputStream,objectOutputStream);
            }
        }
    }

    public void broadcastStart(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.objectOutputStream.writeByte(3);
                clientHandler.objectOutputStream.flush();
            }catch(IOException e){
                closeEverything(socket,objectInputStream,objectOutputStream);
            }
        }
    }

    public void broadcastUpdate(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.objectOutputStream.writeByte(4);
                clientHandler.objectOutputStream.flush();
            }catch(IOException e){
                closeEverything(socket,objectInputStream,objectOutputStream);
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

        // send info to others
        sendPlayerNames();
        sendPlayerStates();

        broadcastUpdate();
    }

    public void closeEverything(Socket skt,ObjectInputStream dIn, ObjectOutputStream dOut){
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