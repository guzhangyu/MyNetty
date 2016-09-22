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
        CompleteHandler completeHandler=new CompleteHandler() {
            public void handle(SocketChannel socketChannel) throws IOException {
                //首次写数据
                client.write("Hello Server");
            }
        };
        client.setCompleteHandler(completeHandler);

        client.addContentHandler(new HalfContentHandler());
        new Thread(new Runnable() {
            public void run() {
                try {
                    client.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        client.write("te");
        client.write("ted");
        client.write("testt");
    }

}
