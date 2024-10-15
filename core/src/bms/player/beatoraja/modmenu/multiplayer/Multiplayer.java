package bms.player.beatoraja.modmenu.multiplayer;

import bms.player.beatoraja.modmenu.multiplayer.*;

import java.util.ArrayList;

import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.modmenu.multiplayer.MultiplayerMenu;
import bms.player.beatoraja.song.SongData;
import imgui.type.ImString;

import java.io.*;
import java.net.*;

public class Multiplayer {
    // Client variables
    public static String username = ""; // This is set in MainController.java
    public static boolean inLobby = false;
    public static boolean lobbyPlaying = false;
    // Lobby Info
    public static String hostIp = "";
    public static String selectedSong = "";
    public static String selectedSongTitle = "";
    public static ArrayList<String> playerNames = new ArrayList<>();
    public static ArrayList<Boolean> playerReady = new ArrayList<>();
    public static ArrayList<Boolean> playerHost = new ArrayList<>();
    public static ArrayList<Boolean> playerPlaying = new ArrayList<>();
    public static ArrayList<Boolean> playerMissing = new ArrayList<>();
    public static int[][] playerScoreData = new int[0][12];
    public static ArrayList<Boolean> playerLoaded = new ArrayList<>();

    // Player Information
    public static boolean isReady = false; // could completely remove isReady and replace with playerState
    public static boolean isHost = true;
    public static boolean isMissing = true;
    
    public static void hostLobby(){ // hostLobby is different from pressing the host button. must be compatitable for pressing the host button AND being transfered host.
        // set up server
        isHost = true;
        MultiplayerServer.hostLobby();
        MultiplayerClient.joinLobby(hostIp);
        MultiplayerClient.sendHost();
    }

    public static void joinLobby(){
        // check if not currently playing e.g. in song select
        // checks for connection
        // if connection succeeds, have host send info
        isHost = false;
        MultiplayerClient.joinLobby(MultiplayerMenu.ipInputText.get());
        MultiplayerMenu.statusText = "Failed to join lobby."; // You didn't actually fail if this runs but it should get overwritten if it join is successful
    }

    public static void leaveLobby(){
        isReady = false;
        MultiplayerClient.closeSocket();
        MultiplayerServer.closeServerSocket();
        MultiplayerMenu.statusText = "You have left the lobby.";
    }

    public static void transferHost(int target, Boolean switchto){
        if (Multiplayer.isHost){ // doesn't hurt to double check?
            MultiplayerClient.toggleHost(target,switchto);
        }
    }

    public static void readyPressed(){
        if (isReady){
            isReady = false;
        }else{
            isReady = true;
        }
        MultiplayerClient.sendReady();
    }

    public static void startPressed(){
        if(isHost){ 
            MultiplayerClient.sendStart();// send start message
        }
    }

    public static void selectSong(String md5, String title){
        if(isHost){
            MultiplayerClient.sendSong(md5, title);
        }
    }

    public static void endPressed(){
        if(isHost){ 
            MultiplayerClient.sendEnd();// send start message
        }
    }

    public static void sendLoaded(boolean isloaded){
        MultiplayerClient.sendLoaded(isloaded);
    }

}
