package com.clickntap.developers;

public interface Library {
  public void publishVersion(String json) throws Exception;

  public String downloadVersion(String version) throws Exception;
}
