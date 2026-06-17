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
package org.olat.modules.selectus.ui.resources;

import java.io.InputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.gui.media.StreamedMediaResource;
import org.olat.core.util.StringHelper;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  10 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class FOPMediaResource extends StreamedMediaResource {

	private String optionalFilename = null;
	/**
	 * @param in Stream
	 * @param charset the character set for the download, e.g. "iso-8859-1"
	 */
	public FOPMediaResource(InputStream in, String charset) {
		super(in, null, "application/pdf; charset="+charset);
	}

	/**
	 * @see org.olat.core.gui.media.MediaResource#prepare(javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void prepare(HttpServletResponse hres) {
		// anything
		hres.setCharacterEncoding("utf-8");
		
		String name ;
		if (optionalFilename == null ) {
			long random = System.currentTimeMillis(); // needed so IE does not cache
			name = "file"+random;
		}
		else {
			name = optionalFilename;
		}
		hres.setHeader("Content-Disposition", "filename=" + StringHelper.urlEncodeUTF8(name) + ".pdf");
		hres.setHeader("Content-Description", "Faculty Recruiting Generated data");
	}

	/**
	 * 
	 * @param fileName String without extension and only with valid chars
	 */
	public void setFilename(String fileName) {
		this.optionalFilename = fileName;
	}
}