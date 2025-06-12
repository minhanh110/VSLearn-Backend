package com.vslearn.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConstantVariables {
    @Value("${security.jwt.token.secret-key}")
    public static String SIGNER_KEY;

    @Value("${security.jwt.token.secret-key}")
    public void setSignerKey(String signerKey) {
        SIGNER_KEY = signerKey;
    }


    @Value("${security.jwt.token.expire-length}")
    public static Long KEY_TIME_OUT;

    @Value("${security.jwt.token.expire-length}")
    public void setKeyTimeOut(Long keyTimeOut) {
        KEY_TIME_OUT = keyTimeOut;
    }

    @Value("${system.mail.account}")
    public static String SYSTEM_EMAIL;

    @Value("${system.mail.account}")
    public void setSystemEmail(String systemEmail) {
        SYSTEM_EMAIL = systemEmail;
    }
    @Value("${system.mail.key}")
    public static String MAIL_KEY;

    @Value("${system.mail.key}")
    public void setMailKey(String mailKey) {
        MAIL_KEY = mailKey;
    }


}
