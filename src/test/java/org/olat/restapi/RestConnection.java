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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.CookieManager;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.ConnectionUtilities;
import org.olat.core.util.httpclient.ConnectionUtilities.NameValuePair;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.user.restapi.UserVO;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

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
public class RestConnection implements AutoCloseable {
	
	private static final Logger log = Tracing.createLoggerFor(RestConnection.class);
	
	static {
		System.setProperty("jdk.httpclient.auth.retrylimit", "0");
		
	}

	private static final JsonFactory jsonFactory = new JsonFactory();
	private static final ObjectMapper mapper = new ObjectMapper(jsonFactory);

	public static final String HEADER_ACCEPT = "Accept";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	
	private int PORT = 9998;
	private String HOST = "localhost";
	private String PROTOCOL = "http";
	private String CONTEXT_PATH = "olat";

	private final CookieManager cookieStore = new CookieManager();
	private final HttpClient httpclient;
	
	private String securityToken;
	
	public RestConnection() {
		httpclient = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.cookieHandler(cookieStore)
			.build();
	}
	
	public RestConnection(boolean withCookieHandler) {
		HttpClient.Builder builder = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1);
		if(withCookieHandler) {
			builder.cookieHandler(cookieStore);
		}
		httpclient = builder.build();
	}
	
	public RestConnection(String username, String password) {
		httpclient = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1)
				.cookieHandler(cookieStore)
				.authenticator(new BasicAuthenticator(username, password))
				.build();
	}
	
	public RestConnection(String username, String password, boolean withCookieHandler) {
		HttpClient.Builder builder = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1);
		if(withCookieHandler) {
			builder.cookieHandler(cookieStore);
		}
		builder.authenticator(new BasicAuthenticator(username, password));
		httpclient = builder.build();
	}

	public RestConnection(IdentityWithLogin identity) {
		this(identity.getLogin(), identity.getPassword());
	}
	
	public RestConnection(URL url) {
		PORT = url.getPort();
		HOST = url.getHost();
		PROTOCOL = url.getProtocol();
		CONTEXT_PATH = url.getPath();
		
		httpclient = HttpClient.newBuilder()
				.cookieHandler(cookieStore)
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

		httpclient = HttpClient.newBuilder()
				.cookieHandler(cookieStore)
				.authenticator(new BasicAuthenticator(user, password))
				.build();
	}
	
	public CookieManager getCookieStore() {
		return cookieStore;
	}
	
	public String getSecurityToken() {
		return securityToken;
	}
	
	/**
	 * Call the protected method /users/me to authenticate.
	 * 
	 * @return A security token if login successful
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InterruptedException 
	 */
	public String callMeForSecurityToken()
	throws IOException, URISyntaxException, InterruptedException {
		URI url = getContextURI().path("/users/me").build();
		HttpRequest request = createGet(url, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = execute(request);
		Assert.assertEquals(200, response.statusCode());
		securityToken = getSecurityToken(response);
		UserVO vo = parse(response, UserVO.class);
		Assert.assertNotNull(vo);
		return securityToken;
	}
	
	public String getSecurityToken(HttpResponse<InputStream> response) {
		if(response == null) return null;
		
		return response.headers()
				.firstValue(RestSecurityHelper.SEC_TOKEN)
				.orElse(null);
	}
	
	public <U> U get(URI uri, Class<U> cl) throws IOException, URISyntaxException, InterruptedException {
		HttpRequest get = createGet(uri, MediaType.APPLICATION_JSON);
		HttpResponse<InputStream> response = execute(get);
		if(200 == response.statusCode()) {
			return parse(response, cl);
		} else {
			consume(response);
			log.error("get return: {}", response.statusCode());
			return null;
		}
	}
	
	public HttpRequest createPut(URI uri, String accept) {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(uri)
				.version(HttpClient.Version.HTTP_1_1)
				.PUT(BodyPublishers.noBody());
		decorateHttpMessage(builder, accept, "en");
		return builder.build();
	}
	
	public HttpRequest createPut(URI uri, Object jsonEntity, String accept) {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(uri)
				.version(HttpClient.Version.HTTP_1_1)
				.PUT(BodyPublishers.ofString(stringuified(jsonEntity), StandardCharsets.UTF_8))
				.header(HEADER_CONTENT_TYPE, MediaType.APPLICATION_JSON);
		decorateHttpMessage(builder, accept, "en");
		return builder.build();
	}
	
	public HttpRequest createPut(URI uri, List<NameValuePair> formParameters, String accept) {
		String form = ConnectionUtilities.urlEncode(formParameters, StandardCharsets.UTF_8);
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(uri)
				.version(HttpClient.Version.HTTP_1_1)
				.header(HEADER_CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
				.PUT(BodyPublishers.ofString(form, StandardCharsets.UTF_8));
		decorateHttpMessage(builder, accept, "en");
		return builder.build();
	}
	
	public HttpRequest createPut(URI uri, File file, String filename, List<NameValuePair> formParameters, String accept) {
		String boundary = ConnectionUtilities.getBoundary();
		byte[] fileData = file == null ? null : toBinary(file);
		BodyPublisher form = ConnectionUtilities.ofMimeMultipartData(filename, MediaType.APPLICATION_OCTET_STREAM, fileData, formParameters, boundary);
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(uri)
				.version(HttpClient.Version.HTTP_1_1)
				.header(HEADER_CONTENT_TYPE, "multipart/form-data;boundary=" + boundary)
				.PUT(form);
		decorateHttpMessage(builder, accept, "en");
		return builder.build();
	}
	
	public HttpRequest createPut(URI uri, Object jsonEntity, String accept, String langage) {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(uri)
				.version(HttpClient.Version.HTTP_1_1)
				.PUT(BodyPublishers.ofString(stringuified(jsonEntity), StandardCharsets.UTF_8))
				.header(HEADER_CONTENT_TYPE, MediaType.APPLICATION_JSON);
		decorateHttpMessage(builder, accept, langage);
		return builder.build();
	}
	
	public HttpRequest createHead(URI uri, String accept) {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(uri)
				.version(HttpClient.Version.HTTP_1_1)
				.method("HEAD", BodyPublishers.noBody());
		decorateHttpMessage(builder, accept, "en");
		return builder.build();
	}
	
	public HttpRequest createGet(URI uri, Header... headers) {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
			.uri(uri)
			.version(HttpClient.Version.HTTP_1_1)
			.GET();
		if(headers != null && headers.length > 0) {
			for(Header header:headers) {
				builder.setHeader(header.name(), header.value());
			}
		}
		return builder.build();
	}
	
	public HttpRequest createGet(URI uri, String accept, Header... headers) {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
			.uri(uri)
			.version(HttpClient.Version.HTTP_1_1)
			.GET();
		decorateHttpMessage(builder, accept, "en");
		if(headers != null && headers.length > 0) {
			for(Header header:headers) {
				builder.setHeader(header.name(), header.value());
			}
		}
		return builder.build();
	}
	
	public HttpRequest createGet(URI uri, String accept, String language) {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
			.uri(uri)
			.version(HttpClient.Version.HTTP_1_1)
			.GET();
		decorateHttpMessage(builder, accept, language);
		return builder.build();
	}
	
	public HttpRequest createPost(URI uri, String accept, Header... headers) {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(uri)
				.version(HttpClient.Version.HTTP_1_1)
				.POST(BodyPublishers.noBody());
		decorateHttpMessage(builder, accept, "en");
		if(headers != null && headers.length > 0) {
			for(Header header:headers) {
				builder.setHeader(header.name(), header.value());
			}
		}
		return builder.build();
	}

	public HttpRequest createPost(URI uri, Object jsonEntity, String accept) {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(uri)
				.version(HttpClient.Version.HTTP_1_1)
				.POST(BodyPublishers.ofString(stringuified(jsonEntity), StandardCharsets.UTF_8))
				.header(HEADER_CONTENT_TYPE, MediaType.APPLICATION_JSON);
		decorateHttpMessage(builder, accept, "en");
		return builder.build();
	}
	
	public HttpRequest createPost(URI uri, List<NameValuePair> formParameters, String accept) {
		String form = ConnectionUtilities.urlEncode(formParameters, StandardCharsets.UTF_8);
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(uri)
				.version(HttpClient.Version.HTTP_1_1)
				.header(HEADER_CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
				.POST(BodyPublishers.ofString(form, StandardCharsets.UTF_8));
		decorateHttpMessage(builder, accept, "en");
		return builder.build();
	}
	
	public HttpRequest createPost(URI uri, File file, String filename, List<NameValuePair> formParameters, String accept) {
		String boundary = ConnectionUtilities.getBoundary();
		byte[] fileData =  file == null ? null : toBinary(file);
		BodyPublisher form = ConnectionUtilities.ofMimeMultipartData(filename, MediaType.APPLICATION_OCTET_STREAM, fileData, formParameters, boundary);
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(uri)
				.version(HttpClient.Version.HTTP_1_1)
				.header(HEADER_CONTENT_TYPE, "multipart/form-data;boundary=" + boundary)
				.POST(form);
		decorateHttpMessage(builder, accept, "en");
		return builder.build();
	}
	
	private byte[] toBinary(File file) {
		try(InputStream in = new FileInputStream(file)) {
			return FileUtils.loadAsBytes(in);
		} catch(Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public HttpRequest createDelete(URI uri, String accept) {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(uri)
				.version(HttpClient.Version.HTTP_1_1)
				.DELETE();
		decorateHttpMessage(builder, accept, "en");
		return builder.build();
	}
	
	private void decorateHttpMessage(HttpRequest.Builder msg, String accept, String langage) {
		if(StringHelper.containsNonWhitespace(accept)) {
			msg = msg.header("Accept", accept);
		}
		if(StringHelper.containsNonWhitespace(langage)) {
			msg = msg.header("Accept-Language", langage);
		}
	}
	
	public HttpResponse<InputStream> execute(HttpRequest request)
	throws IOException, URISyntaxException, InterruptedException {
		return httpclient.send(request, BodyHandlers.ofInputStream());
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
	
	public Header autorizationheader(String username, String password) {
		String credentials = username + ":" + password;
		return new Header("Authorization", "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8)));
	}
	
	public String stringuified(Object obj) {
		try(StringWriter w = new StringWriter()) {
			mapper.writeValue(w, obj);
			return w.toString();
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public static final String toString(HttpResponse<InputStream> response) {
		if(response == null) return null;
		
		try(InputStream body=response.body();
				InputStreamReader inputReader = new InputStreamReader(body, StandardCharsets.UTF_8);
				BufferedReader bReader = new BufferedReader(inputReader)) {
			return bReader
					.lines()
					.collect(Collectors.joining());
		} catch (Exception e) {
			log.error("UTF8 Stream to string", e);
			return null;
		}
	}
	
	public static final void consume(HttpResponse<InputStream> response) {
		if(response != null) {
			try(InputStream body=response.body()) {
				byte[] buffer = new byte[4096];
				while (body.read(buffer, 0, buffer.length) != -1) {
		    		//consume
				}
			} catch (Exception e) {
				log.error("Consume", e);
			}
		}
	}
	
	public <U> U parse(HttpResponse<InputStream> response, Class<U> cl) {
		try(InputStream body = response.body()) {
			return mapper.readValue(body, cl);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	public <U> List<U> parseList(HttpResponse<InputStream> response, Class<U> cl) {
		CollectionType javaType = mapper.getTypeFactory()
			      .constructCollectionType(List.class, cl);
		try(InputStream in=response.body()) {
			return mapper.readValue(in, javaType);
		} catch(Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	@Override
	public void close() {
		httpclient.close();
	}
	
	public void shutdown() {
		httpclient.shutdownNow();
	}
	
	public record Header(String name, String value) {
		//
	}
	
	public class BasicAuthenticator extends Authenticator {
		private final String name;
		private final String password;

		public BasicAuthenticator(String name, String password) {
			this.name = name;
			this.password = password;
		}

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(name, password.toCharArray());
		}
	}
}