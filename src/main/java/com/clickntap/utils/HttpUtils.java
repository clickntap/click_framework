package com.clickntap.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

public class HttpUtils {

  public static String get(String url, Map<String, String> headers) throws Exception {
    CloseableHttpClient client = HttpClientBuilder.create().build();
    HttpGet request = new HttpGet(url);
    headers(request, headers);
    CloseableHttpResponse response = client.execute(request);
    String responseAsString = response(response);
    response.close();
    client.close();
    return responseAsString;
  }

  public static String post(String url, Map<String, String> headers, Map<String, String> params) throws Exception {
    CloseableHttpClient client = HttpClientBuilder.create().build();
    HttpPost request = new HttpPost(url);
    if (params != null) {
      ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
      for (String key : params.keySet()) {
        postParameters.add(new BasicNameValuePair(key, params.get(key)));
      }
      HttpEntity entity = new UrlEncodedFormEntity(postParameters);
      request.setEntity(entity);
    }
    headers(request, headers);
    CloseableHttpResponse response = client.execute(request);
    String responseAsString = response(response);
    response.close();
    client.close();
    return responseAsString;
  }

  private static void headers(HttpRequest request, Map<String, String> headers) {
    if (headers == null)
      return;
    for (String key : headers.keySet()) {
      request.addHeader(key, headers.get(key));
    }
  }

  public static String post(String url, Map<String, String> headers, InputStream in) throws Exception {
    CloseableHttpClient client = HttpClientBuilder.create().build();
    HttpPost request = new HttpPost(url);
    if (in != null) {
      HttpEntity entity = new InputStreamEntity(in, ContentType.MULTIPART_FORM_DATA);
      request.setEntity(entity);
    }
    headers(request, headers);
    CloseableHttpResponse response = client.execute(request);
    String responseAsString = response(response);
    response.close();
    client.close();
    return responseAsString;
  }

  private static String response(CloseableHttpResponse response) throws Exception {
    String responseAsString = null;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    response.getEntity().writeTo(out);
    responseAsString = out.toString("UTF-8");
    out.close();
    return responseAsString;
  }

  public static void main(String args[]) throws Exception {
    JSONObject data = new JSONObject();
    data.put("email", "devManager");
    data.put("password", "pass");
    InputStream in = new ByteArrayInputStream(data.toString().getBytes(ConstUtils.UTF_8));
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Content-Type", "application/json");
    System.out.println(new JSONObject(HttpUtils.post("https://dev.neosurancecloud.net/setup/authorize", headers, in)).toString(2));
    in.close();
  }

}
