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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.gui.media;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;

/**
 * @author Mike Stock
 */
public class HttpRequestMediaResource implements MediaResource {

	HttpMethod meth;

	/**
	 * @param meth
	 */
	public HttpRequestMediaResource(HttpMethod meth) {
		this.meth = meth;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getContentType()
	 */
	public String getContentType() {
		Header h = meth.getResponseHeader("Content-Type");
		return h == null ? "" : h.getValue();

	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getSize()
	 */
	public Long getSize() {
		Header h = meth.getResponseHeader("Content-Length");
		return h == null ? null : new Long(h.getValue());
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getInputStream()
	 */
	public InputStream getInputStream() {
		try {
			InputStream in = meth.getResponseBodyAsStream();
			//meth.releaseConnection();
			return in;
		} catch (Exception e) {
			//  
		}
		return null;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getLastModified()
	 */
	public Long getLastModified() {
		Header h = meth.getResponseHeader("Last-Modified");
		if (h != null) {
			try {
				DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
				Date d = df.parse(h.getValue());
				long l = d.getTime();
				return new Long(l);
			} catch (ParseException e) {
				//
			}
		}
		return null;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#release()
	 */
	public void release() {
		meth.releaseConnection();
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#prepare(javax.servlet.http.HttpServletResponse)
	 */
	public void prepare(HttpServletResponse hres) {
		//deliver content-disposition if available to forward this information 
		//e.g. when someone is delivering generated files and sets the header himself
		Header h = meth.getResponseHeader("Content-Disposition");
		if (h == null) return;
		if (h.getValue().toLowerCase().contains("filename")) {
			hres.setHeader("Content-Disposition", h.getValue());
		} else {
			hres.setHeader("Content-Disposition", "filename=" + h.getValue());
		}
		
	}

}