package com.netty;

import com.netty.hander.CompleteHandler;
import com.netty.handlers.HalfContentHandler;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * nio 客户端
 * Created by guzy on 16/9/18.
 */
public class ClientTest extends Client {


    public ClientTest(String host, int port, String name) throws IOException {
        super(host, port, name);
    }

    public static void main(String[] args) throws IOException {
        final ClientTest client=new ClientTest("127.0.0.1",8888,"client");
        CompleteHandler completeHandler=new CompleteHandler() {
            public void handle(SocketChannel socketChannel) throws IOException {
                //首次写数据
                client.writeContent(socketChannel,"Hello,Server");
            }
        };
        client.setCompleteHandler(completeHandler);
//        client.contentHandlers.add(new ContentHandler() {
//            public Object write(AbstractSelectableChannel channel, Object o,List<Object> outs) {
//                Object result=((String)o).getBytes();
//                outs.add(result);
//                return result;
//            }
//
//            public Object read(AbstractSelectableChannel channel, Object o,List<Object> outs) {
//                Object result= new String((byte[])o);
//                outs.add(result);
//                return result;
//            }
//        });
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

        try {
            Thread.sleep(5000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("begin write");
        client.write("te");
        client.write("ted");
        client.write("testt");
    }

}
