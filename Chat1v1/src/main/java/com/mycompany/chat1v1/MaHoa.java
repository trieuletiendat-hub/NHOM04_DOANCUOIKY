/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chat1v1;

/**
 *
 * @author PhuongUyen
 */
import java.util.Base64;

public class MaHoa {
    public static String encrypt(String plainText) {
        if (plainText == null) return null;
        return Base64.getEncoder().encodeToString(plainText.getBytes());
    }

    public static String decrypt(String cipherText) {
        if (cipherText == null) return null;
        byte[] decodedBytes = Base64.getDecoder().decode(cipherText);
        return new String(decodedBytes);
    }
}
