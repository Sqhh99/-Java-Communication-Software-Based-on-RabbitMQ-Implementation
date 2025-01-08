package com.ui;
import com.client.Message;
import com.client.SqClient;
import com.client.User;
import com.client.XmlHandler;
import com.rabbitmq.ConnectionUtil;
import com.rabbitmq.SerializationUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ClientFrame extends JFrame{
    JPanel jPanel = new JPanel();
    JLabel hostLabel = new JLabel("I         P");
    JTextField hostTextField = new JTextField(15);
    JLabel portLabel = new JLabel("端口");
    JTextField portTextField = new JTextField(15);
    JLabel userNameLabel = new JLabel("用户名");
    JTextField userNameTextField = new JTextField(20);
    JLabel passWordLabel = new JLabel("密码");
    JTextField passWordTextField = new JTextField(15);
    public static JTextArea noticeTextArea = new JTextArea();
    JLabel msgLabel = new JLabel("消息");
    JButton sendBtn = new JButton("发送");
    JTextField msgTextField = new JTextField(15);
    JButton loginBth = new JButton("登录");
    JButton registerBtn = new JButton("注册");
    JButton connBtn = new JButton("连接");

    Box loginComp = Box.createHorizontalBox();
    Box connComp = Box.createHorizontalBox();
    Box sendComp = Box.createHorizontalBox();
    Box allComp = Box.createVerticalBox();
    Box scrollPaneComp = Box.createHorizontalBox();
    String file_path = "Data.xml";
    DefaultListModel listModel = new DefaultListModel<>();
    JList users = new JList();
    Channel channel;
    SqClient sqClient;
    String queueName = "sq_message_queue";
    String username;
    JLabel fileLabel = new JLabel("文件");
    JTextField fileTextField = new JTextField(15);
    JButton fileSendBtn = new JButton("发送");
    JButton fileChooserBtn = new JButton("...");
    Box fileComp = Box.createHorizontalBox();
    List<String> userNames = new ArrayList<>();
    ActionListener actionListener = e -> {
        if (e.getSource() == loginBth) {
            on_loginBth();
        } else if (e.getSource() == registerBtn) {
            on_registerBtn();
        } else if (e.getSource() == connBtn) {
            on_connBtn();
        } else if (e.getSource() == sendBtn) {
            try {
                on_sendBtn();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else if (e.getSource() == fileChooserBtn) {
            on_fileChooserBtn();
        } else if (e.getSource() == fileSendBtn) {
            on_fileSendBtn();
        }
    };

    String sendFilePath = null;
    void on_connBtn() {
        try {
            // 创建频道
            channel = ConnectionUtil.getChannel();

            List<User> squsers = XmlHandler.readXml(file_path);
            for (User User : squsers) {
                listModel.addElement(User.getUsername());
                userNames.add(User.getUsername());
            }
            connBtn.setEnabled(false);
            noticeTextArea.append("连接成功\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        loginBth.setEnabled(true);
        registerBtn.setEnabled(false);

    }
    void on_loginBth() {
        boolean exists = false;
        try {
            exists = XmlHandler.userExists(file_path, userNameTextField.
                    getText(), passWordTextField.getText());
            if (exists) {
                noticeTextArea.append(userNameTextField.getText() + " 登录成功\n");
                username = userNameTextField.getText();
                sqClient = new SqClient(userNameTextField.getText(), userNames);
                sqClient.start();
                loginBth.setEnabled(false);
                registerBtn.setEnabled(true);
                sendBtn.setEnabled(true);
                fileChooserBtn.setEnabled(true);
            } else noticeTextArea.append(userNameTextField.getText() + "登录失败\n");
        } catch (Exception e) {throw new RuntimeException(e);}}
    void on_registerBtn() {
        try {
            if (!userNameTextField.getText().isEmpty() && !passWordTextField.getText().isEmpty()) {
                List<User> list = new ArrayList<>();
                list.add(new User(userNameTextField.getText(),passWordTextField.getText()));
                XmlHandler.writeXml(file_path,list);
                listModel.addElement(userNameTextField.getText());
                sqClient.configExchange(userNameTextField.getText());
                noticeTextArea.append(userNameTextField.getText() + " 注册成功\n");
            } else noticeTextArea.append("请输入账号和密码\n");
        } catch (Exception e) {throw new RuntimeException(e);}
    }
    void on_fileChooserBtn() {
        // 创建文件选择器
        JFileChooser fileChooser = new JFileChooser();
        // 设置文件选择器只能选择文件
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // 限制文件选择器只能选择 .txt 文件
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
        // 显示文件选择对话框
        int returnValue = fileChooser.showOpenDialog(null);
        // 检查是否选择了文件
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            noticeTextArea.append("选中的文件: " + selectedFile.getAbsolutePath() + "\n");
            fileTextField.setText(selectedFile.getAbsolutePath());
            fileSendBtn.setEnabled(true);
            sendFilePath = selectedFile.getAbsolutePath();
        } else {
            noticeTextArea.append("没有选择文件\n");
        }
    }
    void on_sendBtn() throws IOException {
        if (!msgTextField.getText().isEmpty()) {
            Message msg;
            System.out.println(users.getSelectedValue().toString());
            if (users.getSelectedValue().equals("all")) {
                System.out.println(users.getSelectedValue().toString());
                msg = new Message("all", username, msgTextField.getText(), "all");
            } else {msg = new Message("text", username, msgTextField.
                    getText(), (String) users.getSelectedValue());
            }sqClient.send(msg);}
    }
    void on_fileSendBtn() {
        if (sendFilePath.isEmpty()) {
            noticeTextArea.append("请选择文件\n");
        } else { try {
                Message msg;
                byte[] fileContent = Files.readAllBytes(Paths.get(sendFilePath));
                if (users.getSelectedValue().equals("all")) {
                    System.out.println(users.getSelectedValue().toString());
                    msg = new Message("file", username,Paths.get(sendFilePath)
                            .getFileName().toString(), "all");
                    msg.setFileContent(fileContent);
                } else {
                    msg = new Message("file", username, Paths.get(sendFilePath)
                            .getFileName().toString(), (String) users.getSelectedValue());
                    msg.setFileContent(fileContent);
                }sqClient.send(msg);
            } catch (IOException e) {throw new RuntimeException(e);}}}
    void initLayout() {
        noticeTextArea.setEditable(false);
        users.setModel(listModel);
        JScrollPane LIstScrollPane = new JScrollPane(users);
        JScrollPane noticeScrollPane = new JScrollPane(noticeTextArea);

        LIstScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        noticeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        noticeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // 设置滚动区域的首选大小
        LIstScrollPane.setPreferredSize(new java.awt.Dimension(50, 420));
        noticeScrollPane.setPreferredSize(new java.awt.Dimension(540, 420));

        GUIUtils.addComponents(loginComp, userNameLabel,
                Box.createHorizontalStrut(10),
                userNameTextField,
                Box.createHorizontalStrut(10),
                passWordLabel,
                Box.createHorizontalStrut(10),
                passWordTextField,
                loginBth,
                registerBtn);
        GUIUtils.addComponents(connComp, hostLabel,
                Box.createHorizontalStrut(10),
                hostTextField,
                Box.createHorizontalStrut(10),
                portLabel,
                Box.createHorizontalStrut(10),
                portTextField,
                connBtn);
        GUIUtils.addComponents(sendComp, msgLabel, Box.createHorizontalStrut(10), msgTextField, sendBtn);
        GUIUtils.addComponents(scrollPaneComp, noticeScrollPane, Box.createHorizontalStrut(10), LIstScrollPane);
        GUIUtils.addComponents(fileComp, fileLabel, Box.createHorizontalStrut(10), fileTextField, fileChooserBtn, fileSendBtn);
        GUIUtils.addComponents(allComp, connComp, loginComp, scrollPaneComp, sendComp, fileComp);
        jPanel.add(allComp);
        this.add(jPanel);
    }

    public static synchronized void  addNoticeTextArea(String content) {
        noticeTextArea.append(content);
    }
    void initNum() {
        hostTextField.setText("101.43.120.237");
        portTextField.setText("5672");
        listModel.addElement("all");
        sendBtn.setEnabled(false);
        users.setSelectedIndex(0);
        users.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loginBth.setEnabled(false);
        registerBtn.setEnabled(false);
        fileSendBtn.setEnabled(false);
        sendBtn.setEnabled(false);
        fileChooserBtn.setEnabled(false);
        fileTextField.setEditable(false);
//        fileChooserBtn.setEnabled(false);
    }


    void initEvent() {
        loginBth.addActionListener(actionListener);
        registerBtn.addActionListener(actionListener);
        connBtn.addActionListener(actionListener);
        sendBtn.addActionListener(actionListener);
        fileSendBtn.addActionListener(actionListener);
        fileChooserBtn.addActionListener(actionListener);
    }
    ClientFrame(String title) {
        this.setTitle(title);
        final int WIDTH = 650;
        final int HEIGHT = 580;
        this.setBounds((GUIUtils.getScreenWidth() - WIDTH) / 2, (GUIUtils.getScreenHeight() - HEIGHT) / 2, WIDTH, HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);  //  不允许窗口改变大小
        initLayout();
        initEvent();
        initNum();
    }

    public static void main(String[] args) {
        ClientFrame clientFrame = new ClientFrame("客户端");
        clientFrame.setVisible(true);
    }
}
