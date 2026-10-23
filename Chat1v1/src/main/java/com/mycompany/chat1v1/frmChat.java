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
    /**
     * Creates new form frmChat
     */
    public frmChat() {
        initComponents();
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

        setTitle("Chat TCP 1-1 - " + username);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(
            javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        lblCurrentUser.setText("Đăng nhập: " + username);
        lblStatus.setText("Đã kết nối");
        txtChat.setEditable(false);
        txtChat.setLineWrap(true);
        txtChat.setWrapStyleWord(true);

        lstUsers.setModel(userModel);

        appendMessage("[Hệ thống] " + loginMessage);
        appendMessage("[Hệ thống] Chọn người nhận rồi nhập tin nhắn.");

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
    
    public void startReceiver() {
        Thread receiverThread = new Thread(() -> {
            try {
                String line;

                while ((line = in.readLine()) != null) {
                    final String message = line;

                    javax.swing.SwingUtilities.invokeLater(
                        () -> processMessage(message)
                    );
                }
            } catch (java.io.IOException e) {
                if (!closing) {
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        lblStatus.setText("Mất kết nối");
                        appendMessage(
                            "[Hệ thống] Kết nối đến server đã bị ngắt."
                        );
                    });
                }
            }
        });

        receiverThread.setDaemon(true);
        receiverThread.start();
    }
private void receiveFile(String sender, String fileName, long fileSize) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            JFileChooser saveChooser = new JFileChooser();
            saveChooser.setSelectedFile(new File(fileName));
            int option = saveChooser.showSaveDialog(this);
            
            if (option == JFileChooser.APPROVE_OPTION) {
                File savedFile = saveChooser.getSelectedFile();
                
                new Thread(() -> {
                    try {
                        InputStream rawIn = socket.getInputStream();
                        FileOutputStream fos = new FileOutputStream(savedFile);
                        byte[] buffer = new byte[4096];
                        long totalRead = 0;
                        int bytesRead;
                        
                        while (totalRead < fileSize) {
                            int bytesToRead = (int) Math.min(buffer.length, fileSize - totalRead);
                            bytesRead = rawIn.read(buffer, 0, bytesToRead);
                            if (bytesRead == -1) break;
                            
                            fos.write(buffer, 0, bytesRead);
                            totalRead += bytesRead;
                        }
                        fos.close();
                        
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            appendMessage("[Hệ thống] Đã nhận file từ [" + sender + "]. Lưu tại: " + savedFile.getAbsolutePath());
                            javax.swing.JOptionPane.showMessageDialog(this, "Tải file thành công:\n" + savedFile.getAbsolutePath());
                        });
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            javax.swing.JOptionPane.showMessageDialog(this, "Lỗi khi lưu file: " + e.getMessage());
                        });
                    }
                }).start();
                
            } else {
                // Nếu hủy, đọc bỏ số byte dữ liệu đang chờ trên socket để không làm lệch buffer
                new Thread(() -> {
                    try {
                        InputStream rawIn = socket.getInputStream();
                        byte[] buffer = new byte[4096];
                        long totalRead = 0;
                        while (totalRead < fileSize) {
                            int bytesToRead = (int) Math.min(buffer.length, fileSize - totalRead);
                            int read = rawIn.read(buffer, 0, bytesToRead);
                            if (read == -1) break;
                            totalRead += read;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
                
                appendMessage("[Hệ thống] Bạn đã hủy nhận file từ " + sender);
            }
        });
    }
    private void processMessage(String incoming) {
        String[] parts = incoming.split("\\|", 4); // Cho phép tách tối đa 4 phần

        switch (parts[0]) {
            case "MSG":
                if (parts.length >= 3) {
                    appendMessage("[" + parts[1] + "] " + parts[2]);
                }
                break;

            // Xử lý khi có người gửi file đến
            case "FILE_START":
                if (parts.length >= 4) {
                    String sender = parts[1];
                    String fileName = parts[2];
                    long fileSize = Long.parseLong(parts[3]);
                    
                    receiveFile(sender, fileName, fileSize);
                }
                break;

            case "INFO":
                if (parts.length >= 2) {
                    String info = parts[1];
                    String prefix = "Nguoi dung dang online: ";

                    if (info.startsWith(prefix)) {
                        updateUsers(info.substring(prefix.length()));
                    } else {
                        appendMessage("[Hệ thống] " + info);
                    }
                }
                break;

            case "SENT":
                if (parts.length >= 2) {
                    appendMessage("[Hệ thống] " + parts[1]);
                }
                break;

            case "ERROR":
                appendMessage("[Lỗi] "
                    + (parts.length >= 2 ? parts[1] : incoming));
                break;

            default:
                appendMessage(incoming);
        }
    }

    private void sendMessage() {
        String content = txtMessage.getText().trim();

        if (content.isEmpty()) {
            return;
        }

        String receiver = lstUsers.getSelectedValue();

        if (receiver == null) {
            javax.swing.JOptionPane.showMessageDialog(
                this, "Vui lòng chọn người nhận trước khi gửi.");
            return;
        }

        if (receiver.equals(username)) {
        javax.swing.JOptionPane.showMessageDialog(
                this, "Bạn không thể gửi tin nhắn cho chính mình.");
        return;
        }

        out.println("@" + receiver + " " + content);

        if (out.checkError()) {
            appendMessage("[Lỗi] Không gửi được tin nhắn.");
            return;
        }

        appendMessage("[Bạn → " + receiver + "] " + content);
        txtMessage.setText("");
        txtMessage.requestFocus();
    }

    public void refreshUsers() {
        if (socket != null && !socket.isClosed()) {
            out.println("/users");
        }
    }

    
    private void updateUsers(String onlineUsers) {
        String selected = lstUsers.getSelectedValue();

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

        if (selected != null) {
            lstUsers.setSelectedValue(selected, true);
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

        btnSendFile.setText("File");
        btnSendFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendFileActionPerformed(evt);
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
                                        .addComponent(txtMessage, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnSend)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnSendFile))
                                    .addComponent(lblConversation)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 441, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(16, 16, 16))
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
                    .addComponent(btnLogout))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCurrentUser)
                    .addComponent(lblStatus))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUsersTitle)
                    .addComponent(lblConversation))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRefresh)
                    .addComponent(txtMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSend)
                    .addComponent(btnSendFile))
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
            
            new Thread(() -> {
                try {
                    if (socket == null || socket.isClosed()) {
                        javax.swing.JOptionPane.showMessageDialog(frmChat.this, "Chưa kết nối tới server!");
                        return;
                    }
                    
                    // Gửi header thông báo qua PrintWriter (out)
                    out.println("@file " + receiver + " " + fileToSend.getName() + " " + fileToSend.length());
                    
                    // Đẩy luồng byte dữ liệu qua OutputStream của socket
                        OutputStream rawOut = socket.getOutputStream();
                    FileInputStream fis = new FileInputStream(fileToSend);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        rawOut.write(buffer, 0, bytesRead);
                    }
                    rawOut.flush();
                    fis.close();
                    
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        appendMessage("[Bạn → " + receiver + "] Đã gửi file thành công: " + fileToSend.getName());
                    });
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        javax.swing.JOptionPane.showMessageDialog(frmChat.this, "Lỗi khi gửi file: " + e.getMessage());
                    });
                }
            }).start();
        }
    }//GEN-LAST:event_btnSendFileActionPerformed

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
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frmChat().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
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
