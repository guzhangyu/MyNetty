package com.netty.demo;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * 该接口定义了通用TCPSelectorServer类与特定协议之间的接口，
 * 它把与具体协议相关的处理各种io的操作分离了出来
 * 以使不同协议都能方便地使用这个基本的服务模式
 * Created by guzy on 16/9/22.
 */
public interface TCPProtocol {


    void handleAccept(SelectionKey key) throws IOException;

    void handleRead(SelectionKey key) throws IOException;

    void handleWrite(SelectionKey key) throws IOException;
}
