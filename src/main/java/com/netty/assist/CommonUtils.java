package com.netty.assist;

import java.nio.channels.SocketChannel;

/**
 * 公共工具
 * Created by guzy on 16/10/12.
 */
public class CommonUtils {

    /**
     * 获取socket的名称
     * @param socketChannel
     * @return
     */
    public static String getSocketName(SocketChannel socketChannel){
        return socketChannel.socket().getInetAddress().getHostName();
    }
}
