package bms.player.beatoraja.modmenu;

import java.util.ArrayList;

import bms.player.beatoraja.PlayerConfig;

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
        
    }

    public static void joinLobby(){
        // checks for connection
        // if connection succeeds, have host send info
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
