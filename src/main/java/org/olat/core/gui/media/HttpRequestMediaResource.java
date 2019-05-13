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

import java.io.Closeable;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * The cache control is set to one hour.
 * 
 * @author Mike Stock
 */
public class HttpRequestMediaResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(HttpRequestMediaResource.class);

	private final HttpResponse response;

	/**
	 * @param meth
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

	/**
	 * @see org.olat.core.gui.media.MediaResource#getContentType()
	 */
	@Override
	public String getContentType() {
		Header h = response.getFirstHeader("Content-Type");
		return h == null ? "" : h.getValue();

	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getSize()
	 */
	@Override
	public Long getSize() {
		Header h = response.getFirstHeader("Content-Length");
		return h == null ? null : new Long(h.getValue());
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		try {
			return response.getEntity().getContent();
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getLastModified()
	 */
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
		if (h == null) return;
		if (h.getValue().toLowerCase().contains("filename")) {
			hres.setHeader("Content-Disposition", h.getValue());
		} else {
			hres.setHeader("Content-Disposition", "filename=" + h.getValue());
		}
	}
}