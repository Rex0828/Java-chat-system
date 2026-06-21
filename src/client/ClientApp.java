package client;

import client.ui.LoginWindow;

/**
 * 客户端主入口。
 *
 * 职责：程序启动的唯一入口点 —— 创建登录窗口并进入 SWT 事件循环。
 * 第一阶段不包含任何业务逻辑，仅展示登录界面。
 *
 * 数据流：
 *   main() → new LoginWindow() → open() → [SWT 事件循环] → 窗口关闭 → 程序退出
 */
public class ClientApp {

    /**
     * Java 程序入口方法。
     *
     * JVM 启动时自动调用本方法，传入命令行参数。
     * 第一阶段 args 不使用（后续可传入服务端 IP 和端口）。
     *
     * @param args 命令行参数（暂未使用）
     */
    public static void main(String[] args) {
        // --------------------------------------------------
        // 第 1 步：创建 LoginWindow 对象
        // --------------------------------------------------
        // new LoginWindow() 只是实例化 Java 对象 —— 在堆中分配内存，
        // 调用隐式的无参构造器。此时还没有创建任何 SWT 控件。
        // SWT 的 Display、Shell、Button 等都是在 open() 内部创建的。
        LoginWindow loginWindow = new LoginWindow();

        // --------------------------------------------------
        // 第 2 步：打开登录窗口
        // --------------------------------------------------
        // open() 内部依次完成：
        //   ① 创建 Display（SWT 与 OS 的桥梁）
        //   ② 创建 Shell（原生窗口）
        //   ③ 构建控件树（Label、Text、Button）并绑定监听器
        //   ④ shell.open() 使窗口可见
        //   ⑤ 进入 while (!shell.isDisposed()) 事件循环 —— 阻塞在这里
        //
        // 当用户点击"退出"或 × 关闭窗口时，shell.dispose() 被调用，
        // 事件循环退出，open() 返回，main() 执行完毕，JVM 退出。
        loginWindow.open();
    }
}
