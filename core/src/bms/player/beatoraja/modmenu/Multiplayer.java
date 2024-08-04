package bms.player.beatoraja.modmenu;

import java.util.ArrayList;

import bms.player.beatoraja.PlayerConfig;

import java.io.*;
import java.net.*;

public class Multiplayer {
    // Client variables
    public static boolean inLobby = false;
    public static ArrayList<String> playerListIp = new ArrayList<String>(); 
    // Server variables

    // Player Information
    public static String playerName = PlayerConfig.name.substring(0, Math.min(PlayerConfig.name.length(), 20));
    public static boolean isReady = false;
    public static boolean isHost = true;

    public static void hostLobby(){ // hostLobby is different from pressing the host button. must be compatitable for pressing the host button AND being transfered host.
        // set up server
        try {
            inLobby = true;
            isHost = true;
            ServerSocket serverSocket = new ServerSocket(64304);
            MultiplayerMenu.statusText = "Server started. Waiting for a client...";

            // Wait for a client to connect
            Socket clientSocket = serverSocket.accept(); // make more of these for more players
            MultiplayerMenu.statusText = "Client connected.";

            // Open input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Exchange data
            // Data from clients
            String message = in.readLine();
            MultiplayerMenu.statusText = ("Client: " + message);
            // Data to clients
            out.println("Hello from server!");

            // Close connections
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void joinLobby(){
        // checks for connection
        // if connection succeeds, have host send info
        try {
            inLobby = true;
            isHost = false;
            Socket socket = new Socket("localhost", 64304);
            MultiplayerMenu.statusText = "Connected to server.";

            // Open input and output streams
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Exchange data
            // Data to server
            out.println("Hello from client!");
            // Data from server
            String response = in.readLine();
            System.out.println("Server: " + response);

            // Close connections
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void leaveLobby(){
        transferHost();
        inLobby = false;
        isReady = false;
        // clear all lobby info
        playerListIp.clear();
    }

    public static void transferHost(){
        // check if host
        // tell target client to hostLobby
        // copy over info to new host
    }


}
