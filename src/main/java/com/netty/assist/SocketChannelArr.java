package com.netty.assist;

import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 服务端缓存的 socket channel 列表，与连接的主机一一对应
 * Created by guzy on 16/10/12.
 */
public class SocketChannelArr {

    Map<String,Queue<SocketChannel>> map=new HashMap<String, Queue<SocketChannel>>();

    public void add(SocketChannel socketChannel){
        String name=CommonUtils.getSocketName(socketChannel);
        synchronized (map){
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
    }

    public int size(){
        return map.size();
    }

    public Map<String,Queue<SocketChannel>> getMap(){
        return map;
    }

    public void remove(SocketChannel socketChannel){
        String name=CommonUtils.getSocketName(socketChannel);

        synchronized (map){
            Queue<SocketChannel> list=map.get(name);
            if(list==null){
                return;
            }
            if(list.contains(socketChannel)){
                list.remove(socketChannel);
            }
            if(list.size()==0){
                map.remove(name);
            }
        }
    }

    public Collection<SocketChannel> get(String name){
        return map.get(name);
    }
}
