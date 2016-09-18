package com.netty.hander;

import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.List;

/**
 * 传递内容处理器
 * Created by guzy on 16/9/18.
 */
public interface ContentHandler {

    Object write(AbstractSelectableChannel channel,Object o,List<Object> outs);

    Object read(AbstractSelectableChannel channel,Object o,List<Object> outs);
}
