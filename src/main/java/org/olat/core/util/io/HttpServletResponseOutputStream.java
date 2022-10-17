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
package org.olat.core.util.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 * Initial date: 27.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HttpServletResponseOutputStream implements HttpServletResponse {
	
	private final ServletOutputStream out;
	
	public HttpServletResponseOutputStream(OutputStream out) {
		this.out = new DelegateServletOutputStream(out);
	}

	@Override
	public String getCharacterEncoding() {
		return null;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return out;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return null;
	}

	@Override
	public void setCharacterEncoding(String charset) {
		//
	}

	@Override
	public void setContentLength(int len) {
		//
	}
	
	@Override
	public void setContentLengthLong(long len) {
		//
	}

	@Override
	public void setContentType(String type) {
		//
	}

	@Override
	public void setBufferSize(int size) {
		//
	}

	@Override
	public int getBufferSize() {
		return 0;
	}

	@Override
	public void flushBuffer() throws IOException {
		//
	}

	@Override
	public void resetBuffer() {
		//
	}

	@Override
	public boolean isCommitted() {
		return false;
	}

	@Override
	public void reset() {
		//
	}

	@Override
	public void setLocale(Locale loc) {
		//
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public void addCookie(Cookie cookie) {
		//
	}

	@Override
	public boolean containsHeader(String name) {
		return false;
	}

	@Override
	public String encodeURL(String url) {
		return null;
	}

	@Override
	public String encodeRedirectURL(String url) {
		return null;
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		//
	}

	@Override
	public void sendError(int sc) throws IOException {
		//
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		//
	}

	@Override
	public void setDateHeader(String name, long date) {
		//
	}

	@Override
	public void addDateHeader(String name, long date) {
		//
	}

	@Override
	public void setHeader(String name, String value) {
		//
	}

	@Override
	public void addHeader(String name, String value) {
		//
	}

	@Override
	public void setIntHeader(String name, int value) {
		//
	}

	@Override
	public void addIntHeader(String name, int value) {
		//
	}

	@Override
	public void setStatus(int sc) {
		//
	}

	@Override
	public int getStatus() {
		return 0;
	}

	@Override
	public String getHeader(String name) {
		return null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		return Collections.emptyList();
	}

	@Override
	public Collection<String> getHeaderNames() {
		return Collections.emptyList();
	}
	
	public static class DelegateServletOutputStream extends ServletOutputStream {
		
		private final OutputStream out;
		
		public DelegateServletOutputStream(OutputStream out) {
			this.out = out;
		}

		@Override
		public void write(int b) throws IOException {
			out.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			out.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
			//
		}

		@Override
		public void flush() throws IOException {
			out.flush();
		}

		@Override
		public void close() throws IOException {
			out.close();
		}
	}
}
