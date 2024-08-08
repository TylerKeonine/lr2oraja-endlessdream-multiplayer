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
    public static boolean inLobby = false;
    // Lobby Info
    public static String hostIp = "";
    public static SongData selectedSong;
    public static ArrayList<String> playerNames = new ArrayList<>();
    public static ArrayList<String> playerStates = new ArrayList<>();

    // Player Information
    public static String playerName = PlayerConfig.name.substring(0, Math.min(PlayerConfig.name.length(), 20));
    public static boolean isReady = false; // could completely remove isReady and replace with playerState
    public static boolean isHost = true;

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
        MultiplayerMenu.statusText = "Failed to join lobby.";
    }

    public static void leaveLobby(){
        isReady = false;
        MultiplayerClient.closeSocket();
        MultiplayerServer.closeServerSocket();
        MultiplayerMenu.statusText = "You have left the lobby.";
    }

    public static void transferHost(){
        // check if host
        // tell target client to hostLobby
        // copy over info to new host
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
            // send start message
        }
    }

    public static void selectSong(SongData song){
        if(isHost){
            MultiplayerClient.sendSong();
        }
    }

}
