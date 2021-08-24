package com.clickntap.platform;

import com.clickntap.api.Auth;

public interface Uploader {

  public Auth getAuth() throws Exception;

  public void pdf(UploadFile file) throws Exception;

  public void image(UploadFile file) throws Exception;

  public void video(UploadFile file) throws Exception;

  public void file(UploadFile file) throws Exception;

}
