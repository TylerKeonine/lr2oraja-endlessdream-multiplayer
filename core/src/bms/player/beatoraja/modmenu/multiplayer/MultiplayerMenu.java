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
                    if(Multiplayer.lobbyPlaying){
                        if(ImGui.button("Force End")) {
                            Multiplayer.endPressed();
                        }                          
                    }else{
                        if(ImGui.button("Start")) {
                            Multiplayer.startPressed();
                        }                              
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

                ImGui.beginTable("Players", 4);
                try{
                for(int i=0;i<Multiplayer.playerNames.size();i++){
                    ImGui.tableNextRow();
                    ImGui.tableSetColumnIndex(0);
                    ImGui.text(Multiplayer.playerNames.get(i));
                    ImGui.tableSetColumnIndex(1);
                    if(Multiplayer.lobbyPlaying){
                        if(Multiplayer.playerPlaying.get(i)){
                            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(0,150,0));
                            ImGui.text("(P)");
                            ImGui.popStyleColor();
                            if (ImGui.isItemHovered()) {
                                tooltip("Playing");
                            }                            
                        }else{
                            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(150,0,0));
                            ImGui.text("(F)");
                            ImGui.popStyleColor();
                            if (ImGui.isItemHovered()) {
                                tooltip("Finished");
                            }     
                        }
                    }else{
                        switch(Multiplayer.playerStates.get(i)){
                            case("Host"):
                                ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(196,196,196));
                                ImGui.text("(H)");
                                ImGui.popStyleColor();
                                if (ImGui.isItemHovered()) {
                                    tooltip("Host");
                                }
                            break;
                            case("Ready"):
                                ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(0,255,0));
                                ImGui.text("(R)");
                                ImGui.popStyleColor();
                                if (ImGui.isItemHovered()) {
                                    tooltip("Ready");
                                }
                            break;
                            case("Not Ready"):
                                ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(255,0,0));
                                ImGui.text("(N)");
                                ImGui.popStyleColor();
                                if (ImGui.isItemHovered()) {
                                    tooltip("Not Ready");
                                }
                            break;

                            default:
                                ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(255,255,255));
                                ImGui.text("(?)");
                                ImGui.popStyleColor();
                                if (ImGui.isItemHovered()) {
                                    tooltip("Unknown");
                                }
                        }          
                    }

                    ImGui.tableSetColumnIndex(2);
                    try{
                        ImGui.text("SCORE: "+Integer.toString((Multiplayer.playerScoreData[i][1]*2+Multiplayer.playerScoreData[i][2]*2)+(Multiplayer.playerScoreData[i][3]+Multiplayer.playerScoreData[i][4])));
                        if(ImGui.isItemHovered()){
                            ImGui.beginTooltip();
                            ImGui.pushTextWrapPos(ImGui.getFontSize() * 35.0f);
                            ImGui.beginTable(Multiplayer.playerNames.get(i), 4);
                            ImGui.tableNextRow();
                            ImGui.tableSetColumnIndex(0);
                            ImGui.text(Multiplayer.playerNames.get(i));
                            ImGui.tableSetColumnIndex(1);
                            ImGui.text("SCORE: 0");
                            ImGui.tableSetColumnIndex(2);
                            ImGui.text("MISS COUNT: 0");
                            ImGui.tableSetColumnIndex(3);
                            ImGui.text("MAX COMBO: 0");

                            ImGui.tableNextRow();
                            ImGui.tableSetColumnIndex(0);
                            ImGui.text("PERFECT:");
                            ImGui.tableSetColumnIndex(1);
                            ImGui.text("0");
                            ImGui.tableSetColumnIndex(2);
                            ImGui.text("EARLY: 0");
                            ImGui.tableSetColumnIndex(3);
                            ImGui.text("LATE: 0");

                            ImGui.tableNextRow();
                            ImGui.tableSetColumnIndex(0);
                            ImGui.text("GREAT:");
                            ImGui.tableSetColumnIndex(1);
                            ImGui.text("0");
                            ImGui.tableSetColumnIndex(2);
                            ImGui.text("EARLY: 0");
                            ImGui.tableSetColumnIndex(3);
                            ImGui.text("LATE: 0");

                            ImGui.tableNextRow();
                            ImGui.tableSetColumnIndex(0);
                            ImGui.text("GOOD:");
                            ImGui.tableSetColumnIndex(1);
                            ImGui.text("0");
                            ImGui.tableSetColumnIndex(2);
                            ImGui.text("EARLY: 0");
                            ImGui.tableSetColumnIndex(3);
                            ImGui.text("LATE: 0");

                            ImGui.tableNextRow();
                            ImGui.tableSetColumnIndex(0);
                            ImGui.text("BAD:");
                            ImGui.tableSetColumnIndex(1);
                            ImGui.text("0");
                            ImGui.tableSetColumnIndex(2);
                            ImGui.text("EARLY: 0");
                            ImGui.tableSetColumnIndex(3);
                            ImGui.text("LATE: 0");

                            ImGui.tableNextRow();
                            ImGui.tableSetColumnIndex(0);
                            ImGui.text("POOR:");
                            ImGui.tableSetColumnIndex(1);
                            ImGui.text("0");
                            ImGui.tableSetColumnIndex(2);
                            ImGui.text("EARLY: 0");
                            ImGui.tableSetColumnIndex(3);
                            ImGui.text("LATE: 0");

                            ImGui.tableNextRow();
                            ImGui.tableSetColumnIndex(0);
                            ImGui.text("MISS:");
                            ImGui.tableSetColumnIndex(1);
                            ImGui.text("0");
                            ImGui.tableSetColumnIndex(2);
                            ImGui.text("EARLY: 0");
                            ImGui.tableSetColumnIndex(3);
                            ImGui.text("LATE: 0");

                            ImGui.tableNextRow();
                            ImGui.tableSetColumnIndex(2);
                            ImGui.text("EARLY: 0");
                            ImGui.tableSetColumnIndex(3);
                            ImGui.text("LATE: 0");

                            ImGui.endTable();
                            ImGui.popTextWrapPos();
                            ImGui.endTooltip();
                        } 
                    }catch(IndexOutOfBoundsException e){
                        ImGui.text("Loading...");
                    }
                    ImGui.tableSetColumnIndex(3);
                    ImGui.text("#1");                    
                }
                }catch(IndexOutOfBoundsException e){
                    ImGui.text("Loading...");
                }   
                ImGui.endTable();
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
