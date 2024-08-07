package bms.player.beatoraja.modmenu.multiplayer;

import bms.player.beatoraja.modmenu.multiplayer.*;

import java.util.ArrayList;

import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.modmenu.multiplayer.MultiplayerMenu;
import bms.player.beatoraja.song.SongData;

import java.io.*;
import java.net.*;

public class Multiplayer {
    // Client variables
    public static boolean inLobby = false;
    // Server variables
    public static SongData selectedSong;

    // Player Information
    public static String playerName = PlayerConfig.name.substring(0, Math.min(PlayerConfig.name.length(), 20));
    public static boolean isReady = false;
    public static boolean isHost = true;

    public static void hostLobby(){ // hostLobby is different from pressing the host button. must be compatitable for pressing the host button AND being transfered host.
        // set up server
        isHost = true;
        MultiplayerServer.hostLobby();
        MultiplayerClient.joinLobby();
    }

    public static void joinLobby(){
        // checks for connection
        // if connection succeeds, have host send info
        isHost = false;
        MultiplayerClient.joinLobby();
        MultiplayerMenu.statusText = "Failed to join lobby.";
    }

    public static void leaveLobby(){
        isReady = false;
        MultiplayerClient.closeSocket();
        MultiplayerServer.closeServerSocket();
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


}
