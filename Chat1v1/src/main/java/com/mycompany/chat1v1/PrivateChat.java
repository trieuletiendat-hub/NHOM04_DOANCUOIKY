/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chat1v1;

/**
 *
 * @author Phuong Uyen
 */
import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;

public class PrivateChat extends javax.swing.JDialog {
    private String receiver;
    private PrintWriter out;
    private JTextArea txtPrivateChat;
    private JTextField txtPrivateMessage;
    private JButton btnSendPrivate;

    public PrivateChat(JFrame parent, String receiver, PrintWriter out) {
        super(parent, "Chat riêng với: " + receiver, false);
        this.receiver = receiver;
        this.out = out;

        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        txtPrivateChat = new JTextArea();
        txtPrivateChat.setEditable(false);
        add(new JScrollPane(txtPrivateChat), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        txtPrivateMessage = new JTextField();
        btnSendPrivate = new JButton("Gửi");
        bottomPanel.add(txtPrivateMessage, BorderLayout.CENTER);
        bottomPanel.add(btnSendPrivate, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Sự kiện gửi tin nhắn riêng
        btnSendPrivate.addActionListener(e -> sendPrivateMessage());
        txtPrivateMessage.addActionListener(e -> sendPrivateMessage());
    }

    private void sendPrivateMessage() {
        String content = txtPrivateMessage.getText().trim();
        if (content.isEmpty()) return;

        // Gửi lên server theo đúng chuẩn @private|receiver|content
        out.println("@private|" + receiver + "|" + content);
        txtPrivateChat.append("[Bạn → " + receiver + "]: " + content + "\n");
        txtPrivateMessage.setText("");
    }

    public void appendMessage(String message) {
        txtPrivateChat.append(message + "\n");
        txtPrivateChat.setCaretPosition(txtPrivateChat.getDocument().getLength());
    }
}
