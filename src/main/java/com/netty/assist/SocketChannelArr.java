package com.netty.assist;

import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by guzy on 16/10/12.
 */
public class SocketChannelArr {

    Map<String,Queue<SocketChannel>> map=new HashMap<String, Queue<SocketChannel>>();

    public void add(SocketChannel socketChannel){
        String name=socketChannel.socket().getInetAddress().getHostName();
        Queue<SocketChannel> list=map.get(name);
        if(list==null){
            list=new ArrayBlockingQueue<SocketChannel>(100);
            map.put(name,list);
        }

        if(list.contains(socketChannel)){
            return;
        }

        list.add(socketChannel);
    }

    public Collection<SocketChannel> get(String name){
        return map.get(name);
    }
}
