package com.mycompany.chat1v1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChatServer {
    private static final int PORT = 5000;

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

                if (clients.putIfAbsent(username, this) != null) {
                    out.println("ERROR|Ten nguoi dung da ton tai.");
                    return;
                }

                out.println("OK|Dang nhap thanh cong voi ten " + username);
                Database.registerUser(username);
                out.println("INFO|Nhap @tenNguoiNhan noiDungDeGui de nhan tin.");
                out.println("INFO|Cu phap gui file: @file tenNguoiNhan tenFile kichThuocBytes");
                out.println("INFO|Nhap /users de xem danh sach nguoi dung.");
                out.println("INFO|Nhap /quit de thoat.");

                System.out.println(username + " da dang nhap.");
                broadcastUserList();

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

                    // Bắt lệnh gửi file
                    if (message.startsWith("@file ")) {
                        handleFileTransfer(message);
                        continue;
                    }

                    sendPrivateMessage(message);
                }

            } catch (IOException e) {
                System.out.println("Mat ket noi voi "
                        + (username == null ? "Client" : username));

            } finally {
                if (username != null) {
                    boolean removed = clients.remove(username, this);

                    if (removed) {
                        System.out.println(username + " da ngat ket noi.");
                        broadcastUserList();
                    }
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Loi dong ket noi: " + e.getMessage());
                }
            }
        }
        
        private static void broadcastUserList() {
            String users = String.join(", ", clients.keySet());
            String message = "INFO|Nguoi dung dang online: " + users;

            for (ClientHandler client : clients.values()) {
                if (client.out != null) {
                    client.out.println(message);
                }
            }

            System.out.println("So nguoi dang online: " + clients.size());
        }

        private void sendPrivateMessage(String message) {
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
            Database.saveMessage(username, receiverName, content);
        }

        // Xử lý chuyển tiếp file từ người gửi sang người nhận
        private void handleFileTransfer(String headerMessage) {
            // Cú pháp: @file receiverName fileName fileSize
            String[] parts = headerMessage.split(" ", 4);
            if (parts.length < 4) {
                out.println("ERROR|Sai cu phap gui file.");
                return;
            }

            String receiverName = parts[1];
            String fileName = parts[2];
            long fileSize;

            try {
                fileSize = Long.parseLong(parts[3]);
            } catch (NumberFormatException e) {
                out.println("ERROR|Kich thuoc file khong hop le.");
                return;
            }

            ClientHandler receiver = clients.get(receiverName);
            if (receiver == null) {
                out.println("ERROR|Nguoi dung " + receiverName + " khong online.");
                discardIncomingFileStream(fileSize);
                return;
            }

            if (receiver == this) {
                out.println("ERROR|Ban khong the gui file cho chinh minh.");
                discardIncomingFileStream(fileSize);
                return;
            }

            try {
                // Báo cho client nhận chuẩn bị nhận luồng file
                receiver.out.println("FILE_START|" + username + "|" + fileName + "|" + fileSize);

                // Chuyển tiếp luồng byte trực tiếp
                InputStream rawIn = socket.getInputStream();
                OutputStream rawOut = receiver.socket.getOutputStream();

                byte[] buffer = new byte[4096];
                long totalRead = 0;
                int bytesRead;

                while (totalRead < fileSize) {
                    int bytesToRead = (int) Math.min(buffer.length, fileSize - totalRead);
                    bytesRead = rawIn.read(buffer, 0, bytesToRead);
                    if (bytesRead == -1) break;

                    rawOut.write(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                }
                rawOut.flush();

                out.println("SENT|Da gui file " + fileName + " den " + receiverName + ".");
                System.out.println(username + " da gui file " + fileName + " cho " + receiverName);

            } catch (IOException e) {
                System.out.println("Loi truyen file: " + e.getMessage());
            }
        }

        // Đọc bỏ luồng dữ liệu file nếu xảy ra lỗi người nhận không hợp lệ
        private void discardIncomingFileStream(long fileSize) {
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
        }
    }
}