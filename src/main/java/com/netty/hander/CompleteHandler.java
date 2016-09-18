package com.netty.hander;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * 连接、绑定完成处理器
 * Created by guzy on 16/9/18.
 */
public interface CompleteHandler {

    void handle(SocketChannel socketChannel) throws IOException;

}
