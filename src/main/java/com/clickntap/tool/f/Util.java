package com.clickntap.tool.f;

import com.clickntap.tool.types.Datetime;
import com.clickntap.utils.ConstUtils;

public class Util {
    public String formatDate(String d, String format, String language) {
        try {
            return formatDate(new Datetime(d), format, language);
        } catch (Exception e) {
            return ConstUtils.EMPTY;
        }
    }

    public String formatDate(Datetime d, String format, String language) {
        try {
            return d.format(format, language);
        } catch (Exception e) {
            return ConstUtils.EMPTY;
        }
    }
}
