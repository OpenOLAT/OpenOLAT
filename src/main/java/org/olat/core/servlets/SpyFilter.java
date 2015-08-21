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
package org.olat.core.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * Don't use it in production: to make pretty output, it's has a big
 * synchronized on every request. And it can print a lot of things
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SpyFilter implements Filter {
	
	private static final String LOCK =  "my-pretty-little-lock";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//
	}

	@Override
	public void destroy() {
		//
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	throws IOException, ServletException {
		//ensure consistent output 
		synchronized(LOCK) {
			HttpServletRequest req = (HttpServletRequest)request;
			System.out.println("*************************************************************************");
			System.out.println("Start: " + req.getMethod() + " at " + req.getRequestURI());
			
			for(Enumeration<String> headers=req.getHeaderNames(); headers.hasMoreElements(); ) {
				String header = headers.nextElement();
				System.out.println("Request header: " + header + " :: " + req.getHeader(header));
			}

			chain.doFilter(request, new ResponseDelegate((HttpServletResponse)response));

			System.out.println("****************************************************************************");
		}
	}
	
	public static class ResponseDelegate implements HttpServletResponse {
		private HttpServletResponse response;
		
		public ResponseDelegate(HttpServletResponse response) {
			this.response = response;
		}

		@Override
		public int getStatus() {
			return response.getStatus();
		}

		@Override
		public String getHeader(String name) {
			return response.getHeader(name);
		}

		@Override
		public Collection<String> getHeaders(String name) {
			return response.getHeaders(name);
		}

		@Override
		public Collection<String> getHeaderNames() {
			return response.getHeaderNames();
		}

		public void addCookie(Cookie cookie) {
			response.addCookie(cookie);
		}

		public boolean containsHeader(String name) {
			return response.containsHeader(name);
		}

		public String encodeURL(String url) {
			return response.encodeURL(url);
		}

		public String getCharacterEncoding() {
			return response.getCharacterEncoding();
		}

		public String encodeRedirectURL(String url) {
			return response.encodeRedirectURL(url);
		}

		public String getContentType() {
			return response.getContentType();
		}

		@SuppressWarnings("deprecation")
		public String encodeUrl(String url) {
			return response.encodeUrl(url);
		}

		@SuppressWarnings("deprecation")
		public String encodeRedirectUrl(String url) {
			return response.encodeRedirectUrl(url);
		}

		public ServletOutputStream getOutputStream() throws IOException {
			System.out.println("getOutputStream");
			return response.getOutputStream();
		}

		public void sendError(int sc, String msg) throws IOException {
			response.sendError(sc, msg);
		}

		public PrintWriter getWriter() throws IOException {
			System.out.println("getWriter");
			return response.getWriter();
		}

		public void sendError(int sc) throws IOException {
			response.sendError(sc);
		}

		public void sendRedirect(String location) throws IOException {
			response.sendRedirect(location);
		}

		public void setCharacterEncoding(String charset) {
			response.setCharacterEncoding(charset);
		}

		public void setDateHeader(String name, long date) {
			System.out.println("setDateHeader: " + name);
			response.setDateHeader(name, date);
		}

		public void addDateHeader(String name, long date) {
			System.out.println("addDateHeader: " + name);
			response.addDateHeader(name, date);
		}

		public void setHeader(String name, String value) {
			System.out.println("setHeader: " + name + " :: " + value);
			response.setHeader(name, value);
		}

		public void setContentLength(int len) {
			System.out.println("setContentLength: " + len);
			response.setContentLength(len);
		}

		@Override
		public void setContentLengthLong(long len) {
			System.out.println("setContentLengthLong: " + len);
			response.setContentLengthLong(len);
		}

		public void setContentType(String type) {
			System.out.println("setContentType: " + type);
			response.setContentType(type);
		}

		public void addHeader(String name, String value) {
			System.out.println("addHeader: " + name + " :: " + value);
			response.addHeader(name, value);
		}

		public void setIntHeader(String name, int value) {
			System.out.println("setIntHeader: " + name + " :: " + value);
			response.setIntHeader(name, value);
		}

		public void addIntHeader(String name, int value) {
			System.out.println("addIntHeader: " + name + " :: " + value);
			response.addIntHeader(name, value);
		}

		public void setBufferSize(int size) {
			System.out.println("setBufferSize: " + size);
			response.setBufferSize(size);
		}

		public void setStatus(int sc) {
			System.out.println("setStatus: " + sc);
			response.setStatus(sc);
		}

		@SuppressWarnings("deprecation")
		public void setStatus(int sc, String sm) {
			System.out.println("setStatus: " + sc);
			response.setStatus(sc, sm);
		}

		public int getBufferSize() {
			return response.getBufferSize();
		}

		public void flushBuffer() throws IOException {
			response.flushBuffer();
		}

		public void resetBuffer() {
			response.resetBuffer();
		}

		public boolean isCommitted() {
			return response.isCommitted();
		}

		public void reset() {
			response.reset();
		}

		public void setLocale(Locale loc) {
			response.setLocale(loc);
		}

		public Locale getLocale() {
			return response.getLocale();
		}
	}
	
	public static class DelegatePrintWriter extends PrintWriter {
		private PrintWriter delegate;
		
		public DelegatePrintWriter(PrintWriter writer) {
			super(writer);
			delegate = writer;
		}

		public void flush() {
			System.out.println("Flush");
			delegate.flush();
		}

		public void close() {
			System.out.println("Close");
			delegate.close();
		}

		public boolean checkError() {
			return delegate.checkError();
		}

		public void write(int c) {
			System.out.print(c);
			delegate.write(c);
		}

		public void write(char[] buf, int off, int len) {
			System.out.print(new String(buf, off, len));
			delegate.write(buf, off, len);
		}

		public void write(char[] buf) {
			System.out.print(new String(buf));
			delegate.write(buf);
		}

		public void write(String s, int off, int len) {
			System.out.print(new String(s.toCharArray(), off, len));
			delegate.write(s, off, len);
		}

		public void write(String s) {
			System.out.print(s);
			delegate.write(s);
		}

		public void print(boolean b) {
			System.out.print(b);
			delegate.print(b);
		}

		public void print(char c) {
			System.out.print(c);
			delegate.print(c);
		}

		public void print(int i) {
			System.out.print(i);
			delegate.print(i);
		}

		public void print(long l) {
			System.out.print(l);
			delegate.print(l);
		}

		public void print(float f) {
			System.out.print(f);
			delegate.print(f);
		}

		public void print(double d) {
			System.out.print(d);
			delegate.print(d);
		}

		public void print(char[] s) {
			System.out.print(s);
			delegate.print(s);
		}

		public void print(String s) {
			System.out.print(s);
			delegate.print(s);
		}

		public void print(Object obj) {
			System.out.print(obj);
			delegate.print(obj);
		}

		public void println() {
			System.out.println();
			delegate.println();
		}

		public void println(boolean x) {
			System.out.println(x);
			delegate.println(x);
		}

		public void println(char x) {
			System.out.println(x);
			delegate.println(x);
		}

		public void println(int x) {
			System.out.println(x);
			delegate.println(x);
		}

		public void println(long x) {
			System.out.println(x);
			delegate.println(x);
		}

		public void println(float x) {
			System.out.println(x);
			delegate.println(x);
		}

		public void println(double x) {
			System.out.println(x);
			delegate.println(x);
		}

		public void println(char[] x) {
			System.out.println(x);
			delegate.println(x);
		}

		public void println(String x) {
			System.out.println(x);
			delegate.println(x);
		}

		public void println(Object x) {
			System.out.println(x);
			delegate.println(x);
		}

		public PrintWriter printf(String format, Object... args) {
			System.out.println(args);
			return delegate.printf(format, args);
		}

		public PrintWriter printf(Locale l, String format, Object... args) {
			System.out.println(args);
			return delegate.printf(l, format, args);
		}

		public PrintWriter format(String format, Object... args) {
			System.out.println(args);
			return delegate.format(format, args);
		}

		public PrintWriter format(Locale l, String format, Object... args) {
			System.out.println(args);
			return delegate.format(l, format, args);
		}

		public PrintWriter append(CharSequence csq) {
			System.out.println(csq);
			return delegate.append(csq);
		}

		public PrintWriter append(CharSequence csq, int start, int end) {
			System.out.println(csq);
			return delegate.append(csq, start, end);
		}

		public PrintWriter append(char c) {
			System.out.println(c);
			return delegate.append(c);
		}
	}
	
	public static class DelegateOutputStream extends ServletOutputStream {
		
		private ServletOutputStream delegate;
		
		public DelegateOutputStream(ServletOutputStream delegate) {
			this.delegate = delegate;
		}

		@Override
		public void write(int b) throws IOException {
			System.out.print((char)b);
			delegate.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			System.out.print(new String(b));
			delegate.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			System.out.print(new String(b, off, len));
			delegate.write(b, off, len);
		}

		public void print(String s) throws IOException {
			System.out.print(s);
			delegate.print(s);
		}

		public void print(boolean b) throws IOException {
			System.out.print(Boolean.toString(b));
			delegate.print(b);
		}

		public void print(char c) throws IOException {
			System.out.print(c);
			delegate.print(c);
		}

		public void print(int i) throws IOException {
			System.out.print(i);
			delegate.print(i);
		}

		public void print(long l) throws IOException {
			System.out.print(l);
			delegate.print(l);
		}

		public void print(float f) throws IOException {
			System.out.print(f);
			delegate.print(f);
		}

		public void print(double d) throws IOException {
			System.out.print(d);
			delegate.print(d);
		}

		public void println() throws IOException {
			System.out.println();
			delegate.println();
		}

		public void println(String s) throws IOException {
			System.out.println(s);
			delegate.println(s);
		}

		public void println(boolean b) throws IOException {
			System.out.println(b);
			delegate.println(b);
		}

		public void println(char c) throws IOException {
			System.out.println(c);
			delegate.println(c);
		}

		public void println(int i) throws IOException {
			System.out.println(i);
			delegate.println(i);
		}

		public void println(long l) throws IOException {
			System.out.println(l);
			delegate.println(l);
		}

		public void println(float f) throws IOException {
			System.out.println(f);
			delegate.println(f);
		}

		public void println(double d) throws IOException {
			System.out.println(d);
			delegate.println(d);
		}

		@Override
		public void flush() throws IOException {
			System.out.println("flush outputStream");
			delegate.flush();
		}

		@Override
		public void close() throws IOException {
			System.out.println("close outputStream");
			delegate.close();
		}

		@Override
		public boolean isReady() {
			System.out.println("isReady");
			return delegate.isReady();
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
			System.out.println("setWriteListener");
			delegate.setWriteListener(writeListener);
		}
	}
}