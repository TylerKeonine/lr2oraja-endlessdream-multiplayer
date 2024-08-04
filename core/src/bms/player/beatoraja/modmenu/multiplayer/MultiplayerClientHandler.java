package bms.player.beatoraja.modmenu.multiplayer;

import java.net.Socket;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.IOException;

public class MultiplayerClientHandler implements Runnable{

    public static ArrayList<MultiplayerClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public MultiplayerClientHandler(Socket socket){
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("Server: "+clientUsername+" has entered the chat!");
        }catch(IOException e){
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    @Override
    public void run(){
        String messageFromClient;

        while(socket.isConnected()){
            try{
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            }catch(IOException e){
                closeEverything(socket,bufferedReader,bufferedWriter);
                break;
            }
        }
    }

    public void broadcastMessage(String messageToSend){
        for(MultiplayerClientHandler clientHandler : clientHandlers){
            try{
                clientHandler.bufferedWriter.write(messageToSend);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            }catch(IOException e){
                closeEverything(socket,bufferedReader,bufferedWriter);
            }
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("Server: "+clientUsername+" has left the chat!");
    }

    public void closeEverything(Socket socket,BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        try{
            if(bufferedReader!=null){
                bufferedReader.close();
            }
            if(bufferedWriter!=null){
                bufferedWriter.close();
            }
            if(socket!=null){
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}