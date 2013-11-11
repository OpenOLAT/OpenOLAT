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
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.UriBuilder;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

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
	
	private int port = WebDAVTestCase.PORT;
	private String host = WebDAVTestCase.HOST;
	private String protocol = WebDAVTestCase.PROTOCOL;
	
	private final BasicCookieStore cookieStore = new BasicCookieStore();
	private final CredentialsProvider provider = new BasicCredentialsProvider();

	private final CloseableHttpClient httpclient;

	public WebDAVConnection() {
		httpclient = HttpClientBuilder.create()
				.setDefaultCookieStore(cookieStore)
				.setDefaultCredentialsProvider(provider)
				.build();
	}
	
	public CookieStore getCookieStore() {
		return cookieStore;
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
	
	public String propfind(URI uri, int depth) throws IOException, URISyntaxException {
		HttpPropFind propfind = new HttpPropFind(uri);
		propfind.addHeader("Depth", Integer.toString(depth));
		HttpResponse response = execute(propfind);
		Assert.assertEquals(207, response.getStatusLine().getStatusCode());
		return EntityUtils.toString(response.getEntity());
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
		IOUtils.closeQuietly(httpclient);
	}
	
	/**
	 * @return http://localhost:9997
	 */
	public UriBuilder getBaseURI() throws URISyntaxException  {
		URI uri = new URI(protocol, null, host, port, null, null, null);
		return UriBuilder.fromUri(uri);
	}
}
