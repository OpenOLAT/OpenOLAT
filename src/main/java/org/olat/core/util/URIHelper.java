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

package org.olat.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * With the URIHelper it is very simple to modify URL query parameters.
 * <P>
 * Initial Date: 11.07.2006 <br>
 * 
 * @author Carsten Weisse
 */
public class URIHelper {

	private String encoding;

	private URI uri;
	private String string;
	private String query;
	private Map<String, String> params;
	private boolean modified;

	public URIHelper(String str) throws URISyntaxException {
		this(str, "UTF-8");
	}

	private URIHelper(String str, String enc) throws URISyntaxException {
		this.uri = new URI(str);
		this.encoding = enc;
		this.modified = true;
		parseQuery();
	}

	/**
	 * Remove a single parameter, if exists.
	 */
	public URIHelper removeParameter(String name) {
		if (params != null && !params.isEmpty()) {
			// don't reset the modification state, because of initial construction
			modified |= (params.remove(name) != null);
		}
		return this;
	}

	/**
	 * Return the value of a single parameter.
	 * @return value; may be <code>null</code> if the parameter doesn't exist.
	 */
	public String getParameter(String name) {
		if (params == null || params.isEmpty()) {
			return null;
		} else {
			return params.get(name);
		}
	}

	public String toString() {
		if (modified) {
			updateQuery();
			updateString();
			modified = false;
		}
		return string;
	}

	private void updateString() {
		StringBuilder sb = new StringBuilder();
		if (uri.getScheme() != null) {
			sb.append(uri.getScheme());
			sb.append(':');
		}
		if (uri.isOpaque()) {
			sb.append(uri.getRawSchemeSpecificPart());
		} else {
			String host = uri.getHost();
			if (host != null) {
				sb.append("//");
				if (uri.getRawUserInfo() != null) {
					sb.append(uri.getRawUserInfo());
					sb.append('@');
				}
				boolean needBrackets = ((host.indexOf(':') >= 0) && !host.startsWith("[") && !host.endsWith("]"));
				if (needBrackets) sb.append('[');
				sb.append(host);
				if (needBrackets) sb.append(']');
				if (uri.getPort() != -1) {
					sb.append(':');
					sb.append(uri.getPort());
				}
			} else if (uri.getRawAuthority() != null) {
				sb.append("//");
				sb.append(uri.getRawAuthority());
			}
		}
		if (uri.getRawPath() != null) sb.append(uri.getRawPath());
		if (query != null) {
			sb.append('?');
			sb.append(query);
		}
		if (uri.getRawFragment() != null) {
			sb.append('#');
			sb.append(uri.getRawFragment());
		}
		string = sb.toString();
	}

	private void parseQuery() {
		query = uri.getRawQuery();
		if (query == null) return;
		// build the map
		modified = true;
		params = new HashMap<String,String>();

		// Split off the given URL from its query string
		StringTokenizer pairParser = new StringTokenizer(query, "&");

		while (pairParser.hasMoreTokens()) {
			try {
				String pair = pairParser.nextToken();
				StringTokenizer valueParser = new StringTokenizer(pair, "=");

				String name = valueParser.nextToken();
				String value = valueParser.nextToken();

				params.put(decode(name), decode(value));
			} catch (Throwable t) {
				// If we cannot parse a parameter, ignore it
			}
		}
	}

	private void updateQuery() {
		// delete query if there are no parameters 
		if (params == null || params.isEmpty()) {
			query = null;
			return;
		}

		// build the query string from parameter map
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> it = params.keySet().iterator(); it.hasNext();) {
			String name = it.next();
			String value = params.get(name);
			if (value.length() == 0) continue;
			sb.append(encode(name)).append('=').append(encode(value));
			sb.append('&');
		}
		// remove the last '&'
		sb.deleteCharAt(sb.length() - 1);
		query = sb.toString();
	}

	private String encode(String orig) {
		try {
			return URLEncoder.encode(orig, encoding);
		} catch (UnsupportedEncodingException e) {
			// can't be but return the orig
			return orig;
		}
	}

	private String decode(String orig) {
		try {
			return URLDecoder.decode(orig, encoding);
		} catch (UnsupportedEncodingException e) {
			// can't be but return the orig
			return orig;
		}
	}
}
