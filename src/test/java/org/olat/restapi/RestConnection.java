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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Description:<br>
 * Manage a connection to the grizzly server used by the unit test
 * with some helpers methods.
 * 
 * <P>
 * Initial Date:  20 déc. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RestConnection {
	
	private static final OLog log = Tracing.createLoggerFor(RestConnection.class);
	
	private int PORT = 9998;
	private String HOST = "localhost";
	private String PROTOCOL = "http";
	private String CONTEXT_PATH = "olat";
	
	private final DefaultHttpClient httpclient;
	private static final JsonFactory jsonFactory = new JsonFactory();

	public RestConnection() {
		httpclient = new DefaultHttpClient();
		HttpClientParams.setCookiePolicy(httpclient.getParams(), CookiePolicy.RFC_2109);
		
		//CookieStore cookieStore = new BasicCookieStore();
		//httpclient.setCookieStore(cookieStore);
	}
	
	public CookieStore getCookieStore() {
		return httpclient.getCookieStore();
	}
	
	public void shutdown() {
		httpclient.getConnectionManager().shutdown();
	}
	
	public boolean login(String username, String password) throws IOException, URISyntaxException {
		httpclient.getCredentialsProvider().setCredentials(
        new AuthScope("localhost", 9998),
        new UsernamePasswordCredentials(username, password));

		URI uri = getContextURI().path("auth").path(username).queryParam("password", password).build();
		HttpGet httpget = new HttpGet(uri);
		HttpResponse response = httpclient.execute(httpget);
    HttpEntity entity = response.getEntity();
    int code = response.getStatusLine().getStatusCode();
    EntityUtils.consume(entity);
    return code == 200;
	}
	
	public <T> T get(URI uri, Class<T> cl) throws IOException {
		HttpGet get = createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = execute(get);
		if(200 == response.getStatusLine().getStatusCode()) {
			HttpEntity entity = response.getEntity();
			return parse(entity.getContent(), cl);
		}
		log.error("get return: " + response.getStatusLine().getStatusCode());
		return null;
	}
	
	public void addEntity(HttpPut put, NameValuePair... pairs)
	throws UnsupportedEncodingException {
		if(pairs == null || pairs.length == 0) return;
		
		List<NameValuePair> pairList = new ArrayList<NameValuePair>();
		for(NameValuePair pair:pairs) {
			pairList.add(pair);
		}
		HttpEntity myEntity = new UrlEncodedFormEntity(pairList, "UTF-8");
		put.setEntity(myEntity);
	}
	
	public HttpPut createPut(URI uri, String accept, String langage, boolean cookie) {
		HttpPut put = new HttpPut(uri);
		decorateHttpMessage(put,accept, langage, cookie);
		return put;
	}
	
	public HttpGet createGet(URI uri, String accept, boolean cookie) {
		HttpGet get = new HttpGet(uri);
		decorateHttpMessage(get,accept, "en", cookie);
		return get;
	}
	
	public HttpPost createPost(URI uri, String accept, boolean cookie) {
		HttpPost get = new HttpPost(uri);
		decorateHttpMessage(get,accept, "en", cookie);
		return get;
	}
	
	private void decorateHttpMessage(HttpMessage msg, String accept, String langage, boolean cookie) {
		if(cookie) {
			HttpClientParams.setCookiePolicy(msg.getParams(), CookiePolicy.RFC_2109);
		}
		if(StringHelper.containsNonWhitespace(accept)) {
			msg.addHeader("Accept", accept);
		}
		if(StringHelper.containsNonWhitespace(langage)) {
			msg.addHeader("Accept-Language", langage);
		}
	}
	
	public HttpResponse execute(HttpUriRequest request)
	throws IOException {
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
	
	public <U> U parse(InputStream body, Class<U> cl) {
		try {
			ObjectMapper mapper = new ObjectMapper(jsonFactory);
			U obj = mapper.readValue(body, cl);
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
