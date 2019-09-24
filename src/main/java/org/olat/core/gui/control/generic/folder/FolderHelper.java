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
* Initial code contributed and copyrighted by<br>
* JGS goodsolutions GmbH, http://www.goodsolutions.ch
* <p>
*/
package org.olat.core.gui.control.generic.folder;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  22.06.2006 <br>
 *
 * @author Felix Jost
 */
public class FolderHelper {
	
	private static final String FILETYPES =  "avi bat bmp css doc docx dvi exe gif gz htm html jpeg jpg log midi mov mp3 mpeg mpg pdf png ppt pptx ps ra ram readme rtf tar tgz txt wav xls xlsx xml xsl zip";
	private static Set<String> knownFileTypes;

	static {
		// initialize known filetypes for faster access
		FolderHelper.knownFileTypes = new HashSet<>();
		StringTokenizer st = new StringTokenizer(FILETYPES, " ");
		while (st.hasMoreElements()) {
			FolderHelper.knownFileTypes.add(st.nextToken());
		}
	}
	

	/**
	 * Extract the type of file based on suffix.
	 * 
	 * @param filePath
	 * @param locale
	 * @return File type based on file extension.
	 */
	public static String extractFileType(String filePath, Locale locale) {
		int lastDot = filePath.lastIndexOf('.');
		if (lastDot > 0) {
			if (lastDot < filePath.length())
				return filePath.substring(lastDot + 1).toLowerCase();
		}
		Translator translator = Util.createPackageTranslator(FolderHelper.class, locale);
		return translator.translate("UnknownFile");
	}


	/**
	 * @param fileType
	 * @return True if filetype is known.
	 */
	public static boolean isKnownFileType(String fileType) { 
		return FolderHelper.knownFileTypes.contains(fileType); 
	}

}
