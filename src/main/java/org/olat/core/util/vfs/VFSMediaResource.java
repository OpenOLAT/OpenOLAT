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

package org.olat.core.util.vfs;

import java.io.InputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.services.image.ImageUtils;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;


public class VFSMediaResource implements MediaResource {

	public static final String MIME_TYPE_OCTET_STREAM = "application/octet-stream";

	private VFSLeaf vfsLeaf;
	private boolean useMaster = false;
	private VFSLeaf vfsMasterLeaf;
	private String encoding;
	boolean unknownMimeType = false;
	private boolean downloadable = false;

	public VFSMediaResource(VFSLeaf vfsLeaf) {
		this.vfsLeaf = vfsLeaf;
	}

	public void setUseMaster(boolean useMaster) {
		this.useMaster = useMaster;
		updateMasterLeaf();
	}

	private void updateMasterLeaf() {
		if (useMaster) {
			VFSContainer parent = vfsLeaf.getParentContainer();
			String masterFileName = VFSTranscodingService.masterFilePrefix + vfsLeaf.getName();
			vfsMasterLeaf = VFSManager.resolveOrCreateLeafFromPath(parent, masterFileName);
		}
	}

	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_ONE_HOUR;
	}

	@Override
	public boolean acceptRanges() {
		return true;
	}
	
	public VFSLeaf getLeaf() {
		if (useMaster) {
			return vfsMasterLeaf;
		}
		return vfsLeaf;
	}
	
	public void setLeaf(VFSLeaf vfsLeaf) {
		this.vfsLeaf = vfsLeaf;
	}

	@Override
	public String getContentType() {
		String name = getLeaf().getName();
		String mimeType = WebappHelper.getMimeType(name);
		if(downloadable) {
			//html, xhtml and javascript are set to force download
			String suffix = FileUtils.getFileSuffix(name);
			if (FolderModule.isContentSusceptibleToForceDownload(suffix, mimeType)) {
				mimeType = MIME_TYPE_OCTET_STREAM;
				unknownMimeType = true;
			} else if (encoding != null) {
				mimeType = mimeType + ";charset=" + encoding;
			}
		} else if (mimeType == null) {
			mimeType = MIME_TYPE_OCTET_STREAM;
			unknownMimeType = true;
		} else {
			// if any encoding is set, append it for the browser
			if (encoding != null) {
				mimeType = mimeType + ";charset=" + encoding;
				unknownMimeType = false;
			}
		}
		return mimeType;
	}

	@Override
	public Long getSize() {
		long size = getLeaf().getSize();
		return (size == VFSStatus.UNDEFINED) ? null : Long.valueOf(size);
	}

	@Override
	public InputStream getInputStream() {
		return getLeaf().getInputStream();
	}

	@Override
	public Long getLastModified() {
		long lastModified = getLeaf().getLastModified();
		return (lastModified == VFSStatus.UNDEFINED) ? null : Long.valueOf(lastModified);
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		String filename = StringHelper.urlEncodeUTF8(getLeaf().getName()).replace("+", "%20");
		if (unknownMimeType || downloadable) {
			hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
		} else {
			hres.setHeader("Content-Disposition", "filename*=UTF-8''" + filename);
		}
		if (ImageUtils.isSvgFileName(filename)) {
			hres.setHeader("Content-Security-Policy", "script-src 'none'");
			hres.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			hres.setHeader("Pragma", "no-cache");
			hres.setDateHeader("Expires", 0);
		}
	}

	@Override
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
	
	/**
	 * Set to true to force the browser to download the resource. This is done by 
	 * a) set the content-disposition to attachment
	 * b) set the mime-type to some non-existing mime-type for browser-executable, 
	 * xss-relevant resource such as html files. Since the browser does not 
	 * understand the mime-type, the file gets downloaded instead of executed. 
	 * 
	 * NOTE: make sure when writing the link to properly set the target or 
	 * download attribute depending on the mime-type or the downloadable nature
	 * of the file!
	 * 
	 * @param downloadable true: force browser to download; false: let browser
	 * decide, might render inline in browser window
	 */
	public void setDownloadable(boolean downloadable) {
		this.downloadable = downloadable;
	}
}
