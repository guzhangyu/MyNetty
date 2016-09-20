package com.netty;

import com.netty.hander.CompleteHandler;
import com.netty.hander.ContentHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by guzy on 16/9/18.
 */
public abstract class CommonWorker {

    protected Selector selector;

    CompleteHandler completeHandler;

    String name;

    private ExecutorService executorService;

    Executor bossExecs;

    private List<ContentHandler> contentHandlers=new ArrayList<ContentHandler>();

    SelectableChannel channel;

    public CommonWorker(String name){
        this.name=name;
        executorService= Executors.newFixedThreadPool(10);
    }

    public CompleteHandler getCompleteHandler() {
        return completeHandler;
    }

    public CommonWorker setCompleteHandler(CompleteHandler completeHandler) {
        this.completeHandler = completeHandler;
        return this;
    }

    public void addContentHandler(ContentHandler contentHandler){
        contentHandlers.add(contentHandler);
    }

    public void start()throws IOException {
       try{
           while(true){
               //registerSelectionKey();
               selector.select();
               final Set<SelectionKey> selectionKeys= selector.selectedKeys();
               for(final SelectionKey selectionKey:selectionKeys){
                   if (selectionKey.isAcceptable()) {
                        handleConnect(selectionKey);
                       continue;
                   }
                   bossExecs.execute(new Runnable() {
                       public void run() {
                           try {
                             //  System.out.println(String.format("selectionKey isWritable:%s,isReadable:%s",selectionKey.isWritable(),selectionKey.isReadable()));
                               handleKey(selectionKey);
                           } catch (IOException ex) {
                               selectionKey.cancel();

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
            executorService.execute(new Runnable() {
                public void run() {
                    List<Object> results = new ArrayList<Object>();
                    receiveBuffer.flip();
                    results.add(receiveBuffer);
                    for (ContentHandler handler : contentHandlers) {
                        List<Object> outs = new ArrayList<Object>();
                        Iterator resultItr = results.iterator();
                        while (resultItr.hasNext()) {
                            Object curResult = resultItr.next();
                            handler.read(channel, curResult, outs);
                        }
                        results = outs;
                    }
                    for (Object curResult : results) {
                        System.out.println(name + "接收数据--:" + new String((byte[]) curResult));
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
    void writeContent(final SocketChannel channel,final Object content) {
        executorService.submit(new Callable<List<Object>>() {
            List<Object> results = new ArrayList<Object>();

            public List<Object> call() {
                results.add(content);
                System.out.println(name + "发送数据 --:" + content);
                for (ContentHandler handler : contentHandlers) {
                    List<Object> outs = new ArrayList<Object>();
                    for (Object result : results) {
                        handler.write(channel, result, outs);
                    }
                    results = outs;
                }
                bossExecs.execute(new Runnable() {
                    public void run() {
                        for(Object result:results){
                            try {
                                writeContent(channel,(ByteBuffer)result);
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

}
