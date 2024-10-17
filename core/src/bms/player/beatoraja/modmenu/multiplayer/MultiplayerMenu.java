package bms.player.beatoraja.modmenu.multiplayer;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowHeight;
import static bms.player.beatoraja.modmenu.ImGuiRenderer.windowWidth;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import imgui.ImColor;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;

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
                ImGui.text(Multiplayer.selectedSongTitle); // this needs to be capped
                
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
                        boolean hostClick = false;
                        if(Multiplayer.playerMissing.get(i)==true){
                            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(140,140,140));
                            ImGui.text("(M)");
                            hostClick = ImGui.isItemClicked();
                            ImGui.popStyleColor();
                            if (ImGui.isItemHovered()) {
                                if(Multiplayer.playerHost.get(i)==true){
                                    tooltip("Missing File (Host)");
                                }else if (Multiplayer.isHost == false){
                                    tooltip("Missing File");
                                }else{
                                    tooltip("Missing File (Click to grant host)");
                                }
                            }
                        }else if(Multiplayer.playerHost.get(i)==true&&i==Multiplayer.leaderIndex){//&&i==Multiplayer.leaderIndex
                            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(196,196,196));
                            ImGui.text("(H)");
                            hostClick = ImGui.isItemClicked();
                            ImGui.popStyleColor();
                            if (ImGui.isItemHovered()) {
                                tooltip("Host");
                            }
                        }else if(Multiplayer.playerReady.get(i)==true){
                            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(0,255,0));
                            ImGui.text("(R)");
                            hostClick = ImGui.isItemClicked();
                            ImGui.popStyleColor();
                            if (ImGui.isItemHovered()) {
                                if(Multiplayer.playerHost.get(i)==true){
                                    tooltip("Ready (Host)");
                                }else if (Multiplayer.isHost == false){
                                    tooltip("Ready");
                                }else{
                                    tooltip("Ready (Click to grant host)");
                                }
                            }                                    
                        }else{
                            ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(255,0,0));
                            ImGui.text("(N)");
                            hostClick = ImGui.isItemClicked();
                            ImGui.popStyleColor();
                            if (ImGui.isItemHovered()) {
                                if(Multiplayer.playerHost.get(i)==true){
                                    tooltip("Not Ready (Host)");
                                }else if (Multiplayer.isHost == false){
                                    tooltip("Not Ready");
                                }else{
                                    tooltip("Not Ready (Click to grant host)");
                                }
                            }                                    
                        }                                 
                        if(hostClick&&Multiplayer.isHost){
                            Multiplayer.transferHost(i, true);
                        }
                    }

                    ImGui.tableSetColumnIndex(2);
                    try{
                        switch(Multiplayer.playerScoreData[i][0]){
                            case(0):
                                ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(3, 211, 252));
                                ImGui.text("GREAT "+Multiplayer.playerScoreData[i][1]);
                                ImGui.popStyleColor();
                            break;
                            case(1):
                                ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(252, 227, 3));
                                ImGui.text("GREAT "+Multiplayer.playerScoreData[i][1]);
                                ImGui.popStyleColor();
                            break;
                            case(2):
                                ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(252, 227, 3));
                                ImGui.text("GOOD "+Multiplayer.playerScoreData[i][1]);
                                ImGui.popStyleColor();
                            break;
                            case(3):
                                ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(217, 0, 0));
                                ImGui.text("BAD "+Multiplayer.playerScoreData[i][1]);
                                ImGui.popStyleColor();
                            break;
                            case(4):
                                ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(217, 0, 0));
                                ImGui.text("POOR "+Multiplayer.playerScoreData[i][1]);
                                ImGui.popStyleColor();
                            break;
                            case(5):
                                ImGui.pushStyleColor(ImGuiCol.Text, ImColor.rgb(217, 0, 0));
                                ImGui.text("MISS "+Multiplayer.playerScoreData[i][1]);
                                ImGui.popStyleColor();
                            break;
                        }
                        ImGui.sameLine();
                        ImGui.text("SCORE: "+Integer.toString((Multiplayer.playerScoreData[i][2]*2+Multiplayer.playerScoreData[i][3]*2)+(Multiplayer.playerScoreData[i][4]+Multiplayer.playerScoreData[i][5])));
                        /* later feature
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
                        } */
                    }catch(IndexOutOfBoundsException e){
                        ImGui.text("Loading...");
                    }
                    ImGui.tableSetColumnIndex(3);
                    //ImGui.text("#1");                    
                }
                }catch(IndexOutOfBoundsException e){
                    ImGui.text("Loading...");
                }   
                ImGui.endTable();
            }
            ImGui.text(statusText); 
            ImGui.text(Multiplayer.username);
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
