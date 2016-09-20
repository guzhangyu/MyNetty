package com.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;

/**
 * Created by guzy on 16/9/20.
 */
public class CommonServer extends CommonWorker{

   // private SelectionKeys selectionKeys=new SelectionKeys();

    ServerSocketChannel serverSocketChannel;


    SocketChannels channels=new SocketChannels();

    public CommonServer(int port,String name) throws IOException {
        super(name);
        bossExecs= Executors.newFixedThreadPool(10);

        serverSocketChannel= ServerSocketChannel.open();
        channel=serverSocketChannel;
        serverSocketChannel.configureBlocking(false);

        ServerSocket serverSocket=serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(port));

        selector= Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("server start --- "+port);
    }

    @Override
    void handleConnect(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();

        final SocketChannel client = server.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        channels.addChannel(client);

        bossExecs.execute(new Runnable() {
            public void run() {
                String clientName=client.socket().getInetAddress().getHostName();
                try {
                    write(clientName,"test server");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // channel.register(selector, SelectionKey.OP_WRITE);
    }

    @Override
    void handleKey(SelectionKey selectionKey) throws IOException {
        final SocketChannel channel = (SocketChannel) selectionKey.channel();

//        if(selectionKey.isWritable()){
//            String channelName=channel.socket().getInetAddress().getHostAddress();
//            this.selectionKeys.addSelectionKey(channelName,selectionKey);
//        }

        if (selectionKey.isReadable()) {
            handleReadable(selectionKey, channel);
        }
    }

    public void write(String name,Object o) throws IOException {
        SocketChannel socketChannel=channels.getChannel(name);
        if(socketChannel!=null){
            writeContent(socketChannel,o);
        }else{
            System.out.println(name+"不存在 channel");
        }
//        SelectionKey selectionKey=selectionKeys.getSelectionKey(name);
//        if(selectionKey!=null){
//            writeContent((SocketChannel)selectionKey.channel(),o);
//        }else{
//            System.out.println(name+"不存在 selectionKey");
//        }
    }
}
