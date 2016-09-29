package com.netty;

import com.netty.handlers.HalfContentHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

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


        commonNIOServer
                .addContentHandler(new HalfContentHandler())
                .start();



//        try {
//            Thread.sleep(1000l);
//            System.exit(0);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}
