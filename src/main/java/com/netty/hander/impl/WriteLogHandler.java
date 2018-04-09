package com.netty.hander.impl;

import com.netty.hander.ContentHandler;

import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.List;

/**
 * Created by guzy on 2018-04-09.
 */
public class WriteLogHandler extends BaseContentHandler  implements ContentHandler{


    @Override
    public Object write(AbstractSelectableChannel channel, Object o, List<Object> outs) {
        System.out.println(  "发送数据 --:" + o);//new String((byte[]) o)
        return super.write(channel,o,outs);
    }
}
