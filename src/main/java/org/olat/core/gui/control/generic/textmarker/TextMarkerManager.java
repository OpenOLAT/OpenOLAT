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

package org.olat.core.gui.control.generic.textmarker;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Description: Methods used to load and store TextMarker objects on the filesystem as XML files and other helper methods
 * 
 * @author gnaegi <www.goodsolutions.ch>
 * Initial Date: Jul 14, 2006
 * 
 */
public interface TextMarkerManager {

	/**
	 * Load the text marker object from the given file
	 * 
	 * @param textMarkerFile The file. Null value is accepted.
	 * @return List of textMarker objects
	 */
	public List<TextMarker> loadTextMarkerList(VFSLeaf textMarkerFile);

	/**
	 * Load a text marker file and convert it to a string that can be indexed by
	 * lucene or used as a text based version of the glossar.
	 * 
	 * @param textMarkerFile The file. Null value is accepted.
	 * @return String
	 */
	public abstract String loadFileAsString(VFSLeaf textMarkerFile);

	/**
	 * Save a list of TextMarker objects to a file
	 * 
	 * @param textMarkerFile The file
	 * @param textMarkerList The list of TextMarker objects
	 */
	public abstract void saveToFile(VFSLeaf textMarkerFile, List<TextMarker> textMarkerList);

	/**
	 * check if a resource has textMakring enabled
	 * @param ureq
	 * @param ores
	 * @return
	 */
	public boolean isTextmarkingEnabled(UserRequest ureq, OLATResourceable ores);

}