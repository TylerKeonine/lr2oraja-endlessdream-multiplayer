package bms.player.beatoraja.modmenu.multiplayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import bms.player.beatoraja.PlayerConfig;

import bms.player.beatoraja.ScoreData;
import bms.player.beatoraja.select.MusicSelector;

import bms.player.beatoraja.song.SongData;

import java.io.*;


public class MultiplayerClient {
    // Class
    private static Socket socket;
    private static DataInputStream dataInputStream;
    private static DataOutputStream dataOutputStream;

    public static MusicSelector selector;
    public static ScoreData liveScoreData;

    public static String outMessage = "{";
    public static String inMessage;

    public MultiplayerClient(Socket socket){
        try {
            this.socket = socket;
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.dataInputStream = new DataInputStream(socket.getInputStream());
        }catch(IOException e){
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
    }

    // Socket Control

    public void listenForMessage(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                while(socket.isConnected()){
                    try{
                        inMessage = dataInputStream.readUTF();
                        String msgType = MultiplayerJson.readMessageString(inMessage, "MessageType");;
                        switch(msgType){
                            case("SendStatusMessage"): // test messages
                                String msg = MultiplayerJson.readMessageString(inMessage, "Message");
                                MultiplayerMenu.statusText = msg;
                            break;
                            case("SendPlayerNames"): //update lobby player names
                                Multiplayer.playerNames = new ArrayList<String>(Arrays.asList(MultiplayerJson.readMessageStringArray(inMessage, "PlayerNames")));
                            break;
                            case("SendPlayerReady"): //update lobby player names
                                Multiplayer.playerReady = new ArrayList<Boolean>(Arrays.asList(MultiplayerJson.readMessageBoolArray(inMessage, "PlayerReady")));
                            break;
                            case("BroadcastStart"): // start message
                                if(selector!=null){
                                    selector.playSong(Multiplayer.selectedSong);
                                }
                            break;
                            case("SendSelectedSong"): // update song
                                Multiplayer.selectedSong = MultiplayerJson.readMessageString(inMessage, "SelectedSong");
                                Multiplayer.selectedSongTitle = MultiplayerJson.readMessageString(inMessage, "SelectedSongTitle");
                                Multiplayer.leaderSocket = MultiplayerJson.readMessageString(inMessage, "LeaderSocket");
                                Multiplayer.leaderIndex = MultiplayerJson.readMessageInt(inMessage, "LeaderIndex");
                                // check if song is missing
                                if(selector.findSongData(Multiplayer.selectedSong)==null){
                                    sendMissing(true);
                                }else{
                                    sendMissing(false);
                                }
                            break;
                            case("SendPlayerPlaying"): // update playing
                                Multiplayer.playerPlaying = new ArrayList<Boolean>(Arrays.asList(MultiplayerJson.readMessageBoolArray(inMessage,"PlayersPlaying")));
                                if(Multiplayer.playerPlaying.contains(true)){
                                    Multiplayer.lobbyPlaying = true;
                                }else{
                                    Multiplayer.lobbyPlaying = false;
                                }
                            break;
                            case("BroadcastEnd"): // force end
                                Multiplayer.playerPlaying.replaceAll(e -> false);
                                Multiplayer.lobbyPlaying = false;
                            break;
                            case("SendPlayerScoreData"): // update score
                                Multiplayer.playerScoreData = MultiplayerJson.readMessageInt2dArray(inMessage, "PlayerScoreData");
                            break;
                            case("SendPlayerMissing"): // update players missing
                                Multiplayer.playerMissing = new ArrayList<Boolean>(Arrays.asList(MultiplayerJson.readMessageBoolArray(inMessage, "PlayersMissing")));
                            break;
                            case ("UpdateHost"): // update host
                                if(MultiplayerJson.readMessageBool(inMessage,"IsHost")==true){
                                    Multiplayer.isHost=true;
                                    MultiplayerMenu.statusText = "You have been granted host";
                                }else{
                                    Multiplayer.isHost=false;
                                    MultiplayerMenu.statusText = "You have lost host";
                                }
                            break;
                            case("SendPlayerLoaded"):
                                Multiplayer.playerLoaded = new ArrayList<Boolean>(Arrays.asList(MultiplayerJson.readMessageBoolArray(inMessage, "PlayersLoaded")));
                            break;        
                            case("SendPlayerHosts"):
                                Multiplayer.playerHost = new ArrayList<Boolean>(Arrays.asList(MultiplayerJson.readMessageBoolArray(inMessage, "PlayerHosts")));
                            break;                          
                        }
                    }catch(IOException e){
                        closeEverything(socket, dataInputStream, dataOutputStream);
                    }
                }
            }
        }).start();
    }

    public static void closeEverything(Socket skt, DataInputStream dIn, DataOutputStream dOut){
        Multiplayer.leaveLobby(); // A bit of lag after host crashes? immediately fixed after pressing join
        MultiplayerMenu.statusText = "CLIENT LOST CONNECTION";
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

    public static void closeSocket(){
        try{
            if(socket!=null){
                socket.close();
                Multiplayer.inLobby = false;
                socket = null;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void joinLobby(String ipInput){
        Socket socket;
        try {
            socket = new Socket(ipInput,5730);
            MultiplayerClient client = new MultiplayerClient(socket);
            client.listenForMessage();
            client.sendJoin(); // TODO thread this so it doesnt freeze
            Multiplayer.inLobby = true;
            Multiplayer.hostIp = ipInput;
        } catch (UnknownHostException e) {
            closeSocket();
        }
        catch (IOException e) {
            closeSocket();
        }
    }

    // Commands
    public static void sendStatusMessage(String msg){
        outMessage=MultiplayerJson.addMessageType(outMessage, "StatusMessage");
        outMessage=MultiplayerJson.addMessageString(outMessage,"Message",msg);
        outMessage=MultiplayerJson.sendMessage(outMessage,dataOutputStream);
    }

    public void sendJoin(){
        outMessage=MultiplayerJson.addMessageType(outMessage,"Join");
        outMessage=MultiplayerJson.addMessageString(outMessage,"Username",Multiplayer.username);
        outMessage=MultiplayerJson.addMessageString(outMessage,"Socket",socket.toString());
        outMessage=MultiplayerJson.sendMessage(outMessage,dataOutputStream);
    }

    public static void sendReady(){
        outMessage = MultiplayerJson.addMessageType(outMessage, "SendReady");
        outMessage = MultiplayerJson.addMessageString(outMessage, "Socket", socket.toString());
        outMessage = MultiplayerJson.sendMessage(outMessage, dataOutputStream);
    }

    // Requests for host status
    public static void sendHost(){
        outMessage = MultiplayerJson.addMessageType(outMessage, "SendHost");
        outMessage = MultiplayerJson.addMessageString(outMessage, "Socket", socket.toString());
        outMessage = MultiplayerJson.sendMessage(outMessage, dataOutputStream);
    }

    public static void sendStart(){
        outMessage = MultiplayerJson.addMessageType(outMessage, "SendStart");
        outMessage = MultiplayerJson.sendMessage(outMessage, dataOutputStream);
    }

    public static void sendSong(String md5, String title){
        outMessage = MultiplayerJson.addMessageType(outMessage, "SendSong");
        outMessage = MultiplayerJson.addMessageString(outMessage, "Socket", socket.toString());
        outMessage = MultiplayerJson.addMessageString(outMessage, "Md5", md5);
        outMessage = MultiplayerJson.addMessageString(outMessage, "Title", title);
        outMessage = MultiplayerJson.sendMessage(outMessage, dataOutputStream);
    }

    public static void sendPlaying(Boolean playing){
        outMessage = MultiplayerJson.addMessageType(outMessage, "SendPlaying");
        outMessage = MultiplayerJson.addMessageString(outMessage, "Socket", socket.toString());
        outMessage = MultiplayerJson.addMessageBool(outMessage, "IsPlaying", playing);
        outMessage = MultiplayerJson.sendMessage(outMessage, dataOutputStream);
    }

    public static void sendEnd(){
        outMessage = MultiplayerJson.addMessageType(outMessage, "SendEnd");
        outMessage = MultiplayerJson.sendMessage(outMessage, dataOutputStream);
    }

    public static void sendScore(int judge, int combo){
        outMessage = MultiplayerJson.addMessageType(outMessage, "SendScore");
        outMessage = MultiplayerJson.addMessageString(outMessage, "Socket", socket.toString());
        int[] newarr = new int[14]; // TODO fix hardcoded value later
        newarr[0]=judge;
        newarr[1]=combo;
        for(int i=2;i<14;i++){
            if(i%2==0){
                newarr[i] = (liveScoreData.getJudgeCount((i-2)/2, true));
            }else{
                newarr[i] = (liveScoreData.getJudgeCount((i-2)/2, false));
            }
        }
        outMessage = MultiplayerJson.addMessageIntArray(outMessage, "PlayerScoreData", newarr);
        outMessage = MultiplayerJson.sendMessage(outMessage, dataOutputStream);
    }
    
    public static void sendMissing(boolean isMissing){
        Multiplayer.isMissing = isMissing; // TODO why is this here
        outMessage = MultiplayerJson.addMessageType(outMessage, "SendMissing");
        outMessage = MultiplayerJson.addMessageString(outMessage, "Socket", socket.toString());
        outMessage = MultiplayerJson.addMessageBool(outMessage, "IsMissing", isMissing);
        outMessage = MultiplayerJson.sendMessage(outMessage, dataOutputStream);
    }

    // Gives/removes host status
    public static void toggleHost(int target, Boolean switchto){
        outMessage = MultiplayerJson.addMessageType(outMessage, "ToggleHost");
        outMessage = MultiplayerJson.addMessageString(outMessage, "Socket", socket.toString());
        outMessage = MultiplayerJson.addMessageInt(outMessage, "TargetIndex", target); // why not use socket instead of index?
        outMessage = MultiplayerJson.addMessageBool(outMessage, "SwitchTo", switchto);
        outMessage = MultiplayerJson.sendMessage(outMessage, dataOutputStream);
    }

    public static void sendLoaded(Boolean isloaded){
        outMessage = MultiplayerJson.addMessageType(outMessage, "SendLoaded");
        outMessage = MultiplayerJson.addMessageString(outMessage, "Socket", socket.toString());
        outMessage = MultiplayerJson.addMessageBool(outMessage, "IsLoaded", isloaded);
        outMessage = MultiplayerJson.sendMessage(outMessage, dataOutputStream);
    }
}
