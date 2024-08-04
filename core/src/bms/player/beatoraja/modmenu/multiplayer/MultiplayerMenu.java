package bms.player.beatoraja.modmenu.multiplayer;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import java.io.IOException;
import java.util.ArrayList;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.*;

import bms.player.beatoraja.PlayerConfig;

public class MultiplayerMenu {

    public static ImBoolean MULTIPLAYER_ENABLED = new ImBoolean(false);

    // GUI variables
    public static String statusText = "";
    private static ImString ipInputText = new ImString(20);
    private static ImString passwordInputText = new ImString(25);
    private static boolean isTyping = false; // to be used later to disable keybinds if true

    public static void show(ImBoolean showMultiplayer) {
        float relativeX = windowWidth * 0.47f;
        float relativeY = windowHeight * 0.06f;
        ImGui.setNextWindowPos(relativeX, relativeY, ImGuiCond.FirstUseEver);

        if(ImGui.begin("Multiplayer", showMultiplayer, ImGuiWindowFlags.AlwaysAutoResize)) {
            if(Multiplayer.inLobby==false){
                // Connection GUI
                ImGui.text("Connect and play with others online.");
                ImGui.text("Enter an IP address or host a lobby.");

                boolean ipInput = ImGui.inputText("IP Address",ipInputText, 260);
                // boolean passInput = ImGui.inputText("Password",passwordInputText, 260);

                if(ipInput) statusText = ipInputText.get();
                // if(passInput) statusText = passwordInputText.get();

                //if(ipInput||passInput) isTyping = true; else isTyping = false;
                
                if(ImGui.button("Join")) {
                    Multiplayer.joinLobby();
                }

                ImGui.sameLine();

                if(ImGui.button("Host")) {
                    Multiplayer.hostLobby();
                }

            }else{
                // Lobby GUI
                ImGui.text("00.000.000.000:00000");
                // ImGui.sameLine();
                // ImGui.text("PasswordPasswordPasswordP");
                ImGui.text("Artist - Song [Chart]"); // this needs to be capped
                ImGui.text("Random: 1234567");
                ImGui.sameLine();
                ImGui.text("Freq: 100%");
                
                if (Multiplayer.isReady){
                    if(ImGui.button("Unready")) {
                        Multiplayer.readyPressed();
                    }                    
                }else{
                    if(ImGui.button("Ready")) {
                        Multiplayer.readyPressed();
                    }                      
                }

                ImGui.sameLine();

                if(ImGui.button("Leave")) {
                    Multiplayer.leaveLobby();
                }

                if(Multiplayer.isHost){
                    ImGui.sameLine();
                    if(ImGui.button("Transfer")) {
                        Multiplayer.isHost = false;
                    }
                    // Kick button later?
                }
                // for loop for each player
                ImGui.text("Player 1");
                if(ImGui.isItemClicked()){
                    statusText = "player clicked";
                }
                if (ImGui.isItemHovered()) {
                    ImGui.beginTooltip();
                    ImGui.pushTextWrapPos(ImGui.getFontSize() * 35.0f);
                    ImGui.textUnformatted("00.000.000.000:00000");
                    ImGui.popTextWrapPos();
                    ImGui.endTooltip();
                }
                ImGui.sameLine();
                ImGui.textDisabled("(H)");
                if (ImGui.isItemHovered()) {
                    ImGui.beginTooltip();
                    ImGui.pushTextWrapPos(ImGui.getFontSize() * 35.0f);
                    ImGui.textUnformatted("Host");
                    ImGui.popTextWrapPos();
                    ImGui.endTooltip();
                }
                ImGui.sameLine();
                //  have these 2 colored gold silver etc depending on placement
                ImGui.text("EX: 1000");
                ImGui.sameLine();
                ImGui.text("#1");
                
            }
            ImGui.text(statusText); 
            ImGui.text(Multiplayer.playerName); 
        }
        ImGui.end();
    }

}
