package com.example.jmt.util;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

public class SMTPAuthenticator extends Authenticator {
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(
                "msllsese@gmail.com", "nfbbeagciudtipmf");
    }
}
