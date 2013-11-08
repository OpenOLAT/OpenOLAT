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

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.olat.restapi.security.RestSecurityHelper;

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
	private String contextPath = WebDAVTestCase.CONTEXT_PATH;
	
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
	
	public String getSecurityToken(HttpResponse response) {
		if(response == null) return null;
		
		Header header = response.getFirstHeader(RestSecurityHelper.SEC_TOKEN);
		return header == null ? null : header.getValue();
	}

	public void close() {
		IOUtils.closeQuietly(httpclient);
	}
	
	public void setCredentials(String username, String password) {
		provider.setCredentials(
				new AuthScope(host, port, "OLAT WebDAV Access", "Basic"),
				new UsernamePasswordCredentials(username, password));
	}
	
	public HttpResponse propfind(URI uri) throws IOException, URISyntaxException {
		HttpPropFind propFind = new HttpPropFind(uri);
		propFind.addHeader("Depth", "1");
		HttpResponse response = httpclient.execute(propFind);
		return response;	
	}
	
	/**
	 * @return http://localhost:9997
	 */
	public UriBuilder getBaseURI() throws URISyntaxException  {
		URI uri = new URI(protocol, null, host, port, null, null, null);
		return UriBuilder.fromUri(uri);
	}
	
	/**
	 * @return http://localhost:9997/webdav
	 */
	public UriBuilder getContextURI()  throws URISyntaxException {
		return getBaseURI().path(contextPath);
	}
}
