package bms.player.beatoraja.modmenu;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import java.util.ArrayList;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.*;

import bms.player.beatoraja.PlayerConfig;

public class MultiplayerMenu {

    public static ImBoolean MULTIPLAYER_ENABLED = new ImBoolean(false);

    // GUI variables
    private static String statusText = "";
    private static ImString ipInputText = new ImString(20);
    private static ImString passwordInputText = new ImString(25);
    private static boolean isTyping = false; // to be used later to disable keybinds if true
    // Client variables
    private static boolean inLobby = false;
    private static ArrayList<String> playerListIp = new ArrayList<String>(); 
    // Server variables

    // Player Information
    private static String playerName = PlayerConfig.name.substring(0, Math.min(PlayerConfig.name.length(), 20));
    private static boolean isReady = false;
    private static boolean isHost = true;

    public static void show(ImBoolean showMultiplayer) {
        float relativeX = windowWidth * 0.47f;
        float relativeY = windowHeight * 0.06f;
        ImGui.setNextWindowPos(relativeX, relativeY, ImGuiCond.FirstUseEver);

        if(ImGui.begin("Multiplayer", showMultiplayer, ImGuiWindowFlags.AlwaysAutoResize)) {
            if(inLobby==false){
                // Connection GUI
                ImGui.text("Connect and play with others online.");
                ImGui.text("Enter an IP address or host a lobby.");

                boolean ipInput = ImGui.inputText("IP Address",ipInputText, 260);
                boolean passInput = ImGui.inputText("Password",passwordInputText, 260);

                if(ipInput) statusText = ipInputText.get();
                if(passInput) statusText = passwordInputText.get();

                //if(ipInput||passInput) isTyping = true; else isTyping = false;
                
                if(ImGui.button("Join")) {
                    inLobby = true;
                    isHost = false;
                }

                ImGui.sameLine();

                if(ImGui.button("Host")) {
                    inLobby = true;
                    isHost = true;
                }

            }else{
                // Lobby GUI
                ImGui.text("00.000.000.000:00000");
                ImGui.sameLine();
                ImGui.text("PasswordPasswordPasswordP");
                ImGui.text("Artist - Song [Chart]"); // this needs to be capped
                ImGui.text("Random: 1234567");
                ImGui.sameLine();
                ImGui.text("Freq: 100%");
                
                if (isReady){
                    if(ImGui.button("Unready")) {
                        isReady = false;
                    }                    
                }else{
                    if(ImGui.button("Ready")) {
                        isReady = true;
                    }                      
                }

                ImGui.sameLine();

                if(ImGui.button("Leave")) {
                    leaveLobby();
                }

                if(isHost){
                    ImGui.sameLine();
                    if(ImGui.button("Transfer")) {
                        isHost = false;
                    }
                    // Kick button later?
                }
                // for loop for each player
                ImGui.text("Player 1");
                ImGui.sameLine();
                ImGui.text("(H)"); // have these hoverable to show text
                ImGui.sameLine();
                //  have these 2 colored gold silver etc depending on placement
                ImGui.text("EX: 1000");
                ImGui.sameLine();
                ImGui.text("#1");
                
            }
            ImGui.text(statusText); 
            ImGui.text(playerName); 
        }
        ImGui.end();
    }

    private static void hostLobby(){ // hostLobby is different from pressing the host button. must be compatitable for pressing the host button AND being transfered host.
        // set up server
        
    }

    private static void joinLobby(){
        // checks for connection
        // if connection succeeds, have host send info
    }

    private static void leaveLobby(){
        transferHost();
        inLobby = false;
        isReady = false;
        // clear all lobby info
        playerListIp.clear();
    }

    private static void transferHost(){
        // check if host
        // tell target client to hostLobby
        // copy over info to new host
    }

}
