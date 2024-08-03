package bms.player.beatoraja.modmenu;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import static bms.player.beatoraja.modmenu.ImGuiRenderer.*;

import org.apache.commons.codec.binary.StringUtils;

public class MultiplayerMenu {

    public static ImBoolean MULTIPLAYER_ENABLED = new ImBoolean(false);

    public static void show(ImBoolean showMultiplayer) {
        float relativeX = windowWidth * 0.47f;
        float relativeY = windowHeight * 0.06f;
        ImGui.setNextWindowPos(relativeX, relativeY, ImGuiCond.FirstUseEver);

        boolean inLobby = false;
        String statusText = "";
        ImString ipInputText = new ImString("00.000.000.000:00000");
        ImString passwordInputText = new ImString(30);
        boolean isTyping = false; // to be used later to disable keybinds if true

        if(ImGui.begin("Multiplayer", showMultiplayer, ImGuiWindowFlags.AlwaysAutoResize)) {
            if(inLobby==false){
                // Connection GUI
                ImGui.text("Connect and play with others online.");
                ImGui.text("Enter an IP address or host a lobby.");

                if(ImGui.inputText("IP Address",ipInputText, 260)){
                    statusText = ipInputText.get();
                    isTyping = true;
                }else isTyping=false;

                if(ImGui.inputText("Password",passwordInputText, 260)){
                    statusText = passwordInputText.get();
                    isTyping = true;
                }else isTyping=false;

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
            ImGui.text(String.valueOf(isTyping)); 
        }
        ImGui.end();
    }




}
