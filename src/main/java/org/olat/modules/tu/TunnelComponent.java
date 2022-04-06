/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.modules.tu;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;
import org.apache.http.util.CharArrayBuffer;
import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.media.AsyncMediaResponsible;
import org.olat.core.gui.media.HttpRequestMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.SimpleHtmlParser;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.tu.TUConfigForm;
import org.olat.course.nodes.tu.TURequest;
import org.olat.modules.ModuleConfiguration;

/**
 * @author Mike Stock Comment:
 */
public class TunnelComponent extends AbstractComponent implements AsyncMediaResponsible {
	private static final Logger log = Tracing.createLoggerFor(TunnelComponent.class);
	private static final ComponentRenderer RENDERER = new TunnelRenderer();
	
	private static final int PAGE_MAX_SIZE = 4 * 1024 * 1024; // 4 MB

	private String proto;
	private String host;
	private Integer port;
	private String query;
	private HttpClient httpClientInstance;
	private ModuleConfiguration config;
	private String htmlHead;
	private String jsOnLoad;
	private String htmlContent;
	private boolean firstCall = true;
	
	private String startUri;

	/**
	 * @param name
	 * @param config
	 * @param ureq
	 */
	public TunnelComponent(String name, ModuleConfiguration config, HttpClient httpClientInstance, UserRequest ureq) {
		super(name);
		this.config = config;
		this.httpClientInstance = httpClientInstance;
		int configVersion = config.getConfigurationVersion();
		
		//since config version 1  
		proto = (String) config.get(TUConfigForm.CONFIGKEY_PROTO);
		host = (String) config.get(TUConfigForm.CONFIGKEY_HOST);
		port = (Integer) config.get(TUConfigForm.CONFIGKEY_PORT);
		
		startUri = (String) config.get(TUConfigForm.CONFIGKEY_URI);
		if(configVersion==2) {
			//query string is available since config version 2
			query = (String) config.get(TUConfigForm.CONFIGKEY_QUERY);
		}
		fetchFirstResource(ureq);	
	}

	/**
	 * @return String
	 */
	public String getType() {
		return "hw";
	}
	
	private void fetchFirstResource(UserRequest ureq) {
		TURequest tureq = new TURequest(); //config, ureq);
		tureq.setContentType(null); // not used
		tureq.setMethod("GET");
		tureq.setParameterMap(Collections.<String,String[]>emptyMap());
		tureq.setQueryString(query);
		if(startUri != null){
			if(startUri.startsWith("/")){
				tureq.setUri(startUri);
			}else{
				tureq.setUri("/"+startUri);
			}
		}
		fillTURequestWithUserInfo(tureq,ureq);

		HttpResponse response = fetch(tureq, httpClientInstance);
		if (response == null) {
			setFetchError();
		}else{

			Header responseHeader = response.getFirstHeader("Content-Type");
			String mimeType;
			if (responseHeader == null) {
				setFetchError();
				mimeType = null;
			} else {
				mimeType = responseHeader.getValue();
			}
	
			if (mimeType != null && mimeType.startsWith("text/html")) {
				// we have html content, let doDispatch handle it for
				// inline rendering, update hreq for next content request
				String body = loadHtml(response.getEntity(), tureq);
				if(body != null) {
					SimpleHtmlParser parser = new SimpleHtmlParser(body);
					if (!parser.isValidHtml()) { // this is not valid HTML, deliver
						// asynchronuous
					}
					htmlHead = parser.getHtmlHead();
					jsOnLoad = parser.getJsOnLoad();
					htmlContent = parser.getHtmlContent();
				}
			} else {
				htmlContent = "Error: cannot display inline :"+tureq.getUri()+": mime type was '" + mimeType + 
					"' but expected 'text/html'. Response header was '" + responseHeader + "'.";
			}
		}
	}
	
	/**
	 * fills the given TURequest with userInformation such as lastName,
	 * firstName, email, ipAddress
	 * 
	 * @param tuRequest
	 * @param userRequest
	 */
	private void fillTURequestWithUserInfo(TURequest tuRequest, UserRequest userRequest){
		if("enabled".equals(CoreSpringFactory.getImpl(BaseSecurityModule.class).getUserInfosTunnelCourseBuildingBlock())) {
			String userName = userRequest.getIdentity().getName();
			User u = userRequest.getIdentity().getUser();
			String lastName = u.getProperty(UserConstants.LASTNAME, null);
			String firstName = u.getProperty(UserConstants.FIRSTNAME, null);
			String email = u.getProperty(UserConstants.EMAIL, null);
			String userIPAdress = userRequest.getUserSession().getSessionInfo().getFromIP();
			
			tuRequest.setEmail(email);
			tuRequest.setFirstName(firstName);
			tuRequest.setLastName(lastName);
			tuRequest.setUserName(userName);
			tuRequest.setUserIPAddress(userIPAdress);
		}
	}

	
	/**
	 * @see org.olat.core.gui.media.AsyncMediaResponsible#getAsyncMediaResource(org.olat.core.gui.UserRequest)
	 */
	public MediaResource getAsyncMediaResource(UserRequest ureq) {

		String moduleURI = ureq.getModuleURI();
		//FIXME:fj: can we distinguish between a ../ call an a click to another component?
		// now works for start uri's like /demo/tunneldemo.php but when in tunneldemo.php
		// a link is used like ../ this link does not work (moduleURI is null). if i use
		// ../index.php instead everything works as expected
		if (moduleURI == null) { // after a click on some other component e.g.
			if (!firstCall) return null;
			firstCall = false; // reset first call
		}

		TURequest tureq = new TURequest(config, ureq);

		fillTURequestWithUserInfo(tureq,ureq);

		HttpResponse response = fetch(tureq, httpClientInstance);
		if (response == null) {
			setFetchError();
			return null;
		}

		Header responseHeader = response.getFirstHeader("Content-Type");
		if (responseHeader == null) {
			setFetchError();
			return null;
		}

		String mimeType = responseHeader.getValue();
		if (mimeType != null && mimeType.startsWith("text/html")) {
			// we have html content, let doDispatch handle it for
			// inline rendering, update hreq for next content request
			String body = loadHtml(response.getEntity(), tureq);
			if(StringHelper.containsNonWhitespace(body)) {
				SimpleHtmlParser parser = new SimpleHtmlParser(body);
				if (!parser.isValidHtml()) {
					// This is not valid HTML, deliver asynchronuous
					return new HttpRequestMediaResource(response);
				}
				htmlHead = parser.getHtmlHead();
				jsOnLoad = parser.getJsOnLoad();
				htmlContent = parser.getHtmlContent();
			}
			setDirty(true);
		} else {
			return new HttpRequestMediaResource(response); // this is a async browser
		}
		// refetch
		return null;
	}
	
	private String loadHtml(final HttpEntity entity, final TURequest tureq) {
        try(InputStream inStream = entity.getContent()) {
            Args.check(entity.getContentLength() <= Integer.MAX_VALUE, "HTTP entity too large to be buffered in memory");
            
            ContentType contentType = ContentType.get(entity);
            int capacity = (int)entity.getContentLength();
            if (capacity < 0) {
                capacity = FileUtils.BSIZE;
            }
            Charset charset = null;
            if (contentType != null) {
                charset = contentType.getCharset();
                if (charset == null) {
                    final ContentType defaultContentType = ContentType.getByMimeType(contentType.getMimeType());
                    charset = defaultContentType != null ? defaultContentType.getCharset() : null;
                }
            }
            if (charset == null) {
                charset = HTTP.DEF_CONTENT_CHARSET;
            }
            final Reader reader = new InputStreamReader(inStream, charset);
            final CharArrayBuffer buffer = new CharArrayBuffer(capacity);
            final char[] tmp = new char[1024];
            int l;
            int count = 0;
            while((l = reader.read(tmp)) != -1) {
            	count += l;
            	if(count > PAGE_MAX_SIZE) {
        			log.error("Problems when tunneling URL::{} too big", tureq.getUri());
            		return "Too big";
            	}
                buffer.append(tmp, 0, l);
            }
            return buffer.toString();
        } catch(Exception e) {
			log.error("Problems when tunneling URL::{}", tureq.getUri(), e);
        	return null;
        }
    }

	private void setFetchError() {
		// some fetch error - reset to generic error message
		htmlHead = null;
		jsOnLoad = null;
		htmlContent = "Server error: No connection to tunneling server. Please check your configuration!";
	}

	/**
	 * @param tuReq
	 * @param client
	 * @return HttpMethod
	 */
	public HttpResponse fetch(TURequest tuReq, HttpClient client) {

		try {
			String modulePath = tuReq.getUri();
			
			URIBuilder builder = new URIBuilder();
			builder.setScheme(proto).setHost(host).setPort(port.intValue());
			if (modulePath == null) {
				modulePath = (startUri == null) ? "" : startUri;
			}
			if (modulePath.length() > 0 && modulePath.charAt(0) != '/') {
				modulePath = "/" + modulePath;
			}
			if(StringHelper.containsNonWhitespace(modulePath)) {
				builder.setPath(modulePath);
			}
	
			HttpUriRequest meth = null;
			String method = tuReq.getMethod();
			if (method.equals("GET")) {
				String queryString = tuReq.getQueryString();
				if (queryString != null) {
					builder.setCustomQuery(queryString);
				}
				meth = new HttpGet(builder.build());
	
			} else if (method.equals("POST")) {
				Map<String,String[]> params = tuReq.getParameterMap();
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
			}
			
			// Add olat specific headers to the request, can be used by external
			// applications to identify user and to get other params
			// test page e.g. http://cgi.algonet.se/htbin/cgiwrap/ug/test.py
			if("enabled".equals(CoreSpringFactory.getImpl(BaseSecurityModule.class).getUserInfosTunnelCourseBuildingBlock())) {
				meth.addHeader("X-OLAT-USERNAME", tuReq.getUserName());
				meth.addHeader("X-OLAT-LASTNAME", tuReq.getLastName());
				meth.addHeader("X-OLAT-FIRSTNAME", tuReq.getFirstName());
				meth.addHeader("X-OLAT-EMAIL", tuReq.getEmail());
				meth.addHeader("X-OLAT-USERIP", tuReq.getUserIPAddress());
			}

			return client.execute(meth);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
	// never called
	}


	@Override
	public String getExtendedDebugInfo() {
		return proto + ", " + host + ", " + port;
	}

	/**
	 * @return Returns the htmlContent.
	 */
	public String getHtmlContent() {
		return htmlContent;
	}

	/**
	 * @return Returns the htmlHead.
	 */
	public String getHtmlHead() {
		return htmlHead;
	}

	/**
	 * @return Returns the jsOnLoad.
	 */
	public String getJsOnLoad() {
		return jsOnLoad;
	}
	
	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		// browser uri: e.g null or
		String browserURI = ureq.getModuleURI();
		boolean redirect = true;
		if (browserURI == null) { // click on a treenode -> return without redirect
			// only if the currentURI is null (blank content)
			// or it is a root file
			if (startUri == null || startUri.indexOf("/") == -1) {
				redirect = false;
			}
		} else if (!ureq.isValidDispatchURI()) { // link from external
			// direct-jump-url or such ->
			// redirect
			redirect = true;
		} else {
			// browser uri != null and normal framework url dispatch = click from
			// within a page; currentURI == browserURI since asyncmedia-call took
			// place before validating.
			// never needs to redirect since browser page calculates relative page and
			// is handled by asyncmediaresponsible
			//FIXME:fj:a let userrequest tell me when is 
			if (browserURI.startsWith("olatcmd")) {
				redirect = true;
			} else {
				redirect = false;
			}
		}
		if (redirect) {
			String newUri = startUri;
			if (newUri.charAt(0) == '/') {
				newUri = newUri.substring(1);
			}
			vr.setNewModuleURI(newUri);
		}
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}