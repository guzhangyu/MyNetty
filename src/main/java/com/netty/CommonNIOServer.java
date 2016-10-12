package com.netty;

import com.netty.assist.HandleStr;
import com.netty.assist.ReadInput;
import com.netty.handlers.HalfContentHandler;

import java.io.IOException;

/**
 * nio 服务类
 * Created by guzy on 16/9/18.
 */
public class CommonNIOServer extends CommonServer {


    public CommonNIOServer(int port, String name) throws IOException {
        super(port, name);
    }

    public static void main(String[] args) throws IOException{
        int port = 8888;
        final CommonNIOServer commonNIOServer=new CommonNIOServer(port,"server");
        commonNIOServer.addContentHandler(new HalfContentHandler());

//        new Thread(new Runnable() {
//            public void run() {
//                try {
//
//                    try {
//                        Thread.sleep(7000l);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    System.out.println("begin write ");
//                    commonNIOServer.write("localhost","test_");
//                    commonNIOServer.write("localhost","test_1");
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

        new Thread(new Runnable() {
            public void run() {
                new ReadInput().read(new HandleStr() {
                    public void handleStr(String str) throws Exception {
                      try{
                          String strs[]=str.split(" ");
                          commonNIOServer.write(strs[0],strs[1]);
                          System.out.println(str);
                      }catch (Exception e){
                          e.printStackTrace();
                      }
                    }
                });
            }
        }).start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                try {
                    //System.out.println("test code");
                    commonNIOServer.shutDownReally();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        commonNIOServer.start();
    }
}
