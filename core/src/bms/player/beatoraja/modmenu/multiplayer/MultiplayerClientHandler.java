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

    public String outMessage = "{";
    public String inMessage;

    public MultiplayerClientHandler(Socket socket){
        try{
            this.socket = socket;
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            inMessage = dataInputStream.readUTF();
            // TODO verify that messagetype is join
            MultiplayerJson.readMessageString(inMessage,"MessageType");
            this.clientUsername = MultiplayerJson.readMessageString(inMessage,"Username");
            this.clientSocket = MultiplayerJson.readMessageString(inMessage,"Socket");
            clientHandlers.add(this);
            sendStatusMessage(clientUsername+" has entered the lobby");
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
        String msgType;
        int index;
        Boolean bool;

        while(socket.isConnected()){
            try{
                inMessage = dataInputStream.readUTF();
                msgType = MultiplayerJson.readMessageString(inMessage, "MessageType");
                switch(msgType){
                    case("StatusMessage"):
                        sendStatusMessage(MultiplayerJson.readMessageString(inMessage, "Message"));
                    break;
                    case("SendReady"): // ready
                        index = socketList.indexOf(MultiplayerJson.readMessageString(inMessage, "Socket"));
                        if(playerStates.get(index).equals("Ready")){
                            playerStates.set(index,"Not Ready");
                        }else{
                            playerStates.set(index,"Ready");
                        }
                        sendPlayerStates();
                    break;
                    case("SendHost"): // host
                        playerStates.set(socketList.indexOf(MultiplayerJson.readMessageString(inMessage, "Socket")),"Host");
                        sendPlayerStates();   
                        sendPlayerScoreData();                 
                    break;
                    case("SendStart"): // start
                        broadcastStart();
                        sendPlayerScoreData();
                    break;     
                    case("SendSong"): // select song
                        selectedSong = MultiplayerJson.readMessageString(inMessage, "Md5");
                        selectedSongTitle = MultiplayerJson.readMessageString(inMessage, "Title");
                        sendSelectedSong();
                    break;
                    case("SendPlaying"): // playing status
                        playerPlaying.set(socketList.indexOf(MultiplayerJson.readMessageString(inMessage, "Socket")), MultiplayerJson.readMessageBool(inMessage, "IsPlaying"));
                        sendPlayerPlaying();
                        sendPlayerScoreData();
                    break;
                    case("SendEnd"): // force end
                        playerPlaying.replaceAll(e -> false);
                        broadcastEnd();
                        sendPlayerScoreData();
                    break;
                    case("SendScore"): // send score
                        MultiplayerMenu.statusText =  inMessage;
                        playerScoreData[socketList.indexOf(MultiplayerJson.readMessageString(inMessage, "Socket"))] = MultiplayerJson.readMessageIntArray(inMessage, "PlayerScoreData");
                        sendPlayerScoreData();
                    break;
                    case("SendMissing"): // send ismissing
                        index = socketList.indexOf(MultiplayerJson.readMessageString(inMessage, "Socket"));
                        playerMissing.set(index,MultiplayerJson.readMessageBool(inMessage, "IsMissing"));
                        // ensures sendPlayerMissing only sends once
                        if(index==playerMissing.size()-1){
                            sendPlayerMissing();
                        }
                    break;
                    case("ToggleHost"):
                        if(playerStates.get(socketList.indexOf(MultiplayerJson.readMessageString(inMessage, "Socket"))).equals("Host")){
                            index = MultiplayerJson.readMessageInt(inMessage, "TargetIndex");
                            bool = MultiplayerJson.readMessageBool(inMessage, "SwitchTo");
                            if (bool==true){
                                playerStates.set(index,"Host");
                            }else{
                                playerStates.set(index,"Not Ready");
                            }
                            sendPlayerStates();
                            updateHost(clientHandlers.get(index),bool);
                        }
                    break;
                }
                
            }catch(IOException e){
                closeEverything(socket,dataInputStream,dataOutputStream);
                break;
            }
        }
    }

    public void sendStatusMessage(String msg){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            outMessage = MultiplayerJson.addMessageType(outMessage, "SendStatusMessage");
            outMessage = MultiplayerJson.addMessageString(outMessage, "Message", msg);
            outMessage = MultiplayerJson.sendMessage(outMessage, clientHandler.dataOutputStream);
        }
    }

    public void sendPlayerNames(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            outMessage = MultiplayerJson.addMessageType(outMessage, "SendPlayerNames");
            outMessage = MultiplayerJson.addMessageStringArray(outMessage, "PlayerNames",playerNames.toArray(new String[0]));
            outMessage = MultiplayerJson.sendMessage(outMessage, clientHandler.dataOutputStream);
        }
    }

    public void sendPlayerStates(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            outMessage = MultiplayerJson.addMessageType(outMessage, "SendPlayerStates");
            outMessage = MultiplayerJson.addMessageStringArray(outMessage, "PlayerState",playerStates.toArray(new String[0]));
            outMessage = MultiplayerJson.sendMessage(outMessage, clientHandler.dataOutputStream);
        }
    }

    public void broadcastStart(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            outMessage = MultiplayerJson.addMessageType(outMessage, "BroadcastStart");
            outMessage = MultiplayerJson.sendMessage(outMessage, clientHandler.dataOutputStream);
        }
    }

    public void sendSelectedSong(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            outMessage = MultiplayerJson.addMessageType(outMessage, "SendSelectedSong");
            outMessage = MultiplayerJson.addMessageString(outMessage, "SelectedSong", selectedSong);
            outMessage = MultiplayerJson.addMessageString(outMessage, "SelectedSongTitle", selectedSongTitle);
            outMessage = MultiplayerJson.sendMessage(outMessage, clientHandler.dataOutputStream);
        }
    }

    public void sendPlayerPlaying(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            outMessage = MultiplayerJson.addMessageType(outMessage, "SendPlayerPlaying");
            outMessage = MultiplayerJson.addMessageBoolArray(outMessage, "PlayersPlaying", playerPlaying.toArray(new Boolean[0]));
            outMessage = MultiplayerJson.sendMessage(outMessage, clientHandler.dataOutputStream);
        }
    }

    public void broadcastEnd(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            outMessage = MultiplayerJson.addMessageType(outMessage, "BroadcastEnd");
            outMessage = MultiplayerJson.sendMessage(outMessage, clientHandler.dataOutputStream);
        }
    }

    public void sendPlayerScoreData() {
        if (System.currentTimeMillis() > debounce+DEBOUNCE_TOLERANCE&&ableToSend==true){
            debounce = System.currentTimeMillis();
            ableToSend = false;
            for(MultiplayerClientHandler clientHandler : clientHandlers){
                // maybe seperate judge/combo and judge counts
                outMessage = MultiplayerJson.addMessageType(outMessage, "SendPlayerScoreData");
                outMessage = MultiplayerJson.addMessageInt2dArray(outMessage, "PlayerScoreData", playerScoreData);
                outMessage = MultiplayerJson.sendMessage(outMessage, clientHandler.dataOutputStream);
            }
            ableToSend = true;
        }
    }

    public void sendPlayerMissing(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            outMessage = MultiplayerJson.addMessageType(outMessage, "SendPlayerMissing");
            outMessage = MultiplayerJson.addMessageBoolArray(outMessage, "PlayersMissing", playerMissing.toArray(new Boolean[0]));
            outMessage = MultiplayerJson.sendMessage(outMessage, clientHandler.dataOutputStream);
        }
    }   

    // update host for target
    public void updateHost(MultiplayerClientHandler clientHandler, Boolean bool){
        outMessage = MultiplayerJson.addMessageType(outMessage, "UpdateHost");
        outMessage = MultiplayerJson.addMessageBool(outMessage, "IsHost", bool);
        outMessage = MultiplayerJson.sendMessage(outMessage, clientHandler.dataOutputStream);
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
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