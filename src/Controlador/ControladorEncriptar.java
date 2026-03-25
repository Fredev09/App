/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author fredd
 */
public class ControladorEncriptar {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    private static final String KEY = generarClaveSegura();
    
    private static String generarClaveSegura() {
        byte[] claveBytes = {
            77, 105, 67, 108, 97, 118, 101, 83, 117, 112, 
            101, 114, 83, 101, 103, 117, 114, 97, 49, 50, 51, 33, 64, 35
        };
        return new String(claveBytes);
    }
    
    public static String encrypt(String data) {
        try {
            if (data == null || data.isEmpty()) return "";
            
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            System.err.println("Error encriptando: " + e.getMessage());
            return "";
        }
    }
    
    public static String decrypt(String encryptedData) {
        try {
            if (encryptedData == null || encryptedData.isEmpty()) return "";
            
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            System.err.println("Error desencriptando: " + e.getMessage());
            return "";
        }
    }
}

