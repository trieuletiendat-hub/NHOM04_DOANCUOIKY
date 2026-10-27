/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.chat1v1;
import javax.swing.DefaultListModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.net.Socket;
import java.nio.file.Files;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 *
 * @author TienDat
 */
public class frmGroupChat extends javax.swing.JFrame {
private final Color BACKGROUND = new Color(15, 23, 42);
    private final Color PANEL = new Color(30, 41, 59);
    private final Color BLUE = new Color(37, 99, 235);
    private final Color TEXT = new Color(226, 232, 240);
    private final Color MUTED = new Color(148, 163, 184);
    private DefaultListModel<String> memberModel = new DefaultListModel<>();
    private String host;
   // ===== COMPONENT =====

    private JLabel lblStatus;

    // ===== TCP =====
    private volatile Socket socket;
    private volatile BufferedReader reader;
    private volatile PrintWriter writer;
    private volatile boolean connected = false;
    
    private String username = "Guest";
    private volatile Socket fileReceiveSocket;
private volatile boolean fileReceiverRunning = false;

private static final long MAX_FILE_SIZE =
        50L * 1024 * 1024;
    /**
     * Creates new form frmGroupChatt
     */
    // Constructor mặc định (phòng hờ)
    public frmGroupChat() {
        initComponents();
        setupUIState();
    }

    // Constructor mới nhận trực tiếp username và host từ frmChat truyền sang
    public frmGroupChat(String username, String host) {
        initComponents();
        this.username = (username == null || username.isEmpty()) ? "Guest" : username;
        this.host = (host == null || host.isEmpty()) ? "localhost" : host;
        
        // Đưa thông tin vào các ô hiển thị
        txtUsername.setText(this.username);
        txtHost.setText(this.host);
        
        // Khóa các ô nhập liệu và ẩn nút kết nối thủ công vì đã tự động hóa hoàn toàn
        txtUsername.setEnabled(false);
        txtHost.setEnabled(false);
        btnConnect.setVisible(false);

        setupUIState();
        
        // Tự động chạy tiến trình kết nối vào group chat server
        autoConnectGroup();
    }

    

private void setupModernGroupUI() {
    Color primary = new Color(81, 70, 216);
    Color primaryHover = new Color(67, 56, 202);
    Color background = new Color(245, 246, 252);
    Color white = Color.WHITE;
    Color text = new Color(39, 42, 68);
    Color muted = new Color(115, 120, 145);
    Color border = new Color(225, 227, 240);
    Color chatBackground = new Color(250, 250, 255);

    Font normal = new Font("Segoe UI", Font.PLAIN, 14);
    Font bold = new Font("Segoe UI", Font.BOLD, 14);

    // Cấu hình cửa sổ
    getContentPane().removeAll();
    getContentPane().setLayout(new BorderLayout(0, 0));
    getContentPane().setBackground(background);

    // =========================
    // 1. THANH TIÊU ĐỀ
    // =========================
    JPanel header = new JPanel(new BorderLayout(16, 0));
    header.setBackground(white);
    header.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, border),
        new EmptyBorder(14, 22, 14, 22)
    ));

    JPanel brandPanel = new JPanel();
    brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
    brandPanel.setBackground(white);

    jLabel1.setText("CHAT CONNECT");
    jLabel1.setFont(new Font("Segoe UI", Font.BOLD, 21));
    jLabel1.setForeground(primary);
    jLabel1.setAlignmentX(Component.LEFT_ALIGNMENT);

    jLabel5.setText("Trò chuyện nhóm");
    jLabel5.setFont(new Font("Segoe UI", Font.BOLD, 16));
    jLabel5.setForeground(text);
    jLabel5.setAlignmentX(Component.LEFT_ALIGNMENT);

    brandPanel.add(jLabel1);
    brandPanel.add(Box.createVerticalStrut(4));
    brandPanel.add(jLabel5);

    JPanel connectionPanel = new JPanel(new FlowLayout(
        FlowLayout.RIGHT, 8, 4
    ));
    connectionPanel.setBackground(white);

    jLabel3.setText("Người dùng");
    jLabel3.setForeground(muted);
    jLabel3.setFont(normal);

    jLabel2.setText("Server IP");
    jLabel2.setForeground(muted);
    jLabel2.setFont(normal);

    txtUsername.setFont(normal);
    txtHost.setFont(normal);
    txtUsername.setPreferredSize(new Dimension(110, 34));
    txtHost.setPreferredSize(new Dimension(135, 34));

    styleTextField(txtUsername, text, border, white);
    styleTextField(txtHost, text, border, white);

    styleGroupButton(btnConnect, "Kết nối", primary, primaryHover);

    connectionPanel.add(jLabel3);
    connectionPanel.add(txtUsername);
    connectionPanel.add(jLabel2);
    connectionPanel.add(txtHost);
    connectionPanel.add(btnConnect);

    header.add(brandPanel, BorderLayout.WEST);
    header.add(connectionPanel, BorderLayout.EAST);

    // =========================
    // 2. DANH SÁCH THÀNH VIÊN
    // =========================
    JPanel memberPanel = new JPanel(new BorderLayout(0, 12));
    memberPanel.setBackground(white);
    memberPanel.setPreferredSize(new Dimension(220, 100));
    memberPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(border),
        new EmptyBorder(16, 14, 14, 14)
    ));

    jLabel4.setText("Thành viên trong nhóm");
    jLabel4.setFont(bold);
    jLabel4.setForeground(primary);

    lstMembers.setModel(memberModel);
    lstMembers.setFont(normal);
    lstMembers.setForeground(text);
    lstMembers.setBackground(white);
    lstMembers.setSelectionBackground(new Color(232, 229, 255));
    lstMembers.setSelectionForeground(primary);
    lstMembers.setFixedCellHeight(38);
    lstMembers.setBorder(new EmptyBorder(5, 5, 5, 5));

    jScrollPane2.setBorder(BorderFactory.createEmptyBorder());
    jScrollPane2.setViewportView(lstMembers);
    jScrollPane2.getViewport().setBackground(white);

    memberPanel.add(jLabel4, BorderLayout.NORTH);
    memberPanel.add(jScrollPane2, BorderLayout.CENTER);

    // =========================
    // 3. KHUNG TRÒ CHUYỆN
    // =========================
    JPanel chatPanel = new JPanel(new BorderLayout(0, 12));
    chatPanel.setBackground(white);
    chatPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(border),
        new EmptyBorder(16, 16, 16, 16)
    ));

    JPanel chatHeader = new JPanel(new BorderLayout());
    chatHeader.setBackground(white);

    JLabel chatTitle = new JLabel("Phòng chat nhóm");
    chatTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
    chatTitle.setForeground(text);

    lblStatus = new JLabel("Đang chờ kết nối...");
    lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    lblStatus.setForeground(muted);

    JPanel titlePanel = new JPanel();
    titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
    titlePanel.setBackground(white);
    titlePanel.add(chatTitle);
    titlePanel.add(Box.createVerticalStrut(4));
    titlePanel.add(lblStatus);

    chatHeader.add(titlePanel, BorderLayout.WEST);

    txtChat.setEditable(false);
    txtChat.setLineWrap(true);
    txtChat.setWrapStyleWord(true);
    txtChat.setFont(normal);
    txtChat.setForeground(text);
    txtChat.setBackground(chatBackground);
    txtChat.setCaretColor(primary);
    txtChat.setMargin(new Insets(14, 14, 14, 14));

    jScrollPane3.setBorder(BorderFactory.createLineBorder(border));
    jScrollPane3.setViewportView(txtChat);
    jScrollPane3.getViewport().setBackground(chatBackground);

    // =========================
    // 4. THANH NHẬP TIN NHẮN
    // =========================
    JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
    inputPanel.setBackground(white);
    inputPanel.setBorder(new EmptyBorder(4, 0, 0, 0));

    txtMessage.setFont(normal);
    txtMessage.setPreferredSize(new Dimension(100, 42));
    txtMessage.setToolTipText("Nhập tin nhắn gửi đến cả nhóm");
    styleTextField(txtMessage, text, border, white);

    styleGroupButton(btnSend, "Gửi", primary, primaryHover);
    styleGroupButton(
        btnSendFile, "Đính kèm tệp",
        new Color(56, 120, 210),
        new Color(37, 99, 190)
    );

    JPanel actionPanel = new JPanel(new FlowLayout(
        FlowLayout.RIGHT, 8, 0
    ));
    actionPanel.setBackground(white);
    actionPanel.add(btnSendFile);
    actionPanel.add(btnSend);

    inputPanel.add(txtMessage, BorderLayout.CENTER);
    inputPanel.add(actionPanel, BorderLayout.EAST);

    chatPanel.add(chatHeader, BorderLayout.NORTH);
    chatPanel.add(jScrollPane3, BorderLayout.CENTER);
    chatPanel.add(inputPanel, BorderLayout.SOUTH);

    // Nhấn Enter để gửi tin nhắn
    txtMessage.addActionListener(e -> btnSend.doClick());

    // =========================
    // 5. GHÉP BỐ CỤC
    // =========================
    JPanel body = new JPanel(new BorderLayout(14, 0));
    body.setBackground(background);
    body.setBorder(new EmptyBorder(16, 18, 18, 18));

    body.add(memberPanel, BorderLayout.WEST);
    body.add(chatPanel, BorderLayout.CENTER);

    getContentPane().add(header, BorderLayout.NORTH);
    getContentPane().add(body, BorderLayout.CENTER);

    setMinimumSize(new Dimension(850, 560));
    setSize(1050, 680);

    revalidate();
    repaint();
}

private void styleTextField(
        JTextField field,
        Color foreground,
        Color borderColor,
        Color background) {

    field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    field.setForeground(foreground);
    field.setBackground(background);
    field.setCaretColor(new Color(81, 70, 216));
    field.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(borderColor),
        new EmptyBorder(7, 10, 7, 10)
    ));
}


private void styleGroupButton(
        JButton button,
        String title,
        Color normalColor,
        Color hoverColor) {

    button.setText(title);
    button.setFont(new Font("Segoe UI", Font.BOLD, 13));
    button.setForeground(Color.WHITE);
    button.setBackground(normalColor);
    button.setOpaque(true);
    button.setContentAreaFilled(true);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    button.setBorder(
        BorderFactory.createEmptyBorder(9, 16, 9, 16)
    );

    // Tránh đăng ký nhiều bộ sự kiện khi hàm được gọi lại
    for (MouseListener listener : button.getMouseListeners()) {
        if (listener instanceof GroupButtonHoverListener) {
            button.removeMouseListener(listener);
        }
    }

    button.addMouseListener(
        new GroupButtonHoverListener(button, normalColor, hoverColor)
    );
}

private static class GroupButtonHoverListener extends MouseAdapter {
    private final JButton button;
    private final Color normalColor;
    private final Color hoverColor;

    GroupButtonHoverListener(
            JButton button,
            Color normalColor,
            Color hoverColor) {
        this.button = button;
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (button.isEnabled()) {
            button.setBackground(hoverColor);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        button.setBackground(normalColor);
    }
}

    
private void setupUIState() {
    setTitle("Chat Connect - Trò chuyện nhóm");
    setDefaultCloseOperation(
        javax.swing.WindowConstants.DISPOSE_ON_CLOSE
    );

    // Kích thước và vị trí cửa sổ
    setSize(950, 620);
    setMinimumSize(new Dimension(800, 520));
    setLocationRelativeTo(null);

    // Danh sách thành viên
    lstMembers.setModel(memberModel);

    // Áp dụng giao diện đồng bộ Chat Connect
    setupModernGroupUI();

    // Trạng thái ban đầu
    btnSend.setEnabled(false);
    btnSendFile.setEnabled(false);

    txtChat.setEditable(false);
    txtChat.setLineWrap(true);
    txtChat.setWrapStyleWord(true);
}

    private void autoConnectGroup() {
        Thread thread = new Thread(() -> {
            Socket newSocket = null;
            try {
                newSocket = new Socket(host, 5001);

                BufferedReader newReader = new BufferedReader(
                    new InputStreamReader(newSocket.getInputStream(), StandardCharsets.UTF_8)
                );

                PrintWriter newWriter = new PrintWriter(
                    newSocket.getOutputStream(), true, StandardCharsets.UTF_8
                );

                // Gửi tên người dùng đến Group Chat Server (Cổng 5001)
                newWriter.println(username);

                socket = newSocket;
                reader = newReader;
                writer = newWriter;
                connected = true;

                SwingUtilities.invokeLater(() -> {
            btnSend.setEnabled(true);
            btnSendFile.setEnabled(true);

            if (lblStatus != null) {
                lblStatus.setText("Đã kết nối • " + host);
                lblStatus.setForeground(new Color(22, 163, 74));
            }

            appendMessage("[SYSTEM] Đã kết nối phòng chat nhóm.");
        });

                startFileReceiver(host);

                String message;
                while (connected && (message = newReader.readLine()) != null) {
                    if (message.startsWith("[MEMBERS]")) {
                        String names = message.substring("[MEMBERS]".length());
                        SwingUtilities.invokeLater(() -> updateMembers(names));
                    } else {
                        String received = message;
                        SwingUtilities.invokeLater(() -> appendMessage(received));
                    }
                }

            } catch (IOException ex) {
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(
                        this,
                        "Không thể kết nối đến Group Chat Server (Cổng 5001)!\n" + ex.getMessage()
                    )
                );
            } finally {
                if (newSocket != null) {
                    try {
                        newSocket.close();
                    } catch (IOException ignored) {}
                }
                socket = null;
                reader = null;
                writer = null;
                connected = false;
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstMembers = new javax.swing.JList<>();
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        txtMessage = new javax.swing.JTextField();
        btnSend = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtChat = new javax.swing.JTextArea();
        btnSendFile = new javax.swing.JButton();
        btnConnect = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        txtHost = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane2.setViewportView(lstMembers);

        jLabel4.setText("Thành viên");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel4)))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(130, Short.MAX_VALUE))
        );

        txtMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMessageActionPerformed(evt);
            }
        });

        btnSend.setText("Gửi");
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });

        jLabel5.setText("Trò Chuyện Nhóm");

        txtChat.setColumns(20);
        txtChat.setRows(5);
        jScrollPane3.setViewportView(txtChat);

        btnSendFile.setText("Gửi Tệp");
        btnSendFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendFileActionPerformed(evt);
            }
        });

        btnConnect.setText("Kết nối");
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 461, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(txtMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 319, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSendFile)))
                .addContainerGap(59, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnConnect)
                .addGap(20, 20, 20))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(9, 9, 9))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(btnConnect)
                        .addGap(18, 18, 18)))
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSend)
                    .addComponent(btnSendFile))
                .addContainerGap(134, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel1.setText("GroupChat");
        jLabel1.setMaximumSize(new java.awt.Dimension(100, 16));

        jLabel2.setText("Sever IP");

        jLabel3.setText("Tên người dùng");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(76, 76, 76)
                .addComponent(jLabel2)
                .addGap(32, 32, 32)
                .addComponent(txtHost)
                .addGap(48, 48, 48))
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMessageActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMessageActionPerformed

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed

    
    String host = txtHost.getText().trim();
    username = txtUsername.getText().trim();

    if (host.isEmpty() || username.isEmpty()) {
        JOptionPane.showMessageDialog(
            this, "Vui lòng nhập IP và tên người dùng!"
        );
        return;
    }

    btnConnect.setEnabled(false);

    Thread thread = new Thread(() -> {
        Socket newSocket = null;

        try {
            newSocket = new Socket(host, 5001);

            BufferedReader newReader = new BufferedReader(
                new InputStreamReader(
                    newSocket.getInputStream(),
                    StandardCharsets.UTF_8
                )
            );

            PrintWriter newWriter = new PrintWriter(
                newSocket.getOutputStream(),
                true,
                StandardCharsets.UTF_8
            );

            newWriter.println(username);

            socket = newSocket;
            reader = newReader;
            writer = newWriter;
            connected = true;

            SwingUtilities.invokeLater(() -> {
    btnConnect.setEnabled(false);
    btnSend.setEnabled(true);
    btnSendFile.setEnabled(true);
    txtHost.setEnabled(false);
    txtUsername.setEnabled(false);

    if (lblStatus != null) {
        lblStatus.setText("Đã kết nối • " + host);
        lblStatus.setForeground(new Color(22, 163, 74));
    }

    appendMessage("[SYSTEM] Đã kết nối Server: " + host);
            startFileReceiver(host);
});

startFileReceiver(host);

           
String message;

while (connected &&
       (message = newReader.readLine()) != null) {

    if (message.startsWith("[MEMBERS]")) {
        String names = message.substring("[MEMBERS]".length());

        SwingUtilities.invokeLater(
            () -> updateMembers(names)
        );

    } else {
        String received = message;

        SwingUtilities.invokeLater(
            () -> appendMessage(received)
        );
        
    }
    
}


        } catch (IOException ex) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                    this,
                    "Không thể kết nối Server!\n"
                    + ex.getMessage()
                )
            );
        } finally {
            if (newSocket != null) {
                try {
                    newSocket.close();
                } catch (IOException ignored) {
                }
            }

            if (socket == newSocket) {
                socket = null;
                reader = null;
                writer = null;
                connected = false;

                SwingUtilities.invokeLater(() -> {
                    btnConnect.setEnabled(true);
                    btnSend.setEnabled(false);
                    txtHost.setEnabled(true);
                    txtUsername.setEnabled(true);
                });
            } else {
                SwingUtilities.invokeLater(
                    () -> btnConnect.setEnabled(true)
                );
            }
        }
    });

    thread.setDaemon(true);
    thread.start();


           // TODO add your handling code here:
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendActionPerformed
String message = txtMessage.getText().trim();

    if (message.isEmpty()) {
        return;
    }

    if (connected && writer != null) {
        writer.println(message);
        txtMessage.setText("");

    }        // TODO add your handling code here:
    }//GEN-LAST:event_btnSendActionPerformed

    private void btnSendFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendFileActionPerformed
    
javax.swing.JFileChooser chooser =
        new javax.swing.JFileChooser();

chooser.setDialogTitle("Chọn tệp cần gửi");

int result = chooser.showOpenDialog(this);

if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
    java.io.File file = chooser.getSelectedFile();
    sendFile(file);



        // Bước tiếp theo: gửi file qua GroupFileServer.
    }       // TODO add your handling code here:
    }//GEN-LAST:event_btnSendFileActionPerformed
private void appendMessage(String message) {
    txtChat.append(message + "\n");
    txtChat.setCaretPosition(
        txtChat.getDocument().getLength()
    );
}

private void updateMembers(String names) {
    memberModel.clear();

    if (names == null || names.isBlank()) {
        return;
    }

    for (String name : names.split(",")) {
        String trimmedName = name.trim();

        if (!trimmedName.isEmpty()) {
            memberModel.addElement(trimmedName);
        }
    }
}

private void startFileReceiver(String serverHost) {
    if (fileReceiverRunning) {
        return;
    }

    fileReceiverRunning = true;

    Thread thread = new Thread(() -> {
        try (Socket s = new Socket(serverHost, 5002)) {
            fileReceiveSocket = s;

            DataOutputStream handshake =
                    new DataOutputStream(s.getOutputStream());

            handshake.writeUTF("LISTEN");
            handshake.writeUTF(username);
            handshake.flush();

            DataInputStream in =
                    new DataInputStream(s.getInputStream());

            while (fileReceiverRunning && !s.isClosed()) {
                String sender = in.readUTF();
                String filename = in.readUTF();
                long size = in.readLong();

                if (size < 0 || size > MAX_FILE_SIZE) {
                    throw new IOException(
                            "Kich thuoc tep khong hop le.");
                }

                byte[] data = new byte[(int) size];
                in.readFully(data);

                SwingUtilities.invokeLater(() -> {
                    int choice = JOptionPane.showConfirmDialog(
                            this,
                            sender + " gửi tệp: " + filename
                            + "\nKích thước: " + size + " bytes"
                            + "\nBạn có muốn lưu tệp không?",
                            "Nhận tệp",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        saveReceivedFile(filename, data);
                    }
                });
            }

        } catch (IOException e) {
            if (fileReceiverRunning) {
                SwingUtilities.invokeLater(() ->
                    appendMessage(
                        "[FILE] Mất kết nối nhận tệp: "
                        + e.getMessage())
                );
            }
        } finally {
            fileReceiverRunning = false;
            fileReceiveSocket = null;
        }
    }, "GroupFileReceiver");

    thread.setDaemon(true);
    thread.start();
}

private void saveReceivedFile(String filename, byte[] data) {
    JFileChooser chooser = new JFileChooser();
    chooser.setSelectedFile(new java.io.File(filename));
    chooser.setDialogTitle("Lưu tệp nhận được");

    if (chooser.showSaveDialog(this)
            != JFileChooser.APPROVE_OPTION) {
        return;
    }

    java.io.File destination = chooser.getSelectedFile();

    if (destination.exists()) {
        int overwrite = JOptionPane.showConfirmDialog(
                this,
                "Tệp đã tồn tại. Bạn có muốn ghi đè không?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (overwrite != JOptionPane.YES_OPTION) {
            return;
        }
    }

    try {
        Files.write(destination.toPath(), data);
        appendMessage("[FILE] Đã lưu tệp: "
                + destination.getAbsolutePath());
    } catch (IOException e) {
        JOptionPane.showMessageDialog(
                this, "Không thể lưu tệp: " + e.getMessage());
    }
}

private void sendFile(java.io.File file) {
    if (!connected) {
        JOptionPane.showMessageDialog(
                this, "Hãy kết nối Group Chat trước.");
        return;
    }

    if (!file.isFile() || !file.canRead()) {
        JOptionPane.showMessageDialog(
                this, "Không thể đọc tệp đã chọn.");
        return;
    }

    long size = file.length();

    if (size > MAX_FILE_SIZE) {
        JOptionPane.showMessageDialog(
                this, "Chỉ gửi tệp tối đa 50 MB.");
        return;
    }

    String serverHost = txtHost.getText().trim();
    String sender = username;

    Thread thread = new Thread(() -> {
        try (Socket s = new Socket(serverHost, 5002);
             DataOutputStream out =
                     new DataOutputStream(s.getOutputStream());
             FileInputStream fileIn =
                     new FileInputStream(file)) {

            out.writeUTF("UPLOAD");
            out.writeUTF(sender);
            out.writeUTF(file.getName());
            out.writeLong(size);

            byte[] buffer = new byte[8192];
            int count;

            while ((count = fileIn.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }

            out.flush();

            SwingUtilities.invokeLater(() ->
                appendMessage("[FILE] Bạn đã gửi: "
                        + file.getName())
            );

        } catch (IOException e) {
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                    this,
                    "Gửi tệp thất bại: " + e.getMessage()
                )
            );
        }
    }, "GroupFileSender");

    thread.setDaemon(true);
    thread.start();
}

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(frmGroupChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frmGroupChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frmGroupChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frmGroupChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frmGroupChat().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnSend;
    private javax.swing.JButton btnSendFile;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList<String> lstMembers;
    private javax.swing.JTextArea txtChat;
    private javax.swing.JTextField txtHost;
    private javax.swing.JTextField txtMessage;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
