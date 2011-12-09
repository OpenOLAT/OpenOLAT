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

import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

/**
 * Description:<br>
 * Factory for different common types of MediaResources
 * <P>
 * Initial Date: 09.01.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class MediaResourceFactory {

	/**
	 * Allow private browser caching of <seconds> seconds hours. After that period
	 * the browser <br>
	 * must revalidate the resource using a If-Modified-Since request header.<br>
	 * Usually the answer will be a Not-Modified, but it gives us the chance<br>
	 * to update CSS and Javascript files ant at least the next day users<br>
	 * will be up to date as well. <br>
	 * Add proxy max ager in case a proxy ignored the private cache settings.<br>
	 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9<br>
	 * <br>
	 * the bevaviour of this method is that it simply overwrites or sets a new
	 * http-response-header:<br>
	 * response.setHeader("Cache-Control", "private, max-age=21600,
	 * s-maxage=21600");<br>
	 * <br>
	 * 
	 * @param originalMediaResource the mediaResource to be cache-enabled
	 * @param seconds determines how many seconds a browser may cache this
	 *          mediaresource before revalidating it
	 * @return the resulting cachable mediaresource
	 */
	// thanks to Florian Gnägi.
	public static MediaResource createCachableMediaResource(MediaResource originalMediaResource, int seconds) {
		return new CachableMediaResource(originalMediaResource, seconds);
	}
}

class CachableMediaResource implements MediaResource {
	private final MediaResource original;
	private final int seconds;

	public CachableMediaResource(MediaResource original, int seconds) {
		this.original = original;
		this.seconds = seconds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.olat.core.gui.media.MediaResource#getContentType()
	 */
	public String getContentType() {
		return original.getContentType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.olat.core.gui.media.MediaResource#getInputStream()
	 */
	public InputStream getInputStream() {
		return original.getInputStream();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.olat.core.gui.media.MediaResource#getLastModified()
	 */
	public Long getLastModified() {
		return original.getLastModified();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.olat.core.gui.media.MediaResource#getSize()
	 */
	public Long getSize() {
		return original.getSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.olat.core.gui.media.MediaResource#prepare(javax.servlet.http.HttpServletResponse)
	 */
	public void prepare(HttpServletResponse hres) {
		original.prepare(hres);
		// add or set additional header
		hres.setHeader("Cache-Control", "private, max-age=" + seconds + ", s-maxage=" + seconds);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.olat.core.gui.media.MediaResource#release()
	 */
	public void release() {
		original.release();
	}

}
