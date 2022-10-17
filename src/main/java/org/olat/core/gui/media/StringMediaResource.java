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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;

/**
 * @author Felix Jost
 */
public class StringMediaResource extends DefaultMediaResource {
	//default - if no encoding is specified we assume iso latin
	private String encoding = "iso-8859-1";
	private String data;
	private boolean downloadable = false;
	private String downloadFileName = null;
	
	@Override
	public boolean acceptRanges() {
		return true;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		ByteArrayInputStream bis = null;
		try {
			bis = new ByteArrayInputStream(data.getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			throw new AssertException(encoding+" encoding not supported??");
			// iso-8859-1 must be supported on the platform
		}
		return new BufferedInputStream(bis);
	}

	/**
	 * @param data
	 */
	public void setData(String data) {
		this.data = data;
	}

	/**
	 * sets the encoding (with which the stream is converted to bytes for the inputstream), e.g. "utf-8" or "iso-8859-1"
	 * @param encoding
	 */
	public void setEncoding(String encoding) {
		this.encoding = StringHelper.check4xMacRoman(encoding);
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		// HTTP 1.1
		hres.setHeader("Cache-Control", "private, no-cache, no-store, must-revalidate, proxy-revalidate, s-maxage=0, max-age=0");
		// HTTP 1.0
		hres.setHeader("Pragma", "no-cache");
		hres.setDateHeader("Expires", 0);
		//
		if (downloadable && downloadFileName != null) {
			String filename = StringHelper.urlEncodeUTF8(downloadFileName);
			hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
		}
	}
	
	@Override
	public String toString() {
		return data;
	}

	/**
	 * Set to true to force the browser to download the resource. This is done
	 * by set the content-disposition to attachment
	 * 
	 * NOTE: make sure when writing the link to properly set the target or
	 * download attribute depending on the mime-type or the downloadable nature
	 * of the file!
	 * 
	 * @param downloadable true: set to download; false: let browser decide
	 * @param downloadFileName name of the attachment if downloadable set to true
	 */
	public void setDownloadable(boolean downloadable, String downloadFileName) {
		this.downloadable = downloadable;
		this.downloadFileName = downloadFileName;		
	}
}