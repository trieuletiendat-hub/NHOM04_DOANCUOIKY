/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chat1v1;

/**
 *
 * @author TienDat
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    public static void main(String[] args) {
        int port = 5000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server dang lang nghe tai cong " + port);
            System.out.println("Dang cho Client ket noi...");

            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(
                                 clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(
                         clientSocket.getOutputStream(), true)) {

                System.out.println("Client da ket noi!");

                String message;

                while ((message = in.readLine()) != null) {
                    System.out.println("Client gui: " + message);
                    out.println("Server da nhan: " + message);
                }

                System.out.println("Client da ngat ket noi.");

                }

            System.out.println("Ket noi da dong.");

        } catch (IOException e) {
            System.out.println("Loi Server: " + e.getMessage());
        }
    }
}
