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
    private static final long DEBOUNCE_TOLERANCE = 0;
    private static long debounce = System.currentTimeMillis();
    private static boolean ableToSend = true;

    // Lobby Information
    // 3d Arrays might be better? desync between arraylists could be possible
    public static ArrayList<String> socketList = new ArrayList<>();
    public static ArrayList<String> playerNames = new ArrayList<>();
    public static ArrayList<Boolean> playerReady = new ArrayList<>();
    public static ArrayList<Boolean> playerHost = new ArrayList<>();
    public static ArrayList<Boolean> playerPlaying = new ArrayList<>();
    public static ArrayList<Boolean> playerMissing = new ArrayList<>();
    public static ArrayList<Boolean> playerLoaded = new ArrayList<>();
    public static int[][] playerScoreData = new int[0][12];
    public static String selectedSong = "";
    public static String selectedSongTitle = "";
    public static String leaderSocket = "";
    public static int leaderIndex = 0;

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
            playerReady.add(false);
            playerHost.add(false);
            playerPlaying.add(false);
            playerMissing.add(true);
            playerLoaded.add(true);
            playerScoreData = Arrays.copyOf(playerScoreData, playerScoreData.length+1);
            playerScoreData[playerScoreData.length-1] = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            // update new player to current info
            sendPlayerNames();
            sendPlayerReady();
            sendPlayerHosts();
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
                MultiplayerMenu.statusText="leader:"+Multiplayer.leaderIndex;
                inMessage = dataInputStream.readUTF();
                msgType = MultiplayerJson.readMessageString(inMessage, "MessageType");
                switch(msgType){
                    case("StatusMessage"):
                        sendStatusMessage(MultiplayerJson.readMessageString(inMessage, "Message"));
                    break;
                    case("SendReady"): // ready
                        index = socketList.indexOf(MultiplayerJson.readMessageString(inMessage, "Socket"));
                        playerReady.set(index,!playerReady.get(index));
                        sendPlayerReady();
                    break;
                    case("SendHost"): // host
                        index = socketList.indexOf(MultiplayerJson.readMessageString(inMessage, "Socket"));
                        playerHost.set(index,true);
                        sendPlayerHosts();   
                        updateHost(clientHandlers.get(index),true);              
                    break;
                    case("SendStart"): // start
                        broadcastStart();
                        sendPlayerScoreData();
                        playerLoaded.replaceAll(e -> false);
                        sendPlayerLoaded();
                    break;     
                    case("SendSong"): // select song
                        leaderSocket = MultiplayerJson.readMessageString(inMessage, "Socket");
                        leaderIndex = socketList.indexOf(leaderSocket);
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
                        playerScoreData[socketList.indexOf(MultiplayerJson.readMessageString(inMessage, "Socket"))] = MultiplayerJson.readMessageIntArray(inMessage, "PlayerScoreData");
                        sendPlayerScoreData();
                    break;
                    case("SendMissing"): // send ismissing
                        index = socketList.indexOf(MultiplayerJson.readMessageString(inMessage, "Socket"));
                        playerMissing.set(index,MultiplayerJson.readMessageBool(inMessage, "IsMissing"));
                        /* TODO need to substitute this with something else
                        // ensures sendPlayerMissing only sends once
                        if(index==playerMissing.size()-1){
                            sendPlayerMissing();
                        }*/
                        sendPlayerMissing();
                    break;
                    case("ToggleHost"):
                        if(playerHost.get(socketList.indexOf(MultiplayerJson.readMessageString(inMessage, "Socket")))==true){
                            index = MultiplayerJson.readMessageInt(inMessage, "TargetIndex");
                            bool = MultiplayerJson.readMessageBool(inMessage, "SwitchTo");
                            playerHost.set(index, bool);
                            sendPlayerHosts();
                            updateHost(clientHandlers.get(index),bool);
                        }
                    break;
                    case("SendLoaded"):
                        index = socketList.indexOf(MultiplayerJson.readMessageString(inMessage, "Socket"));
                        playerLoaded.set(index,MultiplayerJson.readMessageBool(inMessage, "IsLoaded")); 
                        sendPlayerLoaded();                    
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

    public void sendPlayerReady(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            outMessage = MultiplayerJson.addMessageType(outMessage, "SendPlayerReady");
            outMessage = MultiplayerJson.addMessageBoolArray(outMessage, "PlayerReady",playerReady.toArray(new Boolean[0]));
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
            outMessage = MultiplayerJson.addMessageString(outMessage, "LeaderSocket", leaderSocket);
            outMessage = MultiplayerJson.addMessageInt(outMessage, "LeaderIndex", leaderIndex);
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

    public void sendPlayerLoaded(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            outMessage = MultiplayerJson.addMessageType(outMessage, "SendPlayerLoaded");
            outMessage = MultiplayerJson.addMessageBoolArray(outMessage, "PlayersLoaded", playerLoaded.toArray(new Boolean[0]));
            outMessage = MultiplayerJson.sendMessage(outMessage, clientHandler.dataOutputStream);
        }
    }
    
    public void sendPlayerHosts(){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            outMessage = MultiplayerJson.addMessageType(outMessage, "SendPlayerHosts");
            outMessage = MultiplayerJson.addMessageBoolArray(outMessage, "PlayerHosts", playerHost.toArray(new Boolean[0]));
            outMessage = MultiplayerJson.sendMessage(outMessage, clientHandler.dataOutputStream);        
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        // every list needs to be updated
        int index = socketList.indexOf(clientSocket.toString());
        socketList.remove(index);
        playerNames.remove(index);
        playerReady.remove(index);
        playerHost.remove(index);
        playerPlaying.remove(index);
        playerMissing.remove(index);
        playerLoaded.remove(index);
        for(int i=index;i<playerScoreData.length-1;i++){
            playerScoreData[i]=playerScoreData[i+1];
        }
        playerScoreData = Arrays.copyOfRange(playerScoreData, 0, playerScoreData.length-1);

        // send info to others
        sendPlayerNames();
        sendPlayerReady();
        sendPlayerHosts();
        sendPlayerPlaying();
        sendPlayerScoreData();
        sendStatusMessage(clientUsername+" has left the lobby");
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