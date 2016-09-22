package com.netty.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

/**
 * Created by guzy on 16/9/22.
 */
public class TCPServerSelector {

    //缓冲区的长度
    private static final int BUFSIZE=256;

    //select 方法等待信道准备好的最长时间
    private static final int TIMEOUT=3000;

    public static void main(String[] args) throws IOException {
        if(args.length<1){
            throw new IllegalArgumentException("Parameters: <port> ...");
        }
        //创建一个选择器
        Selector selector=Selector.open();
        for(String arg:args){
            //实例化一个信道
            ServerSocketChannel listnChannel = ServerSocketChannel.open();
            //将该信道绑定到指定端口
            listnChannel.socket().bind(new InetSocketAddress(Integer.parseInt(arg)));

            //配置信道为非阻塞模式
            listnChannel.configureBlocking(false);
            //将选择器注册到各个信道
            listnChannel.register(selector, SelectionKey.OP_ACCEPT);
        }
        //创建一个实现了协议接口的对象
        TCPProtocol protocol=new EchoSelectorProtocol(BUFSIZE);

        //不断轮询select方法，获取准备好的信道所关联的key集合
        while(true){
            //一直等待，直到有信道准备好了I/O操作
            if(selector.select(TIMEOUT)==0){
                System.out.print(".");
                continue;
            }

            //获取准备好的信道所关联的key集合的iterator实例
            Iterator<SelectionKey> keyItr=selector.selectedKeys().iterator();
            //循环获取集合中的每个键值
            while(keyItr.hasNext()){
                SelectionKey key=keyItr.next();

                //如果服务端信道感兴趣的I/O操作为accept
                if(key.isAcceptable()){
                    protocol.handleAccept(key);
                }

                //如果客户端信道感兴趣的io操作为read
                if(key.isReadable()){
                    protocol.handleRead(key);
                }

                //如果该键值有效，并且其对应的客户端信道感兴趣的io操作为write
                if(key.isValid() && key.isWritable()){
                    protocol.handleWrite(key);
                }

                //这里需要手动从键集中移除当前的key
                keyItr.remove();
            }
        }


    }
}
