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
*/
package org.olat.core.gui.media;

import java.io.File;

import javax.servlet.http.HttpServletResponse;

import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;

/**
 * Description:<br>
 * A temporary media resource that uses another name for the download than the
 * actual name of the file. This is handy when the file has a temporary
 * filename. The mime type is computed from the provided file name.
 * <p>
 * The file is delivered as attachment. Use the FileMediaResource to deliver
 * a file inline.
 * 
 * <P>
 * Initial Date: 03.09.2008 <br>
 * 
 * @author gnaegi
 */
public class NamedFileMediaResource extends FileMediaResource {
	private String fileName;
	private String fileDescription;
	private boolean deleteAfterDelivery;

	/**
	 * @param theFile
	 *            The file to be delivered
	 * @param fileName
	 *            The file name as it should be saved in the client browser
	 * @param fileDescription
	 *            The description or NULL (no linebreak, will be used as
	 *            Content-Description)
	 * @param deleteAfterDelivery
	 *            true: file will be deleted from disk when release() is called
	 *            (after delivery); false: file remains untouched after delivery
	 */
	public NamedFileMediaResource(File theFile, String fileName, String fileDescription, boolean deleteAfterDelivery) {
		super(theFile);
		this.fileName = fileName;
		this.fileDescription = fileDescription;
		this.deleteAfterDelivery = deleteAfterDelivery;
	}
	
	@Override
	public String getContentType() {
		String mimeType = WebappHelper.getMimeType(fileName);
		if (mimeType == null) mimeType = "application/octet-stream";
		return mimeType;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		// encode filename in ISO8859-1; does not really help but prevents from filename not being displayed at all
		// if it contains non-US-ASCII characters which are not allowed in header fields.
		hres.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(fileName));
		hres.setHeader("Content-Description", StringHelper.urlEncodeUTF8(fileDescription));
	}

	@Override
	public void release() {
		if (deleteAfterDelivery && file.exists()) {
			FileUtils.deleteFile(file);
		}
	}
}
