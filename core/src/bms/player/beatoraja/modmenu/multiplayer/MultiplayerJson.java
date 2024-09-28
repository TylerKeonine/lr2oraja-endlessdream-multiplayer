package bms.player.beatoraja.modmenu.multiplayer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.bytedeco.ffmpeg.avformat.av_format_control_message;

public class MultiplayerJson {
    public static String addMessageType(String outMessage, String type){
        outMessage += "\"MessageType\":\"" + type + "\",";
        return outMessage;
    }

    public static String addMessageString(String outMessage, String name, String str){
        outMessage += '\"'+name+"\":\"" + str + "\",";
        return outMessage;
    }

    public static String addMessageInt(String outMessage, String name, int val){
        outMessage += '\"'+name+"\":\"" + val + "\",";
        return outMessage;
    }

    public static String addMessageBool(String outMessage, String name, Boolean val){
        // not sure how bool converts?
        outMessage += '\"'+name+"\":\"" + val + "\",";
        return outMessage;
    }

    public static String addMessageIntArray(String outMessage, String name, int arr[]){
        outMessage += '\"'+name+"\":[";
        for(int i=0;i<arr.length;i++){
            outMessage += arr[i] + ',';
        }
        outMessage = outMessage.substring(0,outMessage.length()-1)+"],";
        return outMessage;
    }

    public static String addMessageInt2dArray(String outMessage, String name, int arr[][]){
        outMessage += '\"'+name+"\":[";
        for(int i=0;i<arr.length;i++){
            outMessage += '[';
            for(int v=0;v<arr[i].length;v++){
                outMessage += arr[i][v] + ',';
            } 
            outMessage = outMessage.substring(0,outMessage.length()-1)+"],";
        }
        outMessage = outMessage.substring(0,outMessage.length()-1)+"],";
        return outMessage;
    }

    public static String addMessageStringArray(String outMessage, String name, String arr[]){
        outMessage += '\"'+name+"\":[";
        for(int i=0;i<arr.length;i++){
            outMessage += '\"' + arr[i] + "\",";
        }
        outMessage = outMessage.substring(0,outMessage.length()-1)+"],";
        return outMessage;
    }

    public static String addMessageBoolArray(String outMessage, String name, Boolean arr[]){
        outMessage += '\"'+name+"\":[";
        for(int i=0;i<arr.length;i++){
            if(arr[i]==true){
                outMessage += "true" + ',';
            }else{
                outMessage += "false" + ',';
            }
        }
        outMessage = outMessage.substring(0,outMessage.length()-1)+"],";
        return outMessage;
    }

    public static String sendMessage(String outMessage, DataOutputStream dataOutputStream){
        try{
            outMessage = outMessage.substring(0,outMessage.length()-1)+'}';
            dataOutputStream.writeUTF(outMessage);
            dataOutputStream.flush();
        }catch(IOException e){
            //closeEverything(socket, dataInputStream, dataOutputStream);
            e.printStackTrace();
        }
        outMessage = "{";
        return outMessage;
    }

    public static String readMessageString(String inMessage, String key){
        String str = "";
        int i = inMessage.indexOf(key)+key.length()+3; // +3 for the quote and semicolon
        while(inMessage.charAt(i)!='\"'){
            str += inMessage.charAt(i++);
        }
        return str;
    }
    public static ArrayList<String> readMessageStringList(String inMessage, String key){
        String str = "";
        ArrayList<String> list = new ArrayList<>();
        //"Array":["123","234","345"],
        int i = inMessage.indexOf(key)+key.length()+3; // +3 for the quote and semicolon
        while(inMessage.charAt(i)!=']'){
            if(inMessage.charAt(i)!='\"'){
                str += inMessage.charAt(i);
            }else{
                //str = "hi";
                //list.add(str);
                str = "";
                if(inMessage.charAt(i+1)==']'){
                    break;
                }else{
                    i=i+3;
                }
                i+=1;
            }
        }
        list.add("hi");
        return list;
    }
}
