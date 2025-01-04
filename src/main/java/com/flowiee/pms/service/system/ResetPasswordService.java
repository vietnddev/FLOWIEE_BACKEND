package com.flowiee.pms.service.system;

public interface ResetPasswordService {
    String resetPassword(Long pAccountId);

    String generatePassword();

    String encodePassword(String pRawPassword);
}