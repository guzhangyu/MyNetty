package com.netty;

import com.netty.assist.SimpleThreadExecutors;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 公共的客户端
 * Created by guzy on 16/9/20.
 */
public class CommonClient extends CommonWorker {

    //客户端channel
    SocketChannel socketChannel;

    //用于写的内容列表
    Queue<Object> toWrites=new ArrayBlockingQueue<Object>(100);

    public CommonClient(String host, int port,String name) throws IOException {
        super(name);
        bossExecs= new SimpleThreadExecutors();//初始化boss线程池

        socketChannel=SocketChannel.open();
        socketChannel.configureBlocking(false);

        selector= Selector.open();
        //  channel.socket().setTcpNoDelay(true);
        socketChannel.register(selector, SelectionKey.OP_CONNECT);//注册连接服务器兴趣
        socketChannel.connect(new InetSocketAddress(host, port));//要连接的服务器

        this.channel=socketChannel;
    }

    /**
     * 根据要写的数据情况，来注册写兴趣
     * @throws ClosedChannelException
     */
    void registerSelectionKey() throws ClosedChannelException {
        if(!toWrites.isEmpty()){
            socketChannel.register(selector, SelectionKey.OP_WRITE);
        }
    }


    /**
     * 对selectionKey的处理
     * @param selectionKey
     */
    void handleKey(final SelectionKey selectionKey){
        bossExecs.execute(new Runnable() {
            public void run() {
                try {
                    // System.out.println(String.format("selectionKey isWritable:%s,isReadable:%s",selectionKey.isWritable(),selectionKey.isReadable()));
                    handleKeyInner(selectionKey);
                } catch (IOException ex) {
                    selectionKey.cancel();
                }
            }
        });
    }

    /**
     * selectionKey线程内处理方法
     * @param selectionKey
     * @throws IOException
     */
    void handleKeyInner(SelectionKey selectionKey) throws IOException {

        final SocketChannel channel = (SocketChannel) selectionKey.channel();

        if (selectionKey.isConnectable()) {
            System.out.println("enter isConnectable ");
            if (channel.isConnectionPending()) {
                channel.finishConnect();
                System.out.println(name + "完成连接!");

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
        if(selectionKey.isWritable() && !toWrites.isEmpty()){
            for(Object o:toWrites){
                writeContent(selectionKey,socketChannel,o);
            }
            toWrites.clear();
            channel.register(selector, SelectionKey.OP_READ);
        }
    }

    public void write(Object o) throws IOException {
        toWrites.add(o);
    }
}
