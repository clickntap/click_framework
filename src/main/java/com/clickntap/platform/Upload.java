package com.clickntap.platform;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import com.clickntap.api.BO;
import com.clickntap.smart.SmartContext;
import com.clickntap.utils.ConstUtils;

public class Upload extends BO {
  private MultipartFile file;
  private String url;
  private JSONObject json;
  private int statusCode;

  public MultipartFile getFile() {
    return file;
  }

  public void setFile(MultipartFile file) {
    this.file = file;
  }

  public void handle(SmartContext ctx, Uploader uploader) throws Exception {
    this.json = new JSONObject();
    HttpServletRequest request = ctx.getRequest();
    String fileInfoAsString = URLDecoder.decode(request.getHeader("f-file"), ConstUtils.UTF_8);
    JSONObject fileInfo = new JSONObject(fileInfoAsString);
    File tmpFile = File.createTempFile("chunk", "upload");
    File chunkFile = UploadFile.tmpFile(fileInfo, fileInfo.getInt("chunkId"));
    file.transferTo(tmpFile);
    FileUtils.moveFile(tmpFile, chunkFile);
    if (fileInfo.getInt("chunkId") == 0) {
      uploadFile(ctx, uploader, new UploadFile(fileInfo));
      this.json = fileInfo.getJSONObject("json");
    }
  }

  public void uploadFile(SmartContext ctx, Uploader uploader, UploadFile file) throws IOException, Exception {
    if (uploader.getAuth().getToken() != null) {
      JSONObject info = file.getInfo().getJSONObject("json");
      if (info.getString("entity").equalsIgnoreCase("media")) {
        if (isPDF(file)) {
          uploader.pdf(file);
        } else if (isImage(file)) {
          uploader.image(file);
        } else {
          uploader.video(file);
        }
        statusCode = HttpStatus.SC_OK;
      } else {
        uploader.file(file);
      }
    } else {
      statusCode = HttpStatus.SC_FORBIDDEN;
    }
  }

  private boolean isImage(UploadFile originalFilename) {
    return originalFilename.getContentType().contains("image");
  }

  private boolean isPDF(UploadFile file) {
    if (FilenameUtils.getExtension(file.getOriginalFilename()).equalsIgnoreCase("pdf")) {
      return true;
    }
    return false;
  }

  public String getContentType() {
    return "application/json; charset=UTF-8";
  }

  public void copyTo(OutputStream out) throws Exception {
    JSONObject info = new JSONObject(json.toString());
    info.put("url", url);
    info.put("statusCode", statusCode);
    out.write(info.toString().getBytes(ConstUtils.UTF_8));
  }

}
