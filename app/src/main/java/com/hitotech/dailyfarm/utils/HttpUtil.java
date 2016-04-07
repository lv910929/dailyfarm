package com.hitotech.dailyfarm.utils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpUtil {

	public static String postRequest(String serverPath,
			Map<String, String> params, String encoding)
			throws ClientProtocolException, IOException {
		List<NameValuePair> paramPair = new ArrayList<NameValuePair>();
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				paramPair.add(new BasicNameValuePair(entry.getKey(), entry
						.getValue()));
			}
		}

		HttpClient client = new DefaultHttpClient();
		serverPath= serverPath.replaceAll(" ", "%20");
		HttpPost post = new HttpPost(serverPath);
		post.setEntity(new UrlEncodedFormEntity(paramPair, HTTP.UTF_8));
		HttpResponse response = client.execute(post);
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));

			String reqData = null;
			String responseData = "";
			while ((reqData = br.readLine()) != null) {
				responseData += reqData;
			}
			br.close();
			return responseData;
		}

		return "postRequest error";
	}
}
