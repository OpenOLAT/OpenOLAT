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
package org.olat.modules.bigbluebutton.manager;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonUriBuilder {

	private static final Logger log = LogManager.getLogger(BigBlueButtonUriBuilder.class);
	
	private String host;
	private String scheme;
	private int port;
	private String startPath;
	private String xmlPayload;

	private final String sharedSecret;
	
	private String operation;
	private final List<Parameter> parameters = new ArrayList<>();

    private BigBlueButtonUriBuilder(URI uri, String sharedSecret) {
        this.sharedSecret = sharedSecret;
    	if(uri.getScheme() != null) {
    		scheme = uri.getScheme();
    	} else {
    		scheme = "https";
    	}
    	port = uri.getPort();
    	host = uri.getHost();
    	startPath = uri.getPath();
    }

    public static BigBlueButtonUriBuilder fromUri(URI uri, String sharedSecret) {
    	return new BigBlueButtonUriBuilder(uri, sharedSecret);
    }
    
    public BigBlueButtonUriBuilder xmlPayload(String xml) {
    	xmlPayload = xml;
    	return this;
    }
    
    public BigBlueButtonUriBuilder operation(String operation) {
    	this.operation = operation;
    	return this;
    }

    public BigBlueButtonUriBuilder parameter(String parameter, String value) {
    	parameters.add(new Parameter(parameter, value));
        return this;
    }
    
    public BigBlueButtonUriBuilder optionalParameter(String parameter, String value) {
    	if(StringHelper.containsNonWhitespace(value)) {
    		parameters.add(new Parameter(parameter, value));
    	}
        return this;
    }
    
    public BigBlueButtonUriBuilder optionalParameter(String parameter, Integer value) {
    	if(value != null) {
    		parameters.add(new Parameter(parameter, value.toString()));
    	}
        return this;
    }
    
    public BigBlueButtonUriBuilder optionalParameter(String parameter, Boolean value) {
    	if(value != null) {
    		parameters.add(new Parameter(parameter, value.toString()));
    	}
        return this;
    }
    
    public URI build() {
    	StringBuilder path = new StringBuilder();
    	path.append(scheme).append("://")
    	    .append(host);
 
    	if(startPath != null) {
    		appendPath(startPath, path);
    	}
    	appendPath("api", path);
    	if(StringHelper.containsNonWhitespace(operation)) {
    		appendPath(operation, path);
    	}

    	StringBuilder query = new StringBuilder();
    	if(!parameters.isEmpty()) {
    		for(Parameter param:parameters) {
    			if(query.length() > 0) {
    				query.append("&");
    			}
        		query
        			.append(param.getParameter())
        			.append("=")
        			.append(urlEncode(param.getValue()));
    		}	
    	}
    	
    	String queryString = query.toString();
    	queryString += "&checksum=" + checksum(operation, queryString, sharedSecret);
    	path.append("?").append(queryString);
    	
    	try {
    		return URI.create(path.toString());
		} catch (Exception e) {
			log.error("Cannot build URI: {} {} {}", scheme, host, port, e);
			return null;
		}
    }
    
    public String getXmlPayload() {
    	return xmlPayload;
    }
    
    public static String urlEncode(String s) {
    	try {
    		return URLEncoder.encode(s, "UTF-8");
    	} catch (Exception e) {
    		log.error("", e);
    	}
    	return "";
    }
    
    private String checksum(String operation, String query, String salt) {
    	String text = operation + query + salt;
    	return checksum(text);
    }
    
    private static String checksum(String s) {
    	String checksum = "";
    	try {
    		checksum = org.apache.commons.codec.digest.DigestUtils.sha1Hex(s);
    	} catch (Exception e) {
    		log.error("Cannot calculate checksum", e);
    	}
    	return checksum;
    }
    
    private static final StringBuilder appendPath(String path, StringBuilder sb) {
    	if(path != null && path.length() > 0) {
    		if((sb.length() == 0 || sb.charAt(sb.length() -1) != '/') && !path.startsWith("/")) {
    			sb.append("/");
    		}
    		sb.append(path);
    	}
    	return sb;
    }
    
    private static class Parameter {
    	
    	private final String parameter;
    	private final String value;
    	
    	public Parameter(String parameter, String value) {
    		this.parameter = parameter;
    		this.value = value;
    	}

		public String getParameter() {
			return parameter;
		}

		public String getValue() {
			return value;
		}
    }
}
