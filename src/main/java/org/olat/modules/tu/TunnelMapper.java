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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.tu;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.HttpRequestMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Rewrite of the intern class with http client 4.3
 * 
 * Initial date: 30.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TunnelMapper implements Mapper {
	
	private static final Logger log = Tracing.createLoggerFor(TunnelMapper.class);
	private static final Set<String> headersToCopy = Set.of("if-range", "range", "connection");
	
	private final String proto;
	private final String host;
	private final Integer port;
	private final String startUri;
	private final String ipAddress;
	private final Identity ident;
	private final HttpClient httpClient;
	
	public TunnelMapper(String proto, String host, Integer port, String startUri, String ipAddress, Identity ident, HttpClient httpClient) {
		this.proto = proto;
		this.host = host;
		this.port = port;
		this.startUri = startUri;
		this.ipAddress = ipAddress;
		this.ident = ident;
		this.httpClient = httpClient;
	}
	
	@Override
	public MediaResource handle(String relPath, HttpServletRequest hreq) {
		String method = hreq.getMethod();
		String uri = relPath;
		HttpUriRequest meth = null;
		
		try {
			URIBuilder builder = new URIBuilder();
			builder.setScheme(proto).setHost(host).setPort(port.intValue());
			if (uri == null) {
				uri = (startUri == null) ? "" : startUri;
			}
			if (uri.length() > 0 && uri.charAt(0) != '/') {
				uri = "/" + uri;
			}
			if(StringHelper.containsNonWhitespace(uri)) {
				builder.setPath(uri);
			}

			if(method.equals("GET")) {
				String queryString = hreq.getQueryString();
				if(StringHelper.containsNonWhitespace(queryString)) {
					builder.setCustomQuery(queryString);
				}
				meth = new HttpGet(builder.build());
			} else if (method.equals("POST")) {
				Map<String,String[]> params = hreq.getParameterMap();
				HttpPost pmeth = new HttpPost(builder.build());
				List<BasicNameValuePair> pairs = new ArrayList<>();
				for (String key: params.keySet()) {
					String[] vals = params.get(key);
					for(String val:vals) {
						pairs.add(new BasicNameValuePair(key, val));
					}
				}

				HttpEntity entity = new UrlEncodedFormEntity(pairs, "UTF-8");
				pmeth.setEntity(entity);
				meth = pmeth;
			} else {
				return new NotFoundMediaResource();
			}
			
			// Add olat specific headers to the request, can be used by external
			// applications to identify user and to get other params
			// test page e.g. http://cgi.algonet.se/htbin/cgiwrap/ug/test.py
			if("enabled".equals(CoreSpringFactory.getImpl(BaseSecurityModule.class).getUserInfosTunnelCourseBuildingBlock())) {
				User u = ident.getUser();
				meth.addHeader("X-OLAT-USERNAME", ident.getName());
				meth.addHeader("X-OLAT-LASTNAME", u.getProperty(UserConstants.LASTNAME, null));
				meth.addHeader("X-OLAT-FIRSTNAME", u.getProperty(UserConstants.FIRSTNAME, null));
				meth.addHeader("X-OLAT-EMAIL", u.getProperty(UserConstants.EMAIL, null));
				meth.addHeader("X-OLAT-USERIP", ipAddress);
			}
			
			for(Enumeration<String> headerIt=hreq.getHeaderNames(); headerIt.hasMoreElements(); ) {
				String name = headerIt.nextElement();
				if(headersToCopy.contains(name.toLowerCase())) {
					String header = hreq.getHeader(name);
					meth.addHeader(name, header);
				} 
			}

			HttpResponse response = httpClient.execute(meth);
			if (response == null) {
				// error
				return new NotFoundMediaResource();
			}

			// get or post successfully
			Header responseHeader = response.getFirstHeader("Content-Type");
			if (responseHeader == null) {
				// error
				EntityUtils.consumeQuietly(response.getEntity());
				return new NotFoundMediaResource();
			}
			return new HttpRequestMediaResource(response);
		} catch (ClientProtocolException | URISyntaxException e) {
			log.error("", e);
			return createMediaResourceWithError(e);
		} catch (IOException e) {
			log.error("Error loading URI: {}", (meth == null ? "???" : meth.getURI()), e);
			return createMediaResourceWithError(e);
		}
	}
	
	/**
	 * Helper to create a media resource that prints the technical message into an
	 * HTML page
	 * 
	 * @param e
	 * @return
	 */
	private MediaResource createMediaResourceWithError(Exception e) {
		StringMediaResource smr = new StringMediaResource();
		String msg = e.getMessage();
		String lookupUrl = "https://google.com/search?q=" + StringHelper.urlEncodeUTF8(e.getMessage());			
		smr.setData("<html></body><b>Error:</b> <a href='" + lookupUrl + "' target='_blank' title='Click to google what this means'>" + msg + "</a></body></html>");
		smr.setContentType("text/html");		
		return smr;
	}
}