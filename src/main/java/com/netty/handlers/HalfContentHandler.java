package com.netty.handlers;

import com.netty.hander.ContentHandler;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.List;

/**
 * 半包、粘包处理器
 * Created by guzy on 16/9/18.
 */
public class HalfContentHandler implements ContentHandler {

    Logger logger=Logger.getLogger(HalfContentHandler.class);


    public Object write(ByteBuffer attach,SocketChannel channel, Object o, List<Object> outs) {
        byte[] result=((String)o).getBytes();

        ByteBuffer buf=attach==null?ByteBuffer.allocate(result.length+4):attach;
        buf.putInt(result.length);
        buf.put(result);

        logger.debug(String.format("before write,outs:%d",outs.size()));
        outs.add(buf);
        logger.debug(String.format("after write,outs:%d",outs.size()));

        return result;
    }

    public Object read(SocketChannel channel, Object o, List<Object> outs) {
        ByteBuffer byteBuffer=(ByteBuffer)o;
        int len=0;
        int curLen=0;
        logger.debug(String.format("before read,outs:%d",outs.size()));
        do{
            //读取长度
            curLen+=4;
           // byteBuffer.flip();
            len=byteBuffer.getInt();
            //logger.debug(len);

            //读取内容
            byte[] arr=new byte[len];
            byteBuffer.position(curLen);
            byteBuffer.get(arr,0,len);

            outs.add(arr);

            curLen+=len;
        }while(curLen<byteBuffer.limit());

        logger.debug(String.format("after read,outs:%d",outs.size()));
        return null;
    }
}
