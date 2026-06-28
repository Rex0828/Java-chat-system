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

/**
 * 多用户聊天服务端：监听端口 -> accept 客户端 -> 消息广播
 */
public class ServerApp {
    protected Shell shell;
    private Text textInformation;
    private Text textPort;
    private Button btnStart;
    private Button btnStop;
    private ServerSocket server = null;
    private volatile boolean isRunning = false;

    private final List<PrintStream> clientOutList =
            Collections.synchronizedList(new ArrayList<>());
    private final List<String> onlineUsers =
            Collections.synchronizedList(new ArrayList<>());

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
                new Thread(() -> startServer(port)).start();
            }
        });

        btnStop = new Button(shell, SWT.NONE);
        btnStop.setBounds(345, 23, 80, 27);
        btnStop.setText("停止服务");
        btnStop.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                stopServer();
            }
        });

        textInformation = new Text(shell,
                SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
        textInformation.setBounds(43, 68, 348, 190);
    }

    private void appendLog(String msg) {
        Display.getDefault().asyncExec(() -> {
            if (!textInformation.isDisposed()) {
                textInformation.append(msg);
                textInformation.setSelection(textInformation.getText().length());
            }
        });
    }

    /**
     * 广播消息给所有在线客户端
     */
    private void broadcast(String msg) {
        synchronized (clientOutList) {
            for (PrintStream out : clientOutList) {
                out.println(msg);
            }
        }
    }

    /**
     * 启动服务：accept 循环，每客户端一个线程
     *
     * 协议：
     *   PEOPLE:name   → 用户上线
     *   MSG:...       → 群聊/私聊消息
     *   QUIT          → 用户断开
     */
    private void startServer(int port) {
        try {
            server = new ServerSocket(port);
            isRunning = true;
            appendLog("系统提示:聊天服务器系统开始启动，端口:" + port + "\r\n");

            while (isRunning && !server.isClosed()) {
                Socket clientSocket = server.accept();
                appendLog("用户连接服务器成功 "
                        + clientSocket.getRemoteSocketAddress() + "\r\n");

                new Thread(() -> {
                    BufferedReader clientIn = null;
                    PrintStream clientOut = null;
                    String clientName = null;
                    try {
                        clientIn = new BufferedReader(
                                new InputStreamReader(clientSocket.getInputStream()));
                        clientOut = new PrintStream(
                                clientSocket.getOutputStream(), true);
                        clientOutList.add(clientOut);

                        String str;
                        while ((str = clientIn.readLine()) != null) {
                            if (str.startsWith("PEOPLE:")) {
                                clientName = str.substring(7).trim();
                                if (!clientName.isEmpty()
                                        && !onlineUsers.contains(clientName)) {
                                    onlineUsers.add(clientName);
                                }
                                // 广播完整在线用户列表
                                StringBuilder sb = new StringBuilder("PEOPLE");
                                synchronized (onlineUsers) {
                                    for (String user : onlineUsers) {
                                        sb.append(":").append(user);
                                    }
                                }
                                broadcast(sb.toString());
                            } else if (str.startsWith("QUIT")) {
                                // 只回复请求者，不广播 QUIT
                                if (clientOut != null) {
                                    clientOut.println("QUIT");
                                }
                                break;
                            } else {
                                // MSG 等消息：广播给所有客户端
                                broadcast(str);
                            }
                        }
                    } catch (IOException ex) {
                        appendLog("客户端断开或IO异常\r\n");
                    } finally {
                        // 下线清理：移除在线用户 + 广播更新列表
                        if (clientName != null) {
                            onlineUsers.remove(clientName);
                            StringBuilder sb = new StringBuilder("PEOPLE");
                            synchronized (onlineUsers) {
                                for (String user : onlineUsers) {
                                    sb.append(":").append(user);
                                }
                            }
                            broadcast(sb.toString());
                        }
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
            clientOutList.clear();
            onlineUsers.clear();
            appendLog("服务已停止监听\r\n");
            try {
                if (server != null) server.close();
            } catch (IOException ignored) {}
            server = null;
        }
    }

    private void stopServer() {
        isRunning = false;
        try {
            if (server != null && !server.isClosed()) {
                server.close();
            }
        } catch (IOException ignored) {}
    }
}
