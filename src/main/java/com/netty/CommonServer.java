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
public abstract class CommonServer {

    protected Selector selector;

    private CompleteHandler completeHandler;

    private Queue<Object> toWrites=new ArrayBlockingQueue<Object>(100);

    private String name;

    private ExecutorService executorService;

    private List<ContentHandler> contentHandlers=new ArrayList<ContentHandler>();

    SelectableChannel channel;

    public CommonServer(String name){
        this.name=name;
        executorService= Executors.newFixedThreadPool(10);
    }

    public CompleteHandler getCompleteHandler() {
        return completeHandler;
    }

    public CommonServer setCompleteHandler(CompleteHandler completeHandler) {
        this.completeHandler = completeHandler;
        return this;
    }

    public void addContentHandler(ContentHandler contentHandler){
        contentHandlers.add(contentHandler);
    }

    public void write(Object o){
        toWrites.offer(o);
    }

    public void start()throws IOException {
       try{
           while(true){
               // System.out.println("before select");

               //registerSelectionKey();
               selector.select(1000);
               // System.out.println("after select");
               handleKey(selector.selectedKeys());
           }
       }finally {
           selector.close();
           channel.close();
       }
    }

    private void registerSelectionKey() throws ClosedChannelException {
        if(!toWrites.isEmpty()){// && (channel.validOps() & SelectionKey.OP_WRITE)==0 ){
            channel.register(selector,SelectionKey.OP_WRITE);
        }
//        else{
//            channel.register(selector,SelectionKey.OP_WRITE);
//        }
    }


    /**
     * 对select到的key进行处理
     * @param selectionKeys
     * @throws IOException
     */
    public void handleKey(Set<SelectionKey> selectionKeys) throws IOException {
        //System.out.println("selectionKeys:"+selectionKeys.size());
        if(selectionKeys.size()==0){
            registerSelectionKey();
            return;
        }
        Iterator<SelectionKey> itr=selectionKeys.iterator();
        while(itr.hasNext()){
            SelectionKey selectionKey=itr.next();
            if(selectionKey.isAcceptable()){
                ServerSocketChannel server=(ServerSocketChannel)selectionKey.channel();

                SocketChannel client=server.accept();
                client.configureBlocking(false);
                client.register(selector,SelectionKey.OP_READ);
               // channel.register(selector, SelectionKey.OP_WRITE);
                itr.remove();
                continue;
            }
            final SocketChannel channel=(SocketChannel)selectionKey.channel();

            if(selectionKey.isConnectable()){
                System.out.println("channel connect");
                if(channel.isConnectionPending()){
                    channel.finishConnect();
                    System.out.println(name+"完成连接!");

                    completeHandler.handle(channel);
                }
                channel.register(selector, SelectionKey.OP_READ);
               // channel.register(selector, SelectionKey.OP_WRITE);
                itr.remove();
            }else if(selectionKey.isReadable()){
                final ByteBuffer receiveBuffer=ByteBuffer.allocate(1024);

                //读取数据
                int count=channel.read(receiveBuffer);
                if(count>0){
//                    String receiveText=new String(,0,count);
//                    System.out.println("客户端接受服务端数据--:"+receiveText);
                   executorService.execute(new Runnable() {
                       public void run() {
                           List<Object> results=new ArrayList<Object>();
                           receiveBuffer.flip();
                           results.add(receiveBuffer);
                           for(ContentHandler handler:contentHandlers){
                               List<Object> outs=new ArrayList<Object>();
                               Iterator resultItr=results.iterator();
                               while(resultItr.hasNext()){
                                   Object curResult=resultItr.next();
                                   handler.read(channel,curResult,outs);
                               }
                               results=outs;
                           }
                           for(Object curResult:results){
                               System.out.println(name+"接收数据--:"+new String((byte[])curResult));
                           }
                       }
                   });
                  //  channel.register(selector, SelectionKey.OP_WRITE);
                }else if(count<0){
                    //对端链路关闭
                    selectionKey.cancel();
                    channel.close();
                }else{
                    //读到0字节，忽略
                }
                //itr.remove();
            }else {
                //if(selectionKey.isWritable())
                if(!toWrites.isEmpty()){
                    //itr.remove();

                    Object content=toWrites.poll();
                    writeContent(channel, content);
                }
            }
        }
    }

    /**
     * 写内容
     * @param channel
     * @param content
     * @throws IOException
     */
    void writeContent(final SocketChannel channel,final Object content) throws IOException {
        List<Object> results= null;
        try {
            results = executorService.submit(new Callable<List<Object>>() {
                public List<Object> call() {
                    List<Object> results = new ArrayList<Object>();
                    results.add(content);
                    System.out.println(name + "发送数据 --:" + content);
                    for (ContentHandler handler : contentHandlers) {
                        List<Object> outs = new ArrayList<Object>();
                        for (Object result : results) {
                            handler.write(channel, result, outs);
                        }
                        results = outs;
                    }
                    return results;
                }
            }).get();
            for(Object result:results){
                writeContent(channel,(ByteBuffer)result);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


//                    ByteBuffer sendBuffer=ByteBuffer.allocate(BLOCK);
//                    sendBuffer.put(content.getBytes());
//                    sendBuffer.flip();
//                    //将缓冲区各标志位复位，因为向里面put了数据，标志被改变，想从中读取数据发向服务端，就要复位
//                    sendBuffer.flip();
//                    channel.write(sendBuffer);

        channel.register(selector, SelectionKey.OP_READ);
    }

    void writeContent(SocketChannel socketChannel,byte[]bytes) throws IOException {
        ByteBuffer sendBuffer=ByteBuffer.allocate(bytes.length);
        sendBuffer.put(bytes);
        sendBuffer.flip();
        socketChannel.write(sendBuffer);
    }

    void writeContent(SocketChannel socketChannel,ByteBuffer sendBuffer) throws IOException {
        sendBuffer.flip();
        socketChannel.write(sendBuffer);
    }

}
