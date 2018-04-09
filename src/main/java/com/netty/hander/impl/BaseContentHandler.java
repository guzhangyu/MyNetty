package com.netty.hander.impl;

import com.netty.hander.ContentHandler;

import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.List;

/**
 * Created by guzy on 2018-04-09.
 */
public class BaseContentHandler implements ContentHandler {

    @Override
    public Object write(AbstractSelectableChannel channel, Object o, List<Object> outs) {
        outs.add(o);
        return o;
    }

    @Override
    public Object read(AbstractSelectableChannel channel, Object o, List<Object> outs) {
        outs.add(o);
        return o;
    }
}
