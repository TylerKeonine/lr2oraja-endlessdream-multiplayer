package bms.player.beatoraja.modmenu.multiplayer;

import java.io.DataOutputStream;
import java.io.IOException;

public class MultiplayerJson {
    public static String addMessageType(String outMessage, String type){
        outMessage += "\"MessageType\":\"" + type + "\",";
        return outMessage;
    }

    public static String addMessageString(String outMessage, String name, String str){
        outMessage += '\"'+name+"\":\"" + str + "\",";
        return outMessage;
    }

    public String addMessageIntArray(String outMessage, String name, int arr[]){
        outMessage += '\"'+name+"\":[";
        for(int i=0;i<arr.length;i++){
            outMessage += arr[i] + ',';
        }
        outMessage += "],";
        return outMessage;
    }

    public static void sendMessage(String outMessage, DataOutputStream dataOutputStream){
        try{
            outMessage += '}';
            dataOutputStream.writeUTF(outMessage);
            dataOutputStream.flush();
            outMessage = "{";
        }catch(IOException e){
            //closeEverything(socket, dataInputStream, dataOutputStream);
            e.printStackTrace();
        }
    }

    public static String readMessageString(String inMessage, String key){
        String str = "";
        int i = inMessage.indexOf(key)+key.length()+3; // +2 for the quote and semicolon
        while(inMessage.charAt(i)!='\"'){
            str += inMessage.charAt(i++);
        }
        return str;
    }
}
