/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chat1v1;

/**
 *
 * @author PhuongUyen
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChatServer {
    private static final int PORT = 5000;

    // Luu danh sach Client dang ket noi theo ten nguoi dung
    private static final ConcurrentMap<String, ClientHandler> clients
            = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server dang lang nghe tai cong " + PORT);
            System.out.println("Dang cho Client ket noi...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Co Client ket noi: "
                        + socket.getRemoteSocketAddress());

                // Moi Client duoc xu ly tren mot thread rieng
                Thread thread = new Thread(new ClientHandler(socket));
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("Loi Server: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {

        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(), StandardCharsets.UTF_8));

                out = new PrintWriter(socket.getOutputStream(), true,
                        StandardCharsets.UTF_8);

                // Nhan ten dang nhap tu Client
                String login = in.readLine();

                if (login == null || !login.startsWith("LOGIN ")) {
                    out.println("ERROR|Vui long dang nhap bang ten nguoi dung.");
                    return;
                }

                username = login.substring(6).trim();

                if (username.isEmpty() || username.contains("|")
                        || username.contains(" ")) {
                    out.println("ERROR|Ten dang nhap khong hop le.");
                    return;
                }

                // Khong cho hai Client dung cung ten
                if (clients.putIfAbsent(username, this) != null) {
                    out.println("ERROR|Ten nguoi dung da ton tai.");
                    return;
                }

                out.println("OK|Dang nhap thanh cong voi ten " + username);
                out.println("INFO|Nhap @tenNguoiNhan noiDungDeGui de nhan tin.");
                out.println("INFO|Vi du: @Bob Xin chao Bob!");
                out.println("INFO|Nhap /users de xem danh sach nguoi dung.");
                out.println("INFO|Nhap /quit de thoat.");

                System.out.println(username + " da dang nhap.");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("/quit")) {
                        break;
                    }

                    if (message.equalsIgnoreCase("/users")) {
                        out.println("INFO|Nguoi dung dang online: "
                                + String.join(", ", clients.keySet()));
                        continue;
                    }

                    sendPrivateMessage(message);
                }

            } catch (IOException e) {
                System.out.println("Mat ket noi voi "
                        + (username == null ? "Client" : username));
            } finally {
                if (username != null) {
                    clients.remove(username, this);
                    System.out.println(username + " da ngat ket noi.");
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Loi dong ket noi: " + e.getMessage());
                }
            }
        }

        private void sendPrivateMessage(String message) {
            // Cu phap: @tenNguoiNhan noi dung tin nhan
            if (!message.startsWith("@") || !message.contains(" ")) {
                out.println("ERROR|Sai cu phap. Vi du: @Bob Xin chao!");
                return;
            }

            int separator = message.indexOf(' ');
            String receiverName = message.substring(1, separator).trim();
            String content = message.substring(separator + 1).trim();

            if (receiverName.isEmpty() || content.isEmpty()) {
                out.println("ERROR|Ten nguoi nhan va noi dung khong duoc de trong.");
                return;
            }

            ClientHandler receiver = clients.get(receiverName);

            if (receiver == null) {
                out.println("ERROR|Nguoi dung " + receiverName
                        + " khong online hoac khong ton tai.");
                return;
            }

            if (receiver == this) {
                out.println("ERROR|Ban khong the gui tin nhan cho chinh minh.");
                return;
            }

            receiver.out.println("MSG|" + username + "|" + content);
            out.println("SENT|Da gui tin nhan den " + receiverName + ".");
        }
    }
}
