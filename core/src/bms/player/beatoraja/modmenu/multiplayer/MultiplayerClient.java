package bms.player.beatoraja.modmenu.multiplayer;

import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import bms.player.beatoraja.PlayerConfig;

import java.io.*;


public class MultiplayerClient {
    // Class
    private static Socket socket;
    private static DataInputStream dataInputStream;
    private static DataOutputStream dataOutputStream;
    private static String username = PlayerConfig.name.substring(0, Math.min(PlayerConfig.name.length(), 20));

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
                String msgFromGroupChat;
                Byte msgType;
                int repeats;
                while(socket.isConnected()){
                    try{
                        //MultiplayerMenu.statusText = String.join(", ", Multiplayer.playerNames);
                        msgType = dataInputStream.readByte();
                        switch(msgType){
                            case(0): // test messages
                                msgFromGroupChat = dataInputStream.readUTF();
                                MultiplayerMenu.statusText = msgFromGroupChat;
                            break;
                            case(1): //update lobby player names
                                repeats = dataInputStream.readInt();
                                Multiplayer.playerNames.clear();
                                for(int i=0;i<repeats;i++){
                                    Multiplayer.playerNames.add(dataInputStream.readUTF());
                                }
                            break;
                            case(2): // update player states
                                repeats = dataInputStream.readInt();
                                Multiplayer.playerStates.clear();
                                for(int i=0;i<repeats;i++){
                                    Multiplayer.playerStates.add(dataInputStream.readUTF());
                                }                                
                            break;
                        }
                    }catch(IOException e){
                        closeEverything(socket, dataInputStream, dataOutputStream);
                    }
                }
                closeSocket();  //TODO loop doesn't know when host leaves
            }
        }).start();
    }

    public static void closeEverything(Socket skt,DataInputStream dIn, DataOutputStream dOut){
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
        } catch (IOException e) {
            closeSocket();
        }
    }

    // Commands
    public void sendJoin(){
        try{
            dataOutputStream.writeUTF(username);
            dataOutputStream.writeUTF(socket.toString());
            dataOutputStream.flush();
        }catch(IOException e){
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
    }

    public static void sendReady(){
        try{
            dataOutputStream.writeByte(1);
            MultiplayerMenu.statusText = socket.toString();
            dataOutputStream.writeUTF(socket.toString());
            dataOutputStream.flush();
        }catch(IOException e){
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
    }

    // Requests for host status
    public static void sendHost(){
        try{
            dataOutputStream.writeByte(2);
            dataOutputStream.writeUTF(socket.toString());
            dataOutputStream.flush();
        }catch(IOException e){
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
    }

    public static void sendSong(){
        /*
        try{
            dataOutputStream.writeUTF(username+" isReady: "+Multiplayer.isReady);
            dataOutputStream.flush();
        }catch(IOException e){
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
        */
    }
}
