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
import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

/**
 * The cache control is set to one hour.
 * 
 * @author Mike Stock
 */
public class HttpRequestMediaResource implements MediaResource {
	
	private static final Set<String> headersToCopy = Set.of("accept-ranges",  "www-authenticate", "content-range", "content-length");

	private final HttpResponse response;

	/**
	 * @param response The response to proxy
	 */
	public HttpRequestMediaResource(HttpResponse response) {
		this.response = response;
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
		if(response instanceof Closeable) {
			IOUtils.closeQuietly((Closeable)response);
		}
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
		
		for(Header header:response.getAllHeaders()) {
			String name = header.getName();
			if(headersToCopy.contains(name.toLowerCase())) {
				hres.setHeader(name, header.getValue());
			}
		}
		
		int bufferSize = hres.getBufferSize();
		try(InputStream in = response.getEntity().getContent();
				InputStream bis = new BufferedInputStream(in, bufferSize);
				OutputStream out = hres.getOutputStream()) {
			IOUtils.copyLarge(bis, out, new byte[bufferSize]);
		} catch(Exception e) {
			ServletUtil.handleIOException("client browser probably abort when serving media resource", e);
		} finally {
			if(response instanceof Closeable) {
				IOUtils.closeQuietly((Closeable)response);
			}
		}
	}
}