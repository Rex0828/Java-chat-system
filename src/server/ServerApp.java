package server;

/**
 * 服务端主入口。
 * 职责：创建 ServerSocket 绑定端口，死循环 accept() 等待客户端连接，
 * 每来一个连接创建新线程并传入 ClientHandler 处理。
 */
public class ServerApp {

    public static void main(String[] args) {
        // TODO: 第一阶段 — 启动服务端监听
    }
}
