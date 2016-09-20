package com.netty;

import java.nio.channels.SocketChannel;

/**
 * Created by guzy on 16/9/20.
 */
public class WriteInfo {

    /**
     * 要写的客户端
     */
    private SocketChannel channel;

    /**
     * 要写的内容
     */
    private Object toWrite;

    public WriteInfo(SocketChannel channel,Object toWrite) {
        this.toWrite = toWrite;
        this.channel = channel;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public WriteInfo setChannel(SocketChannel channel) {
        this.channel = channel;
        return this;
    }

    public Object getToWrite() {
        return toWrite;
    }

    public WriteInfo setToWrite(Object toWrite) {
        this.toWrite = toWrite;
        return this;
    }
}
