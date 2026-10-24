/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chat1v1;

/**
 *
 * @author TAI
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/chat_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // Thay đổi username MySQL của bạn
    private static final String PASS = "12345678";     // Thay đổi password MySQL của bạn

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // Hàm lưu tin nhắn vào database
    public static void saveMessage(String sender, String receiver, String content) {
        String sql = "INSERT INTO messages (sender, receiver, content) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sender);
            pstmt.setString(2, receiver);
            pstmt.setString(3, content);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Lỗi lưu tin nhắn vào DB: " + e.getMessage());
        }
    }

    // Hàm kiểm tra hoặc đăng ký user vào database
    public static void registerUser(String username) {
        String sql = "INSERT IGNORE INTO users (username) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Lỗi lưu user vào DB: " + e.getMessage());
        }
    }
}