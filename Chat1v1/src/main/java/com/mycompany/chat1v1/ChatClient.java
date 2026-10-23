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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ChatClient {
     private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Nhap ten dang nhap: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty() || username.contains(" ")
                || username.contains("|")) {
            System.out.println("Ten dang nhap khong hop le.");
            return;
        }

        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(socket.getOutputStream(),
                     true, StandardCharsets.UTF_8)) {

            // Gui thong tin dang nhap
            out.println("LOGIN " + username);

            String response = in.readLine();

            if (response == null || !response.startsWith("OK|")) {
                System.out.println(response == null
                        ? "Server da dong ket noi."
                        : response.replace("|", ": "));
                return;
            }

            System.out.println(response.substring(3));
            System.out.println("Ban co the bat dau nhan tin.");

            // Luong rieng de lien tuc nhan tin nhan tu Server
            Thread receiverThread = new Thread(() -> {
                try {
                    String incoming;

                    while ((incoming = in.readLine()) != null) {
                        String[] parts = incoming.split("\\|", 3);

                        if (parts.length >= 2 && parts[0].equals("MSG")) {
                            String sender = parts[1];
                            String content = parts.length == 3 ? parts[2] : "";
                            System.out.println("\n[" + sender + "]: " + content);
                            System.out.print("Ban: ");
                        } else if (parts.length >= 2) {
                            System.out.println("\n" + parts[1]);
                            System.out.print("Ban: ");
                        } else {
                            System.out.println("\n" + incoming);
                            System.out.print("Ban: ");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("\nMat ket noi voi Server.");
                }
            });

            receiverThread.setDaemon(true);
            receiverThread.start();

            // Doc tin nhan tu ban phim va gui den Server
            // Doc tin nhan tu ban phim va gui den Server
              while (true) {
                  System.out.print("Ban: ");
                  String message = scanner.nextLine();

                  if (message.startsWith("@file ")) {
                      // Cú pháp console: @file Bob C:\path\to\file.txt
                      String[] parts = message.split(" ", 3);
                      if (parts.length < 3) {
                          System.out.println("Sai cu phap. Vi du: @file Bob C:/file.txt");
                          continue;
                      }
                      String receiver = parts[1];
                      File fileToSend = new File(parts[2]);
                      if (!fileToSend.exists()) {
                          System.out.println("File khong ton tai!");
                          continue;
                      }

                      out.println("@file " + receiver + " " + fileToSend.getName() + " " + fileToSend.length());
                      
                      OutputStream rawOut = socket.getOutputStream();
                      FileInputStream fis = new FileInputStream(fileToSend);
                      byte[] buffer = new byte[4096];
                      int bytesRead;
                      while ((bytesRead = fis.read(buffer)) != -1) {
                          rawOut.write(buffer, 0, bytesRead);
                      }
                      rawOut.flush();
                      fis.close();
                      System.out.println("Da gui file thanh cong!");
                      continue;
                  }

                  out.println(message);

                  if (message.equalsIgnoreCase("/quit")) {
                      break;
                  }
              }

        } catch (IOException e) {
            System.out.println("Khong the ket noi Server: "
                    + e.getMessage());
        }
    }
}
