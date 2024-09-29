package bms.player.beatoraja.modmenu.multiplayer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

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
                outMessage += Integer.toString(arr[i][v]) + ',';
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
            if(arr[i].equals(true)){
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
        int i = inMessage.indexOf('\"'+key+'\"')+key.length()+4; // +4 for the quote and colon
        while(inMessage.charAt(i)!='\"'){
            str += inMessage.charAt(i++);
        }
        return str;
    }
    public static String[] readMessageStringArray(String inMessage, String key){
        // find array size
        int i = inMessage.indexOf('\"'+key+'\"')+key.length()+2; // +2 quotes
        int size = 0;
        while(inMessage.charAt(i)!=']'&&i<inMessage.length()-1){
            if(inMessage.charAt(i)=='\"'){
                size++;
            }
            i++;
        }
        size/=2;
        // read to array
        int end = i;
        String str = "";
        String arr[] = new String[size];
        int element = 0;
        i = inMessage.indexOf('\"'+key+'\"')+key.length()+5; // +5 for the quote, colon, bracket
        while(i<=end&&element<size){
            if(inMessage.charAt(i)!='\"'){
                str += inMessage.charAt(i++);
            }else{
                arr[element++] = str;
                str = "";
                if(inMessage.charAt(i+1)==']'){
                    break;
                }else{
                    i=i+2;
                }
                i++;
            }
        }
        return arr;
    }

    public static Boolean[] readMessageBoolArray(String inMessage, String key){
        //"Array":[true,false],
        // find array size
        int i = inMessage.indexOf('\"'+key+'\"')+key.length()+3; // +3 quotes and colon
        int size = 0;
        while(inMessage.charAt(i)!=']'&&i<inMessage.length()-1){
            if((inMessage.charAt(i)=='['&&inMessage.charAt(i+1)!=']')||inMessage.charAt(i)==','){
                size++;
            }
            i++;            
        }
        // read to array
        int end = i;
        Boolean arr[] = new Boolean[size];
        int element = 0;
        i = inMessage.indexOf('\"'+key+'\"')+key.length()+3; // +5 for the quote, colon, bracket
        while(i<=end&&element<size){
            if(inMessage.charAt(i)==','||inMessage.charAt(i)=='['){
                if(inMessage.charAt(i+1)=='t'||inMessage.charAt(i+1)=='T'){
                    arr[element++]=true;
                    i+=5;
                }else{
                    arr[element++]=false;
                    i+=6;
                }
                
            }else{
                if(inMessage.charAt(i+1)==']'){
                    break;
                }
                i++;
            }
        }
        return arr;
    }

    public static int[][] readMessageInt2dArray(String inMessage, String key){
        //"key":[[1,2,3],[4,5,6],[7,8,9]],
        //"key":[[1,2,3]]
        // find array size
        int i = inMessage.indexOf('\"'+key+'\"')+key.length()+4; // +2 quotes
        int sizex = 0;
        int sizey = 0;
        while((inMessage.charAt(i)!=']'||inMessage.charAt(i+1)!=']')&&i<inMessage.length()-1){
            if(inMessage.charAt(i)=='['){
                sizex++;
            }
            if(inMessage.charAt(i)==','||inMessage.charAt(i)==']'){
                sizey++;
            }
            i++;
        }
        if(inMessage.charAt(i-1)!='['){
            sizey++;
        }
        sizey/=sizex;
        // read to array
        int end = i;
        String str = "";
        int arr[][] = new int[sizex][sizey];
        int temp[] = new int[sizey];
        i = inMessage.indexOf('\"'+key+'\"')+key.length()+5; // +5 for the quote, colon, bracket
        for(int x=0;x<sizex;x++){
            for(int y=0;y<sizey;y++){
                str = "";
                while(inMessage.charAt(i)!=','&&inMessage.charAt(i)!=']'&&i<=end){
                    str += inMessage.charAt(i++);
                }
                if(inMessage.charAt(i)==']'){
                    break;
                }
                i++;
                temp[y] = Integer.parseInt(str);
            }
            i+=3;
            arr[x] = temp;
                str = "";
            temp = new int[sizey];
        }
        return arr;
    }
}
