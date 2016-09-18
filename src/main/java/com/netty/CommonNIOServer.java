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
public class CommonNIOServer extends CommonServer{

    public CommonNIOServer(int port,String name)throws IOException {
        super(name);
        ServerSocketChannel serverSocketChannel= ServerSocketChannel.open();
        channel=serverSocketChannel;
        serverSocketChannel.configureBlocking(false);

        ServerSocket serverSocket=serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(port));

        selector=Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("server start --- "+port);
    }


    public static void main(String[] args) throws IOException{
        int port = 8888;
        CommonNIOServer server=new CommonNIOServer(port,"server");
        server.addContentHandler(new HalfContentHandler());
        server.start();
    }
}
