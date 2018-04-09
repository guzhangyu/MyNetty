package com.netty.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 控制台输入处理
 * Created by guzy on 2018-04-09.
 */
public class SystemInHandle {

    public static void handle(StrOp strOp) throws IOException {
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        String line=null;
        while((line=br.readLine())!=null){
            strOp.deal(line);
        }
    }
}
