package com.flowiee.sms.core.vld;

import com.flowiee.sms.core.BaseAuthorize;
import com.flowiee.sms.model.ACTION;
import org.springframework.stereotype.Component;

@Component
public class ValidateModuleCategory extends BaseAuthorize {
    public boolean readCategory(boolean throwException) {
        return isAuthorized(ACTION.CTG_READ.name(), throwException);
    }

    public boolean insertCategory(boolean throwException) {
        return isAuthorized(ACTION.CTG_CREATE.name(), throwException);
    }

    public boolean updateCategory(boolean throwException) {
        return isAuthorized(ACTION.CTG_UPDATE.name(), throwException);
    }

    public boolean deleteCategory(boolean throwException) {
        return isAuthorized(ACTION.CTG_DELETE.name(), throwException);
    }

    public boolean importCategory(boolean throwException) {
        return isAuthorized(ACTION.CTG_IMPORT.name(), throwException);
    }
}