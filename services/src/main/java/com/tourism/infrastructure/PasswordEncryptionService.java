package com.tourism.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncryptionService {

    @Value("${encrypt.password}")
    private String encryptionPassword;

    public String encryptPassword(String password) {
        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword(encryptionPassword);
        return encryptor.encrypt(password);
    }

    public String decryptPassword(String encryptedPassword) {
        BasicTextEncryptor encryptor = new BasicTextEncryptor();
        encryptor.setPassword(encryptionPassword);
        return encryptor.decrypt(encryptedPassword);
    }

    public boolean checkPassword(String input, String password) {
        return input.equals(this.decryptPassword(password));
    }
}
