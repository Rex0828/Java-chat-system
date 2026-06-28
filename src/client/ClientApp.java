package client;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;
import client.ui.LoginWindow;

/**
 * 多用户聊天客户端
 */
public class ClientApp {

    protected Shell shell;
    private Text ipAddress;
    private Text textPort;
    private Text textClientName;
    private Text talkMessage;
    private Text textAreaMessage;
    private List clientsList;

    Socket socket = null;
    BufferedReader cin = null;
    PrintStream cout = null;
    String clientName = "";
    volatile boolean isRun = false;

    private Font contentFont;
    private Font buttonFont;

    public static void main(String[] args) {
        try {
            ClientApp window = new ClientApp();
            window.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动流程：登录 -> 聊天
     */
    public void start() {
        Display display = new Display();
        LoginWindow loginWindow = new LoginWindow();
        loginWindow.open(display);
        if (loginWindow.isLoginSuccessful()) {
            String username = loginWindow.getLoggedInUsername();
            open(display, username);
        }
        display.dispose();
    }

    /**
     * 打开聊天窗口
     */
    public void open(Display display, String clientName) {
        this.clientName = clientName;
        createContents();
        shell.open();
        shell.layout();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        disposeFonts();
    }

    protected void createContents() {
        shell = new Shell();
        shell.setSize(900, 650);
        shell.setText("Java Chat System");

        Display display = Display.getDefault();
        FontData[] systemFD = display.getSystemFont().getFontData();
        contentFont = new Font(display,
                new FontData(systemFD[0].getName(), 12, SWT.NONE));
        buttonFont = new Font(display,
                new FontData(systemFD[0].getName(), 11, SWT.NONE));

        Label lblip = new Label(shell, SWT.NONE);
        lblip.setBounds(12, 14, 72, 20);
        lblip.setText("服务器 IP");

        ipAddress = new Text(shell, SWT.BORDER);
        ipAddress.setText("127.0.0.1");
        ipAddress.setBounds(90, 10, 130, 28);
        ipAddress.setFont(contentFont);

        Label label = new Label(shell, SWT.NONE);
        label.setBounds(232, 14, 36, 20);
        label.setText("端口");

        textPort = new Text(shell, SWT.BORDER);
        textPort.setText("9999");
        textPort.setBounds(272, 10, 60, 28);
        textPort.setFont(contentFont);

        Label label_2 = new Label(shell, SWT.NONE);
        label_2.setBounds(346, 14, 72, 20);
        label_2.setText("客户名称");

        textClientName = new Text(shell, SWT.BORDER);
        textClientName.setText(clientName);
        textClientName.setBounds(422, 10, 130, 28);
        textClientName.setFont(contentFont);

        Button connectServer = new Button(shell, SWT.NONE);
        connectServer.setBounds(566, 6, 125, 35);
        connectServer.setText("连接服务器");
        connectServer.setFont(buttonFont);

        Button disconnect = new Button(shell, SWT.NONE);
        disconnect.setBounds(700, 6, 125, 35);
        disconnect.setText("断开连接");
        disconnect.setFont(buttonFont);

        Label separator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setBounds(12, 50, 876, 2);

        Label onlineUsersLabel = new Label(shell, SWT.NONE);
        onlineUsersLabel.setBounds(12, 58, 72, 20);
        onlineUsersLabel.setText("在线用户");

        clientsList = new List(shell, SWT.BORDER | SWT.V_SCROLL);
        clientsList.setBounds(12, 80, 180, 490);
        clientsList.setFont(contentFont);

        Label chatLogLabel = new Label(shell, SWT.NONE);
        chatLogLabel.setBounds(204, 58, 72, 20);
        chatLogLabel.setText("聊天记录");

        textAreaMessage = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        textAreaMessage.setBounds(204, 80, 684, 490);
        textAreaMessage.setFont(contentFont);

        Label inputLabel = new Label(shell, SWT.NONE);
        inputLabel.setBounds(12, 582, 36, 20);
        inputLabel.setText("消息");

        talkMessage = new Text(shell, SWT.BORDER);
        talkMessage.setBounds(54, 580, 680, 28);
        talkMessage.setFont(contentFont);

        Button send = new Button(shell, SWT.NONE);
        send.setBounds(746, 577, 125, 35);
        send.setText("发送");
        send.setFont(buttonFont);

        connectServer.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                connectToServer();
            }
        });

        send.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                sendMessage();
            }
        });

        disconnect.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                requestQuit();
            }
        });

        // 窗口关闭时主动断开连接
        shell.addListener(SWT.Close, event -> {
            if (socket != null && !socket.isClosed() && cout != null) {
                try {
                    cout.println("QUIT");
                } catch (Exception ignored) {}
            }
            isRun = false;
        });
    }

    private void disposeFonts() {
        if (contentFont != null && !contentFont.isDisposed()) {
            contentFont.dispose();
        }
        if (buttonFont != null && !buttonFont.isDisposed()) {
            buttonFont.dispose();
        }
    }

    /**
     * 连接服务器，注册用户名，启动接收线程
     */
    private void connectToServer() {
        if (socket != null) {
            textAreaMessage.append("系统提示:已经连接到服务器，请勿重复连接\n");
            return;
        }
        try {
            InetAddress ip = InetAddress.getByName(ipAddress.getText());
            int port = Integer.parseInt(textPort.getText());
            socket = new Socket(ip, port);
            textAreaMessage.append("系统提示:与服务器开始连接...... \n");
        } catch (IOException e1) {
            textAreaMessage.append("服务器端口打开出错\n");
        } catch (NumberFormatException e1) {
            textAreaMessage.append("端口号格式错误\n");
        }

        if (socket != null) {
            textAreaMessage.append("系统提示:与服务器连接成功...... \n");
            clientName = textClientName.getText().trim();
            try {
                cin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                cout = new PrintStream(socket.getOutputStream(), true);
                cout.println("PEOPLE:" + clientName);
                isRun = true;
                new ReadMessageThread().start();
            } catch (IOException e3) {
                textAreaMessage.append("输入输出异常\n");
            }
        }
    }

    /**
     * 发送群聊消息：MSG:BROAD:sender:content
     */
    private void sendMessage() {
        if (cout == null || socket == null) {
            textAreaMessage.append("系统提示:尚未连接服务器，无法发送信息\n");
            return;
        }
        String msgContent = talkMessage.getText();
        String str = "MSG:BROAD:" + clientName + ":" + msgContent;
        cout.println(str);
        cout.flush();
        talkMessage.setText("");
    }

    /**
     * 发送断开请求
     */
    private void requestQuit() {
        if (cout == null || socket == null) {
            textAreaMessage.append("系统提示:尚未连接服务器\n");
            return;
        }
        cout.println("QUIT");
        textAreaMessage.append("客户请求断开连接\n");
    }

    /**
     * 消息接收线程：
     *   PEOPLE:name1:name2  → 更新在线列表
     *   MSG:BROAD:sender:msg → 群聊消息
     *   MSG:SINGLE:target:sender:msg → 私聊消息
     *   QUIT → 断开确认
     */
    class ReadMessageThread extends Thread {

        public void list(final ArrayList<String> imessage) {
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    if (clientsList.isDisposed()) return;
                    clientsList.removeAll();
                    for (String temp : imessage)
                        clientsList.add(temp);
                }
            });
        }

        public void appendTextArea(String str) {
            final String str1 = str;
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    if (textAreaMessage.isDisposed()) return;
                    textAreaMessage.append(str1);
                }
            });
        }

        public void run() {
            String line;
            while (isRun) {
                try {
                    line = cin.readLine();
                } catch (IOException e) {
                    appendTextArea("输入输出异常\n");
                    break;
                }
                if (line == null) {
                    appendTextArea("系统提示:与服务器的连接已断开\n");
                    break;
                }
                StringTokenizer st = new StringTokenizer(line, ":");
                if (!st.hasMoreTokens()) continue;
                String keyword = st.nextToken();

                if (keyword.equalsIgnoreCase("QUIT")) {
                    try { socket.close(); } catch (IOException e) {}
                    appendTextArea("接收到服务器同意断开信息，套接字关闭\n");
                    isRun = false;
                } else if (keyword.equalsIgnoreCase("PEOPLE")) {
                    ArrayList<String> imessage = new ArrayList<String>();
                    while (st.hasMoreTokens())
                        imessage.add(st.nextToken());
                    list(imessage);
                } else if (keyword.equalsIgnoreCase("MSG")) {
                    String typeOrSender = st.nextToken();
                    if (typeOrSender.equalsIgnoreCase("BROAD")) {
                        String sender = st.nextToken();
                        String content = st.nextToken("\0").substring(1);
                        appendTextArea(sender + ": " + content + "\n");
                    } else if (typeOrSender.equalsIgnoreCase("SINGLE")) {
                        String target = st.nextToken();
                        String sender = st.nextToken();
                        String content = st.nextToken("\0").substring(1);
                        appendTextArea("[私聊] " + sender + " -> " + target + ": " + content + "\n");
                    } else {
                        String sender = typeOrSender;
                        String content = st.nextToken("\0").substring(1);
                        appendTextArea(sender + ": " + content + "\n");
                    }
                } else {
                    String message = st.nextToken("\0").substring(1);
                    appendTextArea(message + "\n");
                }
            }
            cleanup();
        }

        private void cleanup() {
            isRun = false;
            try {
                if (socket != null && !socket.isClosed())
                    socket.close();
            } catch (IOException e) {}
            socket = null;
            cin = null;
            cout = null;
        }
    }
}
