package com.clickntap.api;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.clickntap.api.BO;
import com.clickntap.tool.bean.BeanUtils;
import com.clickntap.utils.ConstUtils;

public class BOSorter {

  public void sort(List<? extends BO> items, final String propertyName) {
    sort(items, propertyName, true);
  }

  public void sort(List<? extends BO> items, final String propertyName, final boolean ascending) {
    Collections.sort(items, new Comparator<BO>() {
      public int compare(BO bo1, BO bo2) {
        try {
          String value1;
          String value2;
          try {
            value1 = BeanUtils.getValue(bo1, propertyName).toString().toLowerCase();
          } catch (Exception e) {
            value1 = ConstUtils.EMPTY;
          }
          try {
            value2 = BeanUtils.getValue(bo2, propertyName).toString().toLowerCase();
          } catch (Exception e) {
            value2 = ConstUtils.EMPTY;
          }
          try {
            int n1 = (int) (Double.parseDouble(value1) * 1000);
            int n2 = (int) (Double.parseDouble(value2) * 1000);
            if (ascending)
              return n1 - n2;
            else
              return n2 - n1;
          } catch (Exception e) {
          }
          if (ascending)
            return value1.compareTo(value2);
          else
            return value2.compareTo(value1);
        } catch (Throwable e) {
          return 0;
        }
      }
    });
  }

}
