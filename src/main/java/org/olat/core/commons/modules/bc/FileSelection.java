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

package org.olat.core.commons.modules.bc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;

public class FileSelection {

	/** HTML form identifier */
	public static final String FORM_ID = "paths";
	
	private List<String> files = new ArrayList<>();
	private String currentContainerRelPath;
	
	public FileSelection(UserRequest ureq, String currentContainerRelPath) {
		if (currentContainerRelPath.equals("/")) currentContainerRelPath = "";
		this.currentContainerRelPath = currentContainerRelPath;
		parse(ureq);
	}

	public List<String> getFiles() {
		return files;
	}
	
	/**
	 * Checks if there is at least one file with invalid file name in the selection.
	 * Returns the list with the invalid filenames.
	 * 
	 * @return
	 */
	public List<String> getInvalidFileNames() {
		List<String> invalidFileNames = new ArrayList<>();
		List<String> filesList = getFiles();
		for(String fileName:filesList) {
			if(!FileUtils.validateFilename(fileName)) {
				invalidFileNames.add(fileName);
			}
		}
		return invalidFileNames;
	}
	
	/**
	 * Parse paths and build BriefcasePath wrappers.
	 * @param base
	 * @param ureq
	 */
	private void parse(UserRequest ureq) {
		String[] sFiles = ureq.getHttpReq().getParameterValues(FORM_ID);
		if (sFiles == null || sFiles.length == 0) return;
		files = Arrays.asList(sFiles);
	}

	/**
	 * Render pathset as HTML.
	 * 
	 * @return HTML Fragment.
	 */
	public String renderAsHtml() {
		StringBuilder sb = new StringBuilder(255);
		sb.append("<ul>");
		for (String filename:files) {
			sb.append("<li>")
			  .append(currentContainerRelPath).append("/").append(StringHelper.escapeHtml(filename))
			  .append("</li>");
		}
		sb.append("</ul>");
		return sb.toString();
	}
}
