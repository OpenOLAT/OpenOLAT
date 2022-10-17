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

import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletResponse;

import org.olat.core.logging.AssertException;

/**
 * @author Felix Jost
 */
public class ExcelMediaResource extends StringMediaResource {

	private String optionalFilename = null;
	/**
	 * @param data
	 * @param charset the character set for the download, e.g. "iso-8859-1"
	 */
	public ExcelMediaResource(String data, String charset) {
		setContentType("application/vnd.ms-excel; charset="+charset);
		setEncoding(charset);
		setLastModified(null); // no timestamp, since always newly created
		setData(data);
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

		hres.setHeader("Content-Disposition", "attachment; filename=" + name + ".xls");
		hres.setHeader("Content-Description", "OLAT Generated data");
	}

	/**
	 * 
	 * @param fileName String without extension and only with valid chars
	 */
	public void setFilename(String fileName) {
		Pattern p = Pattern.compile("[a-zA-Z0-9]*");
		if (!p.matcher(fileName).matches())	{
			throw new AssertException(fileName + " is not a valid filename");
		}
		this.optionalFilename = fileName;
	}
}