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
import java.util.ArrayList; //用于存储客户信息
import java.util.StringTokenizer; //引入 StringTokenizer 类，用于信息分离
import client.ui.LoginWindow;     //【v1.3 新增】登录窗口 — 聊天窗口启动前先显示

public class ClientApp {

	protected Shell shell;
	private Text ipAddress;
	private Text textPort;
	private Text textClientName;
	private Text talkMessage;
	private Text textAreaMessage; //聊天信息显示区
	private List clientsList;      //在线用户列表

	//—— 网络通信相关成员变量（教材 7.4.7 第 4 步）——
	Socket socket = null;            //与服务器通信的套接字
	BufferedReader cin = null;       //从服务器读取信息的输入流
	PrintStream cout = null;         //向服务器发送信息的输出流
	String clientName = "";          //用于存储客户登录名称
	volatile boolean isRun = false;  //控制接收线程是否继续运行

	//——【v1.4 新增】UI 字体资源 ——
	private Font contentFont;    // 12pt — 聊天记录、在线列表、输入框
	private Font buttonFont;     // 11pt — 所有按钮

	/**
	 * 程序唯一入口。
	 * 【v1.3 修改】先显示 LoginWindow，登录成功后再打开聊天窗口。
	 * @param args 命令行参数（暂未使用）
	 */
	public static void main(String[] args) {
		try {
			ClientApp window = new ClientApp();
			window.start();   //【v1.3】改为调用 start()，内部先显示登录窗口再进入聊天
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 【v1.3 新增】应用启动流程：先显示登录窗口，登录成功后再打开聊天窗口。
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
	 * 打开聊天窗口，进入 SWT 事件循环。
	 * 【v1.4 修改】事件循环结束后释放字体资源。
	 *
	 * @param display    外部传入的 Display
	 * @param clientName 从 LoginWindow 传入的用户名
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
		//【v1.4 新增】释放字体资源
		disposeFonts();
	}

	/**
	 * 【v1.4 UI Modernization】创建聊天窗口界面。
	 *
	 * 布局：顶部连接栏 → 分隔线 → 左侧在线用户(180px) | 右侧聊天记录 → 底部消息输入栏
	 * 窗口尺寸：900 × 650
	 * 所有 Label 宽度适配中文完整显示，所有按钮 125×35，内容区字体 12pt。
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(900, 650);
		shell.setText("Java Chat System");

		// ==========【v1.4 新增】创建 UI 字体 ==========
		// 从系统默认字体派生 12pt（内容区）和 11pt（按钮），
		// 字号提升后在高分辨率屏幕上更清晰可读
		Display display = Display.getDefault();
		FontData[] systemFD = display.getSystemFont().getFontData();
		contentFont = new Font(display,
				new FontData(systemFD[0].getName(), 12, SWT.NONE));
		buttonFont = new Font(display,
				new FontData(systemFD[0].getName(), 11, SWT.NONE));

		// ======================== 顶部连接栏 ========================
		// Label 宽度：中文约 12px/字，"服务器 IP"=4字→48+边距=68→取72
		Label lblip = new Label(shell, SWT.NONE);
		lblip.setBounds(12, 14, 72, 20);
		lblip.setText("服务器 IP");

		// Text 高度 28px，适配 12pt 字体
		ipAddress = new Text(shell, SWT.BORDER);
		ipAddress.setText("127.0.0.1");
		ipAddress.setBounds(90, 10, 130, 28);
		ipAddress.setFont(contentFont);

		// "端口"=2字→24+边距=36
		Label label = new Label(shell, SWT.NONE);
		label.setBounds(232, 14, 36, 20);
		label.setText("端口");

		textPort = new Text(shell, SWT.BORDER);
		textPort.setText("9999");
		textPort.setBounds(272, 10, 60, 28);
		textPort.setFont(contentFont);

		// "客户名称"=4字→48+边距=72
		Label label_2 = new Label(shell, SWT.NONE);
		label_2.setBounds(346, 14, 72, 20);
		label_2.setText("客户名称");

		textClientName = new Text(shell, SWT.BORDER);
		textClientName.setText(clientName);
		textClientName.setBounds(422, 10, 130, 28);
		textClientName.setFont(contentFont);

		// 按钮 125×35，字体 11pt
		Button connectServer = new Button(shell, SWT.NONE);
		connectServer.setBounds(566, 6, 125, 35);
		connectServer.setText("连接服务器");
		connectServer.setFont(buttonFont);

		Button disconnect = new Button(shell, SWT.NONE);
		disconnect.setBounds(700, 6, 125, 35);
		disconnect.setText("断开连接");
		disconnect.setFont(buttonFont);

		// ======================== 水平分隔线 ========================
		Label separator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setBounds(12, 50, 876, 2);

		// ======================== 左侧在线用户面板 ========================
		Label onlineUsersLabel = new Label(shell, SWT.NONE);
		onlineUsersLabel.setBounds(12, 58, 72, 20);
		onlineUsersLabel.setText("在线用户");

		clientsList = new List(shell, SWT.BORDER | SWT.V_SCROLL);
		clientsList.setBounds(12, 80, 180, 490);
		clientsList.setFont(contentFont);

		// ======================== 右侧聊天记录面板 ========================
		Label chatLogLabel = new Label(shell, SWT.NONE);
		chatLogLabel.setBounds(204, 58, 72, 20);
		chatLogLabel.setText("聊天记录");

		textAreaMessage = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		textAreaMessage.setBounds(204, 80, 684, 490);
		textAreaMessage.setFont(contentFont);

		// ======================== 底部消息输入栏 ========================
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

		//—— 连接服务器按钮事件（教材 7.4.7 第 6 步）——
		connectServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				connectToServer();
			}
		});

		//—— 发送信息按钮事件（教材 7.4.7 第 7 步）——
		send.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sendMessage();
			}
		});

		//—— 断开连接按钮事件（教材 7.4.7 第 8 步）——
		disconnect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				requestQuit();
			}
		});
	}

	/**
	 * 【v1.4 新增】释放 UI 字体资源。
	 * SWT Font 必须手动释放，否则造成系统 GDI 资源泄漏。
	 */
	private void disposeFonts() {
		if (contentFont != null && !contentFont.isDisposed()) {
			contentFont.dispose();
		}
		if (buttonFont != null && !buttonFont.isDisposed()) {
			buttonFont.dispose();
		}
	}

	/**
	 * 连接服务器：建立套接字、收发流，向服务器注册并启动接收线程。
	 * 对应教材 7.4.7 第 6 步。
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
				cout = new PrintStream(socket.getOutputStream());
				String str = "PEOPLE:" + clientName;
				cout.println(str);
				isRun = true;
				ReadMessageThread readThread = new ReadMessageThread();
				readThread.start();
			} catch (IOException e3) {
				textAreaMessage.append("输入输出异常\n");
			}
		}
	}

	/**
	 * 发送聊天信息：按约定格式 MSG:名称:内容 发送给服务器。
	 * 对应教材 7.4.7 第 7 步。
	 */
	private void sendMessage() {
		if (cout == null || socket == null) {
			textAreaMessage.append("系统提示:尚未连接服务器，无法发送信息\n");
			return;
		}
		String str = talkMessage.getText();
		str = "MSG:" + clientName + ":" + str;
		cout.println(str);
		talkMessage.setText("");
	}

	/**
	 * 请求断开连接：向服务器发送 QUIT 请求。
	 * 对应教材 7.4.7 第 8 步。
	 */
	private void requestQuit() {
		if (cout == null || socket == null) {
			textAreaMessage.append("系统提示:尚未连接服务器\n");
			return;
		}
		String str = "QUIT";
		cout.println(str);
		textAreaMessage.append("客户请求断开连接\n");
	}

	/**
	 * 读取服务器信息线程（教材 7.4.7 第 5 步）。
	 */
	class ReadMessageThread extends Thread {

		public void list(final ArrayList<String> imessage) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					if (clientsList.isDisposed())
						return;
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
					if (textAreaMessage.isDisposed())
						return;
					textAreaMessage.append(str1);
				}
			});
		}

		public void run() {
			String line = "";
			while (isRun) {
				try {
					line = cin.readLine();
				} catch (IOException e) {
					this.appendTextArea("输入输出异常\n");
					break;
				}
				if (line == null) {
					this.appendTextArea("系统提示:与服务器的连接已断开\n");
					break;
				}
				StringTokenizer st = new StringTokenizer(line, ":");
				if (!st.hasMoreTokens())
					continue;
				String keyword = st.nextToken();
				if (keyword.equalsIgnoreCase("QUIT")) {
					try {
						socket.close();
						this.appendTextArea("接收到服务器同意断开信息，套接字关闭\n");
					} catch (IOException e) {
						this.appendTextArea("套接字关闭异常\n");
					}
					isRun = false;
				} else if (keyword.equalsIgnoreCase("PEOPLE")) {
					ArrayList<String> imessage = new ArrayList<String>();
					while (st.hasMoreTokens())
						imessage.add(st.nextToken());
					this.list(imessage);
				} else {
					String message = st.nextToken("\0");
					message = message.substring(1);
					this.appendTextArea(message + "\n");
				}
			}
			cleanup();
		}

		private void cleanup() {
			isRun = false;
			try {
				if (socket != null && !socket.isClosed())
					socket.close();
			} catch (IOException e) {
			}
			socket = null;
			cin = null;
			cout = null;
		}
	}
}
