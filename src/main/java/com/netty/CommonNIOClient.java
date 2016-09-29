package com.netty;

import com.netty.hander.CompleteHandler;
import com.netty.handlers.HalfContentHandler;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * nio 客户端
 * Created by guzy on 16/9/18.
 */
public class CommonNIOClient extends CommonClient {


    public CommonNIOClient(String host, int port, String name) throws IOException {
        super(host, port, name);
    }

    public static void main(String[] args) throws IOException {
        final CommonNIOClient client=new CommonNIOClient("127.0.0.1",8888,"client");
        client.setCompleteHandler(new CompleteHandler() {
                    public void handle(SocketChannel socketChannel) throws IOException {
                        //首次写数据
                        client.write("Hello Server");
                    }
                })//连接成功处理器
                .addContentHandler(new HalfContentHandler())//增加内容过滤器
                .start();//启动进程

            new Thread(new Runnable() {
                public void run() {
                    try {
                        client.write("te");
                        client.write("ted");
                        client.write("testt");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

    }

}
