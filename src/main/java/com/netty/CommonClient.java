package com.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
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
   // private SelectionKey selectionKey;

    SocketChannel socketChannel;

    Queue<Object> toWrites=new ArrayBlockingQueue<Object>(100);

    public CommonClient(String host, int port,String name) throws IOException {
        super(name);
        bossExecs= new SimpleThreadExecutors();

        socketChannel=SocketChannel.open();
        socketChannel.configureBlocking(false);

        selector= Selector.open();
        //  channel.socket().setTcpNoDelay(true);
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        socketChannel.connect(new InetSocketAddress(host, port));

        this.channel=socketChannel;
    }

    @Override
    void handleConnect(SelectionKey selectionKey) throws IOException {

    }

    @Override
    void handleKey(SelectionKey selectionKey) throws IOException {

        final SocketChannel channel = (SocketChannel) selectionKey.channel();

        if (selectionKey.isConnectable()) {
            System.out.println("channel connect");
            if (channel.isConnectionPending()) {
                channel.finishConnect();
                System.out.println(name + "完成连接!");

                completeHandler.handle(channel);
            }
            if(toWrites.size()>0){
                //channel.register(selector,SelectionKey.OP_WRITE);
                while(toWrites.size()>0){
                    writeContent(socketChannel,toWrites.poll());
                }
            }
            channel.register(selector, SelectionKey.OP_READ);

            return;
        }

//         if(selectionKey.isWritable()){
//            this.selectionKey=selectionKey;
//
//            while(toWrites.size()>0){
//                writeContent(socketChannel,toWrites.poll());
//            }
//            System.out.println("init ");
//        }

        if (selectionKey.isReadable()) {
            handleReadable(selectionKey, channel);
        }
    }

    public void write(Object o) throws IOException {
        if(!socketChannel.isConnected()){
            toWrites.add(o);
        }else{
            writeContent(socketChannel,o);
        }

        //if(this.selectionKey!=null){
//            writeContent(socketChannel,o);
//        }else{
//            toWrites.add(o);
//            socketChannel.register(selector,SelectionKey.OP_WRITE);
//            System.out.println("selectionKey empty ");
//        }
    }
}
