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
import java.net.Socket;

public class ChatClient {
     public static void main(String[] args) {
        String host = "localhost";
        int port = 5000;

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(
                             socket.getInputStream()));
             PrintWriter out = new PrintWriter(
                     socket.getOutputStream(), true)) {

            System.out.println("Ket noi Server thanh cong!");

            out.println("Xin chao Server!");

            String response = in.readLine();

            System.out.println("Server phan hoi: " + response);

        } catch (IOException e) {
            System.out.println("Khong the ket noi Server: "
                    + e.getMessage());
        }
    }
}
