package com.netty.hander.impl;

import com.netty.hander.ContentHandler;

import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.List;

/**
 * Created by guzy on 2018-04-09.
 */
public class ReadLogHandler extends BaseContentHandler {

    @Override
    public Object read(AbstractSelectableChannel channel, Object o, List<Object> outs) {
        System.out.println( "接收数据--:" + new String((byte[]) o));
        return super.read(channel,o,outs);
    }
}
