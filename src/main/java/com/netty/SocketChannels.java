package com.netty;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by guzy on 16/9/20.
 */
public class SocketChannels {

    Map<String,SocketChannel> socketChannelMap=new HashMap<String, SocketChannel>();

    public void addChannel(SocketChannel channel){
        String name=channel.socket().getInetAddress().getHostName();
        System.out.println("hostName:"+name);
        socketChannelMap.put(name,channel);
    }

    public SocketChannel getChannel(String name){
        return socketChannelMap.get(name);
    }
}
