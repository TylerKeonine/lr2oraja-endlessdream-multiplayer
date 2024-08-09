package bms.player.beatoraja.modmenu.multiplayer;

import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.beatoraja.song.SongData;

import java.io.*;


public class MultiplayerClient {
    // Class
    private static Socket socket;
    private static ObjectInputStream objectInputStream;
    private static ObjectOutputStream objectOutputStream;
    private static String username = PlayerConfig.name.substring(0, Math.min(PlayerConfig.name.length(), 20));

    public static MusicSelector selector;

    public MultiplayerClient(Socket socket){
        try {
            this.socket = socket;
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
        }catch(IOException e){
            closeEverything(socket, objectInputStream, objectOutputStream);
        }
    }

    // Socket Control

    public void listenForMessage(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                String msgFromGroupChat;
                Byte msgType;
                int repeats;
                while(socket.isConnected()){
                    try{
                        //MultiplayerMenu.statusText = String.join(", ", Multiplayer.playerNames);
                        msgType = objectInputStream.readByte();
                        switch(msgType){
                            case(0): // test messages
                                msgFromGroupChat = objectInputStream.readUTF();
                                MultiplayerMenu.statusText = msgFromGroupChat;
                            break;
                            case(1): //update lobby player names
                                repeats = objectInputStream.readInt();
                                Multiplayer.playerNames.clear();
                                for(int i=0;i<repeats;i++){
                                    Multiplayer.playerNames.add(objectInputStream.readUTF());
                                }
                            break;
                            case(2): // update player states
                                repeats = objectInputStream.readInt();
                                Multiplayer.playerStates.clear();
                                for(int i=0;i<repeats;i++){
                                    Multiplayer.playerStates.add(objectInputStream.readUTF());
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
                            case(4): //update gui.
                                // switching to from data to object streams doesn't update the gui without having to run an empty byte. not sure why
                            break;
                            case(5): // update song
                                msgFromGroupChat = objectInputStream.readUTF();
                                Multiplayer.selectedSong = msgFromGroupChat;
                            break;
                        }
                    }catch(IOException e){
                        closeEverything(socket, objectInputStream, objectOutputStream);
                    }
                }
                closeSocket();  //TODO loop doesn't know when host leaves
            }
        }).start();
    }

    public static void closeEverything(Socket skt,ObjectInputStream dIn, ObjectOutputStream dOut){
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
            requestUpdate();
            Multiplayer.inLobby = true;
            Multiplayer.hostIp = ipInput;
        } catch (UnknownHostException e) {
            closeSocket();
        } catch (IOException e) {
            closeSocket();
        }
    }

    // Commands
    public void sendJoin(){
        try{
            objectOutputStream.writeUTF(username);
            objectOutputStream.writeUTF(socket.toString());
            objectOutputStream.flush();
            requestUpdate();
        }catch(IOException e){
            closeEverything(socket, objectInputStream, objectOutputStream);
        }
    }

    public static void sendReady(){
        try{
            objectOutputStream.write(1);
            MultiplayerMenu.statusText = socket.toString();
            objectOutputStream.writeUTF(socket.toString());
            objectOutputStream.flush();
            requestUpdate();
        }catch(IOException e){
            closeEverything(socket, objectInputStream, objectOutputStream);
        }
    }

    // Requests for host status
    public static void sendHost(){
        try{
            objectOutputStream.write(2);
            objectOutputStream.writeUTF(socket.toString());
            objectOutputStream.flush();
            requestUpdate();
        }catch(IOException e){
            closeEverything(socket, objectInputStream, objectOutputStream);
        }
    }

    public static void sendStart(){
        try{
            objectOutputStream.write(3);
            objectOutputStream.flush();
            requestUpdate();
        }catch(IOException e){
            closeEverything(socket, objectInputStream, objectOutputStream);
        }
    }

    public static void requestUpdate(){
        try{
            objectOutputStream.write(4);
            objectOutputStream.flush();
        }catch(IOException e){
            closeEverything(socket, objectInputStream, objectOutputStream);
        }
    }

    public static void sendSong(String song){
        try{
            objectOutputStream.write(5);
            objectOutputStream.writeUTF(song);
            objectOutputStream.flush();
        }catch(IOException e){
            closeEverything(socket, objectInputStream, objectOutputStream);
        }
    }
}
