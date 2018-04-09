package com.netty.demo;

import com.netty.Server;
import com.netty.hander.impl.HalfContentHandler;
import com.netty.hander.impl.ReadLogHandler;
import com.netty.hander.impl.WriteLogHandler;

import java.io.IOException;

/**
 * nio 服务类
 * Created by guzy on 16/9/18.
 */
public class ServerTest extends Server {


    public ServerTest(int port, String name) throws IOException {
        super(port, name);
    }

    public static void main(String[] args) throws IOException{
        int port = 8888;
        final ServerTest server=new ServerTest(port,"server");
        server.addContentHandlers(new WriteLogHandler(),new HalfContentHandler(),new ReadLogHandler());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        SystemInHandle.handle(new StrOp() {
            @Override
            public void deal(String str) throws IOException {
                String[] arr=str.split(":");
                server.write(arr[0],arr[1]);
            }
        });
    }
}
