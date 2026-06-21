package common.protocol;

/**
 * 消息类型枚举。
 * 约束通信双方的消息协议，客户端与服务端共享同一套定义。
 * 第一阶段仅需 LOGIN / LOGIN_ACK / ERROR 三种类型。
 */
public enum MessageType {

    LOGIN,
    LOGIN_ACK,
    ERROR

}
