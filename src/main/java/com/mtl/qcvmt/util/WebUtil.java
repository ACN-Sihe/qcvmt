package com.mtl.qcvmt.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class WebUtil {

  private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  private WebUtil() {
  }

  public static String getDateTimeNow() {
    return LocalDateTime.now().format(ISO_FORMATTER);
  }

  public static LocalDateTime getTime() {
    return LocalDateTime.now();
  }

  public static LocalDateTime getPreMonthTime() {
    return LocalDateTime.now().minusMonths(1);
  }

  public static LocalDateTime dataFormatTransfer(String dateStr) {
    return LocalDateTime.parse(dateStr);
  }

  public static LocalDateTime DataFormatTransfer(String dateStr) {
    return dataFormatTransfer(dateStr);
  }
}
