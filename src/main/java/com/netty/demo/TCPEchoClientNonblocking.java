package com.netty.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by guzy on 16/9/22.
 */
public class TCPEchoClientNonblocking  {

    public static void main(String[] args) throws IOException {
        if( args.length<2 || args.length>3){
            throw new IllegalArgumentException("参数不正确");
        }

        //第一个参数作为要连接的服务器的主机名或ip
        String server= args[0];
        //第二个参数作为要发送到服务器端的字符串
        byte []argument=args[1].getBytes();
        //如果有第三个参数，则作为端口号，如果没有，则端口号设为7
        int serverPort=(args.length==3)?Integer.parseInt(args[2]):7;

        //创建一个信道，设置为非阻塞模式
        SocketChannel clntChan = SocketChannel.open();
        clntChan.configureBlocking(false);
        //向服务端发起连接
        if(!clntChan.connect(new InetSocketAddress(server,serverPort))){
            //不断地轮询连接状态，直到连接完成
            while(!clntChan.finishConnect()){
                System.out.print(".");
            }
        }

        System.out.println("");

        //分别实例化用来读写的缓冲区
        ByteBuffer writeBuf = ByteBuffer.wrap(argument);
        ByteBuffer readBuf = ByteBuffer.allocate(argument.length);

        //接收到的总字节数
        int totalBytesRcvd=0;
        int bytesRcvd;

        while(totalBytesRcvd<argument.length){
            //如果用来向通道中写数据的缓冲区中还有剩余的字节，则继续将数据写入信道
            if(writeBuf.hasRemaining()){
                clntChan.write(writeBuf);
            }

            //如果read()接收到-1,表明服务端关闭，抛出异常
            if((bytesRcvd = clntChan.read(readBuf))==-1){
                throw new SocketException("Connection closed prematurely");
            }

            //计算接收到的总字节数
            totalBytesRcvd+=bytesRcvd;

            System.out.print(".");

        }

        System.out.println("received:"+new String(readBuf.array(),0,totalBytesRcvd));

        //关闭信道
        clntChan.close();
    }
}
