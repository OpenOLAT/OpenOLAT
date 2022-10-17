/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.restapi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Description:<br>
 * Manage a connection to the server used by the unit test
 * with some helpers methods.
 * 
 * <P>
 * Initial Date:  20 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RestConnection {
	
	private static final Logger log = Tracing.createLoggerFor(RestConnection.class);
	
	private int PORT = 9998;
	private String HOST = "localhost";
	private String PROTOCOL = "http";
	private String CONTEXT_PATH = "olat";

	private final BasicCookieStore cookieStore = new BasicCookieStore();
	private final BasicCredentialsProvider provider = new BasicCredentialsProvider();
	private final CloseableHttpClient httpclient;
	private static final JsonFactory jsonFactory = new JsonFactory();

	private String securityToken;
	
	public RestConnection() {
		httpclient = HttpClientBuilder.create()
				.setDefaultCookieStore(cookieStore)
				.setDefaultCredentialsProvider(provider)
				.build();
	}
	
	public RestConnection(boolean enableCookieStore, boolean enableCredentialProvider) {
		HttpClientBuilder builder = HttpClientBuilder.create();
		if(enableCookieStore) {
			builder = builder.setDefaultCookieStore(cookieStore);
		}
		if(enableCredentialProvider) {
			builder = builder.setDefaultCredentialsProvider(provider);
		}
		httpclient = builder.build();
	}
	
	public RestConnection(URL url) {
		PORT = url.getPort();
		HOST = url.getHost();
		PROTOCOL = url.getProtocol();
		CONTEXT_PATH = url.getPath();
		
		httpclient = HttpClientBuilder.create()
				.setDefaultCookieStore(cookieStore)
				.setDefaultCredentialsProvider(provider)
				.build();
	}
	
	/**
	 * Build a client with basic authentication delegated
	 * to the connection manager
	 * @param url
	 * @param user
	 * @param password
	 */
	public RestConnection(URL url, String user, String password) {
		PORT = url.getPort();
		HOST = url.getHost();
		PROTOCOL = url.getProtocol();
		CONTEXT_PATH = url.getPath();
		
		provider.setCredentials(new AuthScope(HOST, PORT), new UsernamePasswordCredentials(user, password));
		
		httpclient = HttpClientBuilder.create()
				.setDefaultCredentialsProvider(provider)
				.setDefaultCookieStore(cookieStore)
				.build();
	}
	
	public CookieStore getCookieStore() {
		return cookieStore;
	}
	
	public String getSecurityToken() {
		return securityToken;
	}
	
	public String getSecurityToken(HttpResponse response) {
		if(response == null) return null;
		
		Header header = response.getFirstHeader(RestSecurityHelper.SEC_TOKEN);
		return header == null ? null : header.getValue();
	}

	public void shutdown() {
		try {
			httpclient.close();
		} catch (IOException e) {
			log.error("", e);
		}
	}
	

	public boolean login(IdentityWithLogin identity) throws IOException, URISyntaxException {
		return login(identity.getLogin(), identity.getPassword());
	}

	public boolean login(String username, String password) throws IOException, URISyntaxException {
		URI uri = getContextURI().path("auth").path(username).queryParam("password", password).build();
		
		//provider credentials
		provider.setCredentials(new AuthScope(HOST, PORT), new UsernamePasswordCredentials(username, password));
		provider.setCredentials(new AuthScope(uri.getHost(), uri.getPort()), new UsernamePasswordCredentials(username, password));

		int code = -1;
		HttpGet httpget = new HttpGet(uri);
		try(CloseableHttpResponse response = httpclient.execute(httpget)) {
			Header header = response.getFirstHeader(RestSecurityHelper.SEC_TOKEN);
			if(header != null) {
				securityToken = header.getValue();
			}
			
		    HttpEntity entity = response.getEntity();
		    code = response.getStatusLine().getStatusCode();
		    EntityUtils.consume(entity);
		} catch(IOException e) {
			log.error("", e);
		}
	    return code == 200;
	}
	
	public <U> U get(URI uri, Class<U> cl) throws IOException, URISyntaxException {
		HttpGet get = createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = execute(get);
		if(200 == response.getStatusLine().getStatusCode()) {
			HttpEntity entity = response.getEntity();
			return parse(entity, cl);
		} else {
			EntityUtils.consume(response.getEntity());
			log.error("get return: " + response.getStatusLine().getStatusCode());
			return null;
		}
	}
	
	public void addEntity(HttpEntityEnclosingRequestBase put, NameValuePair... pairs)
	throws UnsupportedEncodingException {
		if(pairs == null || pairs.length == 0) return;
		
		List<NameValuePair> pairList = new ArrayList<>();
		for(NameValuePair pair:pairs) {
			pairList.add(pair);
		}
		HttpEntity myEntity = new UrlEncodedFormEntity(pairList, "UTF-8");
		put.setEntity(myEntity);
	}
	
	/**
	 * Add an object (application/json)
	 * @param put The request
	 * @param obj The object which will be serialized as json in UTF-8
	 * @throws UnsupportedEncodingException
	 */
	public void addJsonEntity(HttpEntityEnclosingRequestBase put, Object obj)
	throws UnsupportedEncodingException {
		if(obj == null) return;
		
		String objectStr = stringuified(obj);
		HttpEntity myEntity = new StringEntity(objectStr, ContentType.APPLICATION_JSON);
		put.setEntity(myEntity);
	}
	
	/**
	 * @param post The request
	 * @param filename The filename (will encoded as UTF-8)
	 * @param file The file (application/octet-stream)
	 * @throws UnsupportedEncodingException
	 */
	public void addMultipart(HttpEntityEnclosingRequestBase post, String filename, File file)
	throws UnsupportedEncodingException {
		
		HttpEntity entity = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				.addTextBody("filename", filename, ContentType.create("text/plain", StandardCharsets.UTF_8))
				.addBinaryBody("file", file, ContentType.APPLICATION_OCTET_STREAM, filename).build();
		post.setEntity(entity);
	}
	
	public HttpPut createPut(URI uri, String accept, boolean cookie) {
		HttpPut put = new HttpPut(uri);
		decorateHttpMessage(put,accept, "en", cookie);
		return put;
	}
	
	public HttpPut createPut(URI uri, String accept, String langage, boolean cookie) {
		HttpPut put = new HttpPut(uri);
		decorateHttpMessage(put,accept, langage, cookie);
		return put;
	}
	
	public HttpHead createHead(URI uri, String accept, boolean cookie) {
		HttpHead head = new HttpHead(uri);
		decorateHttpMessage(head,accept, "en", cookie);
		return head;
	}
	
	public HttpGet createGet(URI uri, String accept, boolean cookie) {
		HttpGet get = new HttpGet(uri);
		decorateHttpMessage(get,accept, "en", cookie);
		return get;
	}

	public HttpPost createPost(URI uri, String accept) {
		HttpPost get = new HttpPost(uri);
		decorateHttpMessage(get,accept, "en", true);
		return get;
	}
	
	public HttpDelete createDelete(URI uri, String accept) {
		HttpDelete del = new HttpDelete(uri);
		decorateHttpMessage(del, accept, "en", true);
		return del;
	}
	
	private void decorateHttpMessage(HttpRequestBase msg, String accept, String langage, boolean cookie) {
		if(cookie) {
			RequestConfig config = RequestConfig.copy(RequestConfig.DEFAULT)
				.setCookieSpec(CookieSpecs.DEFAULT)
				.build();
			msg.setConfig(config);
		}
		if(StringHelper.containsNonWhitespace(accept)) {
			msg.addHeader("Accept", accept);
		}
		if(StringHelper.containsNonWhitespace(langage)) {
			msg.addHeader("Accept-Language", langage);
		}
	}
	
	public HttpResponse execute(HttpUriRequest request)
	throws IOException, URISyntaxException {
		HttpResponse response = httpclient.execute(request);
		return response;
	}
	
	/**
	 * @return http://localhost:9998
	 */
	public UriBuilder getBaseURI() throws URISyntaxException  {
		URI uri = new URI(PROTOCOL, null, HOST, PORT, null, null, null);
		return UriBuilder.fromUri(uri);
	}
	
	/**
	 * @return http://localhost:9998/olat
	 */
	public UriBuilder getContextURI()  throws URISyntaxException {
		return getBaseURI().path(CONTEXT_PATH);
	}
	
	public String stringuified(Object obj) {
		try(StringWriter w = new StringWriter()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			mapper.writeValue(w, obj);
			return w.toString();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public <U> U parse(HttpEntity entity, Class<U> cl) {
		try(InputStream body = entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			return mapper.readValue(body, cl);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public <U> U parse(HttpResponse response, Class<U> cl) {
		try(InputStream body = response.getEntity().getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			return mapper.readValue(body, cl);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}