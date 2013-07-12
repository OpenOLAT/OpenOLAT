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

import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
public class XssClient extends XmlRpcServlet implements HttpClient {
	
	final static String DEFAULT_ENCODING = "UTF-8";
	
	final static String DEFAULT_REMOTE_ENCODING = "Unicode";
	final static String DEFAULT_CLIENT_ENCODING = "iso-8859-1";
	final static String DEFAULT_BODY_ENCODING = "UTF-16";
	final static String DEFAULT_SCRIPT_ENCODING = "UTF-7";
	
	final static int DEFAULT_THREAD_COUNT = 100;
	final static int DEFAULT_FAKE_USER_COUNT = 100;
	final static int DEFAULT_CONCURRENT_USER_COUNT = 100;
	
	enum XssStrategy{
		TRICK_ESCAPING,
		CLOSE_TAGS,
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
	
	private int threadCount;
	private int fakeUserCount;
	private int concurrentUserCount;
	
	private List<Script> scripts;
	
	public XssClient(){
		this.defaultEncoding = DEFAULT_ENCODING;
		
		this.remoteEncoding = DEFAULT_REMOTE_ENCODING;
		this.clientEncoding = DEFAULT_CLIENT_ENCODING;
		this.bodyEncoding = DEFAULT_BODY_ENCODING;
		this.scriptEncoding = DEFAULT_SCRIPT_ENCODING;
		
		this.connection = new Socket();
		this.out = null;
		
		this.threadCount = DEFAULT_THREAD_COUNT;
		this.fakeUserCount = DEFAULT_FAKE_USER_COUNT;
		this.concurrentUserCount = DEFAULT_CONCURRENT_USER_COUNT;
		
		this.scripts = new ArrayList<Script>();
		
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
	
	@Override
	public void connect(String host, int port) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHttpHeader(byte[] buffer) {
		this.header = buffer;
	}

	@Override
	public void httpGet(byte[] data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void httpPut(byte[] data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void httpDelete(byte[] data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void httpPost(byte[] data) {
		// TODO Auto-generated method stub
		
	}

	public void attack(String path, HttpMethod method, HashSet<String> parameter, XssStrategy strategy, String snipped){
		this.attack("localhost", 8080, path, method, parameter, strategy, snipped);
	}
	
	private void attack(String host, int port, String path, HttpMethod method, HashSet<String> parameter, XssStrategy strategy, String snipped){
		connect(host, port);
		
		byte[] header = HttpUtil.createHttpHeader(method, parameter, getClientEncoding(), getBodyEncoding());
		
		
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
