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

package org.olat.core.util;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.olat.core.logging.AssertException;

/**
 * Initial Date:  Jul 20, 2004
 *     
 * @author gnaegi
 *     
 * Comment: 
 *     
 */
public class ExportUtil {

	/**
	 * Creates a file in a Directory and writes a string to this file. If the file
	 * already exists an AssertExeption is thrown.
	 * 
	 * @param fileName
	 * @param content
	 * @param exportDirectory
	 */

	public static void writeContentToFile(String fileName, String content, File exportDirectory, String enc) {
		File f = new File(exportDirectory, fileName);
		if (f.exists()) { throw new AssertException("File " + fileName + " already exists!"); }
		FileUtils.save(f, content, enc);
	}

	/**
	 * Appends a timestamp and a desired suffix to a string. 
	 * 
	 * @param prefix (e.g.: myFile)
	 * @param suffix (e.g.: xls)
	 * @return myFile_2004-09-28_02-36-40.xls
	 */

	public static String createFileNameWithTimeStamp(String prefix, String suffix) {
		StringBuilder fn = new StringBuilder();
		fn.append(Formatter.makeStringFilesystemSave(prefix));
		fn.append("_");
		DateFormat myformat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String timestamp = myformat.format(new Date());
		fn.append(timestamp);
		fn.append(".");
		fn.append(suffix);
		return fn.toString();
	}

}