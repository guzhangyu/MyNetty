package com.netty;

import com.netty.handlers.HalfContentHandler;

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
        server.addContentHandler(new HalfContentHandler());
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

        server.write("127.0.0.1","fdaf");
    }
}
