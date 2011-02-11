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

package org.olat.core.util.vfs;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FilesInfoMBean;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.WebappHelper;

public class VFSMediaResource implements MediaResource {

	private static final String MIME_TYPE_OCTET_STREAM = "application/octet-stream";
	//use this pseudo mime-type to force download on ie 6
	private static final String MIME_TYPE_FORCE_DOWNLOAD = "application/force-download";
	private VFSLeaf vfsLeaf;
	private String encoding;
	boolean unknownMimeType = false;
	private boolean downloadable = false;
	private FilesInfoMBean filesInfoMBean;

	public VFSMediaResource(VFSLeaf vfsLeaf) {
		this.vfsLeaf = vfsLeaf;
		this.filesInfoMBean = (FilesInfoMBean) CoreSpringFactory.getBean(FilesInfoMBean.class.getCanonicalName());
	}

	public String getContentType() {
		String mimeType;
		if(downloadable) {
			unknownMimeType = true;
			mimeType = MIME_TYPE_FORCE_DOWNLOAD;
		} else {
			mimeType = WebappHelper.getMimeType(vfsLeaf.getName());
			if (mimeType == null) {
				mimeType = MIME_TYPE_OCTET_STREAM;
				unknownMimeType = true;
			} else {
				// if any encoding is set, append it for the browser
				if (encoding != null) {
					mimeType = mimeType + ";charset=" + encoding;
					unknownMimeType = false;
				}
			}
		}
		return mimeType;
	}

	public Long getSize() {
		long size = vfsLeaf.getSize();
		return (size == VFSConstants.UNDEFINED) ? null : new Long(size);
	}

	public InputStream getInputStream() {
		filesInfoMBean.logDownload(getSize());
		return vfsLeaf.getInputStream();
	}

	public Long getLastModified() {
		long size = vfsLeaf.getLastModified();
		return (size == VFSConstants.UNDEFINED) ? null : new Long(size);
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#prepare(javax.servlet.http.HttpServletResponse)
	 */
	public void prepare(HttpServletResponse hres) {
		//http headers are ASCII only therefore we encode filenames
		String filename = "";
		try {
			filename = URLEncoder.encode(vfsLeaf.getName(), "utf-8");
		} catch (UnsupportedEncodingException wontHappen) {
			//nothing};
		}
		if (unknownMimeType) hres.setHeader("Content-Disposition", "attachment; filename=" + filename);
		else hres.setHeader("Content-Disposition", "filename=" + filename);
	}

	public void release() {
	// nothing to do here
	}

	/**
	 * if set, then content type will be modified such that the encoding is
	 * appended, e.g. "text/html;charset=utf-8". Makes sense only for non-binary
	 * data
	 * 
	 * @param encoding e.g. "iso-8859-1", or "utf-8"
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	public void setDownloadable(boolean downloadable) {
		this.downloadable = downloadable;
	}
}
