package com.netty;

import com.netty.hander.CompleteHandler;
import com.netty.hander.ContentHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * server 与 client 的基类
 * Created by guzy on 16/9/18.
 */
public abstract class NettyImpl {
    String name;

    ExecutorService mainReactor,subReactor;

    CompleteHandler completeHandler;
    private List<ContentHandler> contentHandlers=new ArrayList<ContentHandler>();

    protected Selector selector;
    SelectableChannel channel;

    public NettyImpl(String name){
        this.name=name;
        subReactor = Executors.newFixedThreadPool(10);
    }

    public void start()throws IOException {
       try{
           while(true){
               //registerSelectionKey();
               selector.select();
               final Set<SelectionKey> selectionKeys= selector.selectedKeys();
               for(final SelectionKey selectionKey:selectionKeys){
                   try{
                     if (selectionKey.isAcceptable()) {
                        handleConnect(selectionKey);
                       continue;
                     }
                   } catch (CancelledKeyException ex) {
                       selectionKey.cancel();
                       ex.printStackTrace();
                       continue;
                   }
                   mainReactor.execute(new Runnable() {
                       public void run() {
                           try {
                               //  System.out.println(String.format("selectionKey isWritable:%s,isReadable:%s",selectionKey.isWritable(),selectionKey.isReadable()));
                               handleKey(selectionKey);
                           } catch (IOException | CancelledKeyException ex) {
                               selectionKey.cancel();
                               ex.printStackTrace();
                           }
                       }
                   });
               }
               selectionKeys.clear();
           }
       }finally {
           selector.close();
           channel.close();
       }
    }

    /**
     * 连接事件的处理
     * @param selectionKey
     * @throws IOException
     */
    abstract void handleConnect(SelectionKey selectionKey) throws IOException;

    /**
     * 对select到的key进行处理
     * @param selectionKey
     * @throws IOException
     */
    abstract void handleKey(final SelectionKey selectionKey) throws IOException;

    /**
     * 读事件的处理
     * @param selectionKey
     * @param channel
     * @throws IOException
     */
    void handleReadable(SelectionKey selectionKey, final SocketChannel channel) throws IOException {
        final ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);

        //读取数据
        int count = channel.read(receiveBuffer);
        if (count > 0) {
           // System.out.println("count:"+count);
            subReactor.execute(new Runnable() {
                public void run() {
                    List<Object> results = new ArrayList<Object>();
                    receiveBuffer.flip();
                    results.add(receiveBuffer);
                    for (ContentHandler handler : contentHandlers) {
                        List<Object> newResults = new ArrayList<Object>();
                        for (Object result : results) {
                            handler.read(channel, result, newResults);
                        }
                        results = newResults;
                    }
                }
            });
            //  channel.register(selector, SelectionKey.OP_WRITE);
        } else if (count < 0) {
            //对端链路关闭
            selectionKey.cancel();
            channel.close();
        } else {
            //读到0字节，忽略
        }
    }

    /**
     * 写内容
     * @param channel
     * @param content
     * @throws IOException
     */
    public void writeContent(final SocketChannel channel,final Object content) {
        subReactor.submit(new Callable<List<Object>>() {
            List<Object> results = new ArrayList<Object>();

            public List<Object> call() {
                results.add(content);
                for (ContentHandler handler : contentHandlers) {
                    List<Object> outs = new ArrayList<Object>();
                    for (Object result : results) {
                        handler.write(channel, result, outs);
                    }
                    results = outs;
                }
                mainReactor.execute(new Runnable() {
                    public void run() {
                        for (Object result : results) {
                            try {
                                writeContent(channel, (ByteBuffer) result);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                return results;
            }
        });

//                    ByteBuffer sendBuffer=ByteBuffer.allocate(BLOCK);
//                    sendBuffer.put(content.getBytes());
//                    sendBuffer.flip();
//                    //将缓冲区各标志位复位，因为向里面put了数据，标志被改变，想从中读取数据发向服务端，就要复位
//                    sendBuffer.flip();
//                    channel.write(sendBuffer);

        //channel.register(selector, SelectionKey.OP_READ);
    }

//    void writeContent(SocketChannel socketChannel,byte[]bytes) throws IOException {
//        ByteBuffer sendBuffer=ByteBuffer.allocate(bytes.length);
//        sendBuffer.put(bytes);
//        sendBuffer.flip();
//        socketChannel.write(sendBuffer);
//    }

    void writeContent(SocketChannel socketChannel,ByteBuffer sendBuffer) throws IOException {
        sendBuffer.flip();
        socketChannel.write(sendBuffer);
    }


    public CompleteHandler getCompleteHandler() {
        return completeHandler;
    }

    public NettyImpl setCompleteHandler(CompleteHandler completeHandler) {
        this.completeHandler = completeHandler;
        return this;
    }

    public NettyImpl addContentHandlers(ContentHandler... contentHandlers){
        for(ContentHandler contentHandler:contentHandlers){
            this.contentHandlers.add(contentHandler);
        }
        return this;
    }

}
