package com.netty.assist;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by guzy on 16/10/12.
 */
public class SocketChannelArr {

    Map<String,List<SocketChannel>> map=new HashMap<String, List<SocketChannel>>();

    public void add(SocketChannel socketChannel){
        String name=socketChannel.socket().getInetAddress().getHostName();
        List<SocketChannel> list=map.get(name);
        if(list==null){
            list=new ArrayList<SocketChannel>();
            map.put(name,list);
        }

        if(list.contains(socketChannel)){
            return;
        }

        list.add(socketChannel);
    }

    public List<SocketChannel> get(String name){
        return map.get(name);
    }
}
