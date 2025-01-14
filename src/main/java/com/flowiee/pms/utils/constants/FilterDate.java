package com.flowiee.pms.utils.constants;

import com.flowiee.pms.exception.BadRequestException;
import com.flowiee.pms.utils.CoreUtils;
import lombok.Getter;

@Getter
public enum FilterDate {
    ToDay("T0"),
    PreviousDay("T-1"),
    SevenDaysAgo("T-7"),
    ThisMonth("M0"),
    PreviousMonth("M-1");

    private final String code;

    FilterDate(String code) {
        this.code = code;
    }

    public static FilterDate getByCode(String pCode) {
        for (FilterDate f : values()) {
            if (f.getCode().equalsIgnoreCase(CoreUtils.trim(pCode))) {
                return f;
            }
        }
        throw new BadRequestException("Date type invalid! " + pCode);
    }
}