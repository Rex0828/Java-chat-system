package server;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerApp {
    protected Shell shell;
    private Text textInformation;
    private Text textPort;
    private Button btnStart;
    private Button btnStop;
    // 全局服务套接字，用于停止服务
    private ServerSocket server = null;
    // 标记服务是否运行
    private volatile boolean isRunning = false;

    // 新增：保存所有客户端输出流，用于广播消息，同步集合防止并发异常
    private final List<PrintStream> clientOutList = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        try {
            ServerApp window = new ServerApp();
            window.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void open() {
        Display display = Display.getDefault();
        createContents();
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        // 窗口关闭时释放服务
        stopServer();
    }

    protected void createContents() {
        shell = new Shell();
        shell.setSize(450, 320);
        shell.setText("SWT TCP服务端");

        Label label = new Label(shell, SWT.NONE);
        label.setBounds(36, 28, 61, 17);
        label.setText("监听端口");

        textPort = new Text(shell, SWT.BORDER);
        textPort.setBounds(115, 25, 140, 23);
        textPort.setText("8888");

        // 启动按钮
        btnStart = new Button(shell, SWT.NONE);
        btnStart.setBounds(260, 23, 80, 27);
        btnStart.setText("开始监听");
        btnStart.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (isRunning) {
                    appendLog("服务已在运行，请勿重复启动\r\n");
                    return;
                }
                int port;
                try {
                    port = Integer.parseInt(textPort.getText().trim());
                    if (port < 1 || port > 65535) {
                        appendLog("端口范围必须是1~65535\r\n");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    appendLog("端口输入不合法，请输入数字\r\n");
                    return;
                }

                // 新开线程启动服务
                new Thread(() -> startServer(port)).start();
            }
        });

        // 停止按钮
        btnStop = new Button(shell, SWT.NONE);
        btnStop.setBounds(345, 23, 80, 27);
        btnStop.setText("停止服务");
        btnStop.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                stopServer();
            }
        });

        // ========== 修复换行关键：增加 MULTI、WRAP、滚动条 ==========
        textInformation = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
        textInformation.setBounds(43, 68, 348, 190);
    }

    // 统一日志输出方法，自带兼容换行
    private void appendLog(String msg) {
        Display.getDefault().asyncExec(() -> {
            if (!textInformation.isDisposed()) {
                textInformation.append(msg);
                // 自动滚动到底部
                textInformation.setSelection(textInformation.getText().length());
            }
        });
    }

    // 新增：广播消息给所有在线客户端
    private void broadcast(String msg) {
        synchronized (clientOutList) {
            for (PrintStream out : clientOutList) {
                out.println(msg);
            }
        }
    }

    // 启动服务逻辑
    private void startServer(int port) {
        try {
            server = new ServerSocket(port);
            isRunning = true;
            appendLog("系统提示:聊天服务器系统开始启动，端口:" + port + "\r\n");

            while (isRunning && !server.isClosed()) {
                Socket clientSocket = server.accept();
                appendLog("用户连接服务器成功 " + clientSocket.getRemoteSocketAddress() + "\r\n");

                // 单客户端处理线程
                new Thread(() -> {
                    BufferedReader clientIn = null;
                    PrintStream clientOut = null;
                    try {
                        clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        // true代表自动刷新缓冲区，消息立刻发出
                        clientOut = new PrintStream(clientSocket.getOutputStream(), true);
                        // 存入集合，用于广播
                        clientOutList.add(clientOut);

                        String str;
                        while ((str = clientIn.readLine()) != null) {
                            appendLog("收到消息：" + str + "\r\n");
                            // 广播给全部客户端
                            broadcast(str);
                        }
                    } catch (IOException ex) {
                        appendLog("客户端断开或IO异常\r\n");
                    } finally {
                        // 下线移除输出流
                        if (clientOut != null) {
                            clientOutList.remove(clientOut);
                            clientOut.close();
                        }
                        try {
                            if (clientIn != null) clientIn.close();
                            clientSocket.close();
                        } catch (IOException ignored) {}
                        appendLog("客户端连接已关闭\r\n");
                    }
                }).start();
            }
        } catch (IOException e1) {
            appendLog("服务器端口打开出错:" + e1.getMessage() + "\r\n");
        } finally {
            isRunning = false;
            // 清空所有客户端连接
            clientOutList.clear();
            appendLog("服务已停止监听\r\n");
            try {
                if (server != null) server.close();
            } catch (IOException ignored) {}
            server = null;
        }
    }

    // 停止服务
    private void stopServer() {
        isRunning = false;
        try {
            if (server != null && !server.isClosed()) {
                server.close();
            }
        } catch (IOException ignored) {}
    }
}
