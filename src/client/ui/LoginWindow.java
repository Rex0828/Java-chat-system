package client.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 登录窗口 v1.2-login-polish。
 *
 * 职责：构建用户名/密码输入框和登录/退出按钮的 SWT 界面，
 * 收集用户输入，后续委托 ServerConnection 发送登录请求，
 * 根据服务端应答更新 UI（成功提示 / 失败提示）。
 *
 * v1.2 新增：
 *   ① 默认焦点 → usernameText.setFocus()
 *   ② Enter 登录 → passwordText 监听 DefaultSelection 复用登录按钮事件
 *   ③ 状态栏   → statusLabel 实时反馈当前状态
 *   ④ 登录状态反馈 → 校验时同步更新状态栏
 *   ⑤ 按钮状态预留 → setConnectingState() / setReadyState()
 *   ⑥ 窗口最小尺寸 → shell.setMinimumSize(500, 280)
 */
public class LoginWindow {

    // ==================== UI 控件 ====================
    private Display display;        // 【v1.3 提升】Display 从局部变量提升为字段，外部注入
    private Shell shell;            // 顶层窗口
    private Text usernameText;      // 用户名输入框
    private Text passwordText;      // 密码输入框
    private Button loginButton;     // 登录按钮 (Sign In)
    private Button exitButton;      // 退出按钮 (Exit)
    private Label statusLabel;      // 【v1.2 新增】状态栏（窗口底部实时状态提示）

    // ==================== 登录状态 ====================
    // 【v1.3 新增】LoginWindow 关闭后，调用者通过这两个字段判断：
    //   - 用户成功登录了，还是点退出关闭了窗口？
    //   - 成功登录的用户名是什么？
    private boolean loginSuccessful = false;   // 登录是否成功
    private String loggedInUsername = null;    // 成功登录的用户名

    // ==================== 入口方法 ====================

    /**
     * 打开登录窗口，进入 SWT 事件循环。
     * 该方法会阻塞直到窗口关闭。
     *
     * 【v1.3 修改】Display 改为外部注入，不再由 LoginWindow 创建和销毁。
     *   这样调用者（ClientApp）可以复用同一个 Display 打开聊天窗口。
     *
     * @param display 外部创建的 Display 实例（由 ClientApp 管理生命周期）
     */
    public void open(Display display) {
        // --------------------------------------------------
        // 第 1 步：保存 Display 引用 — SWT 与操作系统的桥梁
        // --------------------------------------------------
        // 【v1.3 修改】Display 由外部注入，不再 new。
        //   Display 代表图形设备的抽象，管理事件循环，
        //   是任何 SWT 应用的起点，一个应用只有一个 Display。
        this.display = display;

        // --------------------------------------------------
        // 第 2 步：创建 Shell — 顶层窗口容器
        // --------------------------------------------------
        // Shell 就是操作系统的"窗口"。
        // SWT.SHELL_TRIM 样式 = 标题栏 + 最小化 + 最大化 + 关闭按钮。
        shell = new Shell(display, SWT.SHELL_TRIM);

        // 设置窗口标题（标题栏文字）
        shell.setText("Java Chat System");

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
        layout.marginWidth = 30;        // 左右边距 30px（比之前宽，现代风格）
        layout.marginHeight = 25;       // 上下边距 25px
        layout.verticalSpacing = 12;    // 行间距 12px
        layout.horizontalSpacing = 12;  // 列间距 12px
        shell.setLayout(layout);

        // ==================================================
        // 第 3.5 步：创建标题区域（跨两列）
        // ==================================================
        // 3.5a. 主标题 — "Java Chat System"
        //       horizontalSpan = 2 → 占满两列
        //       SWT.CENTER → 文字水平居中
        Label titleLabel = new Label(shell, SWT.CENTER);
        titleLabel.setText("Java Chat System");

        // 从系统默认字体派生更大的粗体字作为标题字体
        FontData[] fontData = display.getSystemFont().getFontData();
        fontData[0].setHeight(18);           // 字号 18pt
        fontData[0].setStyle(SWT.BOLD);      // 加粗
        Font titleFont = new Font(display, fontData[0]);
        titleLabel.setFont(titleFont);

        // 标题的 GridData：跨两列、水平填满、居中
        GridData titleGridData = new GridData();
        titleGridData.horizontalSpan = 2;           // 跨两列
        titleGridData.horizontalAlignment = GridData.FILL;
        titleGridData.grabExcessHorizontalSpace = true;
        titleGridData.verticalIndent = 5;           // 顶部额外留白 5px
        titleLabel.setLayoutData(titleGridData);

        // 3.5b. 副标题 — "Java Network Programming Course Project"
        Label subtitleLabel = new Label(shell, SWT.CENTER);
        subtitleLabel.setText("Java Network Programming Course Project");

        // 副标题用系统默认字体，颜色由系统主题决定
        GridData subtitleGridData = new GridData();
        subtitleGridData.horizontalSpan = 2;        // 跨两列
        subtitleGridData.horizontalAlignment = GridData.FILL;
        subtitleGridData.grabExcessHorizontalSpace = true;
        subtitleGridData.verticalIndent = 0;        // 紧跟主标题
        subtitleLabel.setLayoutData(subtitleGridData);

        // 3.5c. 标题与表单之间的分隔间距
        //       用一个高度为 10px 的空 Label 作为视觉分隔
        Label spacerLabel = new Label(shell, SWT.NONE);
        GridData spacerGridData = new GridData();
        spacerGridData.horizontalSpan = 2;
        spacerGridData.heightHint = 10;             // 固定高度 10px
        spacerLabel.setLayoutData(spacerGridData);

        // ==================================================
        // 第 4 步：创建用户名行（标签 + 输入框）
        // ==================================================
        // 4a. "Username" 标签 — 占第一列
        Label usernameLabel = new Label(shell, SWT.NONE);
        usernameLabel.setText("Username");

        // 4b. 用户名输入框 — 占第二列
        //     SWT.BORDER 加上边框，让输入框可见
        usernameText = new Text(shell, SWT.BORDER);
        // GridData 控制控件在网格中的布局行为
        // GridData.HORIZONTAL_ALIGN_FILL → 水平方向填满网格单元格
        // GridData.GRAB_HORIZONTAL → 窗口拉伸时，该列跟随拉伸
        GridData usernameGridData = new GridData();
        usernameGridData.horizontalAlignment = GridData.FILL;
        usernameGridData.grabExcessHorizontalSpace = true;
        usernameGridData.widthHint = 200;           // 输入框最小宽度
        usernameText.setLayoutData(usernameGridData);

        // ==================================================
        // 第 5 步：创建密码行（标签 + 输入框）
        // ==================================================
        // 5a. "Password" 标签 — 占第一列
        Label passwordLabel = new Label(shell, SWT.NONE);
        passwordLabel.setText("Password");

        // 5b. 密码输入框 — 占第二列
        //     SWT.PASSWORD 样式 → 输入内容显示为 ● 圆点，隐藏明文
        //     SWT.BORDER 样式 → 加上边框
        passwordText = new Text(shell, SWT.BORDER | SWT.PASSWORD);
        GridData passwordGridData = new GridData();
        passwordGridData.horizontalAlignment = GridData.FILL;
        passwordGridData.grabExcessHorizontalSpace = true;
        passwordGridData.widthHint = 200;           // 输入框最小宽度
        passwordText.setLayoutData(passwordGridData);

        // ==================================================
        // 第 6 步：创建按钮行（水平占两列）
        // ==================================================
        // 6a. 按钮行上方留一点间距
        //     通过 verticalIndent 在第一个按钮上实现

        // 6b. 登录按钮 — Sign In
        loginButton = new Button(shell, SWT.PUSH);
        loginButton.setText("Sign In");
        // 按钮占两列宽的一半：水平填满，抢占剩余空间
        GridData loginBtnGridData = new GridData();
        loginBtnGridData.horizontalAlignment = GridData.FILL;
        loginBtnGridData.grabExcessHorizontalSpace = true;
        loginBtnGridData.verticalIndent = 5;        // 按钮行上方留 5px 间距
        loginButton.setLayoutData(loginBtnGridData);

        // 6c. 退出按钮 — Exit
        exitButton = new Button(shell, SWT.PUSH);
        exitButton.setText("Exit");
        GridData exitBtnGridData = new GridData();
        exitBtnGridData.horizontalAlignment = GridData.FILL;
        exitBtnGridData.grabExcessHorizontalSpace = true;
        exitBtnGridData.verticalIndent = 5;         // 与登录按钮对齐
        exitButton.setLayoutData(exitBtnGridData);

        // ==================================================
        // 第 6.5 步：【v1.2 新增】状态栏（窗口底部，跨两列）
        // ==================================================
        // 设计意图：
        //   状态栏提供非阻塞的实时反馈 —— 用户无需关闭弹窗就能看到当前状态。
        //   与 MessageBox 互补：状态栏显示状态变化，MessageBox 做阻塞式交互。
        //   后续接入服务端时，可即时显示 "Connecting..." / "Login Success" / "Login Failed"。
        //   横跨两列、左对齐，符合桌面应用状态栏惯例。
        statusLabel = new Label(shell, SWT.NONE);
        statusLabel.setText("Status: Ready");
        GridData statusGridData = new GridData();
        statusGridData.horizontalSpan = 2;              // 跨两列
        statusGridData.horizontalAlignment = GridData.FILL;
        statusGridData.grabExcessHorizontalSpace = true;
        statusGridData.verticalIndent = 8;              // 与按钮行保持 8px 间距
        statusLabel.setLayoutData(statusGridData);

        // ==================================================
        // 第 7 步：绑定事件监听器
        // ==================================================
        // 7a. 登录按钮点击事件 — 表单验证 + MessageBox 反馈 + 状态栏更新
        loginButton.addListener(SWT.Selection, event -> {
            // 读取用户输入，去除首尾空白字符
            String username = usernameText.getText().trim();
            String password = passwordText.getText().trim();

            // 校验 1：用户名为空 → 弹出警告对话框 + 更新状态栏
            if (username.isEmpty()) {
                // 【v1.2 新增】状态栏即时反馈校验失败原因
                statusLabel.setText("Status: Username Required");
                // MessageBox 是 SWT 的原生系统对话框
                //   SWT.ICON_WARNING → 显示黄色警告图标
                //   SWT.OK → 只显示一个"确定"按钮
                MessageBox emptyUserBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
                emptyUserBox.setText("提示");               // 对话框标题栏文字
                emptyUserBox.setMessage("请输入用户名");     // 对话框正文内容
                emptyUserBox.open();                        // 打开对话框（模态：阻塞直到用户点击确定）
                return;                                     // 提前返回，不再执行后续校验
            }

            // 校验 2：密码为空 → 弹出警告对话框 + 更新状态栏
            if (password.isEmpty()) {
                // 【v1.2 新增】状态栏即时反馈校验失败原因
                statusLabel.setText("Status: Password Required");
                MessageBox emptyPassBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
                emptyPassBox.setText("提示");
                emptyPassBox.setMessage("请输入密码");
                emptyPassBox.open();
                return;
            }

            // 校验 3：用户名和密码均非空 → 状态栏成功提示 + 弹出欢迎对话框
            // 【v1.2 新增】状态栏更新为登录成功状态
            statusLabel.setText("Status: Login Success");

            // 【v1.3 新增】记录登录状态，供 ClientApp 在 open() 返回后读取
            loginSuccessful = true;
            loggedInUsername = username;

            MessageBox welcomeBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
            welcomeBox.setText("登录成功");                  // SWT.ICON_INFORMATION → 蓝色信息图标
            welcomeBox.setMessage("欢迎你：" + username);    // 拼接欢迎语
            welcomeBox.open();

            // 【v1.3 新增】登录成功后关闭登录窗口
            //   shell.close() → 触发 SWT.Close 事件 → shell.dispose()
            //   → while (!shell.isDisposed()) 循环退出 → open() 返回
            //   → ClientApp 继续执行，打开聊天窗口
            shell.close();
            // TODO: 后续阶段此处将改为调用 ServerConnection 连接服务端验证
        });

        // 7a2. 【v1.2 新增】密码框 Enter 键 → 自动触发登录
        //       设计意图：
        //         真实软件登录界面标准行为 —— 用户在密码框按 Enter 即可登录，
        //         无需鼠标点击按钮，提升操作效率。
        //       实现方式：
        //         使用 SWT.DefaultSelection（文本控件的原生 Enter 事件）监听密码框。
        //         在监听器中调用 loginButton.notifyListeners() ——
        //         这会触发按钮上已注册的 SWT.Selection 监听器，
        //         从而复用已有校验逻辑，零代码重复。
        //         notifyListeners 需要传入一个 Event 对象（new Event() 即可，
        //         按钮监听器只使用了 event 的 type 字段，不依赖其他字段）。
        passwordText.addListener(SWT.DefaultSelection, event -> {
            loginButton.notifyListeners(SWT.Selection, new Event());
        });

        // 7b. 退出按钮点击事件
        //     关闭 Shell → 事件循环退出 → 程序结束
        exitButton.addListener(SWT.Selection, event -> {
            shell.close();
        });

        // 7c. 点击窗口右上角 × 按钮的处理
        //     与退出按钮行为一致：关闭窗口
        

        // ==================================================
        // 第 8 步：设置窗口大小并居中
        // ==================================================
        // 设置窗口初始尺寸（宽 500px，高 280px）
        shell.setSize(500, 280);

        // 【v1.2 新增】设置窗口最小尺寸
        //   防止用户拖拽窗口过小导致布局错乱。
        //   500×280 与初始尺寸一致，确保所有控件始终可见。
        shell.setMinimumSize(500, 280);

        // 将窗口移动到屏幕正中央
        // 算法：(屏幕尺寸 - 窗口尺寸) / 2
        Rectangle screenBounds = display.getPrimaryMonitor().getBounds();
        Rectangle shellBounds = shell.getBounds();
        int x = (screenBounds.width - shellBounds.width) / 2;
        int y = (screenBounds.height - shellBounds.height) / 2;
        shell.setLocation(x, y);

        // 【v1.2 新增】初始化按钮状态为就绪
        //   确保窗口首次打开时按钮处于正确的交互状态。
        //   后续接入服务端时，在发送网络请求前调用 setConnectingState() 即可。
        setReadyState();

        // ==================================================
        // 第 9 步：打开窗口并进入事件循环
        // ==================================================
        // shell.open() → 使窗口可见
        shell.open();

        // 【v1.2 新增】默认焦点设置
        //   窗口显示后，光标自动定位到用户名输入框。
        //   shell.open() 之后调用 setFocus() —— SWT 要求控件必须可见后才能设置焦点。
        //   这样用户无需点击即可直接输入用户名，符合登录界面的用户体验标准。
        usernameText.setFocus();

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

        // --------------------------------------------------
        // 释放 SWT 图形资源（按创建顺序的逆序）
        // --------------------------------------------------
        // 【v1.3 修改】display.dispose() 已移除 — Display 生命周期由 ClientApp 管理。
        //   LoginWindow 只负责释放自己创建的 SWT 资源（字体、颜色等）。
        titleFont.dispose();    // 标题字体
    }

    // ==================================================
    // 【v1.2 新增】按钮状态管理方法
    // ==================================================

    /**
     * 切换到"正在连接"状态。
     *
     * 调用时机：用户点击 Sign In → 校验通过 → 即将调用 ServerConnection 之前。
     * 效果：
     *   ① 禁用 Sign In 按钮（防止重复点击导致多次连接）
     *   ② 按钮文字变为 "Connecting..."（给用户即时视觉反馈）
     *
     * 设计意图：
     *   网络连接可能耗时数秒。如果按钮不禁用，
     *   用户可能在等待期间多次点击，导致重复提交或 UI 异常。
     *   这是桌面应用的标准防御性设计。
     *
     * 后续接入流程：
     *   loginButton 点击 → 校验 → setConnectingState() → ServerConnection 连接 → 结果返回 → setReadyState()
     */
    private void setConnectingState() {
        loginButton.setEnabled(false);          // 禁用按钮：阻止重复点击
        loginButton.setText("Connecting...");   // 文字反馈：告知用户正在处理
    }

    /**
     * 恢复到"就绪"状态。
     *
     * 调用时机：服务端应答返回后（无论成功或失败），或窗口初始化时。
     * 效果：
     *   ① 恢复 Sign In 按钮可用
     *   ② 按钮文字恢复为 "Sign In"
     *
     * 设计意图：
     *   与 setConnectingState() 配对使用。
     *   登录请求完成后（成功/失败/超时/异常），需要让用户可以再次尝试登录。
     */
    private void setReadyState() {
        loginButton.setEnabled(true);           // 恢复按钮：允许再次点击
        loginButton.setText("Sign In");         // 文字恢复：告知用户可以登录
    }

    // ==================================================
    // 【v1.3 新增】登录状态查询方法
    // ==================================================

    /**
     * 查询用户是否成功登录。
     * ClientApp 在 open() 返回后调用此方法判断下一步操作。
     *
     * @return true = 用户通过校验并点击了 Sign In，false = 用户点击了 Exit 或 ×
     */
    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

    /**
     * 获取成功登录的用户名。
     * 仅在 isLoginSuccessful() 返回 true 时有意义。
     *
     * @return 用户输入的用户名（已 trim）
     */
    public String getLoggedInUsername() {
        return loggedInUsername;
    }
}
