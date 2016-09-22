package com.netty.assist;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by guzy on 16/9/20.
 */
public class SelectionKeys {

    private Map<String,SelectionKey> selectionKeyMap=new HashMap<String, SelectionKey>();

    public void addSelectionKey(String name,SelectionKey selectionKey){
        //SelectableChannel channel=selectionKey.channel();
        selectionKeyMap.put(name,selectionKey);
    }

    public SelectionKey getSelectionKey(String name){
        return selectionKeyMap.get(name);
    }
}
