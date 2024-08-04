package bms.player.beatoraja.modmenu.multiplayer;

import bms.player.beatoraja.modmenu.multiplayer.*;

import java.util.ArrayList;

import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.modmenu.multiplayer.MultiplayerMenu;

import java.io.*;
import java.net.*;

public class Multiplayer {
    // Client variables
    public static boolean inLobby = false;
    // Server variables

    // Player Information
    public static String playerName = PlayerConfig.name.substring(0, Math.min(PlayerConfig.name.length(), 20));
    public static boolean isReady = false;
    public static boolean isHost = true;

    public static void hostLobby(){ // hostLobby is different from pressing the host button. must be compatitable for pressing the host button AND being transfered host.
        // set up server
        inLobby = true;
        isHost = true;
        MultiplayerServer.hostLobby();
        MultiplayerClient.joinLobby();
    }

    public static void joinLobby(){
        // checks for connection
        // if connection succeeds, have host send info
        inLobby = true;
        isHost = false;
        MultiplayerClient.joinLobby();
    }

    public static void leaveLobby(){
        inLobby = false;
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
