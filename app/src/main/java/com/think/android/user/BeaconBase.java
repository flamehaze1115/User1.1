package com.think.android.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.graphics.Color;


/**
 * Created by Think on 2016/11/28.
 */
public class BeaconBase implements Runnable {
    private Socket socket;
    private ServerSocket serverSocket;
    private int number;
    private MyApplication app;

    public BeaconBase(Socket socket, ServerSocket serverSocket,int number,MyApplication app){
        this.socket=socket;
        this.serverSocket=serverSocket;
        this.number=number;
        this.app=app;
    }
    @Override
    public void run() {
        try{
            serverSocket = new ServerSocket(number+2000);
            socket = serverSocket.accept();
            switch (number){
                case 1:app.connect1=true;break;
                case 2:app.connect2=true;break;
                case 3:app.connect3=true;break;
                case 4:app.connect4=true;break;
                case 5:app.connect5=true;break;
                case 6:app.connect6=true;break;
                case 7:app.connect7=true;break;
                case 8:app.connect8=true;break;
            }

            MainActivity.tv1[number].post(new Runnable(){
                public void run()
                {
                    MainActivity.tv1[number].setBackgroundColor(Color.GREEN);
                }
            });

            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
            while(true)
            {
                BufferedReader  bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line = bufferedReader.readLine();
                while(line!=null)
                {
                    if(line.equals("520"))
                    {
                        app.FeedbackStart = app.FeedbackStart +1;
                    }
                    if(line.equals("420"))
                    {
                        switch (number){
                            case 1:app.end1=true;break;
                            case 2:app.end2=true;break;
                            case 3:app.end3=true;break;
                            case 4:app.end4=true;break;
                            case 5:app.end5=true;break;
                            case 6:app.end6=true;break;
                            case 7:app.end7=true;break;
                            case 8:app.end8=true;break;
                        }
                        MainActivity.tv2[number].post(new Runnable(){
                            public void run(){
                                MainActivity.tv2[number].setBackgroundColor(Color.GREEN);
                            }
                        });
                    }
                    line = bufferedReader.readLine();
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
