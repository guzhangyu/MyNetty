package com.netty.assist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 从控制台读入内容
 * Created by guzy on 16/10/11.
 */
public class ReadInput {

    /**
     * 读取控制台
     * @param handleStr
     */
    public void read(HandleStr handleStr){
        synchronized (System.in){
//            try {
//                System.in.wait();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
            String line=null;
            try {
                while((line=reader.readLine())!=null){

                    handleStr.handleStr(line);

                    if(line.equalsIgnoreCase("EOF")){
                        reader.close();
                        // System.out.println("test");
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
