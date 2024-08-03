package bms.player.beatoraja.modmenu;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.*;

import bms.player.beatoraja.PlayerConfig;

public class MultiplayerMenu {

    public static ImBoolean MULTIPLAYER_ENABLED = new ImBoolean(false);

    private static boolean inLobby = false;
    private static String statusText = "";
    private static ImString ipInputText = new ImString(20);
    private static ImString passwordInputText = new ImString(25);
    private static boolean isTyping = false; // to be used later to disable keybinds if true
    private static String playerName = PlayerConfig.name.substring(0, Math.min(PlayerConfig.name.length(), 20));;

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
                }

                ImGui.sameLine();

                if(ImGui.button("Host")) {
                    inLobby = true;
                }

            }else{
                // Lobby GUI
                ImGui.text("LOBBY saofkjdsaklofjdsa kofjdsa  ");
                ImGui.text("Text 2 fdasohf sdaijohfjoasidh foas ");     
                if(ImGui.button("Leave")) {
                    inLobby = false;
                }           
            }
            ImGui.text(statusText); 
            ImGui.text(playerName); 
        }
        ImGui.end();
    }




}
