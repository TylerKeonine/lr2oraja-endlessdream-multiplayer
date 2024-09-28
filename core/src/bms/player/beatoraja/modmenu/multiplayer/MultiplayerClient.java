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

    public String outMessage = "{";
    public String inMessage;

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
                        //MultiplayerMenu.statusText = String.join(", ", Multiplayer.playerNames);
                        MultiplayerMenu.statusText = inMessage;
                        String msgType = MultiplayerJson.readMessageString(inMessage, "MessageType");
                        //MultiplayerMenu.statusText = msgType;
                        switch(msgType){
                            /* 
                            case(0): // test messages
                                msgFromGroupChat = dataInputStream.readUTF();
                                MultiplayerMenu.statusText = msgFromGroupChat;
                            break;
                            */
                            case("SendPlayerNames"): //update lobby player names
                                MultiplayerMenu.statusText = MultiplayerJson.readMessageStringList(inMessage, "PlayerNames").toString();
                                //Multiplayer.playerNames = MultiplayerJson.readMessageStringList(inMessage, "PlayerNames");
                                //Multiplayer.playerNames.add("test");
                            break;
                            /*
                            case(2): // update player states
                                repeats = dataInputStream.readInt();
                                Multiplayer.playerStates.clear();
                                for(int i=0;i<repeats;i++){
                                    Multiplayer.playerStates.add(dataInputStream.readUTF());
                                }                 
                            break;
                            case(3): // start message
                                // do playSong from MusicSelector.java using Multiplayer.selectedSong
                                // prone to crashing ever since adding this? happens on selecting a song too fast, not even pressing start.
                                if(selector!=null){
                                    //MultiplayerMenu.statusText = "Start!";
                                    selector.playSong(Multiplayer.selectedSong);
                                }
                            break;
                            case(4): //update
                            break;
                            case(5): // update song
                                Multiplayer.selectedSong = dataInputStream.readUTF();
                                Multiplayer.selectedSongTitle = dataInputStream.readUTF();
                                // check if song is missing
                                if(selector.findSongData(Multiplayer.selectedSong)==null){
                                    sendMissing(true);
                                }else{
                                    sendMissing(false);
                                }
                            break;
                            case(6): // update playing
                                repeats = dataInputStream.readInt();
                                Multiplayer.playerPlaying.clear();
                                for(int i=0;i<repeats;i++){
                                    Multiplayer.playerPlaying.add(dataInputStream.readBoolean());
                                }
                                if(Multiplayer.playerPlaying.contains(true)){
                                    Multiplayer.lobbyPlaying = true;
                                }else{
                                    Multiplayer.lobbyPlaying = false;
                                }
                            break;
                            case(7): // force end
                                Multiplayer.playerPlaying.replaceAll(e -> false);
                                Multiplayer.lobbyPlaying = false;
                            break;
                            case(8): // update score
                                repeats = dataInputStream.readInt();
                                int[][] temparr = new int[repeats][14];
                                for (int i=0;i<repeats*14;i++){
                                    temparr[i/14][i%14] = dataInputStream.readInt();
                                    Multiplayer.playerScoreData = temparr;
                                }
                            break;
                            case(9): // update players missing
                                repeats = dataInputStream.readInt();
                                Multiplayer.playerMissing.clear();
                                for(int i=0;i<repeats;i++){
                                    Multiplayer.playerMissing.add(dataInputStream.readBoolean());
                                }
                            break;
                            case (10): // update host
                                if(dataInputStream.readBoolean()==true){
                                    Multiplayer.isHost=true;
                                }else{
                                    Multiplayer.isHost=false;
                                }
                            break;
                            */
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
            client.sendJoin();
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
    public void sendJoin(){
        outMessage=MultiplayerJson.addMessageType(outMessage,"Join");
        outMessage=MultiplayerJson.addMessageString(outMessage,"Username",Multiplayer.username);
        outMessage=MultiplayerJson.addMessageString(outMessage,"Socket",socket.toString());
        MultiplayerJson.sendMessage(outMessage,dataOutputStream);
    }

    public static void sendReady(){
        try{
            dataOutputStream.write(1);
            dataOutputStream.writeUTF(socket.toString());
            dataOutputStream.flush();
        }catch(IOException e){
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
    }

    // Requests for host status
    public static void sendHost(){
        try{
            dataOutputStream.write(2);
            dataOutputStream.writeUTF(socket.toString());
            dataOutputStream.flush();
        }catch(IOException e){
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
    }

    public static void sendStart(){
        try{
            dataOutputStream.write(3);
            dataOutputStream.flush();
        }catch(IOException e){
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
    }

    public static void requestUpdate(){
        try{
            dataOutputStream.write(4);
            dataOutputStream.flush();
        }catch(IOException e){
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
    }

    public static void sendSong(String md5, String title){
        try{
            dataOutputStream.write(5);
            dataOutputStream.writeUTF(md5);
            dataOutputStream.writeUTF(title);
            dataOutputStream.flush();
        }catch(IOException e){
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
    }

    public static void sendPlaying(Boolean playing){
        try{
            dataOutputStream.write(6);
            dataOutputStream.writeUTF(socket.toString());
            dataOutputStream.writeBoolean(playing);
            dataOutputStream.flush();
        }catch(IOException e){
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
    }

    public static void sendEnd(){
        try{
            dataOutputStream.write(7);
            dataOutputStream.flush();
        }catch(IOException e){
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
    }

    public static void sendScore(int judge, int combo){
        try{
            dataOutputStream.write(8);
            dataOutputStream.writeUTF(socket.toString());
            dataOutputStream.writeInt(judge);
            dataOutputStream.writeInt(combo);
            for(int i=0;i<12;i++){
                if(i%2==0){
                    dataOutputStream.writeInt(liveScoreData.getJudgeCount(i/2, true));
                }else{
                    dataOutputStream.writeInt(liveScoreData.getJudgeCount(i/2, false));
                }
            }
            dataOutputStream.flush();
        }catch(IOException e){
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
    }
    
    public static void sendMissing(boolean isMissing){
        Multiplayer.isMissing = isMissing;
        try{
            dataOutputStream.write(9);
            dataOutputStream.writeUTF(socket.toString());
            dataOutputStream.writeBoolean(isMissing);
            dataOutputStream.flush();            
        }catch(IOException e){
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
    }

    // Gives/removes host status
    public static void toggleHost(int target, Boolean switchto){
        try{
            dataOutputStream.write(10);
dataOutputStream.writeUTF(socket.toString());
            dataOutputStream.writeInt(target);
            dataOutputStream.writeBoolean(switchto);
            dataOutputStream.flush();            
        }catch(IOException e){
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
    }
}
