package bms.player.beatoraja.modmenu.multiplayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class MultiplayerClientHandler implements Runnable{

    // Socket Variables
    public static ArrayList<MultiplayerClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String clientUsername;
    private String clientSocket;

    // Score send limits
    private static final long DEBOUNCE_TOLERANCE = 100;
    private static long debounce = System.currentTimeMillis();
    private static boolean ableToSend = true;

    // Lobby Information
    // 3d Arrays might be better? desync between arraylists could be possible
    public static ArrayList<String> socketList = new ArrayList<>();
    public static ArrayList<String> playerNames = new ArrayList<>();
    public static ArrayList<String> playerStates = new ArrayList<>();
    public static ArrayList<Boolean> playerPlaying = new ArrayList<>();
    public static ArrayList<Boolean> playerMissing = new ArrayList<>();
    public static int[][] playerScoreData = new int[0][12];
    public static String selectedSong = "";
    public static String selectedSongTitle = "";


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
            playerMissing.add(true);
            playerScoreData = Arrays.copyOf(playerScoreData, playerScoreData.length+1);
            playerScoreData[playerScoreData.length-1] = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0}; // TODO: only supports 7sp. make it work for all keymodes
            // update new player to current info
            sendPlayerNames();
            sendPlayerStates();
            sendSelectedSong();
            sendPlayerPlaying();
            sendPlayerScoreData();
            sendPlayerMissing();
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
                        sendPlayerScoreData();                 
                    break;
                    case(3): // start
                        broadcastStart();
                        sendPlayerScoreData();
                    break;               
                    case(4): // update
                        broadcastUpdate();
                    break;
                    case(5): // select song
                        selectedSong = dataInputStream.readUTF();
                        selectedSongTitle = dataInputStream.readUTF();
                        sendSelectedSong();
                    break;
                    case(6): // playing status
                        messageFromClient = dataInputStream.readUTF();
                        index = socketList.indexOf(messageFromClient);
                        bool = dataInputStream.readBoolean();
                        playerPlaying.set(index, bool);
                        sendPlayerPlaying();
                        sendPlayerScoreData();
                    break;
                    case(7): // force end
                        playerPlaying.replaceAll(e -> false);
                        broadcastEnd();
                        sendPlayerScoreData();
                    break;
                    case(8): // send score
                        messageFromClient = dataInputStream.readUTF();
                        index = socketList.indexOf(messageFromClient);
                        int[] newarr = new int[playerScoreData[index].length];
                        for(int i=0;i<playerScoreData[index].length;i++){
                            newarr[i] = dataInputStream.readInt(); 
                        }
                        playerScoreData[index] = newarr;
                        sendPlayerScoreData();
                    break;
                    case(9): // send ismissing
                        messageFromClient = dataInputStream.readUTF();
                        index = socketList.indexOf(messageFromClient);
                        bool = dataInputStream.readBoolean();
                        playerMissing.set(index,bool);
                        // ensures sendPlayerMissing only sends once
                        if(index==playerMissing.size()-1){
                            sendPlayerMissing();
                        }
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
                clientHandler.dataOutputStream.write(0); // note these will be a different set of msgTypes
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
                clientHandler.dataOutputStream.write(1);
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
                clientHandler.dataOutputStream.write(2);
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
                clientHandler.dataOutputStream.write(3);
                clientHandler.dataOutputStream.flush();
            }catch(IOException e){
                closeEverything(socket,dataInputStream,dataOutputStream);
            }
        }
    }

    public void broadcastUpdate(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.dataOutputStream.write(4);
                clientHandler.dataOutputStream.flush();
            }catch(IOException e){
                closeEverything(socket,dataInputStream,dataOutputStream);
            }
        }
    }

    public void sendSelectedSong(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.dataOutputStream.write(5);
                clientHandler.dataOutputStream.writeUTF(selectedSong);
                clientHandler.dataOutputStream.writeUTF(selectedSongTitle);
                clientHandler.dataOutputStream.flush();
            }catch(IOException e){
                closeEverything(socket,dataInputStream,dataOutputStream);
            }
        }
    }

    public void sendPlayerPlaying(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.dataOutputStream.write(6);
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
                clientHandler.dataOutputStream.write(7);
                clientHandler.dataOutputStream.flush();
            }catch(IOException e){
                closeEverything(socket,dataInputStream,dataOutputStream);
            }
        }
    }

    public void sendPlayerScoreData() {
        if (System.currentTimeMillis() > debounce+DEBOUNCE_TOLERANCE&&ableToSend==true){
            debounce = System.currentTimeMillis();
            ableToSend = false;
            for(MultiplayerClientHandler clientHandler : clientHandlers){
                try{
                    clientHandler.dataOutputStream.write(8);
                    int repeats = playerScoreData.length;
                    clientHandler.dataOutputStream.writeInt(repeats);
                    for(int i=0;i<repeats;i++){
                        for(int v=0;v<playerScoreData[i].length;v++){
                            clientHandler.dataOutputStream.writeInt(playerScoreData[i][v]);
                        }

                    }
                    clientHandler.dataOutputStream.flush();
                }catch(IOException e){
                    closeEverything(socket, dataInputStream, dataOutputStream);
                }          
            }  
            ableToSend = true;
        }
    }

    public void sendPlayerMissing(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.dataOutputStream.write(9);
                int repeats = playerMissing.size();
                clientHandler.dataOutputStream.writeInt(repeats);
                for(int i=0;i<repeats;i++){
                    clientHandler.dataOutputStream.writeBoolean(playerMissing.get(i));
                }
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
        playerMissing.remove(index);
        for(int i=index;i<playerScoreData.length-1;i++){
            playerScoreData[i]=playerScoreData[i+1];
        }
        playerScoreData = Arrays.copyOfRange(playerScoreData, 0, playerScoreData.length-1);

        // send info to others
        sendPlayerNames();
        sendPlayerStates();
        sendPlayerPlaying();
        sendPlayerScoreData();
    }

    public void closeEverything(Socket skt,DataInputStream dIn, DataOutputStream dOut){
        //Multiplayer.leaveLobby();
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