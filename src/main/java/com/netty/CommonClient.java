package com.netty;

import com.netty.hander.CompleteHandler;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 公共的客户端
 * Created by guzy on 16/9/20.
 */
public class CommonClient extends CommonWorker {

    Logger logger=Logger.getLogger(CommonClient.class);

    //客户端channel
    private SocketChannel socketChannel;

    //用于写的内容列表
    private Queue<Object> toWrites=new ArrayBlockingQueue<Object>(100);

    /**
     * 连接完成的处理器
     */
    CompleteHandler completeHandler;

    public CommonClient setCompleteHandler(CompleteHandler completeHandler) {
        this.completeHandler = completeHandler;
        return this;
    }

    public CommonClient(String host, int port,String name) throws IOException {
        super(name);

        socketChannel=SocketChannel.open();
        socketChannel.configureBlocking(false);

        selector= Selector.open();
      //   socketChannel.socket().setTcpNoDelay(true);
        socketChannel.register(selector, SelectionKey.OP_CONNECT);//注册连接服务器兴趣
        socketChannel.connect(new InetSocketAddress(host, port));//要连接的服务器

        this.channel=socketChannel;
    }

    void handleFirstConnect(SelectionKey selectionKey,List<Object> results){
    }

    /**
     * 对selectionKey的处理
     * @param selectionKey
     */
    void handleKey(final SelectionKey selectionKey) throws IOException {
        handleKeyInner(selectionKey);
    }

    /**
     * selectionKey线程内处理方法
     * @param selectionKey
     * @throws IOException
     */
    void handleKeyInner(SelectionKey selectionKey) throws IOException {

        final SocketChannel channel = (SocketChannel) selectionKey.channel();

        if (selectionKey.isConnectable()) {
            if (channel.isConnectionPending()) {
                channel.finishConnect();
                logger.debug(name + "完成连接!");

                if(completeHandler!=null){
                    completeHandler.handle(channel);
                }
            }
            channel.register(selector, SelectionKey.OP_READ);
            return;
        }

        if (selectionKey.isReadable()) {
            handleReadable(selectionKey, channel);
        }
//        if(selectionKey.isWritable() && !toWrites.isEmpty()){
//            for(Object o:toWrites){
//                writeContent(selectionKey,socketChannel,o);
//            }
//            toWrites.clear();
//            channel.register(selector, SelectionKey.OP_READ);
//        }
    }

    public void write(Object o) throws IOException {
        toWrites.add(o);
        handleNotWritten();
    }

    /**
     * 处理关闭事件
     * @param selectionKey
     */
     void handleClose(SelectionKey selectionKey){
        logger.debug("server closed!");
         try {
             shutDown();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }

    /**
     * 将所有需要写的数据一次性写掉
     */
    synchronized void handleNotWritten() {
        if(socketChannel.isConnected()){
            for(Object toWrite:toWrites){
                writeContent(null,socketChannel,toWrite);
                toWrites.remove(toWrite);
            }
           // toWrites.clear();
            //channel.register(selector, SelectionKey.OP_READ);
        }
    }
}
