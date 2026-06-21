package server.network;

/**
 * 单客户端线程处理器。
 * 职责：实现 Runnable，在独立线程中读取客户端登录请求，
 * 调用 AuthService 验证，将结果写回客户端，最后关闭连接。
 */
public class ClientHandler implements Runnable {

    // TODO: 第一阶段 — 实现登录请求处理逻辑

    @Override
    public void run() {
        // TODO: 读取请求 → 调用认证 → 写回应答 → 关闭连接
    }
}
