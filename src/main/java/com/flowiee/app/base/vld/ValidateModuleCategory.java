package com.flowiee.app.base.vld;

import com.flowiee.app.base.BaseAuthorize;
import com.flowiee.app.utils.AppConstants;
import org.springframework.stereotype.Component;

@Component
public class ValidateModuleCategory extends BaseAuthorize {
    public boolean readCategory(boolean throwException) {
        return isAuthorized(AppConstants.CATEGORY_ACTION.CTG_READ.name(), throwException);
    }

    public boolean insertCategory(boolean throwException) {
        return isAuthorized(AppConstants.CATEGORY_ACTION.CTG_CREATE.name(), throwException);
    }

    public boolean updateCategory(boolean throwException) {
        return isAuthorized(AppConstants.CATEGORY_ACTION.CTG_UPDATE.name(), throwException);
    }

    public boolean deleteCategory(boolean throwException) {
        return isAuthorized(AppConstants.CATEGORY_ACTION.CTG_DELETE.name(), throwException);
    }

    public boolean importCategory(boolean throwException) {
        return isAuthorized(AppConstants.CATEGORY_ACTION.CTG_IMPORT.name(), throwException);
    }
}