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

package org.olat.modules.scorm;

import java.io.File;

/**
 * Description:<br>
 * Interface for getting the scorm related files
 * 
 * @author guido
 */
public interface ISettingsHandler {

	/**
	 * @return a filehandler to the ims manifest file (imsmanifest.xml)
	 */
	public File getManifestFile();

	/**
	 * @return a filehandler to the sequencer file that stores which items are
	 *         "completed", "not attemded", "incomlete"
	 */
	public File getScoItemSequenceFile();

	/**
	 * @param itemId
	 * @return a filehandler to a single Scorm SCO and its cmi dataModel
	 */
	public File getScoDataModelFile(String itemId);

	/**
	 * @return a string that points to
	 */
	public String getScoItemSequenceFilePath();

	/**
	 * @return the students full name
	 */
	public String getStudentName();

	/**
	 * @return a unique student id
	 */
	public String getStudentId();

	/**
	 * @return the lesson mode like
	 */
	public String getLessonMode();

	/**
	 * @return the credit mode
	 */
	public String getCreditMode();

}
