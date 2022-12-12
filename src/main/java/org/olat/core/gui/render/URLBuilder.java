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
* <p>
*/ 

package org.olat.core.gui.render;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * @author Felix Jost
 */
public class URLBuilder {
	
	private static final Logger log = Tracing.createLoggerFor(URLBuilder.class);
	
	private static final Pattern p1 = Pattern.compile("\\+");
	private static final Pattern p2 = Pattern.compile("%2F");

	private final String uriPrefix;
	private final String windowID;
	private final String timestampID;
	private final String csrfToken;
	private String componentID;
	private String componentTimestamp;

	/**
	 * @param uriPrefix
	 * @param windowID
	 * @param timestampID
	 * @param businessControlPath may be null
	 */
	public URLBuilder(String uriPrefix, String windowID, String timestampID, String csrfToken) {
		this.uriPrefix = uriPrefix; // e.g. /olat/auth
		this.windowID = windowID;
		this.timestampID = timestampID;
		this.csrfToken = csrfToken;
	}

	/**
	 * @return
	 */
	public URLBuilder createCopyFor(Component source) {
		URLBuilder n = new URLBuilder(uriPrefix, windowID, timestampID, csrfToken);
		// adjust the component id of the urlbuilder for the new component
		n.componentID = source.getDispatchID();
		// for ajax-mode needed: (but we set it anyway)
		// we also provide the component's timestamp in addition to the window's timestamp to enabled component-based timestamp checking.
		// the window-based timestamp checking is still needed for link which are not in ajax-mode and for the asynmediaresponsible
		n.componentTimestamp = source.getTimestamp();
		return n;
	}
	
	/**
	 * builds an uri. neither key nor values may contain the character
	 * UserRequest.PARAM_DELIM which is a ":" (colon). in case you think you
	 * should transfer a filename using a param, then may be it is better to use a
	 * model anyway to avoid having special characters in the url (even though it
	 * is utf-8 encoded, but some browsers/webservers do not seem to handle
	 * special chars in the url pathinfo part)
	 * 
	 * @param buf the buffer to write to
	 * @param keys the keys
	 * @param values the values.
	 * @param modURI
	 * @param mode indicates what kind of link it is (0 normal, 1 into background-iframe, ...)
	 */
	public void buildURI(StringOutput buf, String[] keys, String[] values, String modURI, int mode) {
		buildURI(buf, keys, values, modURI, mode, true);
	}
	
	public void buildURI(StringOutput buf, String[] keys, String[] values, String modURI, int mode, boolean csrf) {
		try(StringOutput result = new StringOutput(100)) {
			result.append(uriPrefix);
			encodeParams(result, mode);
			
			if (keys != null) {
				for (int i = 0; i < keys.length; i++) {
					result.append(UserRequest.PARAM_DELIM)
					      .append(keys[i])
					      .append(UserRequest.PARAM_DELIM)
					      .append(values[i]);
				}
			}
			
			if(csrf) {
				result.append(UserRequest.PARAM_DELIM)
				      .append(Form.FORM_CSRF)
				      .append(UserRequest.PARAM_DELIM)
				      .append(csrfToken);
			}
			
			result.append("/");
			if (modURI != null) {
				result.append(modURI);
			}
			buf.append(encodeUrl(result.toString()));
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	public String getJavascriptURI() {
		try(StringOutput result = new StringOutput(100)) {
			result.append(uriPrefix);
			encodeParams(result, AJAXFlags.MODE_TOBGIFRAME);
			result.append("/");
			return result.toString();
		} catch(IOException e) {
			log.error("", e);
			return null;
		}
	}
	

	/**
	 * @param buf
	 * @param keys
	 * @param values
	 */
	public void buildURI(StringOutput buf, String[] keys, String[] values) {
		buildURI(buf, keys, values, null, 0);
	}
	
	/**
	 * 
	 * @param buf
	 * @param keys
	 * @param values
	 * @param mode
	 */
	public void buildURI(StringOutput buf, String[] keys, String[] values, int mode) {
		buildURI(buf, keys, values, null, mode);
	}
	
	public void buildURI(StringOutput buf, int mode, NameValuePair... pairs) {
		final String[] keys;
		final String[] values;
		if(pairs == null || pairs.length == 0) {
			keys = values = new String[0];
		} else {
			keys = new String[pairs.length];
			values = new String[pairs.length];
			for(int i=pairs.length; i-->0; ) {
				keys[i] = pairs[i].getName();
				values[i] = pairs[i].getValue();
			}
		}

		buildURI(buf, keys, values, null, mode);
	}
	
	public StringOutput buildHrefAndOnclick(StringOutput sb, boolean ajaxEnabled, NameValuePair... commands) {
		return buildHrefAndOnclick(sb, null, null, ajaxEnabled, true, true, commands);
	}
	
	public StringOutput buildHrefAndOnclick(StringOutput sb, String urlEnding, boolean ajaxEnabled, boolean dirtyCheck, boolean pushState, NameValuePair... commands) {
		return buildHrefAndOnclick(sb, null, urlEnding, ajaxEnabled, dirtyCheck, pushState, commands);	
	}
	
	public StringOutput buildHrefAndOnclick(StringOutput sb, String bPathUrl, String urlEnding, boolean ajaxEnabled, boolean dirtyCheck, boolean pushState, NameValuePair... commands) {
		sb.append(" href=\"");
		if(ajaxEnabled) {
			if(StringHelper.containsNonWhitespace(bPathUrl)) {
				sb.append(bPathUrl);
			} else {
				sb.append("javascript:;");
			}
		} else {
			buildURI(sb, AJAXFlags.MODE_NORMAL, commands);
		}
		sb.append("\" onclick=\"");
		if(ajaxEnabled) {
			String escapedUrlEnding = StringHelper.escapeJavaScript(urlEnding);
			buildXHREvent(sb, escapedUrlEnding, dirtyCheck, pushState, commands).append(" return false;");
		} else if(dirtyCheck) {
			sb.append("return o2cl();");
		}
		sb.append("\" ");
		return sb;
	}
	
	public StringOutput buildXHREvent(StringOutput sb, String urlEnding, boolean dirtyCheck, boolean pushState, NameValuePair... commands) {
		return openXHREvent(sb, urlEnding, dirtyCheck, pushState, commands).append(");");
	}
	
	public StringOutput openXHREvent(StringOutput sb, String urlEnding, boolean dirtyCheck, boolean pushState, NameValuePair... commands) {
		sb.append("o_XHREvent('").append(uriPrefix);
		encodeParams(sb, AJAXFlags.MODE_TOBGIFRAME);
		sb.append("/");
		if(StringHelper.containsNonWhitespace(urlEnding)) {
			sb.append(urlEnding);
		}
		sb.append("',").append(dirtyCheck).append(",").append(pushState);
		commandParameters(sb, commands);
		commandParameters(sb, new NameValuePair(Form.FORM_CSRF, csrfToken));
		return sb;
	}
	
	public StringOutput openXHRScormEvent(StringOutput sb, NameValuePair... commands) {
		sb.append("o_XHRScormEvent('").append(uriPrefix);
		encodeParams(sb, AJAXFlags.MODE_TOBGIFRAME);
		sb.append("/'");
		commandParameters(sb, commands);
		commandParameters(sb, new NameValuePair(Form.FORM_CSRF, csrfToken));
		return sb;
	}

	public StringOutput getXHRNoResponseEvent(StringOutput sb, String urlEnding, NameValuePair... commands) {
		return openXHRNoResponseEvent(sb, urlEnding, commands).append(");");
	}
	
	public StringOutput openXHRNoResponseEvent(StringOutput sb, String urlEnding, NameValuePair... commands) {
		sb.append("o_XHRNFEvent('").append(uriPrefix);
		encodeParams(sb, AJAXFlags.MODE_TOBGIFRAME);
		sb.append("/");
		if(StringHelper.containsNonWhitespace(urlEnding)) {
			sb.append(urlEnding);
		}
		sb.append("'");
		commandParameters(sb, commands);
		commandParameters(sb, new NameValuePair(Form.FORM_CSRF, csrfToken));
		//no response marker
		commandParameters(sb, new NameValuePair(Window.NO_RESPONSE_PARAMETER_MARKER, Window.NO_RESPONSE_VALUE_MARKER));
		return sb;
	}
	
	private final void commandParameters(StringOutput sb, NameValuePair... commands) {
		if(commands != null && commands.length > 0 && commands[0] != null) {
			for(NameValuePair command:commands) {
				sb.append(",'")
			      .append(command.getName()).append("','").append(command.getValue()).append("'");
			}
		}
	}

	/**
	 * encodes the internal params (timestamp, window id, and component id)
	 * 
	 * @return
	 */
	private StringOutput encodeParams(StringOutput result, int mode) {
		// encode framework parameters
		result.append(windowID == null ? "0" : windowID);
		result.append(UserRequest.PARAM_DELIM);
		
		result.append(timestampID == null ? "0" : timestampID);
		result.append(UserRequest.PARAM_DELIM);
		
		result.append(componentID == null ? "0" : componentID);
		result.append(UserRequest.PARAM_DELIM);

		result.append(componentTimestamp == null ? "0" : componentTimestamp);
		result.append(UserRequest.PARAM_DELIM);
		
		result.append(mode);
		return result;
	}
	
	/**
	 * @param url
	 * @return encoded string
	 */
	public static String encodeUrl(String url) {
		String encodedURL;
		try {
			encodedURL = URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			/*
			 * from java.nio.Charset Standard charsets Every implementation of the
			 * Java platform is required to support the following standard charsets...
			 * ... UTF-8 Eight-bit UCS Transformation Format ...
			 */
			throw new AssertException("utf-8 encoding is needed for proper encoding, but not offered on this java platform????");
		}
		encodedURL = p1.matcher(encodedURL).replaceAll("%20");
		encodedURL = p2.matcher(encodedURL).replaceAll("/");
		return encodedURL;
	}

	String getUriPrefix() {
		return uriPrefix;
	}
}