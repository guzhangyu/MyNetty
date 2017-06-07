package com.netty.assist;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务端的selectionKeys列表，与客户端socket一一对应
 * Created by guzy on 16/9/20.
 */
public class SelectionKeys {

    private Map<String,SelectionKey> selectionKeyMap=new HashMap<String, SelectionKey>();

    public void addSelectionKey(String name,SelectionKey selectionKey){
        selectionKeyMap.put(name,selectionKey);
    }

    public Boolean containsKey(String key){
        return selectionKeyMap.containsKey(key);
    }

    public Boolean containsValue(SelectionKey value){
        return selectionKeyMap.containsValue(value);
    }

    public void addSelectionKey(SelectionKey selectionKey){
        SocketChannel channel=(SocketChannel)selectionKey.channel();
        selectionKeyMap.put(CommonUtils.getSocketName(channel),selectionKey);
    }

    public Map<String,SelectionKey> getMap(){
        return selectionKeyMap;
    }

    public void remove(String key){
        selectionKeyMap.remove(key);
    }

    public SelectionKey getSelectionKey(String name){
        return selectionKeyMap.get(name);
    }
}
