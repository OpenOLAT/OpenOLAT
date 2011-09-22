
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
* Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package de.bps.onyx.plugin;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.ims.qti.QTIResultSet;

/**
 * Description:<br>
 * This class exports results of onyx test as zip-archiv with result.xml
 *
 * <P>
 * Initial Date:  16.11.2009 <br>
 * @author thomasw@bps-system.de
 */
public class OnyxExportManager {

	private static OnyxExportManager instance = new OnyxExportManager();

	/**
	 * Constructor for OnyxExportManager.
	 */
	private OnyxExportManager() {
		//
	}

	/**
	 * @return OnyxExportManager
	 */
	public static OnyxExportManager getInstance() {
		return instance;
	}

	/**
	 * Method exports the result.xmls of the given ResultSets in a zip file.
	 * @param resultSets The resultsets to export.
	 * @param exportDir The directory to store the zip file.
	 * @return The filename of the exported zip file.
	 */
	public String exportResults(List<QTIResultSet> resultSets, File exportDir, CourseNode currentCourseNode) {

		String filename = createTargetFilename(currentCourseNode.getShortTitle(), "TEST");
		if (!exportDir.exists()) {
			exportDir.mkdir();
		}

		File archiveDir = new File(exportDir, filename + "__TMP/");
		if (!archiveDir.exists()) {
			archiveDir.mkdir();
		}
		File archiveName = new File(exportDir, filename);
		File fUserdataRoot = new File(WebappHelper.getUserDataRoot());

		for (QTIResultSet rs : resultSets) {
			String resultXml = getResultXml(rs.getIdentity().getName(),
					currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString(),
					currentCourseNode.getIdent(), rs.getAssessmentID());
			File xml = new File(fUserdataRoot, resultXml);
			if(xml != null && xml.exists()) {
				File file_s = new File(exportDir, filename + "__TMP/"
						+ rs.getIdentity().getName() + "_" + rs.getCreationDate() + ".xml");
				//xml.copyTo(file_s);
				FileUtils.copyFileToFile(xml, file_s, false);
			}
		}

		boolean success = ZipUtil.zipAll(archiveDir, archiveName);
		if (success) {
			for (File file : archiveDir.listFiles()) {
				file.delete();
			}
			archiveDir.delete();
		}
		return filename;
	}

	/**
	 * Method exports the result.xmls of the given Survey in a zip file
	 * without the usernames.
	 * @param surveyPath The FolderPath where the survey results are stored.
	 * @param exportDir The directory to store the zip file.
	 * @return The filename of the exported zip file.
	 */
	public String exportResults(String surveyPath, File exportDir, CourseNode currentCourseNode) {
		String filename = createTargetFilename(currentCourseNode.getShortTitle(), "QUEST");

		File directory = new File(surveyPath);
		Set<String> files = new HashSet<String>();
		if(directory.exists()) {
			String[] allXmls = directory.list(new myFilenameFilter(currentCourseNode.getIdent()));
			if (allXmls != null && allXmls.length > 0) {
				for (String file : allXmls){
					files.add(file);
				}
			}
		}

		File archiveName = new File(exportDir, filename);
		if (files != null && files.size() > 0) {
			ZipUtil.zip(files, directory, archiveName);
		}
		return filename;
	}

	private String createTargetFilename(String shortTitle, String type) {
		StringBuilder tf = new StringBuilder();
		tf.append(type);
		tf.append(Formatter.makeStringFilesystemSave(shortTitle));
		tf.append("_");
		DateFormat myformat = new SimpleDateFormat("yyyy-MM-dd__hh-mm-ss__SSS");
		String timestamp = myformat.format(new Date());
		tf.append(timestamp);
		tf.append(".zip");
		return tf.toString();
	}

	/**
	 * Gets the result xml file from the file system.
	 * @param username
	 * @param assessmentType
	 * @param nodeId
	 * @return
	 */
	private String getResultXml(String username, String assessmentType, String nodeId, long assassmentId) {
		String filename;
		String path = OnyxResultManager.getResReporting() + File.separator + username + File.separator
			+ assessmentType + File.separator;
		filename = path + nodeId + "v" + assassmentId + ".xml";

		return filename;
	}

	/**
	 * Description:<br>
	 * Filters the filenames of the "File.list()" method so that only files
	 * witch passes the method "accept" are returned.
	 *
	 * <P>
	 * Initial Date:  25.09.2009 <br>
	 * @author thomasw@bps-system.de
	 */
	private class myFilenameFilter implements FilenameFilter {

		private String nodeId;

		public myFilenameFilter(String nodeId) {
			this.nodeId = nodeId;
		}
		/**
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		public boolean accept(File diretory, String name) {
			if (name.startsWith(nodeId)) {
				return true;
			} else {
				return false;
			}
		}
	}
}
