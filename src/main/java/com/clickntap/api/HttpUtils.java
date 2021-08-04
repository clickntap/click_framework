package com.clickntap.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;

public class HttpUtils {

	public static String get(String url) throws Exception {
		return get(url, new HashMap<String, String>());
	}

	public static String get(String url, Map<String, String> headers) throws Exception {
		CloseableHttpClient client = newClient();
		HttpGet request = new HttpGet(url);
		headers(request, headers);
		CloseableHttpResponse response = client.execute(request);
		String responseAsString = response(response);
		response.close();
		client.close();
		return responseAsString;
	}

	public static void post(String url, Map<String, String> headers, Map<String, String> params, OutputStream out) throws Exception {
		CloseableHttpClient client = newClient();
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
		response.getEntity().writeTo(out);
		response.close();
		client.close();
	}

	public static CloseableHttpClient newClient() throws Exception {
		SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
		return HttpClients.custom().setRedirectStrategy(new DefaultRedirectStrategy()).setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier())).build();
	}

	public static long size(String url) throws Exception {
		CloseableHttpClient client = newClient();
		HttpHead request = new HttpHead(url);
		CloseableHttpResponse response = client.execute(request);
		long size = Long.parseLong(response.getFirstHeader("Content-Length").getValue());
		response.close();
		client.close();
		return size;
	}

	public static String post(String url, Map<String, String> headers, Map<String, String> params) throws Exception {
		CloseableHttpClient client = newClient();
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
		CloseableHttpClient client = newClient();
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

	public static void post(String url, Map<String, String> headers, InputStream in, OutputStream out) throws Exception {
		CloseableHttpClient client = newClient();
		HttpPost request = new HttpPost(url);
		if (in != null) {
			HttpEntity entity = new InputStreamEntity(in, ContentType.MULTIPART_FORM_DATA);
			request.setEntity(entity);
		}
		headers(request, headers);
		CloseableHttpResponse response = client.execute(request);
		response.getEntity().writeTo(out);
		response.close();
		client.close();
	}

	public static void get(String url, OutputStream out) throws Exception {
		CloseableHttpClient client = newClient();
		HttpGet request = new HttpGet(url);
		CloseableHttpResponse response = client.execute(request);
		response.getEntity().writeTo(out);
		response.close();
		client.close();
	}

	public static String response(CloseableHttpResponse response) throws Exception {
		String responseAsString = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		response.getEntity().writeTo(out);
		responseAsString = out.toString("UTF-8");
		out.close();
		return responseAsString;
	}

	public static int getFileSize(URL url) {
		URLConnection conn = null;
		try {
			conn = url.openConnection();
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection) conn).setRequestMethod("HEAD");
			}
			conn.getInputStream();
			return conn.getContentLength();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection) conn).disconnect();
			}
		}
	}

}
