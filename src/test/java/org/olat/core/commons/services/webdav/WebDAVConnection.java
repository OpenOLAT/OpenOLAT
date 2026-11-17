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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.ConnectionUtilities;
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
	
	private final String username;
	private final String password;
	
	private final CookieManager cookieStore = new CookieManager();
	private final HttpClient httpclient;
	
	private static final Pattern DIGEST_PARAM = Pattern.compile("(\\w+)=(?:\"([^\"]*)\"|([^,\\s]*))");
	private static final SecureRandom RANDOM = new SecureRandom();
	private volatile DigestChallenge digestChallenge;

	public WebDAVConnection() {
		this(WebDAVTestCase.PROTOCOL, WebDAVTestCase.HOST, WebDAVTestCase.PORT, null, null);
	}
	
	public WebDAVConnection(IdentityWithLogin user) {
		this(WebDAVTestCase.PROTOCOL, WebDAVTestCase.HOST, WebDAVTestCase.PORT, user.getLogin(), user.getPassword());
	}
	
	public WebDAVConnection(String protocol, String host, int port, String username, String password) {
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		
		httpclient = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1)
				.cookieHandler(cookieStore)
				.build();
	}
	
	public CookieManager getCookieStore() {
		return cookieStore;
	}
	
	/**
	 * Send the request. If credentials are set, the request is authenticated
	 * with HTTP Digest (RFC 7616, MD5, qop=auth): preemptively if a challenge
	 * is already known, otherwise after the 401 challenge of the server.
	 */
	public HttpResponse<InputStream> execute(HttpRequest request)
	throws IOException, URISyntaxException, InterruptedException {
		if(!StringHelper.containsNonWhitespace(username)) {
			return httpclient.send(request, BodyHandlers.ofInputStream());
		}
		
		DigestChallenge challenge = digestChallenge;
		HttpRequest authRequest = challenge == null ? request : authorize(request, challenge);
		HttpResponse<InputStream> response = httpclient.send(authRequest, BodyHandlers.ofInputStream());
		if(response.statusCode() == 401) {
			Optional<DigestChallenge> newChallenge = response.headers().allValues("WWW-Authenticate").stream()
					.filter(h -> h.regionMatches(true, 0, "Digest", 0, 6))
					.map(DigestChallenge::parse)
					.findFirst();
			if(newChallenge.isPresent()) {
				ConnectionUtilities.consume(response);
				digestChallenge = newChallenge.get();
				response = httpclient.send(authorize(request, newChallenge.get()), BodyHandlers.ofInputStream());
			}
		}
		return response;
	}
	
	private HttpRequest authorize(HttpRequest request, DigestChallenge challenge) {
		URI uri = request.uri();
		String digestUri = uri.getRawPath();
		if(uri.getRawQuery() != null) {
			digestUri += "?" + uri.getRawQuery();
		}
		String nc = String.format("%08x", challenge.nonceCount().incrementAndGet());
		byte[] cnonceBytes = new byte[8];
		RANDOM.nextBytes(cnonceBytes);
		String cnonce = HexFormat.of().formatHex(cnonceBytes);
		
		String ha1 = Encoder.md5hash(username + ":" + challenge.realm() + ":" + password);
		String ha2 = Encoder.md5hash(request.method() + ":" + digestUri);
		String digest = Encoder.md5hash(ha1 + ":" + challenge.nonce() + ":" + nc + ":" + cnonce + ":auth:" + ha2);
		
		String authorization = "Digest username=\"" + username + "\", realm=\"" + challenge.realm()
				+ "\", nonce=\"" + challenge.nonce() + "\", uri=\"" + digestUri
				+ "\", qop=auth, nc=" + nc + ", cnonce=\"" + cnonce + "\", response=\"" + digest
				+ "\", algorithm=MD5";
		return HttpRequest.newBuilder(request, (name, _) -> !"Authorization".equalsIgnoreCase(name))
				.header("Authorization", authorization)
				.build();
	}
	
	private record DigestChallenge(String realm, String nonce, AtomicInteger nonceCount) {
		
		static DigestChallenge parse(String header) {
			String realm = null;
			String nonce = null;
			Matcher m = DIGEST_PARAM.matcher(header);
			while(m.find()) {
				String value = m.group(2) != null ? m.group(2) : m.group(3);
				if("realm".equalsIgnoreCase(m.group(1))) {
					realm = value;
				} else if("nonce".equalsIgnoreCase(m.group(1))) {
					nonce = value;
				}
			}
			return new DigestChallenge(realm, nonce, new AtomicInteger());
		}
	}
	
	public HttpResponse<InputStream> head(URI uri)
	throws IOException, URISyntaxException, InterruptedException {
		HttpRequest propfind = HttpRequest.newBuilder()
				.HEAD()
				.uri(uri)
				.version(HttpClient.Version.HTTP_1_1)
				.build();
		return execute(propfind);
	}
	
	public HttpResponse<InputStream> head(URI uri, String userAgent)
	throws IOException, URISyntaxException, InterruptedException {
		HttpRequest propfind = HttpRequest.newBuilder()
				.HEAD()
				.uri(uri)
				.version(HttpClient.Version.HTTP_1_1)
				.setHeader("User-Agent", userAgent)
				.build();
		return execute(propfind);
	}
	
	public String propfind(URI uri, int depth)
	throws IOException, URISyntaxException, InterruptedException {
		HttpRequest propfind = createPropfind(uri, depth);
		HttpResponse<InputStream> response = execute(propfind);
		String text = ConnectionUtilities.toString(response);
		Assert.assertEquals(207, response.statusCode());
		return text;
	}
	
	public int propfindTry(URI uri, int depth)
	throws IOException, URISyntaxException, InterruptedException {
		HttpRequest propfind = createPropfind(uri, depth);
		HttpResponse<InputStream> response = execute(propfind);
		ConnectionUtilities.consume(response);
		return response.statusCode();
	}
	
	public HttpRequest createPropfind(URI uri, int depth)
	throws IOException, URISyntaxException, InterruptedException {
		return HttpRequest.newBuilder()
				.method("PROPFIND", BodyPublishers.noBody())
				.uri(uri)
				.header("Depth", Integer.toString(depth))
				.build();
	}
	
	public int mkcol(URI uri)
	throws IOException, URISyntaxException, InterruptedException {
		HttpRequest mkcol = HttpRequest.newBuilder()
				.method("MKCOL", BodyPublishers.noBody())
				.uri(uri)
				.build();
		HttpResponse<InputStream> response = execute(mkcol);
		int returnCode = response.statusCode();
		ConnectionUtilities.consume(response);
		return returnCode;
	}
	
	public int move(URI uri, String destination)
	throws IOException, URISyntaxException, InterruptedException {
		HttpRequest move = HttpRequest.newBuilder()
				.method("MOVE", BodyPublishers.noBody())
				.uri(uri)
				.header("Destination", destination)
				.build();
		HttpResponse<InputStream> response = execute(move);
		int returnCode = response.statusCode();
		ConnectionUtilities.consume(response);
		return returnCode;
	}
	
	public int copy(URI uri, String destination)
	throws IOException, URISyntaxException, InterruptedException {
		HttpRequest copy = HttpRequest.newBuilder()
				.method("COPY", BodyPublishers.noBody())
				.uri(uri)
				.header("Destination", destination)
				.build();
		HttpResponse<InputStream> response = execute(copy);
		int returnCode = response.statusCode();
		ConnectionUtilities.consume(response);
		return returnCode;
	}
	
	/**
	 * @return The lock token returned by the server
	 * @throws InterruptedException 
	 */
	public String lock(URI uri, String lockToken)
	throws IOException, URISyntaxException, InterruptedException {
		HttpRequest lock = decorateLockRequest(uri, lockToken);
		HttpResponse<InputStream> response = execute(lock);
		Assert.assertEquals(200, response.statusCode());
		Optional<String> responseToken = response.headers().firstValue("Lock-Token");
		ConnectionUtilities.consume(response);
		return responseToken.get();
	}
	
	/**
	 * @return the return code of the request
	 * @throws InterruptedException 
	 */
	public int lockTry(URI uri, String lockToken)
	throws IOException, URISyntaxException, InterruptedException {
		HttpRequest lock = decorateLockRequest(uri, lockToken);
		HttpResponse<InputStream> response = execute(lock);
		int returnCode = response.statusCode();
		ConnectionUtilities.consume(response);
		return returnCode;
	}
	
	private HttpRequest decorateLockRequest(URI uri, String lockToken) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>")
		  .append("<D:lockinfo xmlns:D='DAV:'>")
		  .append("  <D:lockscope><D:exclusive/></D:lockscope>")
		  .append("  <D:locktype><D:write/></D:locktype>")
		  .append("  <D:owner>")
		  .append("       <D:href>").append(uri.toString()).append("</D:href>")
		  .append("  </D:owner>")
		  .append(" </D:lockinfo>");
		
		return HttpRequest.newBuilder()
				.method("LOCK", BodyPublishers.ofString(sb.toString(), StandardCharsets.UTF_8))
				.uri(uri)
				.header("Lock-Token", lockToken)
				.build();
	}
	
	public int unlock(URI uri, String lockToken)
	throws IOException, URISyntaxException, InterruptedException {
		HttpRequest unlock = HttpRequest.newBuilder()
				.method("UNLOCK", BodyPublishers.noBody())
				.uri(uri)
				.header("Lock-Token", lockToken)
				.build();
		HttpResponse<InputStream> response = execute(unlock);
		int returnCode = response.statusCode();
		ConnectionUtilities.consume(response);
		return returnCode;
	}
	
	public HttpRequest createOptions(URI uri) throws IOException, URISyntaxException {
		return HttpRequest.newBuilder()
				.method("OPTIONS", BodyPublishers.noBody())
				.uri(uri)
				.build();
	}
	
	public HttpRequest createPut(URI uri, BodyPublisher publisher) {
		return HttpRequest.newBuilder()
				.PUT(publisher)
				.uri(uri)
				.header("Accept", "*/*")
				.build();
	}
	
	public HttpRequest createDelete(URI uri) {
		return HttpRequest.newBuilder()
				.DELETE()
				.uri(uri)
				.build();
	}
	
	public HttpRequest createDelete(URI uri, String lockTocken) {
		return HttpRequest.newBuilder()
				.DELETE()
				.uri(uri)
				.header("Lock-Token", lockTocken)
				.build();
	}
	
	public HttpRequest createPropPatch(URI uri, BodyPublisher publisher) {
		return HttpRequest.newBuilder()
				.method("PROPPATCH", publisher)
				.uri(uri)
				.header("Accept", "*/*")
				.build();
	}
	
	public HttpRequest createGet(URI uri) {
		return HttpRequest.newBuilder()
				.GET()
				.uri(uri)
				.header("Accept", "*/*")
				.build();
	}
	
	@Override
	public void close() {
		try {
			httpclient.close();
		} catch (Exception e) {
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
