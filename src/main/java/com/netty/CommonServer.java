package com.netty;

import com.netty.assist.SimpleThreadExecutors;
import com.netty.assist.SocketChannels;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端公共类
 * Created by guzy on 16/9/20.
 */
public class CommonServer extends CommonWorker{

    Logger logger=Logger.getLogger(CommonServer.class);

   // private SelectionKeys selectionKeys=new SelectionKeys();

    ServerSocketChannel serverSocketChannel;

    SocketChannels channels=new SocketChannels();

    ConcurrentHashMap<String,List<Object>> toWriteMap=new ConcurrentHashMap<String, List<Object>>();

    public CommonServer(int port,String name) throws IOException {

        super(name);

        logger.debug(String.format("serverName:%s",name));

        //bossExecs= Executors.newFixedThreadPool(10);
        bossExecs=new SimpleThreadExecutors();

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

    void handleConnect(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();

        final SocketChannel client = server.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);

        selectionKey.attach(ByteBuffer.allocate(1024));
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

//    Map<String,List<Object>> filterAndCreateMap(Set<SelectionKey> selectionKeys){
//        Map<String,List<Object>> resultMap=new HashMap<String, List<Object>>();
//        for(SelectionKey selectionKey:selectionKeys){
//            if(selectionKey.isWritable()){
//                SocketChannel socketChannel=(SocketChannel)selectionKey.channel();
//                String clientName=socketChannel.socket().getInetAddress().getHostName();
//                if(toWriteMap.containsKey(clientName)){
//                    toWriteMap.remove(clientName);
//                    resultMap.put(clientName,toWriteMap.get(clientName));
//                }
//            }
//        }
//        return resultMap;
//    }



    @Override
    void handleKey(final SelectionKey selectionKey) {
        if (selectionKey.isAcceptable()) {
            try {
                handleConnect(selectionKey);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        bossExecs.execute(new Runnable() {
            public void run() {
                try {
                    //System.out.println(String.format("selectionKey isWritable:%s,isReadable:%s",selectionKey.isWritable(),selectionKey.isReadable()));
                    handleKey(selectionKey,toWriteMap);
                } catch (IOException ex) {
                    selectionKey.cancel();
                }
            }
        });
    }


    void handleKey(SelectionKey selectionKey,Map<String,List<Object>> map) throws IOException {
        final SocketChannel channel = (SocketChannel) selectionKey.channel();

        if (selectionKey.isReadable()) {
            handleReadable(selectionKey, channel);
        }
        if(selectionKey.isWritable()){
            String clientName=channel.socket().getInetAddress().getHostName();
            List<Object> toWrites=map.get(clientName);
            if(toWrites!=null){
                map.remove(clientName);
                for(Object o:toWrites){
                    writeContent(selectionKey,channel,o);
                }
                toWrites.clear();
            }
            channel.register(selector, SelectionKey.OP_READ);
        }
    }

    public void write(String name,Object o) throws IOException {
        List<Object> toWrites=toWriteMap.get(name);
        if(toWrites==null){
            toWrites=new ArrayList<Object>();
            toWriteMap.putIfAbsent(name,toWrites);
        }
        toWrites.add(o);
    }
}
