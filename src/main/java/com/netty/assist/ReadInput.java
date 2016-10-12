package com.netty.assist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by guzy on 16/10/11.
 */
public class ReadInput {

    public void read(HandleStr handleStr){
        BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
        String line=null;
        try {
            while((line=reader.readLine())!=null){

                if(line.equalsIgnoreCase("EOF")){
                    reader.close();
                    System.out.println("test");
                }

                handleStr.handleStr(line);

                if(line.equalsIgnoreCase("EOF")){
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
