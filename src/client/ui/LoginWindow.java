package client.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 登录窗口。
 * 职责：构建用户名/密码输入框和登录/退出按钮的 SWT 界面，
 * 收集用户输入，后续委托 ServerConnection 发送登录请求，
 * 根据服务端应答更新 UI（成功提示 / 失败提示）。
 */
public class LoginWindow {

    // ==================== UI 控件 ====================
    private Shell shell;            // 顶层窗口
    private Text usernameText;      // 用户名输入框
    private Text passwordText;      // 密码输入框
    private Button loginButton;     // 登录按钮
    private Button exitButton;      // 退出按钮

    // ==================== 入口方法 ====================

    /**
     * 打开登录窗口，进入 SWT 事件循环。
     * 该方法会阻塞直到窗口关闭。
     */
    public void open() {
        // --------------------------------------------------
        // 第 1 步：创建 Display — SWT 与操作系统的桥梁
        // --------------------------------------------------
        // Display 代表图形设备的抽象，管理事件循环，
        // 是任何 SWT 应用的起点，一个应用只有一个 Display。
        Display display = new Display();

        // --------------------------------------------------
        // 第 2 步：创建 Shell — 顶层窗口容器
        // --------------------------------------------------
        // Shell 就是操作系统的"窗口"。
        // SWT.SHELL_TRIM 样式 = 标题栏 + 最小化 + 最大化 + 关闭按钮。
        shell = new Shell(display, SWT.SHELL_TRIM);

        // 设置窗口标题
        shell.setText("Java Chat — 登录");

        // ==================================================
        // 第 3 步：设置 GridLayout 布局管理器
        // ==================================================
        // GridLayout 将窗口分割为网格：
        //   numColumns = 2 → 每行两列（标签列 + 输入框列）
        //   marginWidth  → 窗口左右内边距
        //   marginHeight → 窗口上下内边距
        //   verticalSpacing → 行与行之间的垂直间距
        //   horizontalSpacing → 列与列之间的水平间距
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;          // 两列：标签 | 输入框
        layout.marginWidth = 20;        // 左右边距 20px
        layout.marginHeight = 20;       // 上下边距 20px
        layout.verticalSpacing = 10;    // 行间距 10px
        layout.horizontalSpacing = 10;  // 列间距 10px
        shell.setLayout(layout);

        // ==================================================
        // 第 4 步：创建用户名行（标签 + 输入框）
        // ==================================================
        // 4a. "用户名："标签 — 占第一列
        Label usernameLabel = new Label(shell, SWT.NONE);
        usernameLabel.setText("用户名：");

        // 4b. 用户名输入框 — 占第二列
        //     SWT.BORDER 加上边框，让输入框可见
        usernameText = new Text(shell, SWT.BORDER);
        // GridData 控制控件在网格中的布局行为
        // GridData.HORIZONTAL_ALIGN_FILL → 水平方向填满网格单元格
        // GridData.GRAB_HORIZONTAL → 窗口拉伸时，该列跟随拉伸
        GridData usernameGridData = new GridData();
        usernameGridData.horizontalAlignment = GridData.FILL;
        usernameGridData.grabExcessHorizontalSpace = true;
        usernameText.setLayoutData(usernameGridData);

        // ==================================================
        // 第 5 步：创建密码行（标签 + 输入框）
        // ==================================================
        // 5a. "密　码："标签 — 占第一列
        Label passwordLabel = new Label(shell, SWT.NONE);
        passwordLabel.setText("密　码：");

        // 5b. 密码输入框 — 占第二列
        //     SWT.PASSWORD 样式 → 输入内容显示为 ● 圆点，隐藏明文
        //     SWT.BORDER 样式 → 加上边框
        passwordText = new Text(shell, SWT.BORDER | SWT.PASSWORD);
        GridData passwordGridData = new GridData();
        passwordGridData.horizontalAlignment = GridData.FILL;
        passwordGridData.grabExcessHorizontalSpace = true;
        passwordText.setLayoutData(passwordGridData);

        // ==================================================
        // 第 6 步：创建按钮行（水平占两列）
        // ==================================================
        // 6a. 先用一个空的 Label 占位（或创建按钮容器）
        //     这里直接放两个按钮，用水平排列

        // 6b. 登录按钮
        loginButton = new Button(shell, SWT.PUSH);
        loginButton.setText("登录");
        // 按钮占两列宽的一半：水平左对齐，抢占剩余空间
        GridData loginBtnGridData = new GridData();
        loginBtnGridData.horizontalAlignment = GridData.FILL;
        loginBtnGridData.grabExcessHorizontalSpace = true;
        loginBtnGridData.horizontalSpan = 1;
        loginButton.setLayoutData(loginBtnGridData);

        // 6c. 退出按钮
        exitButton = new Button(shell, SWT.PUSH);
        exitButton.setText("退出");
        GridData exitBtnGridData = new GridData();
        exitBtnGridData.horizontalAlignment = GridData.FILL;
        exitBtnGridData.grabExcessHorizontalSpace = true;
        exitButton.setLayoutData(exitBtnGridData);

        // ==================================================
        // 第 7 步：绑定事件监听器
        // ==================================================
        // 7a. 登录按钮点击事件
        //     第一阶段只打印日志，不连接服务器
        loginButton.addListener(SWT.Selection, event -> {
            System.out.println("登录按钮被点击");
        });

        // 7b. 退出按钮点击事件
        //     关闭 Shell → 事件循环退出 → 程序结束
        exitButton.addListener(SWT.Selection, event -> {
            shell.close();
        });

        // 7c. 点击窗口右上角 × 按钮的处理
        //     与退出按钮行为一致：关闭窗口
        shell.addListener(SWT.Close, event -> {
            shell.close();
        });

        // ==================================================
        // 第 8 步：设置窗口大小并居中
        // ==================================================
        // 设置窗口初始尺寸（宽 400px，高 180px）
        shell.setSize(400, 180);

        // 将窗口移动到屏幕正中央
        // 算法：(屏幕尺寸 - 窗口尺寸) / 2
        Rectangle screenBounds = display.getPrimaryMonitor().getBounds();
        Rectangle shellBounds = shell.getBounds();
        int x = (screenBounds.width - shellBounds.width) / 2;
        int y = (screenBounds.height - shellBounds.height) / 2;
        shell.setLocation(x, y);

        // ==================================================
        // 第 9 步：打开窗口并进入事件循环
        // ==================================================
        // shell.open() → 使窗口可见
        shell.open();

        // SWT 事件循环：
        //   while (!shell.isDisposed()) {
        //       if (!display.readAndDispatch())  // 处理事件
        //           display.sleep();             // 无事件时休眠省 CPU
        //   }
        // 这个循环一直运行，直到 shell 被关闭（dispose）
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        // 窗口关闭后，释放 Display 资源
        display.dispose();
    }
}
