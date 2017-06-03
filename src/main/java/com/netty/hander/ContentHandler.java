package com.netty.hander;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * 传递内容处理器
 * Created by guzy on 16/9/18.
 */
public interface ContentHandler {

    /**
     * 写内容处理器
     * @param attach 附件ByteBuffer
     * @param channel 频道
     * @param o 写的对象
     * @param outs 输出列表
     * @return
     */
    Object write(ByteBuffer attach,SocketChannel channel,Object o,List<Object> outs);

    /**
     * 读内容处理器
     * @param channel
     * @param o 读的对象
     * @param outs 输出列表
     * @return
     */
    Object read(SocketChannel channel,Object o,List<Object> outs);
}
