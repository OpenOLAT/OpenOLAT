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

package org.olat.core.gui.media;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.ModalCloseable;
import org.olat.core.util.httpclient.CancellingInputStream;

/**
 * The cache control is set to one hour.
 * 
 * @author Mike Stock
 */
public class HttpRequestMediaResource implements MediaResource {
	
	private static final Set<String> headersToCopy = Set.of("accept-ranges",  "www-authenticate", "content-range", "content-length");

	private final ClassicHttpResponse response;
	private final Cancellable request;
	private boolean fullyStreamed = false;

	/**
	 * @param response The response to proxy (closed by release() or prepare())
	 * @param request The request of the response, cancelled if the body is not fully streamed (can be null)
	 */
	public HttpRequestMediaResource(ClassicHttpResponse response, Cancellable request) {
		this.response = response;
		this.request = request;
	}
	
	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_ONE_HOUR;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		Header h = response.getFirstHeader("Content-Type");
		return h == null ? "" : h.getValue();

	}

	@Override
	public Long getSize() {
		Header h = response.getFirstHeader("Content-Length");
		return h == null ? null : Long.valueOf(h.getValue());
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		Header h = response.getFirstHeader("Last-Modified");
		if (h != null) {
			try {
				DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
				Date d = df.parse(h.getValue());
				long l = d.getTime();
				return Long.valueOf(l);
			} catch (ParseException e) {
				//
			}
		}
		return null;
	}

	@Override
	public void release() {
		closeResponse();
	}
	
	@Override
	public void prepare(HttpServletResponse hres) {
		//deliver content-disposition if available to forward this information 
		//e.g. when someone is delivering generated files and sets the header himself
		Header h = response.getFirstHeader("Content-Disposition");
		if(h != null) {
			if (h.getValue().toLowerCase().contains("filename")) {
				hres.setHeader("Content-Disposition", h.getValue());
			} else {
				hres.setHeader("Content-Disposition", "filename=" + h.getValue());
			}
		}
		
		for(Header header:response.getHeaders()) {
			String name = header.getName();
			if(headersToCopy.contains(name.toLowerCase())) {
				hres.setHeader(name, header.getValue());
			}
		}
		
		int bufferSize = hres.getBufferSize();
		// If the browser aborts, closing the stream must not read the rest of the body
		try(InputStream in = request == null ? response.getEntity().getContent() : CancellingInputStream.content(response, request);
				InputStream bis = new BufferedInputStream(in, bufferSize);
				OutputStream out = hres.getOutputStream()) {
			IOUtils.copyLarge(bis, out, new byte[bufferSize]);
			fullyStreamed = true;
		} catch(Exception e) {
			ServletUtil.handleIOException("client browser probably abort when serving media resource", e);
		} finally {
			closeResponse();
		}
	}

	/**
	 * HttpClient 5 reads the rest of the body when a response is closed
	 * gracefully. If the body was not fully streamed, cancel the request
	 * and close the response without draining the rest of the content
	 * (same behavior as HttpClient 4.x).
	 */
	private void closeResponse() {
		if(fullyStreamed) {
			IOUtils.closeQuietly(response);
		} else {
			if(request != null) {
				request.cancel();
			}
			if(response instanceof ModalCloseable closeable) {
				closeable.close(CloseMode.IMMEDIATE);
			} else {
				IOUtils.closeQuietly(response);
			}
		}
	}
}
