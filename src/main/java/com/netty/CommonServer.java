package com.netty;

import com.netty.assist.CommonUtils;
import com.netty.assist.SelectionKeys;
import com.netty.assist.SocketChannelArr;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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

    private SocketChannelArr socketChannelArr=new SocketChannelArr();

    /**
     * 主机-昵称的对应关系
     */
    ConcurrentHashMap<String,Queue<Object>> hostNickMap=new ConcurrentHashMap<String, Queue<Object>>();

    /**
     * 要通过selectionKey 昵称 写的内容
     */
    ConcurrentHashMap<String,Queue<Object>> toWriteMap=new ConcurrentHashMap<String, Queue<Object>>();

    /**
     * 要通过channel host 写的内容
     */
    ConcurrentHashMap<String,Queue<Object>> toWriteMap4Cha=new ConcurrentHashMap<String, Queue<Object>>();

    public CommonServer(int port,String name) throws IOException {

        super(name);

        logger.debug(String.format("serverName:%s",name));

        ServerSocketChannel serverSocketChannel= ServerSocketChannel.open();
        channel=serverSocketChannel;
        serverSocketChannel.configureBlocking(false);

        ServerSocket serverSocket=serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress(port));

        selector= Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        logger.debug("server start --- " + port);
    }

    void handleFirstConnect(SelectionKey selectionKey,List<Object> results){
        if(selectionKeys.containsValue(selectionKey)){
            logger.debug("already has selectionKey");
            return;
        }
        for(Object result:results){
            String res=new String((byte[])result);
            if(res.startsWith("MyName:")){
                String name=res.substring(7);
                addObjToMap(CommonUtils.getSocketName((SocketChannel)selectionKey.channel()),name,hostNickMap);
                logger.debug("client name:"+name);
                selectionKeys.addSelectionKey(name,selectionKey);
                return;//??
            }
        }
    }

    /**
     * 处理关闭事件
     * @param selectionKey
     */
    void handleClose(SelectionKey selectionKey){

        SocketChannel socketChannel=(SocketChannel)selectionKey.channel();
        logger.debug(String.format("before close:%s",socketChannelArr.getMap()));
        socketChannelArr.remove(socketChannel);
        logger.debug(String.format("after close:%s",socketChannelArr.getMap()));

        String name=CommonUtils.getSocketName(socketChannel);
        logger.debug(String.format("%s close",name));
        Queue nicks=hostNickMap.get(name);
        if(nicks!=null && nicks.size()>0){
            logger.debug(String.format("before selectionKeys:%s",selectionKeys.getMap()));
            for(Object nick:nicks){
                if(selectionKeys.getSelectionKey((String)nick).equals(selectionKey)){
                    selectionKeys.remove((String)nick);
                }
            }
            logger.debug(String.format("after selectionKeys:%s",selectionKeys.getMap()));
        }
    }
//
//    void handleClose(SelectionKey selectionKey){
//
//        SocketChannel socketChannel=(SocketChannel)selectionKey.channel();
//        logger.debug(String.format("before close:%d",socketChannelArr.size()));
//        socketChannelArr.remove(socketChannel);
//        logger.debug(String.format("after close:%d",socketChannelArr.size()));
//
//        String name=CommonUtils.getSocketName(socketChannel);
//        logger.debug(String.format("%s close",name));
//        Queue nicks=hostNickMap.get(name);
//        if(nicks!=null && nicks.size()>0){
//            logger.debug(String.format("before selectionKeys:%s",selectionKeys.getMap()));
//            for(Object nick:nicks){
//                selectionKeys.remove((String)nick);
//            }
//            logger.debug(String.format("after selectionKeys:%s",selectionKeys.getMap()));
//        }
//    }

    void handleConnect(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();

        final SocketChannel client = server.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE);


        selectionKey.attach(ByteBuffer.allocate(1024));
       // channels.addChannel(client);
       // final String clientName=client.socket().getInetAddress().getHostName();
        socketChannelArr.add(client);
      //  selectionKey.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        //selectionKeys.addSelectionKey(clientName,selectionKey);

        //write(clientName, "test server");
        writeContent(null,client,"服务端连接反馈！");
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
    }

    void shutDown(){
        start();
    }


    public void shutDownReally() throws IOException {
        logger.debug("shut down really");
        running=false;
        selector.close();
        channel.close();
    }

    private void addObjToMap(String name,Object o,Map<String,Queue<Object>> map){
        Queue<Object> toWrites=map.get(name);
        if(toWrites==null){
            toWrites=new ArrayBlockingQueue<Object>(100);
            Queue<Object> queue=map.putIfAbsent(name,toWrites);
            if(queue!=null){
                toWrites=queue;
            }
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
                    logger.error(String.format("%s selectionKey is null",toWrite.getKey()));
                    continue;
                }
                SocketChannel socketChannel=(SocketChannel)key.channel();
                if(socketChannel!=null && socketChannel.isConnected()){
                    toWriteMap.remove(toWrite.getKey());
                    for(Object o1:list){
                        writeContent(key,socketChannel,o1);
                    }
                }
               // list.clear();
            }
        }

        for(Map.Entry<String,Queue<Object>> toWrite:toWriteMap4Cha.entrySet()){
            Queue<Object> list=toWrite.getValue();
            if(list!=null && list.size()>0){
                Collection<SocketChannel> socketChannels=socketChannelArr.get(toWrite.getKey());
                if(socketChannels==null || socketChannels.size()==0){
                    logger.error(String.format("%s socketChannels is empty,%s",toWrite.getKey(),socketChannels));
                    continue;//此时如果remove就可以解决那种不断轮询这个主机的情况
                }

                toWriteMap4Cha.remove(toWrite.getKey());//先移除，保证不会在此时再被其他线程写入

               for(SocketChannel socketChannel:socketChannels){
                   if(socketChannel!=null && socketChannel.isConnected()){
                       for(Object o1:list){
                           writeContent(null,socketChannel,o1);
                       }
                   }
               }
                //list.clear();
            }
        }
    }
}
