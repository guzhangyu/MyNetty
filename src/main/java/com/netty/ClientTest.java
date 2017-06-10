package com.netty;

import com.netty.assist.HandleStr;
import com.netty.assist.ReadInput;
import com.netty.hander.CompleteHandler;
import com.netty.handlers.HalfContentHandler;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * nio 客户端
 * Created by guzy on 16/9/18.
 */
public class ClientTest {

    private final static Logger logger=Logger.getLogger(ClientTest.class);


    public static void main(String[] args) throws IOException {
        final NioClient client=new NioClient("127.0.0.1",8889,"client");
        final CountDownLatch la=new CountDownLatch(1);

        client.setCompleteHandler(new CompleteHandler() {
            public void handle(SocketChannel socketChannel) throws IOException {

                logger.info("MyName:?");
                synchronized (System.in){
                    BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
                    String myName=reader.readLine();
                    //首次写数据
                    client.write("MyName:"+myName);
                }
                la.countDown();
            }
        })//连接成功处理器
        .addContentHandler(new HalfContentHandler());//增加内容过滤器

        new Thread(new Runnable() {
            public void run() {
              client.start();//启动进程
            }
        }).start();

//        try {
//            Thread.sleep(2000l);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        new Thread(new Runnable() {
            public void run() {
                try {
                    la.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new ReadInput().read(new HandleStr() {
                    public void handleStr(String str) throws Exception {
                        client.write(str);
                        //System.out.println(str);
                    }
                });
            }
        }).start();

//         new Thread(new Runnable() {
//                public void run() {
//                    try {
//                        System.out.println("begin write");
//                        client.write("te");
//                        client.write("ted");
//                        client.write("testt");
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();

    }

}