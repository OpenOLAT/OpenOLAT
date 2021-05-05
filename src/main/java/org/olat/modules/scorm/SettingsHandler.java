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
import java.io.IOException;

import org.jdom.JDOMException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.WebappHelper;
import org.olat.modules.scorm.contentpackaging.NoItemFoundException;
import org.olat.modules.scorm.contentpackaging.ScormPackageHandler;

public class SettingsHandler {
	private final String storagePath;
	private final String filePath;
	private final String username;
	private final String userid;

	private String cmi_lesson_mode;
	private String cmi_credit_mode;
	private File sequenceFile;
	private File manifestFile;
	private int controllerHashCode;

	/**
	 * @param repositoryPath in olat style this means a path like:
	 *          /home/guido/temp/olatdata/bcroot/repository/71560839208618/
	 * @param repoId
	 * @param courseId
	 * @param userHome a path to the usersHome
	 * @param username
	 * @param userid
	 * @param lesson_mode
	 * @param credit_mode
	 */
	public SettingsHandler(String repositoryPath, String repoId, String courseId, String storagePath, String username, String userid,
			String lesson_mode, String credit_mode, int controllerHashCode) {
		this.controllerHashCode = controllerHashCode;
		this.storagePath = storagePath;
		this.username = username;
		this.userid = userid;
		this.cmi_lesson_mode = lesson_mode;
		this.cmi_credit_mode = credit_mode;
		// after properties set
		filePath = buildFilePath(repoId, courseId);
		//this file holds information of the SCO's like 'completed' or 'browsed' and ...
		sequenceFile = new File(getFilePath(), "reload-settings.xml");
		manifestFile = new File(repositoryPath, "imsmanifest.xml");
	}
	
	/**
	 * @return a string either to the userhome or the olat temp dir.
	 */
	private String buildFilePath(String repoId, String courseId) {
		if (cmi_lesson_mode.equals(ScormConstants.SCORM_MODE_BROWSE) || cmi_lesson_mode.equals(ScormConstants.SCORM_MODE_REVIEW)) {
			StringBuilder tempPath = new StringBuilder();
			tempPath.append(WebappHelper.getTmpDir())
			  .append("/tmp").append(WebappHelper.getInstanceId()).append("scorm/")
			  .append(controllerHashCode).append("/")
			  .append(userid).append("/");
			if(courseId != null) {
				tempPath.append(courseId);
			}
			if(repoId != null) {
				tempPath.append(repoId);
			}
			if(courseId == null && repoId == null) {
				tempPath.append(Long.toString(CodeHelper.getForeverUniqueID()));
			}
			return tempPath.toString();
		} else {
			StringBuilder path = new StringBuilder();
			path.append(storagePath);
			path.append("/scorm/");
			path.append(userid);
			path.append("/");
			if(courseId != null) {
				path.append(courseId);
			}
			if(repoId != null) {
				path.append(repoId);
			}
			return path.toString();
		}
	}

	/**
	 * @see ISettingsHandler#getManifestFile()
	 */
	public File getManifestFile() {
		return manifestFile;
	}

	/**
	 * returns the reload-settings.xml file
	 * @see ISettingsHandler#getScoItemSequenceFile()
	 */
	public File getScoItemSequenceFile() {
		// check if Scormpackage has been changed so that the reload-settings.xml will be updated.
		try {
			ScormPackageHandler pm = new ScormPackageHandler(this);
			if (!sequenceFile.exists() || pm.checkIfScormPackageHasChanged()) {
				try {
					pm.buildSettings();
				} catch (NoItemFoundException e) {
					throw new OLATRuntimeException(SettingsHandler.class, "Problem loading the reload-settings.xml file. No item found in scorm item sequence file!",e);
				}
			}
		} catch (JDOMException e) {
			throw new OLATRuntimeException(SettingsHandler.class, "Problem loading the reload-settings.xml file.",e);
		} catch (IOException e) {
			throw new OLATRuntimeException(SettingsHandler.class, "Problem loading the reload-settings.xml file.",e);
		}
		return sequenceFile;
	}

	/**
	 * @return a file handle path to the sequence file
	 */
	public String getScoItemSequenceFilePath() {
		return getFilePath() + "/reload-settings.xml";
	}

	/**
	 * returns the sco data model (cmi data model) file
	 * @see ISettingsHandler#getScoDataModelFile(java.lang.String)
	 */
	public File getScoDataModelFile(String itemId) {
		return (new File(getFilePath(), itemId + ".xml"));
	}

	public String getStudentName() {
		return username;
	}

	public String getStudentId() {
		return userid;
	}

	public String getLessonMode() {
		return cmi_lesson_mode;
	}

	public String getCreditMode() {
		return cmi_credit_mode;
	}
	
	/**
	 * @return a String to the location the scorm sco data is beeing saved.
	 */
	public String getFilePath(){ 
		return filePath;
	}
}