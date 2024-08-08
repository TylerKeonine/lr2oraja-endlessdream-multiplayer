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
                while(socket.isConnected()){
                    try{
                        msgType = dataInputStream.readByte();
                        msgFromGroupChat = dataInputStream.readUTF();
                        MultiplayerMenu.statusText = msgFromGroupChat;
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
            dataOutputStream.flush();
        }catch(IOException e){
            closeEverything(socket, dataInputStream, dataOutputStream);
        }
    }

    public static void sendReady(){
        try{
            dataOutputStream.writeByte(0);
            dataOutputStream.writeUTF(username+" isReady: "+Multiplayer.isReady);
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
