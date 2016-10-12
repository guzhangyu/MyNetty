package com.netty.assist;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by guzy on 16/9/20.
 */
public class SelectionKeys {

    public Map<String,SelectionKey> selectionKeyMap=new HashMap<String, SelectionKey>();

    public void addSelectionKey(String name,SelectionKey selectionKey){
        //SelectableChannel channel=selectionKey.channel();
        selectionKeyMap.put(name,selectionKey);
    }

    public Boolean containsKey(String key){
        return selectionKeyMap.containsKey(key);
    }

    public void addSelectionKey(SelectionKey selectionKey){
        SocketChannel channel=(SocketChannel)selectionKey.channel();
        selectionKeyMap.put(channel.socket().getInetAddress().getHostName(),selectionKey);
    }

    public SelectionKey getSelectionKey(String name){
        return selectionKeyMap.get(name);
    }
}
