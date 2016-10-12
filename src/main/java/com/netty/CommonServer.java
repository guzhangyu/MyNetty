package com.netty;

import com.netty.assist.SelectionKeys;
import com.netty.assist.SocketChannelArr;
import com.netty.assist.SocketChannels;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端公共类
 * Created by guzy on 16/9/20.
 */
public class CommonServer extends CommonWorker{

    Logger logger=Logger.getLogger(CommonServer.class);

    private SelectionKeys selectionKeys=new SelectionKeys();

    SocketChannelArr socketChannelArr=new SocketChannelArr();

    ServerSocketChannel serverSocketChannel;

    //SocketChannels channels=new SocketChannels();

    ConcurrentHashMap<String,Queue<Object>> toWriteMap=new ConcurrentHashMap<String, Queue<Object>>();

    ConcurrentHashMap<String,Queue<Object>> toWriteMap4Cha=new ConcurrentHashMap<String, Queue<Object>>();

    public CommonServer(int port,String name) throws IOException {

        super(name);

        logger.debug(String.format("serverName:%s",name));


        serverSocketChannel= ServerSocketChannel.open();
        channel=serverSocketChannel;
        serverSocketChannel.configureBlocking(false);

        ServerSocket serverSocket=serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(port));

        selector= Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        logger.debug("server start --- " + port);
    }

    void registerSelectionKey() throws ClosedChannelException {
//        if(!toWriteMap.isEmpty()){
//            for(String name:toWriteMap.keySet()){
//                SocketChannel socketChannel=channels.getChannel(name);
//                if(socketChannel!=null){
//                    socketChannel.register(selector, SelectionKey.OP_WRITE);
//                }else{
//                    logger.debug(name + "不存在 channel");
//                }
//            }
//        }
    }

    void handleFirstConnect(SelectionKey selectionKey,List<Object> results){
        if(selectionKeys.selectionKeyMap.containsValue(selectionKey)){
            logger.debug("already has selectionKey");
            return;
        }
        for(Object result:results){
            String res=new String((byte[])result);
            if(res.startsWith("MyName:")){
                String name=res.substring(7);
                logger.debug("client name:"+name);
                selectionKeys.addSelectionKey(name,selectionKey);
                return;
            }
        }
    }

    void handleConnect(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();

        final SocketChannel client = server.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);


        selectionKey.attach(ByteBuffer.allocate(1024));
       // channels.addChannel(client);
        final String clientName=client.socket().getInetAddress().getHostName();
        socketChannelArr.add(client);
      //  selectionKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        //selectionKeys.addSelectionKey(clientName,selectionKey);

        //write(clientName, "test server");
        writeContent(null,client,"test server");
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
    void handleKey(final SelectionKey selectionKey) throws IOException {
        handleKey(selectionKey, toWriteMap);
    }


    void handleKey(SelectionKey selectionKey,Map<String,Queue<Object>> map) throws IOException {
        if (selectionKey.isAcceptable()) {
            try {
                handleConnect(selectionKey);
            } catch (IOException e) {
                logger.error(e);
                e.printStackTrace();
            }
            return;
        }

        final SocketChannel channel = (SocketChannel) selectionKey.channel();

        if (selectionKey.isReadable()) {
            handleReadable(selectionKey, channel);
        }

//        if(selectionKey.isWritable()){
//            String clientName=channel.socket().getInetAddress().getHostName();
//            List<Object> toWrites=map.get(clientName);
//            if(toWrites!=null){
//                map.remove(clientName);
//                for(Object o:toWrites){
//                    writeContent(selectionKey,channel,o);
//                }
//                toWrites.clear();
//            }
//            channel.register(selector, SelectionKey.OP_READ);
//        }
    }

    void shutDown() throws IOException{
        //start();
    }


    public void shutDownReally() throws IOException {
        //System.out.println("shut down");
        logger.debug("shut down really");
        running=false;
        selector.close();
        channel.close();
    }

    private void addObjToMap(String name,Object o,Map<String,Queue<Object>> map){
        Queue<Object> toWrites=map.get(name);
        if(toWrites==null){
            toWrites=new ArrayBlockingQueue<Object>(100);
            map.putIfAbsent(name,toWrites);
        }
        toWrites.add(o);
    }

    public void write(String name,Object o) throws IOException {
       if(selectionKeys.containsKey(name)){
           addObjToMap(name,o,toWriteMap);
       }else{
           addObjToMap(name,o,toWriteMap4Cha);
       }

        handleNotWritten();
//        String clientName=channel.socket().getInetAddress().getHostName();
//        List<Object> toWrites=map.get(clientName);
//        if(toWrites!=null){
//            map.remove(clientName);
//            for(Object o:toWrites){
//                writeContent(selectionKey,channel,o);
//            }
//            toWrites.clear();
//        }
//        channel.register(selector, SelectionKey.OP_READ);
    }


    synchronized void handleNotWritten() {
        for(Map.Entry<String,Queue<Object>> toWrite:toWriteMap.entrySet()){
            Queue<Object> list=toWrite.getValue();
            if(list!=null && list.size()>0){
               // SocketChannel socketChannel=channels.getChannel(toWrite.getKey());
                SelectionKey key=selectionKeys.getSelectionKey(toWrite.getKey());
                if(key==null){
                    logger.error(String.format("%s selectionKey is null,%s",toWrite.getKey(),selectionKeys.selectionKeyMap));
                    continue;
                }
                SocketChannel socketChannel=(SocketChannel)key.channel();
                if(socketChannel!=null && socketChannel.isConnected()){
                    toWriteMap.remove(toWrite.getKey());
                    for(Object o1:list){
                        writeContent(key,socketChannel,o1);
                    }
                }
                list.clear();
            }
        }

        for(Map.Entry<String,Queue<Object>> toWrite:toWriteMap4Cha.entrySet()){
            Queue<Object> list=toWrite.getValue();
            if(list!=null && list.size()>0){
                // SocketChannel socketChannel=channels.getChannel(toWrite.getKey());
                Collection<SocketChannel> socketChannels=socketChannelArr.get(toWrite.getKey());
                if(socketChannels==null || socketChannels.size()==0){
                    logger.error(String.format("%s socketChannels is empty,%s",toWrite.getKey(),socketChannels));
                    continue;
                }

                toWriteMap4Cha.remove(toWrite.getKey());
               for(SocketChannel socketChannel:socketChannels){
                   if(socketChannel!=null && socketChannel.isConnected()){
                       for(Object o1:list){
                           writeContent(null,socketChannel,o1);
                       }
                   }
               }
                list.clear();
            }
        }
    }
}
