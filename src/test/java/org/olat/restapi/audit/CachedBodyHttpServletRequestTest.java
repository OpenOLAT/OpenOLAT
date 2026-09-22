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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Initial date: 16 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class CachedBodyHttpServletRequestTest {
	
	private HttpServletRequest mockRequest(String body, String contentType) throws IOException {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getContentType()).thenReturn(contentType);
		Mockito.when(request.getContentLengthLong()).thenReturn(Long.valueOf(bytes.length));
		Mockito.when(request.getCharacterEncoding()).thenReturn("UTF-8");
		Mockito.when(request.getInputStream()).thenReturn(new ByteArrayServletInputStream(bytes));
		return request;
	}
	
	@Test
	public void wrapsJsonBodyAndKeepsStreamReadable() throws IOException {
		HttpServletRequest request = mockRequest("{\"a\":1}", "application/json;charset=UTF-8");
		CachedBodyHttpServletRequest wrapper = new CachedBodyHttpServletRequest(request, 16384);
		
		String readByTheResource = IOUtils.toString(wrapper.getInputStream(), StandardCharsets.UTF_8);
		Assert.assertEquals("{\"a\":1}", readByTheResource);
		Assert.assertEquals("{\"a\":1}", wrapper.getCachedBody());
		Assert.assertFalse(wrapper.isTruncated());
	}
	
	@Test
	public void truncatesLargeBody() throws IOException {
		String big = "{\"a\":\"" + "x".repeat(5000) + "\"}";
		HttpServletRequest request = mockRequest(big, "application/json");
		CachedBodyHttpServletRequest wrapper = new CachedBodyHttpServletRequest(request, 1024);
		
		// the resource still receives the whole body
		String readByTheResource = IOUtils.toString(wrapper.getInputStream(), StandardCharsets.UTF_8);
		Assert.assertEquals(big, readByTheResource);
		
		Assert.assertTrue(wrapper.isTruncated());
		Assertions.assertThat(wrapper.getCachedBody()).endsWith(CachedBodyHttpServletRequest.TRUNCATED_MARKER);
		Assert.assertEquals(1024 + CachedBodyHttpServletRequest.TRUNCATED_MARKER.length(),
				wrapper.getCachedBody().length());
	}
	
	@Test
	public void readerAndStreamShareTheCache() throws IOException {
		HttpServletRequest request = mockRequest("{\"b\":2}", "application/json");
		CachedBodyHttpServletRequest wrapper = new CachedBodyHttpServletRequest(request, 16384);
		
		String viaReader = IOUtils.toString(wrapper.getReader());
		Assert.assertEquals("{\"b\":2}", viaReader);
		Assert.assertEquals("{\"b\":2}", wrapper.getCachedBody());
	}
	
	@Test
	public void unreadBodyGivesNull() throws IOException {
		HttpServletRequest request = mockRequest("{\"c\":3}", "application/json");
		CachedBodyHttpServletRequest wrapper = new CachedBodyHttpServletRequest(request, 16384);
		
		Assert.assertNull(wrapper.getCachedBody());
		Assert.assertFalse(wrapper.isTruncated());
	}
	
	@Test
	public void byteByByteReadIsCaptured() throws IOException {
		HttpServletRequest request = mockRequest("{\"d\":4}", "application/json");
		CachedBodyHttpServletRequest wrapper = new CachedBodyHttpServletRequest(request, 16384);
		
		ServletInputStream in = wrapper.getInputStream();
		StringBuilder sb = new StringBuilder();
		for(int read = in.read(); read >= 0; read = in.read()) {
			sb.append((char)read);
		}
		Assert.assertEquals("{\"d\":4}", sb.toString());
		Assert.assertEquals("{\"d\":4}", wrapper.getCachedBody());
	}
	
	@Test
	public void missingEncodingFallsBackToUtf8() throws IOException {
		byte[] bytes = "{\"e\":\"ä\"}".getBytes(StandardCharsets.UTF_8);
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		Mockito.when(request.getCharacterEncoding()).thenReturn(null);
		Mockito.when(request.getInputStream()).thenReturn(new ByteArrayServletInputStream(bytes));
		CachedBodyHttpServletRequest wrapper = new CachedBodyHttpServletRequest(request, 16384);
		
		IOUtils.toString(wrapper.getInputStream(), StandardCharsets.UTF_8);
		Assert.assertEquals("{\"e\":\"ä\"}", wrapper.getCachedBody());
	}
	
	@Test
	public void shouldWrapDecidesOnMethodAndContentType() {
		Assert.assertTrue(CachedBodyHttpServletRequest.shouldWrap("PUT", "application/json"));
		Assert.assertTrue(CachedBodyHttpServletRequest.shouldWrap("POST", "application/json;charset=UTF-8"));
		Assert.assertTrue(CachedBodyHttpServletRequest.shouldWrap("DELETE", "application/json"));
		Assert.assertTrue(CachedBodyHttpServletRequest.shouldWrap("PATCH", "APPLICATION/JSON"));
		
		Assert.assertFalse(CachedBodyHttpServletRequest.shouldWrap("GET", "application/json"));
		Assert.assertFalse(CachedBodyHttpServletRequest.shouldWrap("PUT", "multipart/form-data; boundary=x"));
		Assert.assertFalse(CachedBodyHttpServletRequest.shouldWrap("POST", "application/x-www-form-urlencoded"));
		Assert.assertFalse(CachedBodyHttpServletRequest.shouldWrap("POST", null));
	}
	
	/**
	 * Minimal servlet input stream over a byte array.
	 */
	private static final class ByteArrayServletInputStream extends ServletInputStream {
		
		private final ByteArrayInputStream in;
		
		public ByteArrayServletInputStream(byte[] bytes) {
			this.in = new ByteArrayInputStream(bytes);
		}
		
		@Override
		public int read() {
			return in.read();
		}
		
		@Override
		public int read(byte[] bytes, int offset, int length) {
			return in.read(bytes, offset, length);
		}
		
		@Override
		public boolean isFinished() {
			return in.available() == 0;
		}
		
		@Override
		public boolean isReady() {
			return true;
		}
		
		@Override
		public void setReadListener(ReadListener listener) {
			//
		}
	}
}
