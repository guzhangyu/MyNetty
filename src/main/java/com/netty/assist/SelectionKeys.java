package com.netty.assist;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 服务端的selectionKeys列表，与客户端socket一一对应
 * Created by guzy on 16/9/20.
 */
public class SelectionKeys {

    private Map<String,Queue<SelectionKey>> selectionKeyMap=new HashMap<String, Queue<SelectionKey>>();

    public void addSelectionKey(String name,SelectionKey selectionKey){
        Queue<SelectionKey> queue=selectionKeyMap.get(name);
        if(queue==null){
            queue=new ArrayBlockingQueue<SelectionKey>(100);
            selectionKeyMap.put(name,queue);
        }
        if(!queue.contains(selectionKey)){
            queue.add(selectionKey);
        }
    }

    public Boolean containsKey(String key){
        return selectionKeyMap.containsKey(key);
    }

    public Boolean containsValue(SelectionKey selectionKey){
        SocketChannel channel=(SocketChannel)selectionKey.channel();
        String name=CommonUtils.getSocketName(channel);
        Queue<SelectionKey> queue=selectionKeyMap.get(name);
        if(queue==null || queue.size()==0){
            return false;
        }
        return queue.contains(selectionKey);
    }

    public void addSelectionKey(SelectionKey selectionKey){
        SocketChannel channel=(SocketChannel)selectionKey.channel();
        addSelectionKey(CommonUtils.getSocketName(channel),selectionKey);
    }

    public Map<String,Queue<SelectionKey>> getMap(){
        return selectionKeyMap;
    }

    public void remove(SelectionKey selectionKey){
        SocketChannel channel=(SocketChannel)selectionKey.channel();
        String name=CommonUtils.getSocketName(channel);
        Queue<SelectionKey> queue=selectionKeyMap.get(name);
        if(queue==null ){
            return;
        }
        queue.remove(selectionKey);
        if(queue.size()==0){
            selectionKeyMap.remove(name);
        }
    }

    public void remove(String key){
        selectionKeyMap.remove(key);
    }

    public Queue<SelectionKey> getSelectionKey(String name){
        return selectionKeyMap.get(name);
    }
}
