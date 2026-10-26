
package com.mycompany.chat1v1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GroupFileServer {

    private static final int PORT = 5002;
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    private static final Set<FileReceiver> receivers =
            ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println(
                    "GroupFileServer dang lang nghe cong " + PORT);

            while (true) {
                Socket socket = server.accept();
                Thread thread = new Thread(
                        () -> handleConnection(socket));
                thread.setDaemon(true);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Loi File Server: " + e.getMessage());
        }
    }

    private static void handleConnection(Socket socket) {
        boolean keepConnection = false;

        try {
            DataInputStream in =
                    new DataInputStream(socket.getInputStream());

            String mode = in.readUTF();

            if ("LISTEN".equals(mode)) {
                String username = in.readUTF();
                FileReceiver receiver =
                        new FileReceiver(username, socket);

                receivers.add(receiver);
                keepConnection = true;

                System.out.println(
                        username + " da ket noi kenh nhan tep.");

                // Giu ket noi cho den khi client dong socket.
                while (in.read() != -1) {
                    // Client khong can gui du lieu tren kenh nay.
                }

            } else if ("UPLOAD".equals(mode)) {
                String sender = in.readUTF();
                String filename = in.readUTF();
                long length = in.readLong();

                if (length < 0 || length > MAX_FILE_SIZE) {
                    throw new IOException(
                            "Tep vuot qua gioi han 50 MB.");
                }

                byte[] data = new byte[(int) length];
                in.readFully(data);

                int delivered = broadcastFile(
                        sender, filename, data);

                System.out.println(
                        sender + " gui " + filename
                        + " (" + length + " bytes), den "
                        + delivered + " client.");

            } else {
                throw new IOException("Lenh khong hop le.");
            }

        } catch (EOFException e) {
            // Client dong ket noi.
        } catch (IOException e) {
            System.err.println(
                    "Loi xu ly ket noi: " + e.getMessage());
        } finally {
            if (!keepConnection) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            } else if (socket.isClosed()) {
                removeReceiver(socket);
            }
        }
    }

    private static int broadcastFile(
            String sender, String filename, byte[] data) {

        int delivered = 0;

        for (FileReceiver receiver : receivers) {
            // Khong gui lai cho nguoi gui.
            if (receiver.username.equals(sender)) {
                continue;
            }

            try {
                synchronized (receiver.out) {
                    receiver.out.writeUTF(sender);
                    receiver.out.writeUTF(filename);
                    receiver.out.writeLong(data.length);
                    receiver.out.write(data);
                    receiver.out.flush();
                }
                delivered++;

            } catch (IOException e) {
                removeReceiver(receiver.socket);
            }
        }

        return delivered;
    }

    private static void removeReceiver(Socket socket) {
        for (FileReceiver receiver : receivers) {
            if (receiver.socket == socket) {
                receivers.remove(receiver);
                System.out.println(
                        receiver.username
                        + " da ngat ket noi nhan tep.");

                try {
                    socket.close();
                } catch (IOException ignored) {
                }
                break;
            }
        }
    }

    private static class FileReceiver {
        final String username;
        final Socket socket;
        final DataOutputStream out;

        FileReceiver(String username, Socket socket)
                throws IOException {
            this.username = username;
            this.socket = socket;
            this.out = new DataOutputStream(
                    socket.getOutputStream());
        }
    }
}
