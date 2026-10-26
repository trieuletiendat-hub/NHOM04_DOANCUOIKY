package com.mycompany.chat1v1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
/**
 *
 * @author Phuong Uyen
 */
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
                System.out.println("Co Client ket noi: " + socket.getRemoteSocketAddress());

                Thread thread = new Thread(new ClientHandler(socket));
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("Loi Server: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {

        private final Socket socket;
        private PrintWriter out;
        private String username;
        private final Object writeLock = new Object();

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        private String readLineFromSocket(InputStream in) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
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

        @Override
        public void run() {
            try {
                InputStream rawIn = socket.getInputStream();
                out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

                String login = readLineFromSocket(rawIn);

                if (login == null || !login.startsWith("LOGIN ")) {
                    out.println("ERROR|Vui long dang nhap bang ten nguoi dung.");
                    return;
                }

                username = login.substring(6).trim();

                if (username.isEmpty() || username.contains("|") || username.contains(" ")) {
                    out.println("ERROR|Ten dang nhap khong hop le.");
                    return;
                }

                if (clients.putIfAbsent(username, this) != null) {
                    out.println("ERROR|Ten nguoi dung da ton tai.");
                    return;
                }

                out.println("OK|Dang nhap thanh cong voi ten " + username);

                Database.registerUser(username);
                out.println("INFO|Phong chat chung da san sang. Nhap tin nhan de gui cho tat ca.");
                out.println("INFO|Cu phap gui file: @file tenNguoiNhan tenFile kichThuocBytes");
                out.println("INFO|Nhap /users de xem danh sach nguoi dung.");
                out.println("INFO|Nhap /quit de thoat.");

                System.out.println(username + " da dang nhap.");
                broadcastUserList();

                String message;

                while ((message = readLineFromSocket(rawIn)) != null) {
                    if (message.equalsIgnoreCase("/quit")) {
                        break;
                    }

                    if (message.equalsIgnoreCase("/users")) {
                        out.println("INFO|Nguoi dung dang online: " + String.join(", ", clients.keySet()));
                        continue;
                    }

                    if (message.startsWith("@file|")) {
                        handleFileTransfer(rawIn, message);
                        continue;
                    }

                    broadcastChatMessage(message);
                }

            } catch (IOException e) {
                System.out.println("Mat ket noi voi " + (username == null ? "Client" : username));

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
        
        private void send(String line) {
            synchronized (writeLock) {
                out.println(line);
            }
        }

        private static void broadcastUserList() {
            String users = String.join(", ", clients.keySet());
            String message = "INFO|Nguoi dung dang online: " + users;

            for (ClientHandler client : clients.values()) {
                if (client.out != null) {
                    client.send(message);
                }
            }
        }

        private void broadcastChatMessage(String message) {
            if (message.trim().isEmpty()) {
                return;
            }

            String encryptedContent = Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));

            for (ClientHandler client : clients.values()) {
                if (client != this && client.out != null) {
                    client.send("MSG|" + username + "|" + encryptedContent);
                }
            }

            Database.saveMessage(username, "ALL", encryptedContent);
            System.out.println("[" + username + " -> GROUP]: " + encryptedContent);
        }

        private void handleFileTransfer(InputStream rawIn, String headerMessage) throws IOException {
            String[] parts = headerMessage.split("\\|", 4);
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
                discardIncomingFileStream(rawIn, fileSize);
                return;
            }

            if (receiver == this) {
                out.println("ERROR|Ban khong the gui file cho chinh minh.");
                discardIncomingFileStream(rawIn, fileSize);
                return;
            }

            boolean relayOk = true;
            byte[] buffer = new byte[8192];
            long totalRead = 0;

            synchronized (receiver.writeLock) {
                receiver.out.println("FILE_START|" + username + "|" + fileName + "|" + fileSize);
                OutputStream rawOut = receiver.socket.getOutputStream();

                while (totalRead < fileSize) {
                    int bytesToRead = (int) Math.min(buffer.length, fileSize - totalRead);
                    int bytesRead = rawIn.read(buffer, 0, bytesToRead);
                    if (bytesRead == -1) {
                        throw new IOException("Nguoi gui ngat ket noi khi dang gui file");
                    }
                    totalRead += bytesRead;

                    if (relayOk) {
                        try {
                            rawOut.write(buffer, 0, bytesRead);
                        } catch (IOException e) {
                            relayOk = false;
                        }
                    }
                }
                if (relayOk) {
                    rawOut.flush();
                }
            }

            if (relayOk) {
                out.println("SENT|Đã gửi tệp tin thành công: " + fileName);
                System.out.println(username + " da truyen file " + fileName + " cho " + receiverName);
            } else {
                out.println("ERROR|Khong gui duoc file den " + receiverName + ".");
            }
        }

        private void discardIncomingFileStream(InputStream rawIn, long fileSize) {
            try {
                byte[] buffer = new byte[8192];
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