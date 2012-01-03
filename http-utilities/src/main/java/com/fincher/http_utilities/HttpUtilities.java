package com.fincher.http_utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

public class HttpUtilities {
	
	public static void downloadFile(File destFile, 
			String url,
			String username,
			String password,
			String proxyHost,
			int proxyPort,
			String proxyUsername,
			String proxyPassword) throws IOException {
		
		HttpClient httpclient = buildHttpClient(proxyHost, proxyPort, proxyUsername, proxyPassword);
		
		if (username != null) {
			
		}
		
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response = httpclient.execute(httpGet);
		
		HttpEntity resEntity = response.getEntity();		

		InputStream input = null;
		FileOutputStream output = null;

		try {		
			input = resEntity.getContent();		
			output = new FileOutputStream(destFile);

			byte[] buf = new byte[4096];
			int bytesRead;

			do {
				bytesRead = input.read(buf);
				if (bytesRead != -1) {
					output.write(buf, 0, bytesRead);
				}
			} while (bytesRead != -1);

		} finally {
			if (input != null) {
				input.close();
			}

			if (output != null) {
				output.close();
			}
		}	
	}
	
	public static final void uploadFile(URL url, 
			File file,
			Map<String, String> params,
			String proxyHost,
			int proxyPort,
			String proxyUsername,
			String proxyPassword) throws IOException {
		
		HttpClient httpclient = buildHttpClient(proxyHost, proxyPort, proxyUsername, proxyPassword);
		
		try {
			HttpPost httppost = new HttpPost(url.toString());

			FileBody bin = new FileBody(file);

			MultipartEntity reqEntity = new MultipartEntity();
			
			for (String paramName: params.keySet()) {
				reqEntity.addPart(paramName, new StringBody(params.get(paramName)));
			}
			
			reqEntity.addPart("bin", bin);            

			httppost.setEntity(reqEntity);

			//System.out.println("executing request " + httppost.getRequestLine());
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntity = response.getEntity();

			//            System.out.println("----------------------------------------");
			//            System.out.println(response.getStatusLine());
			if (resEntity != null) {
				System.out.println("Response content length: " + resEntity.getContentLength());
			}
			EntityUtils.consume(resEntity);
			//        } catch (Exception e) {
			//        	e.printStackTrace();
		} finally {
			try { 
				httpclient.getConnectionManager().shutdown(); 
			} catch (Exception ignore) {}
		}        
	}
	
	@SuppressWarnings("deprecation")
	public static final HttpClient buildHttpClient(String proxyHost, 
			int proxyPort,
			String proxyUsername, 
			String proxyPassword) throws IOException {
		try {
			SSLContext sslContext = SSLContext.getInstance("SSL");

			// set up a TrustManager that trusts everything
			sslContext.init(null, new TrustManager[] { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					System.out.println("getAcceptedIssuers =============");
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs,
						String authType) {
					System.out.println("checkClientTrusted =============");
				}

				public void checkServerTrusted(X509Certificate[] certs,
						String authType) {
					System.out.println("checkServerTrusted =============");
				}
			} }, new SecureRandom());

			SSLSocketFactory sf = new SSLSocketFactory(sslContext);
			sf.setHostnameVerifier(new X509HostnameVerifier() {
				
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
				
				@Override
				public void verify(String arg0, String[] arg1, String[] arg2)
						throws SSLException {
				}
				
				@Override
				public void verify(String arg0, X509Certificate arg1) throws SSLException {
				}
				
				@Override
				public void verify(String arg0, SSLSocket arg1) throws IOException {
				}
			});
			
			Scheme httpsScheme = new Scheme("https", sf, 443);
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(httpsScheme);

			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

			HttpParams params = new BasicHttpParams();
			ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
			DefaultHttpClient httpclient = new DefaultHttpClient(cm, params);
			
			if (proxyHost != null) {				
				if (proxyUsername != null) {
					httpclient.getAuthSchemes().register("ntlm", new NTLMSchemeFactory());	 
					httpclient.getCredentialsProvider().setCredentials(	 
							new AuthScope(proxyHost, proxyPort),
							new NTCredentials(proxyUsername, proxyPassword, "158.114.194.94", "NORTHGRUM"));
				}


				HttpHost proxy = new HttpHost(proxyHost, proxyPort);

				httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

			}
			return httpclient;
		} catch (Exception e) {
			throw new IOException(e);
		}

	}

}
