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
package org.olat.restapi.audit;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * Wraps the request of a write call and keeps a copy of the first bytes of the
 * body while the resource reads the stream. The resource always receives the
 * whole body, only the copy of the audit log is truncated.
 * 
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
	
	public static final String TRUNCATED_MARKER = "...[truncated]";
	
	private final int maxSize;
	private TeeServletInputStream tee;
	private BufferedReader reader;
	
	public CachedBodyHttpServletRequest(HttpServletRequest request, int maxSize) {
		super(request);
		this.maxSize = maxSize;
	}
	
	/**
	 * Only the JSON bodies of write calls are cached. Multipart uploads are not
	 * wrapped, the stream would be consumed twice.
	 * 
	 * @param method The HTTP method of the request
	 * @param contentType The content type of the request
	 * @return true if the body of this request is worth caching
	 */
	public static boolean shouldWrap(String method, String contentType) {
		if(contentType == null) {
			return false;
		}
		boolean write = "POST".equals(method) || "PUT".equals(method)
				|| "DELETE".equals(method) || "PATCH".equals(method);
		return write && contentType.toLowerCase(Locale.ROOT).startsWith("application/json");
	}
	
	@Override
	public ServletInputStream getInputStream() throws IOException {
		if(tee == null) {
			tee = new TeeServletInputStream(super.getInputStream(), maxSize);
		}
		return tee;
	}
	
	@Override
	public BufferedReader getReader() throws IOException {
		if(reader == null) {
			reader = new BufferedReader(new InputStreamReader(getInputStream(), charset()));
		}
		return reader;
	}
	
	/**
	 * @return The cached body, null if the resource did not read the stream,
	 * 		with {@link #TRUNCATED_MARKER} appended if the body was longer than the maximum size
	 */
	public String getCachedBody() {
		if(tee == null || tee.capturedSize() == 0) {
			return null;
		}
		String body = new String(tee.captured(), charset());
		return tee.isTruncated() ? body + TRUNCATED_MARKER : body;
	}
	
	public boolean isTruncated() {
		return tee != null && tee.isTruncated();
	}
	
	private Charset charset() {
		String encoding = getCharacterEncoding();
		if(encoding == null) {
			return StandardCharsets.UTF_8;
		}
		try {
			return Charset.forName(encoding);
		} catch (Exception e) {
			return StandardCharsets.UTF_8;
		}
	}
	
	/**
	 * Delegates every read and copies the bytes in a buffer until the maximum size is reached.
	 */
	private static final class TeeServletInputStream extends ServletInputStream {
		
		private final int maxSize;
		private final ServletInputStream delegate;
		private final ByteArrayOutputStream buffer;
		private boolean truncated;
		
		public TeeServletInputStream(ServletInputStream delegate, int maxSize) {
			this.delegate = delegate;
			this.maxSize = maxSize;
			this.buffer = new ByteArrayOutputStream(Math.min(Math.max(maxSize, 64), 4096));
		}
		
		@Override
		public int read() throws IOException {
			int read = delegate.read();
			if(read >= 0) {
				capture(new byte[] { (byte)read }, 0, 1);
			}
			return read;
		}
		
		@Override
		public int read(byte[] bytes, int offset, int length) throws IOException {
			int read = delegate.read(bytes, offset, length);
			if(read > 0) {
				capture(bytes, offset, read);
			}
			return read;
		}
		
		private void capture(byte[] bytes, int offset, int length) {
			int room = maxSize - buffer.size();
			if(room <= 0) {
				truncated = true;
			} else if(length > room) {
				buffer.write(bytes, offset, room);
				truncated = true;
			} else {
				buffer.write(bytes, offset, length);
			}
		}
		
		public byte[] captured() {
			return buffer.toByteArray();
		}
		
		public int capturedSize() {
			return buffer.size();
		}
		
		public boolean isTruncated() {
			return truncated;
		}
		
		@Override
		public boolean isFinished() {
			return delegate.isFinished();
		}
		
		@Override
		public boolean isReady() {
			return delegate.isReady();
		}
		
		@Override
		public void setReadListener(ReadListener listener) {
			delegate.setReadListener(listener);
		}
		
		@Override
		public void close() throws IOException {
			delegate.close();
		}
	}
}
