package com.netty;

import java.io.IOException;

/**
 * Created by guzy on 16/9/24.
 */
public class BufferTest {

    public void testPoolBuffer(){
        int loop=3000000;
        long startTime=System.currentTimeMillis();
    }

    public static void main(String[] args) throws IOException {
        byte[] bytes=new byte[1024];
        while(System.in.read(bytes)>0){
            System.out.println(new String(bytes));
        }
    }
}
