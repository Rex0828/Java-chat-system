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
 * 登录窗口
 */
public class LoginWindow {

    private Display display;
    private Shell shell;
    private Text usernameText;
    private Text passwordText;
    private Button loginButton;
    private Button exitButton;
    private Label statusLabel;

    private boolean loginSuccessful = false;
    private String loggedInUsername = null;

    /**
     * 打开登录窗口
     */
    public void open(Display display) {
        this.display = display;

        shell = new Shell(display, SWT.SHELL_TRIM);
        shell.setText("Java Chat System");

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 30;
        layout.marginHeight = 25;
        layout.verticalSpacing = 12;
        layout.horizontalSpacing = 12;
        shell.setLayout(layout);

        // 标题
        Label titleLabel = new Label(shell, SWT.CENTER);
        titleLabel.setText("Java Chat System");
        FontData[] fontData = display.getSystemFont().getFontData();
        fontData[0].setHeight(18);
        fontData[0].setStyle(SWT.BOLD);
        Font titleFont = new Font(display, fontData[0]);
        titleLabel.setFont(titleFont);

        GridData titleGridData = new GridData();
        titleGridData.horizontalSpan = 2;
        titleGridData.horizontalAlignment = GridData.FILL;
        titleGridData.grabExcessHorizontalSpace = true;
        titleGridData.verticalIndent = 5;
        titleLabel.setLayoutData(titleGridData);

        // 副标题
        Label subtitleLabel = new Label(shell, SWT.CENTER);
        subtitleLabel.setText("Java Network Programming Course Project");
        GridData subtitleGridData = new GridData();
        subtitleGridData.horizontalSpan = 2;
        subtitleGridData.horizontalAlignment = GridData.FILL;
        subtitleGridData.grabExcessHorizontalSpace = true;
        subtitleGridData.verticalIndent = 0;
        subtitleLabel.setLayoutData(subtitleGridData);

        // 分隔间距
        Label spacerLabel = new Label(shell, SWT.NONE);
        GridData spacerGridData = new GridData();
        spacerGridData.horizontalSpan = 2;
        spacerGridData.heightHint = 10;
        spacerLabel.setLayoutData(spacerGridData);

        // 用户名
        Label usernameLabel = new Label(shell, SWT.NONE);
        usernameLabel.setText("Username");
        usernameText = new Text(shell, SWT.BORDER);
        GridData usernameGridData = new GridData();
        usernameGridData.horizontalAlignment = GridData.FILL;
        usernameGridData.grabExcessHorizontalSpace = true;
        usernameGridData.widthHint = 200;
        usernameText.setLayoutData(usernameGridData);

        // 密码
        Label passwordLabel = new Label(shell, SWT.NONE);
        passwordLabel.setText("Password");
        passwordText = new Text(shell, SWT.BORDER | SWT.PASSWORD);
        GridData passwordGridData = new GridData();
        passwordGridData.horizontalAlignment = GridData.FILL;
        passwordGridData.grabExcessHorizontalSpace = true;
        passwordGridData.widthHint = 200;
        passwordText.setLayoutData(passwordGridData);

        // 登录按钮
        loginButton = new Button(shell, SWT.PUSH);
        loginButton.setText("Sign In");
        GridData loginBtnGridData = new GridData();
        loginBtnGridData.horizontalAlignment = GridData.FILL;
        loginBtnGridData.grabExcessHorizontalSpace = true;
        loginBtnGridData.verticalIndent = 5;
        loginButton.setLayoutData(loginBtnGridData);

        // 退出按钮
        exitButton = new Button(shell, SWT.PUSH);
        exitButton.setText("Exit");
        GridData exitBtnGridData = new GridData();
        exitBtnGridData.horizontalAlignment = GridData.FILL;
        exitBtnGridData.grabExcessHorizontalSpace = true;
        exitBtnGridData.verticalIndent = 5;
        exitButton.setLayoutData(exitBtnGridData);

        // 状态栏
        statusLabel = new Label(shell, SWT.NONE);
        statusLabel.setText("Status: Ready");
        GridData statusGridData = new GridData();
        statusGridData.horizontalSpan = 2;
        statusGridData.horizontalAlignment = GridData.FILL;
        statusGridData.grabExcessHorizontalSpace = true;
        statusGridData.verticalIndent = 8;
        statusLabel.setLayoutData(statusGridData);

        // 登录按钮事件
        loginButton.addListener(SWT.Selection, event -> {
            String username = usernameText.getText().trim();
            String password = passwordText.getText().trim();

            if (username.isEmpty()) {
                statusLabel.setText("Status: Username Required");
                MessageBox emptyUserBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
                emptyUserBox.setText("提示");
                emptyUserBox.setMessage("请输入用户名");
                emptyUserBox.open();
                return;
            }

            if (password.isEmpty()) {
                statusLabel.setText("Status: Password Required");
                MessageBox emptyPassBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
                emptyPassBox.setText("提示");
                emptyPassBox.setMessage("请输入密码");
                emptyPassBox.open();
                return;
            }

            statusLabel.setText("Status: Login Success");
            loginSuccessful = true;
            loggedInUsername = username;

            MessageBox welcomeBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
            welcomeBox.setText("登录成功");
            welcomeBox.setMessage("欢迎你：" + username);
            welcomeBox.open();
            shell.close();
        });

        // 密码框 Enter 自动登录
        passwordText.addListener(SWT.DefaultSelection, event -> {
            loginButton.notifyListeners(SWT.Selection, new Event());
        });

        // 退出按钮事件
        exitButton.addListener(SWT.Selection, event -> {
            shell.close();
        });

        shell.setSize(500, 280);
        shell.setMinimumSize(500, 280);

        Rectangle screenBounds = display.getPrimaryMonitor().getBounds();
        Rectangle shellBounds = shell.getBounds();
        int x = (screenBounds.width - shellBounds.width) / 2;
        int y = (screenBounds.height - shellBounds.height) / 2;
        shell.setLocation(x, y);

        setReadyState();
        shell.open();
        usernameText.setFocus();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        titleFont.dispose();
    }

    private void setConnectingState() {
        loginButton.setEnabled(false);
        loginButton.setText("Connecting...");
    }

    private void setReadyState() {
        loginButton.setEnabled(true);
        loginButton.setText("Sign In");
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

    public String getLoggedInUsername() {
        return loggedInUsername;
    }
}
