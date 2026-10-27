/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.chat1v1;

    
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author Nguyen Bich Anh
 */
public class frmChat extends javax.swing.JFrame {
    private java.net.Socket socket;
    private java.io.BufferedReader in;
    private java.io.PrintWriter out;
    private String username;
    private DataOutputStream dos;

    private final javax.swing.DefaultListModel<String> userModel = new javax.swing.DefaultListModel<>();
    private boolean closing = false;
    // Khóa để tin nhắn và file không bị ghi xen kẽ vào cùng một socket
    private final Object sendLock = new Object();

private final java.util.Map<String, StringBuilder> privateHistories = new java.util.HashMap<>();
private String currentSelectedUser = null;
    /**
     * Creates new form frmChat
     */
    public frmChat() {
        initComponents();
        setupModernUI();
        setLocationRelativeTo(null);
    }
    
    public frmChat(
      String username,
        java.net.Socket socket,
        java.io.BufferedReader in,
        java.io.PrintWriter out,
        String loginMessage) {

        this.username = username;
        this.socket = socket;
        this.in = in;
        this.out = out;
        
        try {
            this.dos = new DataOutputStream(socket.getOutputStream()); // Khởi tạo luồng ghi nhị phân
        } catch (IOException e) {
            e.printStackTrace();
        }

        initComponents();
        setupModernUI();
        
        setTitle("Chat TCP 1-1 - " + username);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(
            javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        lblCurrentUser.setText("Người dùng: " + username);
        lblStatus.setText("Đã kết nối");
        txtChat.setEditable(false);
        txtChat.setLineWrap(true);
        txtChat.setWrapStyleWord(true);

        lstUsers.setModel(userModel);

        //appendMessage("[Hệ thống] " + loginMessage);
        //appendMessage("[Hệ thống] Chọn người nhận rồi nhập tin nhắn.");

        btnSend.addActionListener(e -> sendMessage());
        txtMessage.addActionListener(e -> sendMessage());
        btnRefresh.addActionListener(e -> refreshUsers());
        btnLogout.addActionListener(e -> disconnect());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                disconnect();
            }
        });
    }
     
    private void setupModernUI() {
        java.awt.Color primary = new java.awt.Color(81, 70, 216);
        java.awt.Color primaryHover = new java.awt.Color(103, 92, 235);
        java.awt.Color background = new java.awt.Color(244, 243, 252);
        java.awt.Color text = new java.awt.Color(37, 39, 68);
        java.awt.Color muted = new java.awt.Color(111, 116, 140);
        java.awt.Color border = new java.awt.Color(224, 223, 240);
        java.awt.Color white = java.awt.Color.WHITE;

    // ===== NỀN CHÍNH =====
        getContentPane().setBackground(background);

        setTitle("Chat Connect | Trò chuyện 1-1");
        setMinimumSize(new java.awt.Dimension(700, 450));

    // ===== TIÊU ĐỀ =====
        lblTitle.setText("CHAT CONNECT");
        lblTitle.setFont(new java.awt.Font(
            "Segoe UI", java.awt.Font.BOLD, 23));
        lblTitle.setForeground(primary);

        lblCurrentUser.setFont(new java.awt.Font(
            "Segoe UI", java.awt.Font.BOLD, 12));
        lblCurrentUser.setForeground(text);

        lblStatus.setFont(new java.awt.Font(
            "Segoe UI", java.awt.Font.PLAIN, 12));
        lblStatus.setForeground(new java.awt.Color(35, 150, 105));

        lblUsersTitle.setText("Người dùng trực tuyến");
        lblUsersTitle.setFont(new java.awt.Font(
            "Segoe UI", java.awt.Font.BOLD, 13));
        lblUsersTitle.setForeground(text);

        lblConversation.setText("Nội dung hội thoại");
        lblConversation.setFont(new java.awt.Font(
            "Segoe UI", java.awt.Font.BOLD, 13));
        lblConversation.setForeground(text);
        // Lắng nghe sự kiện khi người dùng click chọn tên trong danh sách online
        lstUsers.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = lstUsers.getSelectedValue();
                if (selectedUser != null && !selectedUser.equals(currentSelectedUser)) {
                    switchChatTarget(selectedUser);
                }
            }
        });

    // ===== DANH SÁCH NGƯỜI DÙNG =====
        lstUsers.setFont(new java.awt.Font(
            "Segoe UI", java.awt.Font.PLAIN, 13));
        lstUsers.setForeground(text);
        lstUsers.setBackground(white);
        lstUsers.setSelectionBackground(new java.awt.Color(229, 226, 255));
        lstUsers.setSelectionForeground(primary);
        lstUsers.setFixedCellHeight(34);
        lstUsers.setBorder(
            javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));

        jScrollPane1.setBorder(
            javax.swing.BorderFactory.createLineBorder(border));
        jScrollPane1.getViewport().setBackground(white);

    // ===== KHUNG CHAT =====
        txtChat.setFont(new java.awt.Font(
            "Segoe UI", java.awt.Font.PLAIN, 13));
        txtChat.setForeground(text);
        txtChat.setBackground(white);
        txtChat.setEditable(false);
        txtChat.setLineWrap(true);
        txtChat.setWrapStyleWord(true);
        txtChat.setMargin(new java.awt.Insets(12, 12, 12, 12));

        jScrollPane2.setBorder(
            javax.swing.BorderFactory.createLineBorder(border));
        jScrollPane2.getViewport().setBackground(white);

    // ===== Ô NHẬP TIN NHẮN =====
        txtMessage.setFont(new java.awt.Font(
            "Segoe UI", java.awt.Font.PLAIN, 13));
        txtMessage.setForeground(text);
        txtMessage.setBackground(white);
        txtMessage.setCaretColor(primary);
        txtMessage.setBorder(
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(border, 1, true),
                    javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10)
            )
        );

    // ===== ĐƯỜNG PHÂN CÁCH =====
        jSeparator1.setForeground(border);
        jSeparator1.setBackground(border);

    // ===== CÁC NÚT =====
        styleButton(btnSend, "Gửi",
            primary, primaryHover, white);

        styleButton(btnSendFile, "Gửi file",
            new java.awt.Color(56, 120, 210),
            new java.awt.Color(75, 140, 230), white);
        
        styleButton(btnJoinGroup, "Tham gia nhóm",
            primary, primaryHover, white);

        styleButton(btnRefresh, "Làm mới",
            new java.awt.Color(231, 228, 255),
            new java.awt.Color(215, 210, 255), primary);

        styleButton(btnLogout, "Đăng xuất",
            new java.awt.Color(220, 75, 95),
            new java.awt.Color(195, 55, 78), white);
    }
    private String getChatKey(String user1, String user2) {
        // Sắp xếp tên theo bảng chữ cái để đảm bảo (A chat với B) giống với (B chat với A)
        if (user1.compareTo(user2) < 0) {
            return user1 + "_" + user2;
        } else {
            return user2 + "_" + user1;
        }
    }

private void switchChatTarget(String newUser) {
    currentSelectedUser = newUser;

    // Xóa nội dung cuộc trò chuyện đang hiển thị.
    txtChat.setText("");

    if (newUser == null || newUser.trim().isEmpty()) {
        lblConversation.setText("Nội dung hội thoại");
        return;
    }

    lblConversation.setText("Trò chuyện với " + newUser);

    String chatKey = getChatKey(username, newUser);
    StringBuilder history = privateHistories.get(chatKey);

    // Hiển thị lại lịch sử đúng của người được chọn.
    if (history != null && history.length() > 0) {
        txtChat.setText(history.toString());
    }

    txtChat.setCaretPosition(txtChat.getDocument().getLength());
    txtMessage.requestFocusInWindow();
}

    // Định dạng nút và tạo hiệu ứng khi rê chuột
    private void styleButton(
        javax.swing.JButton button,
        String title,
        java.awt.Color normal,
        java.awt.Color hover,
        java.awt.Color foreground) {

        button.setText(title);
        button.setFont(new java.awt.Font(
            "Segoe UI", java.awt.Font.BOLD, 12));
        button.setForeground(foreground);
        button.setBackground(normal);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new java.awt.Cursor(
            java.awt.Cursor.HAND_CURSOR));

        button.setBorder(
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(normal, 1, true),
                    javax.swing.BorderFactory.createEmptyBorder(7, 12, 7, 12)
                )
        );

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(normal);
            }
        });
    }

    private String readLineFromSocket(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') {
                break;
            }
            if (b != '\r') {
                baos.write(b);
            }
        }
        if (baos.size() == 0 && b == -1) {
            return null;
        }
        return baos.toString("UTF-8");
    }

    public void startReceiver() {
        Thread receiverThread = new Thread(() -> {
            try {
                InputStream rawIn = socket.getInputStream();
                String line;

                while ((line = readLineFromSocket(rawIn)) != null) {
                    // Gặp header file: đọc đúng số byte của file NGAY TẠI LUỒNG NÀY
                    // rồi mới đọc tiếp tin nhắn, để byte file không bị coi là chữ.
                    if (line.startsWith("FILE_START|")) {
                        handleIncomingFile(rawIn, line);
                        continue;
                    }

                    final String message = line;

                    javax.swing.SwingUtilities.invokeLater(
                        () -> processMessage(message)
                    );
                }
            } catch (java.io.IOException e) {
                if (!closing) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        lblStatus.setText("Mất kết nối");
                        appendMessage("[Hệ thống] Kết nối đến server đã bị ngắt.");
                    });
                }
            }
        });

        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    // Nhận file: đọc đủ fileSize byte từ socket, lưu vào Downloads, chỉ báo kết quả lên chat
    private void handleIncomingFile(InputStream rawIn, String header) throws IOException {
        String[] parts = header.split("\\|", 4);
        if (parts.length < 4) {
            return;
        }

        final String sender = parts[1];
        final String fileName = new File(parts[2]).getName(); // chặn đường dẫn lạ
        long fileSize;
        try {
            fileSize = Long.parseLong(parts[3]);
        } catch (NumberFormatException e) {
            return;
        }

        File downloadDir = new File(System.getProperty("user.home"), "Downloads");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        File savedFile = uniqueFile(downloadDir, fileName);

        boolean ok = true;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(savedFile);
        } catch (IOException e) {
            ok = false; // vẫn phải đọc hết byte để socket không bị lệch
        }

        try {
            byte[] buffer = new byte[8192];
            long totalRead = 0;
            while (totalRead < fileSize) {
                int bytesToRead = (int) Math.min(buffer.length, fileSize - totalRead);
                int n = rawIn.read(buffer, 0, bytesToRead);
                if (n == -1) {
                    throw new IOException("Mất kết nối khi đang nhận file");
                }
                totalRead += n;

                if (ok) {
                    try {
                        fos.write(buffer, 0, n);
                    } catch (IOException e) {
                        ok = false;
                    }
                }
            }
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                }
            }
        }

        final boolean success = ok;
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (success) {
                appendMessage("[Hệ thống] Đã nhận tệp tin thành công từ [" + sender + "]: "
                        + fileName + " (đã lưu vào thư mục Downloads)");
            } else {
                appendMessage("[Lỗi hệ thống] Không thể lưu tệp tin nhận được từ " + sender);
            }
        });
    }

    // Nếu trùng tên thì thêm (1), (2)... để không ghi đè file cũ
    private File uniqueFile(File dir, String name) {
        File f = new File(dir, name);
        if (!f.exists()) {
            return f;
        }
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String ext = dot > 0 ? name.substring(dot) : "";
        int i = 1;
        while (f.exists()) {
            f = new File(dir, base + " (" + i + ")" + ext);
            i++;
        }
        return f;
    }

private void processMessage(String incoming) {
    String[] parts = incoming.split("\\|", 4);

    switch (parts[0]) {
        case "MSG":
            if (parts.length >= 3) {
                String sender = parts[1];
                String encryptedContent = parts[2];
                String content;

                try {
                    byte[] decoded = java.util.Base64.getDecoder()
                            .decode(encryptedContent);
                    content = new String(
                            decoded,
                            java.nio.charset.StandardCharsets.UTF_8
                    );
                } catch (IllegalArgumentException e) {
                    content = encryptedContent;
                }

                // Không nhận và hiển thị tin nhắn của chính mình
                // trong nhánh xử lý tin nhắn đến.
                if (sender.equals(username)) {
                    break;
                }

                String chatKey = getChatKey(username, sender);
                String formattedMsg = "[" + sender + "] : " + content;

                privateHistories.putIfAbsent(
                        chatKey, new StringBuilder()
                );
                privateHistories.get(chatKey)
                        .append(formattedMsg)
                        .append("\n");

                // Chỉ hiển thị nếu đang mở đúng cuộc trò chuyện.
                if (sender.equals(currentSelectedUser)) {
                    txtChat.append(formattedMsg + "\n");
                    txtChat.setCaretPosition(
                            txtChat.getDocument().getLength()
                    );
                }
            }
            break;

        case "INFO":
            if (parts.length >= 2) {
                String info = parts[1];
                String prefix1 = "Nguoi dung dang online: ";
                String prefix2 = "Người dùng đang online: ";

                if (info.startsWith(prefix1)) {
                    updateUsers(info.substring(prefix1.length()));
                } else if (info.startsWith(prefix2)) {
                    updateUsers(info.substring(prefix2.length()));
                } else {
                    lblStatus.setText(info);
                }
            }
            break;

        case "SENT":
            // Thông báo gửi file không được đưa nhầm vào hội thoại
            // với người đang chọn.
            lblStatus.setText(parts.length >= 2
                    ? parts[1] : "Đã gửi tệp");
            break;

        case "ERROR":
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    parts.length >= 2 ? parts[1] : incoming,
                    "Thông báo",
                    javax.swing.JOptionPane.WARNING_MESSAGE
            );
            break;

        default:
            lblStatus.setText(incoming);
            break;
    }
}

    
private void sendMessage() {
    String content = txtMessage.getText().trim();

    if (content.isEmpty()) {
        return;
    }

    String receiver = currentSelectedUser;

    if (receiver == null || receiver.isEmpty()) {
        JOptionPane.showMessageDialog(
                this, "Vui lòng chọn người dùng để trò chuyện."
        );
        return;
    }

    if (receiver.equals(username)) {
        JOptionPane.showMessageDialog(
                this, "Bạn không thể gửi tin nhắn cho chính mình."
        );
        return;
    }

    if (out == null || socket == null || socket.isClosed()) {
        JOptionPane.showMessageDialog(
                this, "Kết nối đến server không còn hoạt động."
        );
        return;
    }

    boolean failed;

    synchronized (sendLock) {
        out.println("@" + receiver + " " + content);
        out.flush();
        failed = out.checkError();
    }

    if (failed) {
        JOptionPane.showMessageDialog(
                this, "Không gửi được tin nhắn."
        );
        return;
    }

    String chatKey = getChatKey(username, receiver);
    String myMsg = "[" + username + "] : " + content;

    privateHistories.putIfAbsent(
            chatKey, new StringBuilder()
    );
    privateHistories.get(chatKey)
            .append(myMsg)
            .append("\n");

    // Chỉ hiển thị tại đúng cuộc trò chuyện đang mở.
    if (receiver.equals(currentSelectedUser)) {
        txtChat.append(myMsg + "\n");
        txtChat.setCaretPosition(
                txtChat.getDocument().getLength()
        );
    }

    txtMessage.setText("");
    txtMessage.requestFocusInWindow();
}

    public void refreshUsers() {
        if (socket != null && !socket.isClosed()) {
            out.println("/users");
        }
    }

    
    
private void updateUsers(String onlineUsers) {
    String selected = currentSelectedUser;

    userModel.clear();

    int totalOnline = 0;

    if (onlineUsers != null && !onlineUsers.trim().isEmpty()) {
        String[] names = onlineUsers.split(",");

        for (String name : names) {
            String trimmed = name.trim();

            if (!trimmed.isEmpty()) {
                totalOnline++;

                if (!trimmed.equals(username)) {
                    userModel.addElement(trimmed);
                }
            }
        }
    }

    // Khôi phục lựa chọn nếu người đó vẫn còn online.
    if (selected != null && userModel.contains(selected)) {
        lstUsers.setSelectedValue(selected, true);
    } else {
        currentSelectedUser = null;
        lstUsers.clearSelection();
        txtChat.setText("");
        lblConversation.setText("Nội dung hội thoại");
    }

    lblStatus.setText("Đã kết nối | Online: " + totalOnline);
}


    private void appendMessage(String message) {
        txtChat.append(message + "\n");
        txtChat.setCaretPosition(txtChat.getDocument().getLength());
    }

    private void disconnect() {
        if (closing) {
            return;
        }

        int choice = javax.swing.JOptionPane.showConfirmDialog(
            this,
            "Bạn có muốn đăng xuất không?",
            "Xác nhận",
            javax.swing.JOptionPane.YES_NO_OPTION
        );

        if (choice != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        closing = true;

        try {
            if (out != null) {
                out.println("/quit");
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (java.io.IOException e) {
            System.err.println("Lỗi đóng kết nối: " + e.getMessage());
        }

        dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jColorChooser1 = new javax.swing.JColorChooser();
        lblTitle = new javax.swing.JLabel();
        lblCurrentUser = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        lblUsersTitle = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstUsers = new javax.swing.JList<>();
        lblConversation = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtChat = new javax.swing.JTextArea();
        txtMessage = new javax.swing.JTextField();
        btnRefresh = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        btnSend = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        btnSendFile = new javax.swing.JButton();
        btnJoinGroup = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        lblTitle.setFont(new java.awt.Font("Segoe UI", 3, 24)); // NOI18N
        lblTitle.setText("CHAT TCP 1-1");

        lblCurrentUser.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblCurrentUser.setText("User :");

        lblStatus.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblStatus.setText("Đang kết nối");

        lblUsersTitle.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblUsersTitle.setText("User đang online :");

        jScrollPane1.setViewportView(lstUsers);

        lblConversation.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblConversation.setText("Nội dung hội thoại :");

        txtChat.setColumns(20);
        txtChat.setRows(5);
        jScrollPane2.setViewportView(txtChat);

        btnRefresh.setBackground(new java.awt.Color(255, 255, 51));
        btnRefresh.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnRefresh.setText("Làm mới");

        btnLogout.setBackground(new java.awt.Color(255, 51, 51));
        btnLogout.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnLogout.setText("Đăng xuất");

        btnSend.setBackground(new java.awt.Color(0, 255, 0));
        btnSend.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnSend.setText("Gửi");

        btnSendFile.setBackground(new java.awt.Color(51, 255, 204));
        btnSendFile.setText("File");
        btnSendFile.setMaximumSize(new java.awt.Dimension(72, 27));
        btnSendFile.setMinimumSize(new java.awt.Dimension(72, 27));
        btnSendFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendFileActionPerformed(evt);
            }
        });

        btnJoinGroup.setBackground(new java.awt.Color(255, 51, 255));
        btnJoinGroup.setText("Tham gia nhóm");
        btnJoinGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnJoinGroupActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnRefresh)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCurrentUser)
                            .addComponent(lblUsersTitle)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(41, 41, 41)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(txtMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnSendFile, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap())
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 441, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(16, 16, 16))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblConversation)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnJoinGroup)
                                        .addGap(41, 41, 41))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblStatus)
                                .addGap(41, 41, 41))))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSeparator1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(237, 237, 237)
                        .addComponent(lblTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(lblTitle))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnLogout)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCurrentUser)
                    .addComponent(lblStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblUsersTitle)
                        .addComponent(lblConversation))
                    .addComponent(btnJoinGroup, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSend, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(7, 7, 7))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnRefresh)
                            .addComponent(txtMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(btnSendFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGap(8, 8, 8))))
                .addGap(16, 16, 16))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSendFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendFileActionPerformed
         String receiver = lstUsers.getSelectedValue();
        if (receiver == null) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn người nhận trước khi gửi file.");
            return;
        }

        if (receiver.equals(username)) {
            javax.swing.JOptionPane.showMessageDialog(this, "Bạn không thể gửi file cho chính mình.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File fileToSend = fileChooser.getSelectedFile();

            if (socket == null || socket.isClosed()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Chưa kết nối tới server!");
                return;
            }

            new Thread(() -> {
                try {
                    synchronized (sendLock) {
                        String safeName = fileToSend.getName().replace("|", "_");

                        // Header điều khiển
                        out.println("@file|" + receiver + "|" + safeName + "|" + fileToSend.length());
                        out.flush();

                        // Byte thô của file
                        OutputStream rawOut = socket.getOutputStream();
                        try (FileInputStream fis = new FileInputStream(fileToSend)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                rawOut.write(buffer, 0, bytesRead);
                            }
                        }
                        rawOut.flush();
                    }
                    // Thông báo "gửi thành công" do server trả về (SENT|...) và hiện ở processMessage

                } catch (Exception e) {
                    e.printStackTrace();
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        javax.swing.JOptionPane.showMessageDialog(frmChat.this, "Lỗi khi gửi file: " + e.getMessage());
                    });
                }
            }).start();
        }
    }//GEN-LAST:event_btnSendFileActionPerformed

    private void btnJoinGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnJoinGroupActionPerformed
if (socket == null || socket.isClosed() || username == null) {
            javax.swing.JOptionPane.showMessageDialog(
                this,
                "Bạn cần đăng nhập hệ thống trước."
            );
            return;
        }

        // Lấy địa chỉ IP của server từ socket hiện tại đang kết nối 1-1
        String groupHost = socket.getInetAddress().getHostAddress();

        // Mở khung Group Chat và truyền thẳng 'username' và 'groupHost' sang
        frmGroupChat groupForm = new frmGroupChat(username, groupHost);
        groupForm.setVisible(true);
    }//GEN-LAST:event_btnJoinGroupActionPerformed

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
            java.util.logging.Logger.getLogger(frmChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frmChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frmChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frmChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(frmChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frmChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frmChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frmChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frmChat().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnJoinGroup;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSend;
    private javax.swing.JButton btnSendFile;
    private javax.swing.JColorChooser jColorChooser1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblConversation;
    private javax.swing.JLabel lblCurrentUser;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblUsersTitle;
    private javax.swing.JList<String> lstUsers;
    private javax.swing.JTextArea txtChat;
    private javax.swing.JTextField txtMessage;
    // End of variables declaration//GEN-END:variables
}
