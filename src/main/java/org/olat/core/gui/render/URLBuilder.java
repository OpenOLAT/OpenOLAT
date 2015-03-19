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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.control.winmgr.WindowBackOfficeImpl;
import org.olat.core.logging.AssertException;

/**
 * 
 * @author Felix Jost
 */
public class URLBuilder {
	
	private static final Pattern p1 = Pattern.compile("\\+");
	private static final Pattern p2 = Pattern.compile("%2F");

	private final String uriPrefix;

	private final String windowID;
	private final String timestampID;
	private String componentID;
	private String componentTimestamp;
	
	private final String iframeTargetName;

	private final WindowBackOfficeImpl wboImpl;

	/**
	 * @param uriPrefix
	 * @param windowID
	 * @param timestampID
	 * @param businessControlPath may be null
	 */
	public URLBuilder(String uriPrefix, String windowID, String timestampID, WindowBackOfficeImpl wboImpl) {
		this.uriPrefix = uriPrefix; // e.g. /olat/auth
		this.windowID = windowID;
		this.timestampID = timestampID;
		this.wboImpl = wboImpl;
		// brasato:: add helper method  a la window.createredirecturi so we need not check on null below - see call hierarchy of this constructor
		this.iframeTargetName = wboImpl == null? null: wboImpl.getIframeTargetName();
	}

	/**
	 * @return
	 */
	public URLBuilder createCopyFor(Component source) {
		URLBuilder n = new URLBuilder(uriPrefix, windowID, timestampID, wboImpl);
		// adjust the component id of the urlbuilder for the new component
		n.componentID = source.getDispatchID();
		// for ajax-mode needed: (but we set it anyway)
		// we also provide the component's timestamp in addition to the window's timestamp to enabled component-based timestamp checking.
		// the window-based timestamp checking is still needed for link which are not in ajax-mode and for the asynmediaresponsible
		n.componentTimestamp = source.getTimestamp();
		return n;
	}
	
	/**
	 * appends the "target" attribute that is needed for ajax-links. the caller has to make sure that this.isAjaxOn() is true.
	 * @param sb
	 */
	public void appendTarget(StringOutput sb) {
		sb.append(" target=\"").append(iframeTargetName).append("\"");
	}

	/**
	 * builds a java script command that executes in background iframe or as document.location.replace depending on the AJAX mode
	 * @param buf the buffer to write to
	 * @param keys the keys
	 * @param values the values.
	 * @param mode
	 */
	public void buildJavaScriptBgCommand(StringOutput buf, String[] keys, String[] values, int mode) {
		if (mode == AJAXFlags.MODE_TOBGIFRAME) {
			buf.append("frames['");
			buf.append(iframeTargetName);
			buf.append("'].");
		}
		buf.append("location.href = \"");			
		// add URI
		buildURI(buf, keys, values, mode);
		buf.append("\"");
		// DON'T append anything after this. The JS code might append some URL
		// parameter to this command!
	}
	
	/**
	 * builds a java script command that executes in background iframe or as document.location.replace depending on the AJAX mode.
	 * This version also includes safety checks for not breaking other running ajax requests
	 * TODO: may only offer one method but check for usage of o_beforeserver first!
	 * @param buf the buffer to write to
	 * @param keys the keys
	 * @param values the values.
	 * @param mode
	 * @param singe or double quote as string termination
	 */
	public void buildJavaScriptBgCommand(StringOutput buf, String[] keys, String[] values, int mode, boolean useSingleQuotes) {
		String quote = "\"";
		if (useSingleQuotes) quote = "'";
		if (mode == AJAXFlags.MODE_TOBGIFRAME) {
			buf.append("if(!o_info.linkbusy){o_beforeserver();frames['");
			buf.append(iframeTargetName);
			buf.append("'].");
		}
		
		buf.append("location.href=").append(quote);			
		// add URI
		buildURI(buf, keys, values, mode);
		if (mode == AJAXFlags.MODE_TOBGIFRAME) buf.append(quote).append(";}");
		else buf.append(quote);
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
		StringBuilder result = new StringBuilder(100);
		result.append(uriPrefix);
		encodeParams(result, mode);
		
		if (keys != null) {
			for (int i = 0; i < keys.length; i++) {
				result.append(UserRequest.PARAM_DELIM);
				result.append(keys[i]);
				result.append(UserRequest.PARAM_DELIM);
				result.append(values[i]);
			}
		}
		
		result.append('/');
		if (modURI != null) result.append(modURI);
		//FIXME:fj:a urlEncodeUTF8 is slow; improve the regexp, also convert only the modURI to utf-8?
		buf.append(encodeUrl(result.toString()));
	}
	
	public void buildURI(StringOutput buf, String[] keys, String[] values, String modURI) {
		buildURI(buf, keys, values, modURI, 0);
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

	/**
	 * encodes the internal params (timestamp, window id, and component id)
	 * 
	 * @return
	 */
	private StringBuilder encodeParams(StringBuilder result, int mode) {
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
	public String encodeUrl(String url) {
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