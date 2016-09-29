package com.netty;

import com.netty.hander.CompleteHandler;
import com.netty.hander.ContentHandler;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * server 与 client 的基类
 * Created by guzy on 16/9/18.
 */
public abstract class CommonWorker {

    Logger logger=Logger.getLogger(CommonWorker.class);

    protected Selector selector;

    /**
     * 连接完成的处理器
     */
    CompleteHandler completeHandler;

    String name;

    //worker 线程，用来处理数据内容
    private ExecutorService executorService;

    Executor bossExecs;

    /**
     * 内容处理链
     */
    private List<ContentHandler> contentHandlers=new ArrayList<ContentHandler>();

    /**
     * 主 channel
     */
    SelectableChannel channel;

    public CommonWorker(String name){
        this.name=name;
        executorService= Executors.newFixedThreadPool(10);
    }

    public CommonWorker setCompleteHandler(CompleteHandler completeHandler) {
        this.completeHandler = completeHandler;
        return this;
    }

    public CommonWorker addContentHandler(ContentHandler contentHandler){
        contentHandlers.add(contentHandler);
        return this;
    }


    /**
     * 读事件的处理
     * @param selectionKey
     * @param channel
     * @throws IOException
     */
    void handleReadable(SelectionKey selectionKey, final SocketChannel channel) throws IOException {
        //TODO:扩容，并发
        final ByteBuffer receiveBuffer = selectionKey.attachment()==null?ByteBuffer.allocate(1024):(ByteBuffer)selectionKey.attachment();

        //读取数据
        int count = channel.read(receiveBuffer);
        if (count > 0) {
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
                        logger.debug(name + "接收数据--:" + new String((byte[]) curResult));
                    }
                }
            });
            //  channel.register(selector, SelectionKey.OP_WRITE); //TODO:可能要改成在这里注册写事件
        } else if (count < 0) {
            //对端链路关闭
            selectionKey.cancel();
            channel.close();
        } else {
            //读到0字节，忽略
        }
    }



    /**
     * 启动方法
     * @throws IOException
     */
    public void start()throws IOException {
        try{
            while(true){
                registerSelectionKey();//注册写兴趣

                int count=selector.select();
                if(count>0){
                    final Set<SelectionKey> selectionKeys= selector.selectedKeys();

                    for(final SelectionKey selectionKey:selectionKeys){
                        handleKey(selectionKey);
                    }
                    selectionKeys.clear();
                }
            }
        }finally {
            selector.close();
            channel.close();
        }
    }

    abstract void handleKey(SelectionKey selectionKey);

    abstract void registerSelectionKey() throws ClosedChannelException;

    /**
     * 写内容
     * @param channel
     * @param content
     * @throws IOException
     */
    void writeContent(final SelectionKey selectionKey,final SocketChannel channel,final Object content) {
        executorService.submit(new Callable<List<Object>>() {
            List<Object> results = new ArrayList<Object>();

            public List<Object> call() {
                results.add(content);
                logger.debug(name + "发送数据 --:" + content);
                for (ContentHandler handler : contentHandlers) {
                    List<Object> outs = new ArrayList<Object>();
                    for (Object result : results) {
                        handler.write((ByteBuffer)selectionKey.attachment(),channel, result, outs);
                    }
                    results = outs;
                }
                bossExecs.execute(new Runnable() {
                    public void run() {
                        try {
                            if(selectionKey.attachment()!=null){
                                writeContent(channel,(ByteBuffer)selectionKey.attachment());
                            }else{
                                for(Object result:results){
                                    writeContent(channel,(ByteBuffer)result);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                return results;
            }
        });

    }

    void writeContent(SocketChannel socketChannel,ByteBuffer sendBuffer) throws IOException {
        sendBuffer.flip();
        while(sendBuffer.hasRemaining()){
            socketChannel.write(sendBuffer);
        }
    }

}
