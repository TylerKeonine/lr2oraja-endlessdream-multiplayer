package bms.player.beatoraja.modmenu.multiplayer;

import imgui.ImColor;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import java.io.IOException;
import java.util.ArrayList;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.*;

import bms.player.beatoraja.MainController;
import bms.player.beatoraja.PlayerConfig;

public class MultiplayerMenu {

    public static ImBoolean MULTIPLAYER_ENABLED = new ImBoolean(false);

    // GUI variables
    public static String statusText = "";
    public static ImString ipInputText = new ImString(20);
    private static boolean isTyping = false; // to be used later to disable keybinds if true
    private static String copytext = "Click to copy me!";

    private static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public static void show(ImBoolean showMultiplayer) {
        float relativeX = windowWidth * 0.47f;
        float relativeY = windowHeight * 0.06f;
        ImGui.setNextWindowPos(relativeX, relativeY, ImGuiCond.FirstUseEver);

        if(ImGui.begin("Multiplayer", showMultiplayer, ImGuiWindowFlags.AlwaysAutoResize)) {
            if(Multiplayer.inLobby==false){
                // Connection GUI
                ImGui.text("Connect and play with others online.");
                ImGui.text("Enter an IP address or host a lobby.");

                ImGui.inputText("IP Address",ipInputText, 260);
                
                if(ImGui.button("Join")) {
                    Multiplayer.joinLobby();
                }

                ImGui.sameLine();

                if(ImGui.button("Host")) {
                    Multiplayer.hostLobby();
                }

            }else{
                // Lobby GUI
                ImGui.text("Lobby: "+Multiplayer.hostIp);
                if (ImGui.isItemClicked()){
                    StringSelection strsel = new StringSelection(Multiplayer.hostIp);
                    clipboard.setContents(strsel, null);
                    copytext = "Copied!";
                }
                if(ImGui.isItemHovered()){
                    tooltip(copytext);
                }else{
                    copytext = "Click to copy me!";
                }
                ImGui.text(Multiplayer.selectedSong); // this needs to be capped
                
                if (Multiplayer.isHost){
                    if(ImGui.button("Start")) {
                        Multiplayer.startPressed();
                    }      
                }else if(Multiplayer.isReady){
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

                // for loop for each player
                for(int i=0;i<Multiplayer.playerNames.size();i++){
                    ImGui.text(Multiplayer.playerNames.get(i));
                    if(ImGui.isItemClicked()){
                        //statusText = "player clicked";
                    }
                    ImGui.sameLine();
                    switch(Multiplayer.playerStates.get(i)){
                        case("Host"):
                            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(196,196,196));
                            ImGui.text("(H)");
                            ImGui.popStyleColor();
                            if (ImGui.isItemHovered()) {
                                tooltip("Host");
                            }
                            ImGui.sameLine();
                        break;
                        case("Ready"):
                            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(0,255,0));
                            ImGui.text("(R)");
                            ImGui.popStyleColor();
                            if (ImGui.isItemHovered()) {
                                tooltip("Ready");
                            }
                            ImGui.sameLine();
                        break;
                        case("Not Ready"):
                            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(255,0,0));
                            ImGui.text("(N)");
                            ImGui.popStyleColor();
                            if (ImGui.isItemHovered()) {
                                tooltip("Not Ready");
                            }
                            ImGui.sameLine();
                        break;

                        default:
                            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(255,255,255));
                            ImGui.text("(?)");
                            ImGui.popStyleColor();
                            if (ImGui.isItemHovered()) {
                                tooltip("Unknown");
                            }
                            ImGui.sameLine();
                    }

                    //  have these 2 colored gold silver etc depending on placement
                    ImGui.text("EX: 1000");
                    ImGui.sameLine();
                    ImGui.text("#1");                    
                }

            }
            ImGui.text(statusText); 
            ImGui.text(Multiplayer.playerName); 
        }
        ImGui.end();
    }

    public static void tooltip(String text){
        ImGui.beginTooltip();
        ImGui.pushTextWrapPos(ImGui.getFontSize() * 35.0f);
        ImGui.textUnformatted(text);
        ImGui.popTextWrapPos();
        ImGui.endTooltip();
    }

}
