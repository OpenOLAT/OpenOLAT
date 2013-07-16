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

package org.olat.util.xss.client;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.olat.util.FunctionalEPortfolioUtil;

/**
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class HttpUtil {
	
	public final static String DEFAULT_HIJACKED_USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.1.5) Gecko/20091102 Firefox/3.5.5 (.NET CLR 3.5.30729)";
	
	private String hijackedUserAgent;
	
	public HttpUtil() {
		this.hijackedUserAgent = DEFAULT_HIJACKED_USER_AGENT;
	}
	
	enum HttpMethod {
		HTTP_PUT,
		HTTP_DELETE,
		HTTP_GET,
		HTTP_POST,
	};
	
	public byte[] createHttpGetHeader(String path, String host,
			String jsessionId, String headerEncoding){
		
		VelocityContext context = new VelocityContext();

		context.put("path", path);
		context.put("host", host);
		context.put("userAgent", hijackedUserAgent);
		context.put("jsessionId", jsessionId);
		
		VelocityEngine engine = null;

		engine = new VelocityEngine();

		StringWriter sw = new StringWriter();

		try {
			engine.evaluate(context, sw, "xssClient_HTTP_GET-Header", HttpUtil.class.getResourceAsStream("xssClient_HTTP_GET-Header.vm"));

		} catch (ParseErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MethodInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] header = CharsetUtil.encode(sw.toString(), headerEncoding);
		
		return(header);
	}

	public byte[] createHttpPostHeader(String path, String host,
			String jsessionId, String headerEncoding, String bodyEncoding, HashMap<String,String> parameters){
		
		StringBuffer stringBuffer = new StringBuffer();
		
		Set<Entry<String,String>> keys = parameters.entrySet();
		Iterator<Entry<String,String>> iter = keys.iterator();
		
		while(iter.hasNext()){
			Entry<String,String> entry = iter.next();
			
			stringBuffer.append(entry.getKey())
			.append("=")
			.append(CharsetUtil.encode(entry.getValue(), bodyEncoding))
			.append('\n');
		}
		
		VelocityContext context = new VelocityContext();

		context.put("path", path);
		context.put("host", host);
		context.put("userAgent", hijackedUserAgent);
		context.put("contentLength", Integer.toString(stringBuffer.length()));
		context.put("jsessionId", jsessionId);
		context.put("parameters", stringBuffer.toString());
		
		VelocityEngine engine = null;

		engine = new VelocityEngine();

		StringWriter sw = new StringWriter();

		try {
			engine.evaluate(context, sw, "xssClient_HTTP_POST-Header", HttpUtil.class.getResourceAsStream("xssClient_HTTP_POST-Header.vm"));

		} catch (ParseErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MethodInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] header = CharsetUtil.encode(sw.toString(), headerEncoding);
		
		return(header);
	}
	
	public String getHijackedUserAgent() {
		return hijackedUserAgent;
	}

	public void setHijackedUserAgent(String hijackedUserAgent) {
		this.hijackedUserAgent = hijackedUserAgent;
	}
}
