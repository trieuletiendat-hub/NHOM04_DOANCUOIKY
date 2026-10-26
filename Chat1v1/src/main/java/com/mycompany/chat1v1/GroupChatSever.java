
package com.mycompany.chat1v1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GroupChatSever {

    private static final int PORT = 5001;

    private static final Set<ClientHandler> clients =
            ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        System.out.println("Dang khoi dong Group Chat Server...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server dang lang nghe cong " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler client = new ClientHandler(socket);
                new Thread(client).start();
            }

        } catch (IOException e) {
            System.err.println("Loi Server: " + e.getMessage());
        }
    }

    private static void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    // Gui danh sach thanh vien hien tai den tat ca client
    private static void broadcastMembers() {
        StringBuilder names = new StringBuilder();

    for (ClientHandler client : clients) {
        if (names.length() > 0) {
            names.append(",");
        }
        names.append(client.username);
    }

    broadcast("[MEMBERS]" + names);

    }

    private static class ClientHandler implements Runnable {

        private final Socket socket;
        private PrintWriter out;
        private String username = "Guest";

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        @Override
        public void run() {
            boolean registered = false;

            try (
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                        socket.getInputStream(),
                        StandardCharsets.UTF_8
                    )
                )
            ) {
                out = new PrintWriter(
                    socket.getOutputStream(),
                    true,
                    StandardCharsets.UTF_8
                );

                // Dong dau tien la ten nguoi dung
                String name = in.readLine();

                if (name == null || name.isBlank()) {
                    return;
                }

                username = name.trim();

                clients.add(this);
                registered = true;

                System.out.println(username + " da ket noi.");

                // Cap nhat danh sach thanh vien cho tat ca client
                broadcastMembers();

                broadcast("[SYSTEM] " + username
                        + " da tham gia phong chat.");

                String message;

                while ((message = in.readLine()) != null) {
                    if ("/quit".equalsIgnoreCase(message.trim())) {
                        break;
                    }

                    if (!message.isBlank()) {
                        // 1. Phát tin nhắn đến tất cả các thành viên đang online trong nhóm
                        broadcast(username + ": " + message);

                        // 2. Lưu tin nhắn vào Bảng Cơ sở dữ liệu
                        // "GROUP" đánh dấu đây là tin nhắn gửi chung trong phòng nhóm
                        try {
                            Database.saveMessage(username, "GROUP", message);
                        } catch (Exception e) {
                            System.err.println("Lỗi khi lưu tin nhắn Group Chat vào Database: " + e.getMessage());
                        }
                    }
                }

            } catch (IOException e) {
                System.out.println(
                    username + " da ngat ket noi."
                );

            } finally {
                if (registered) {
                    clients.remove(this);

                    // Cap nhat danh sach sau khi thanh vien roi phong
                    broadcastMembers();

                    broadcast("[SYSTEM] " + username
                            + " da roi phong chat.");
                }

                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
