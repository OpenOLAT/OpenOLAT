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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;

/**
 * The file media resource delivers the given file using the mime type of the
 * file and the name of the file as the download file name.
 * <p>
 * See the NambedFileMediaResource if the download file should have another name
 * as the source file.
 * <p>
 * See DownloadableMediaResource if the download file should always have
 * application/octet-stream as mime type to force the download of the file
 * instead of displaying it in the browser.
 * <p>
 * You can also use the second constructor to specify if the file should be
 * delivered as attachment (download) on rendered inline. By default, the file
 * is delivered for inline rendering.
 * 
 * @author Felix Jost
 */
public class FileMediaResource implements MediaResource {
	//TODO:fj:a clean up on all filemediaresources subclasses
	protected File file;
	private boolean unknownMimeType = false;
	private boolean deliverAsAttachment = false;

	/**
	 * file assumed to exist, but if it does not exist or cannot be read,
	 * getInputStream() will return null and the class will behave properly.
	 * <p>
	 * The file will be delivered inline
	 * 
	 * @param file
	 */
	public FileMediaResource(File file) {
		this(file, false);
		
	}

	/**
	 * file assumed to exist, but if it does not exist or cannot be read,
	 * getInputStream() will return null and the class will behave properly.
	 * <p>
	 * The file can be delivered inline or as attachment
	 * @param file
	 * @param deliverAsAttachment true: deliver as attachment; false: deliver inline
	 */
	public FileMediaResource(File file, boolean deliverAsAttachment) {
		this.file = file;
		this.deliverAsAttachment = deliverAsAttachment;
	}
	
	@Override
	public boolean acceptRanges() {
		return true;
	}
	
	/**
	 * @see org.olat.core.gui.media.MediaResource#getContentType()
	 */
	@Override
	public String getContentType() {
		String fileName = file.getName();
		String mimeType = WebappHelper.getMimeType(fileName);
		
		mimeType = WebappHelper.getMimeType(fileName);
		//html, xhtml and javascript are set to force download
		if (mimeType == null || "text/html".equals(mimeType)
				|| "application/xhtml+xml".equals(mimeType)
				|| "application/javascript".equals(mimeType)
				|| "image/svg+xml".equals(mimeType)) {
			mimeType = "application/force-download";
			unknownMimeType = true;
		}
		return mimeType;
	}

	/**
	 * @return @see org.olat.core.gui.media.MediaRequest#getSize()
	 */
	@Override
	public Long getSize() {
		return new Long(file.length());
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream( new FileInputStream(file) );
		} catch (FileNotFoundException e) {
			//
		}
		return bis;
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#getLastModified()
	 */
	@Override
	public Long getLastModified() {
		return new Long(file.lastModified());
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#release()
	 */
	@Override
	public void release() {
	// void
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#prepare(javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void prepare(HttpServletResponse hres) {
		if (deliverAsAttachment) {
			// encode filename in ISO8859-1; does not really help but prevents from filename not being displayed at all
			// if it contains non-US-ASCII characters which are not allowed in header fields.
			String name = StringHelper.urlEncodeUTF8(file.getName());
			if (unknownMimeType) {
				hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + name);
				hres.setHeader("Content-Description", name);
			} else {
				hres.setHeader("Content-Disposition", "filename*=UTF-8''" + name);
			}
		} else {
			hres.setHeader("Content-Disposition", "inline");
		}
	}

	@Override
	public String toString() {
		return "FileMediaResource:"+file.getAbsolutePath();
	}
}