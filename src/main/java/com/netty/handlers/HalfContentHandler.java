package com.netty.handlers;

import com.netty.hander.ContentHandler;

import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.List;

/**
 * 半包、粘包处理器
 * Created by guzy on 16/9/18.
 */
public class HalfContentHandler implements ContentHandler {


    public Object write(AbstractSelectableChannel channel, Object o, List<Object> outs) {
        byte[] result=((String)o).getBytes();
        ByteBuffer buf=ByteBuffer.allocate(result.length+4);
        buf.putInt(result.length);
        buf.put(result);
        outs.add(buf);
        return result;
    }

    public Object read(AbstractSelectableChannel channel, Object o, List<Object> outs) {
        ByteBuffer byteBuffer=(ByteBuffer)o;
        int len=0;
        int curLen=0;
        do{
            curLen+=4;
           // byteBuffer.flip();
            len=byteBuffer.getInt();
            System.out.println(len);
            byte[] arr=new byte[len];
            byteBuffer.position(curLen);
            byteBuffer.get(arr,0,len);
            outs.add(arr);

            curLen+=len;
        }while(curLen<byteBuffer.limit());

        return null;
    }
}
