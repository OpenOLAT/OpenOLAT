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
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.net.InetSocketAddress;

import org.apache.xmlrpc.webserver.XmlRpcServlet;

import org.olat.util.xss.client.HttpUtil;
import org.olat.util.xss.client.HttpUtil.HttpMethod;

/**
 * WARNING: this software may not be used on public networks especially over an internet
 * connection nor within your ISPs WAN. It may potentially damage your infrastructure.
 * XssClient should be used carefully and only for error detection. It uses its very own
 * implementation of the HTTP protocol and may break international telecommunication contracts.
 * 
 * @author jkraehemann, joel.kraehemann@frentix.com, frentix.com
 */
public class XssClient extends XmlRpcServlet {
	
	final static String DEFAULT_ENCODING = "UTF-8";
	
	final static String DEFAULT_REMOTE_ENCODING = "Unicode";
	final static String DEFAULT_CLIENT_ENCODING = "iso-8859-1";
	final static String DEFAULT_BODY_ENCODING = "UTF-16";
	final static String DEFAULT_SCRIPT_ENCODING = "UTF-7";
	
	final static int DEFAULT_FIELD_LENGTH_LIMITATION = 255;
	
	final static int DEFAULT_THREAD_COUNT = 100;
	final static int DEFAULT_FAKE_USER_COUNT = 100;
	final static int DEFAULT_CONCURRENT_USER_COUNT = 100;
	final static int DEFAULT_DISTRIBUTED_CHUNK_SIZE = 65535;
	
	final static String DEFAULT_ESCAPING_PATTERN = "\\\\\\//";
	final static String DEFAULT_CLOSING_TAGS_PATTERN = "</body></html>${\"xssCommonInjectionCode\"}";
	final static String DEFAULT_CLOSING_JSON_PATTERN = "',xssAlert: ${\"xssJSonInjectionCode\"};{";
	final static String DEFAULT_COMMENT_OUT_PATTERN = "${\"xssInlineInjectionCode\"}<!--";
	final static String DEFAULT_SCRIPTIFY_PATTERN = "${\"xssInlineInjectionCode\"}<javascript>";
	final static String DEFAULT_FRAMEIFY_PATTERN = "${\"xssInlineInjectionCode\"}<frame src=\"javascript:void(){window.document.body}\" />";
	final static String DEFAULT_IFRAMEIFY_PATTERN = "${\"xssInlineInjectionCode\"}<iframe src=\"javascript:void(){window.document.body}\" />";
	final static String DEFAULT_TOPLEVEL_FRAME = "<iframe style=\"z-index: -1;\" src=\"javascript:void(){${\"xssSnippedInjectionCode\"}}\">";
	final static String DEFAULT_B_MAIN_ONLY_PATTERN = "<div id=\"b_main\" class=\"javascript:void(){${\"xssSnippedInjectionCode\"}}\"/>";
	
	enum XssStrategy{
		TRICK_ESCAPING,
		CLOSE_TAGS,
		COMMENT_OUT,
		SCRIPTIFY,
		FRAMEIFY,
		IFRAMIFY,
		TOPLEVEL_FRAME,
		B_MAIN_ONLY,
		CLOSE_JSON,
		MASQUERADE_ENCODING,
		FAKE_USERS,
		CONCURRENT_USERS,
		RANDOM_ENCODING,
		PACKAGE_FRAGMENTS,
		GENERATE_DATABASE_TIMEOUTS,
		LOW_LATENCY_RESEND,
	}
	
	private String defaultEncoding;
	
	private String remoteEncoding;
	private String clientEncoding;
	private String bodyEncoding;
	private String scriptEncoding;
	
	private byte[] header;
	private byte[] jsessionId;
	
	private Socket connection;
	private OutputStream out;
	
	private int fieldLengthLimitation;
	
	private int threadCount;
	private int fakeUserCount;
	private int concurrentUserCount;
	private int distributedChunkSize;
	
	private String escapingPattern;
	private String closingTagsPattern;
	
	private List<Script> scripts;
	
	private HttpUtil httpUtil;
	
	public XssClient(){
		this.defaultEncoding = DEFAULT_ENCODING;
		
		this.remoteEncoding = DEFAULT_REMOTE_ENCODING;
		this.clientEncoding = DEFAULT_CLIENT_ENCODING;
		this.bodyEncoding = DEFAULT_BODY_ENCODING;
		this.scriptEncoding = DEFAULT_SCRIPT_ENCODING;
		
		this.connection = new Socket();
		this.out = null;
		
		this.fieldLengthLimitation = DEFAULT_FIELD_LENGTH_LIMITATION;
		
		this.threadCount = DEFAULT_THREAD_COUNT;
		this.fakeUserCount = DEFAULT_FAKE_USER_COUNT;
		this.concurrentUserCount = DEFAULT_CONCURRENT_USER_COUNT;
		this.distributedChunkSize = DEFAULT_DISTRIBUTED_CHUNK_SIZE;
		
		this.escapingPattern = DEFAULT_ESCAPING_PATTERN;
		this.closingTagsPattern = DEFAULT_CLOSING_TAGS_PATTERN;
		
		this.scripts = new ArrayList<Script>();
		
		this.httpUtil = new HttpUtil();
		
		reloadScripts();
	}

	public void reloadScripts(){
		Script script = new CommonScript();
		script.load();
		scripts.add(script);
		
		script = new InlineScript();
		script.load();
		scripts.add(script);
		
		script = new IFrameScript();
		script.load();
		scripts.add(script);
	}
	
	public void connect(String host, int port) throws IOException {
		connection.connect(new InetSocketAddress(host, port));
		out = connection.getOutputStream();
	}

	public void setHttpHeader(byte[] buffer) {
		this.header = buffer;
	}
	
	private HashMap<String,String> trickEscaping(HashMap<String,String> parameter, int space){
		
		if(parameter == null){
			return(null);
		}
		
		HashMap<String,String> injectionCode = new HashMap<String,String>();
		Iterator<String> iter = parameter.keySet().iterator();
		int iNext = escapingPattern.length();
		
		while(iter.hasNext()){
			String key = iter.next();
			StringBuffer stringBuffer = new StringBuffer();
			
			for(int i = 0;
					iNext < fieldLengthLimitation &&
					iNext < distributedChunkSize &&
					iNext < space;
					i = iNext){
				stringBuffer.append(escapingPattern);
				
				iNext = i + escapingPattern.length();
			}
			
			injectionCode.put(key, stringBuffer.toString());
		}
		
		return(injectionCode);
	}

	private HashMap<String,String> closeTags(HashMap<String,String> parameter, int space){

		if(parameter == null){
			return(null);
		}
		
		HashMap<String,String> injectionCode = new HashMap<String,String>();
		Iterator<String> iter = parameter.keySet().iterator();
		int iNext = escapingPattern.length();
		
		while(iter.hasNext()){
			String key = iter.next();
			StringBuffer stringBuffer = new StringBuffer();
			
			for(int i = 0;
					iNext < fieldLengthLimitation &&
					iNext < distributedChunkSize &&
					iNext < space;
					i = iNext){
				stringBuffer.append(closingTagsPattern);
				
				iNext = i + closingTagsPattern.length();
			}
			
			injectionCode.put(key, stringBuffer.toString());
		}
		
		return(injectionCode);
	}
	
	private HashMap<String,String> commentOut(HashMap<String,String> parameter){
		HashMap<String,String> injectionCode = new HashMap<String,String>();
		
		//TODO:JK: implement me
		
		return(injectionCode);
		
	}
	
	private HashMap<String,String> scriptify(HashMap<String,String> parameter){
		HashMap<String,String> injectionCode = new HashMap<String,String>();
		
		//TODO:JK: implement me
		
		return(injectionCode);
		
	}
	
	private HashMap<String,String> frameify(HashMap<String,String> parameter){
		HashMap<String,String> injectionCode = new HashMap<String,String>();
		
		//TODO:JK: implement me
		
		return(injectionCode);
		
	}
	
	private HashMap<String,String> b_main_only(HashMap<String,String> parameter){
		HashMap<String,String> injectionCode = new HashMap<String,String>();
		
		//TODO:JK: implement me
		
		return(injectionCode);
		
	}
	
	private HashMap<String,String> applyStrategy(XssStrategy[] strategy, HashMap<String,String> parameter){
		HashMap<String,String> injectionCode = new HashMap<String,String>();
		
		//TODO:JK: implement me
		
		return(injectionCode);
	}
	
	public void attack(String path,
			HttpMethod method, HashMap<String,String> parameter, String jsessionId,
			XssStrategy[] strategy, String snipped, boolean distributed) throws IOException{
		this.attack("localhost", 8080, path,
				method, parameter, jsessionId,
				strategy, snipped, distributed);
	}
	
	private void attack(String host, int port, String path,
			HttpMethod method, HashMap<String,String> parameter, String jsessionId,
			XssStrategy[] strategy, String snipped, boolean distributed) throws IOException {
		connect(host, port);
		
		//TODO:JK: implement other methods
		byte[] header = null;
		
		switch(method){
		case HTTP_GET:
		{
			header = httpUtil.createHttpGetHeader(path, host + ":" + port,
					jsessionId, getClientEncoding());
		}
		break;
		case HTTP_POST:
		{
			HashMap<String,String> injectionCode = applyStrategy(strategy, parameter);
			
			header = httpUtil.createHttpPostHeader(path, host + ":" + port,
					jsessionId, getClientEncoding(), getBodyEncoding(), injectionCode);
		}
		break;
		default:
			break;
		}
		
		out.write(header);
	}
	
	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	public String getRemoteEncoding() {
		return remoteEncoding;
	}

	public void setRemoteEncoding(String remoteEncoding) {
		this.remoteEncoding = remoteEncoding;
	}

	public String getClientEncoding() {
		return clientEncoding;
	}

	public void setClientEncoding(String clientEncoding) {
		this.clientEncoding = clientEncoding;
	}

	public String getBodyEncoding() {
		return bodyEncoding;
	}

	public void setBodyEncoding(String bodyEncoding) {
		this.bodyEncoding = bodyEncoding;
	}

	public String getScriptEncoding() {
		return scriptEncoding;
	}

	public void setScriptEncoding(String scriptEncoding) {
		this.scriptEncoding = scriptEncoding;
	}

	public byte[] getHeader() {
		return header;
	}

	public void setHeader(byte[] header) {
		this.header = header;
	}

	public byte[] getJSessionId() {
		return jsessionId;
	}

	public void setJSessionId(byte[] jsessionId) {
		this.jsessionId = jsessionId;
	}

	public Socket getConnection() {
		return connection;
	}

	public void setConnection(Socket connection) {
		this.connection = connection;
	}

	public OutputStream getOut() {
		return out;
	}

	public void setOut(OutputStream out) {
		this.out = out;
	}

	public int getFieldLengthLimitation() {
		return fieldLengthLimitation;
	}

	public void setFieldLengthLimitation(int fieldLengthLimitation) {
		this.fieldLengthLimitation = fieldLengthLimitation;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public int getFakeUserCount() {
		return fakeUserCount;
	}

	public void setFakeUserCount(int fakeUserCount) {
		this.fakeUserCount = fakeUserCount;
	}

	public int getConcurrentUserCount() {
		return concurrentUserCount;
	}

	public void setConcurrentUserCount(int concurrentUserCount) {
		this.concurrentUserCount = concurrentUserCount;
	}

	public int getDistributedChunkSize() {
		return distributedChunkSize;
	}

	public void setDistributedChunkSize(int distributedChunkSize) {
		this.distributedChunkSize = distributedChunkSize;
	}

	public String getEscapingPattern() {
		return escapingPattern;
	}

	public void setEscapingPattern(String escapingPattern) {
		this.escapingPattern = escapingPattern;
	}

	public String getClosingTagsPattern() {
		return closingTagsPattern;
	}

	public void setClosingTagsPattern(String closingTagsPattern) {
		this.closingTagsPattern = closingTagsPattern;
	}

	public List<Script> getScripts() {
		return scripts;
	}

	public void setScripts(List<Script> scripts) {
		this.scripts = scripts;
	}
	
	public abstract class Script{
		private List<String> variants;
		
		public Script(){
			variants = new ArrayList<String>();
		}
		
		public abstract void load();

		public List<String> getVariants() {
			return variants;
		}

		public void setVariants(List<String> variants) {
			this.variants = variants;
		}
	}
	
	public class CommonScript extends Script {

		@Override
		public void load() {
			// TODO Auto-generated method stub
			
		}
	}
	
	public class InlineScript extends Script {

		@Override
		public void load() {
			// TODO Auto-generated method stub
			
		}
	}
	
	public class IFrameScript extends Script {

		@Override
		public void load() {
			// TODO Auto-generated method stub
			
		}
	}
}
