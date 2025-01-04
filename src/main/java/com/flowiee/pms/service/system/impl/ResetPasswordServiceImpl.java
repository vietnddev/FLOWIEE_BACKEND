package com.flowiee.pms.service.system.impl;

import com.flowiee.pms.config.Core;
import com.flowiee.pms.entity.system.Account;
import com.flowiee.pms.entity.system.SystemConfig;
import com.flowiee.pms.exception.EntityNotFoundException;
import com.flowiee.pms.repository.system.AccountRepository;
import com.flowiee.pms.service.BaseService;
import com.flowiee.pms.service.system.ResetPasswordService;
import com.flowiee.pms.utils.constants.ConfigCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ResetPasswordServiceImpl extends BaseService implements ResetPasswordService {
    private final AccountRepository mvAccountRepository;

    public static final int DEFAULT_PASSWORD_LENGTH = 8;
    public static final int DEFAULT_PASSWORD_VALIDITY_PERIOD_DAY = 30;

    @Override
    public String resetPassword(Long pAccountId) {
        Account lvAccount = mvAccountRepository.findById(pAccountId)
                .orElseThrow(() -> new EntityNotFoundException(new Object[] {"account"}, null, null));
        String lvEncodedOldPassword = lvAccount.getPassword();
        String lvRawNewPassword;
        String lvEncodedNewPassword;
        do
        {
            // regenerate password if it is the same!
            lvRawNewPassword = generatePassword();
            lvEncodedNewPassword = encodePassword(lvRawNewPassword);
        } while (lvEncodedOldPassword.equals(lvEncodedNewPassword));

        lvAccount.setPassword(lvEncodedNewPassword);
        lvAccount.setPasswordExpireDate(LocalDate.now().plusDays(getPasswordValidityPeriod()));
        lvAccount.setResetTokens(null);
        mvAccountRepository.save(lvAccount);

        return lvEncodedNewPassword;
    }

    @Override
    public String generatePassword() {
        int lvMaxPasswordLength = getPasswordLength();
        Random lvPasswordRandom = new Random(System.currentTimeMillis());

        char[] lvNewPassword = new char[lvMaxPasswordLength];
        String lvAlphabetCharacters = "ABCDEFGHJKLMNPRTUVWXYabcdefghijkmnprtuvwxy";
        String lvNumericCharacters = "346789";

        for (int i = 0; i < lvNewPassword.length; i++) {
            String lvPasswordCharacterRange = (i < lvNewPassword.length / 2 ? lvAlphabetCharacters : lvNumericCharacters);
            lvNewPassword[i] = lvPasswordCharacterRange.charAt(lvPasswordRandom.nextInt(lvPasswordCharacterRange.length()));
        }

        return new String(lvNewPassword);
    }

    @Override
    public String encodePassword(String pRawPassword) {
        BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();
        return bCrypt.encode(pRawPassword);
    }

    private int getPasswordLength() {
        SystemConfig pwdLengthCnf = Core.mvSystemConfigList.get(ConfigCode.passwordLength);
        if (isConfigAvailable(pwdLengthCnf)) {
            return pwdLengthCnf.getIntValue();
        }
        return DEFAULT_PASSWORD_LENGTH;
    }

    private int getPasswordValidityPeriod() {
        SystemConfig pwdValidityPeriodCnf = Core.mvSystemConfigList.get(ConfigCode.passwordValidityPeriod);
        if (isConfigAvailable(pwdValidityPeriodCnf)) {
            return pwdValidityPeriodCnf.getIntValue();
        }
        return DEFAULT_PASSWORD_VALIDITY_PERIOD_DAY;
    }
}