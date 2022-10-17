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
package org.olat.core.commons.services.webdav;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.olat.core.logging.Tracing;
import org.olat.test.JunitTestHelper.IdentityWithLogin;

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
public class WebDAVConnection implements Closeable {
	
	private static final Logger log = Tracing.createLoggerFor(WebDAVConnection.class);
	
	private final int port;
	private final String host;
	private final String protocol;
	
	private final BasicCookieStore cookieStore = new BasicCookieStore();
	private final CredentialsProvider provider = new BasicCredentialsProvider();

	private final CloseableHttpClient httpclient;

	public WebDAVConnection() {
		this(WebDAVTestCase.PROTOCOL, WebDAVTestCase.HOST, WebDAVTestCase.PORT, null);
	}
	
	public WebDAVConnection(String userAgent) {
		this(WebDAVTestCase.PROTOCOL, WebDAVTestCase.HOST, WebDAVTestCase.PORT, userAgent);
	}
	
	public WebDAVConnection(String protocol, String host, int port, String userAgent) {
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		
		HttpClientBuilder builder = HttpClientBuilder.create()
				.setDefaultCookieStore(cookieStore)
				.setDefaultCredentialsProvider(provider);
		if(userAgent != null) {
			builder.setUserAgent(userAgent);
		}
		httpclient = builder.build();
	}
	
	public CookieStore getCookieStore() {
		return cookieStore;
	}
	

	public void setCredentials(IdentityWithLogin identity) {
		setCredentials(identity.getLogin(), identity.getPassword());
	}
	
	public void setCredentials(String username, String password) {
		provider.setCredentials(
				new AuthScope(host, port, "OLAT WebDAV Access", "Basic"),
				new UsernamePasswordCredentials(username, password));
	}
	
	public HttpResponse execute(HttpUriRequest request)
	throws IOException, URISyntaxException {
		HttpResponse response = httpclient.execute(request);
		return response;
	}
	
	public HttpResponse head(URI uri) throws IOException, URISyntaxException {
		HttpHead propfind = new HttpHead(uri);
		return execute(propfind);
	}
	
	public String propfind(URI uri, int depth) throws IOException, URISyntaxException {
		HttpPropFind propfind = new HttpPropFind(uri);
		propfind.addHeader("Depth", Integer.toString(depth));
		HttpResponse response = execute(propfind);
		String text = EntityUtils.toString(response.getEntity());
		Assert.assertEquals(207, response.getStatusLine().getStatusCode());
		return text;
	}
	
	public int propfindTry(URI uri, int depth) throws IOException, URISyntaxException {
		HttpPropFind propfind = new HttpPropFind(uri);
		propfind.addHeader("Depth", Integer.toString(depth));
		HttpResponse response = execute(propfind);
		EntityUtils.consumeQuietly(response.getEntity());
		return response.getStatusLine().getStatusCode();
	}
	
	public int mkcol(URI uri) throws IOException, URISyntaxException {
		HttpMkcol mkcol = new HttpMkcol(uri);
		HttpResponse response = execute(mkcol);
		int returnCode = response.getStatusLine().getStatusCode();
		EntityUtils.consume(response.getEntity());
		return returnCode;
	}
	
	public int move(URI uri, String destination) throws IOException, URISyntaxException {
		HttpMove move = new HttpMove(uri);
		move.setHeader("Destination", destination);
		HttpResponse response = execute(move);
		int returnCode = response.getStatusLine().getStatusCode();
		EntityUtils.consume(response.getEntity());
		return returnCode;
	}
	
	public int copy(URI uri, String destination) throws IOException, URISyntaxException {
		HttpCopy copy = new HttpCopy(uri);
		copy.setHeader("Destination", destination);
		HttpResponse response = execute(copy);
		int returnCode = response.getStatusLine().getStatusCode();
		EntityUtils.consume(response.getEntity());
		return returnCode;
	}
	
	/**
	 * @return The lock token returned by the server
	 */
	public String lock(URI uri, String lockToken) throws IOException, URISyntaxException {
		HttpLock lock = new HttpLock(uri);
		decorateLockRequest(lock, lockToken);
		HttpResponse response = execute(lock);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		Header responseToken = response.getFirstHeader("Lock-Token");
		Assert.assertNotNull(responseToken);
		EntityUtils.consume(response.getEntity());
		return responseToken.getValue();
	}
	
	/**
	 * @return the return code of the request
	 */
	public int lockTry(URI uri, String lockToken) throws IOException, URISyntaxException {
		HttpLock lock = new HttpLock(uri);
		decorateLockRequest(lock, lockToken);
		HttpResponse response = execute(lock);
		int returnCode = response.getStatusLine().getStatusCode();
		EntityUtils.consume(response.getEntity());
		return returnCode;
	}
	
	private void decorateLockRequest(HttpLock lock, String lockToken) throws UnsupportedEncodingException {
		lock.addHeader("Lock-Token", lockToken);
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>")
		  .append("<D:lockinfo xmlns:D='DAV:'>")
		  .append("  <D:lockscope><D:exclusive/></D:lockscope>")
		  .append("  <D:locktype><D:write/></D:locktype>")
		  .append("  <D:owner>")
		  .append("       <D:href>").append(lock.getURI().toString()).append("</D:href>")
		  .append("  </D:owner>")
		  .append(" </D:lockinfo>");
		lock.setEntity(new StringEntity(sb.toString()));
	}
	
	public int unlock(URI uri, String lockToken) throws IOException, URISyntaxException {
		HttpUnlock unlock = new HttpUnlock(uri);
		unlock.addHeader("Lock-Token", lockToken);
		HttpResponse response = execute(unlock);
		int returnCode = response.getStatusLine().getStatusCode();
		EntityUtils.consume(response.getEntity());
		return returnCode;
	}
	
	public HttpOptions createOptions(URI uri) throws IOException, URISyntaxException {
		HttpOptions options = new HttpOptions(uri);
		return options;	
	}
	
	public HttpPut createPut(URI uri) {
		HttpPut put = new HttpPut(uri);
		put.addHeader("Accept", "*/*");
		return put;
	}
	
	public HttpDelete createDelete(URI uri) {
		HttpDelete delete = new HttpDelete(uri);
		return delete;
	}
	
	public HttpPropPatch createPropPatch(URI uri) {
		HttpPropPatch proppatch = new HttpPropPatch(uri);
		proppatch.addHeader("Accept", "*/*");
		return proppatch;
	}
	
	public HttpGet createGet(URI uri) {
		HttpGet get = new HttpGet(uri);
		get.addHeader("Accept", "*/*");
		return get;
	}
	
	@Override
	public void close() {
		try {
			httpclient.close();
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	/**
	 * @return http://localhost:9997
	 */
	public UriBuilder getBaseURI() throws URISyntaxException  {
		URI uri = new URI(protocol, null, host, port, null, null, null);
		return UriBuilder.fromUri(uri);
	}
}
