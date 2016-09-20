package com.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Created by guzy on 16/9/20.
 */
public class CommonServer extends CommonWorker{

   // private SelectionKeys selectionKeys=new SelectionKeys();

    ServerSocketChannel serverSocketChannel;


    SocketChannels channels=new SocketChannels();

    Map<String,List<Object>> toWriteMap=new ConcurrentHashMap<String, List<Object>>();

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

    void registerSelectionKey() throws ClosedChannelException {
        if(!toWriteMap.isEmpty()){
            for(String name:toWriteMap.keySet()){
                SocketChannel socketChannel=channels.getChannel(name);
                if(socketChannel!=null){
                    socketChannel.register(selector, SelectionKey.OP_WRITE);
                }else{
                    System.out.println(name+"不存在 channel");
                }
            }
        }
    }

    @Override
    void handleConnect(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();

        final SocketChannel client = server.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        channels.addChannel(client);
        final String clientName=client.socket().getInetAddress().getHostName();

        bossExecs.execute(new Runnable() {
            public void run() {
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
        if(selectionKey.isWritable()){
            String clientName=channel.socket().getInetAddress().getHostName();
            List<Object> toWrites=toWriteMap.get(clientName);
            for(Object o:toWrites){
                writeContent(channel,o);
            }
            toWriteMap.remove(clientName);
        }
    }

    public void write(String name,Object o) throws IOException {
        List<Object> toWrites=toWriteMap.get(name);
        if(toWrites==null){
            toWrites=new ArrayList<Object>();
            toWriteMap.put(name,toWrites);
        }
        toWrites.add(o);
//        SelectionKey selectionKey=selectionKeys.getSelectionKey(name);
//        if(selectionKey!=null){
//            writeContent((SocketChannel)selectionKey.channel(),o);
//        }else{
//            System.out.println(name+"不存在 selectionKey");
//        }
    }
}
