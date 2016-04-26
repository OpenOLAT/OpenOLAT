/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.onyx.plugin;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
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
 * <P>
 * Initial Date: 16.11.2009 <br>
 * 
 * @author thomasw@bps-system.de
 */
public class OnyxExportManager {

	private static final OLog log = Tracing.createLoggerFor(OnyxExportManager.class);
	
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
	 * 
	 * @param resultSets The resultsets to export.
	 * @param exportDir The directory to store the zip file.
	 * @return The filename of the exported zip file.
	 */
	public String exportResults(final List<QTIResultSet> resultSets, final File exportDir, final CourseNode currentCourseNode) {

		final String filename = createTargetFilename(currentCourseNode.getShortTitle(), "TEST");
		if (!exportDir.exists()) {
			exportDir.mkdir();
		}

		final File archiveDir = new File(exportDir, filename + "__TMP/");
		if (!archiveDir.exists()) {
			archiveDir.mkdir();
		}
		final File archiveName = new File(exportDir, filename);
		final File fUserdataRoot = new File(WebappHelper.getUserDataRoot());

		for (final QTIResultSet rs : resultSets) {
			final String resultXml = getResultXml(rs.getIdentity().getName(),
					currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString(), currentCourseNode.getIdent(), rs.getAssessmentID());
			final File xml = new File(fUserdataRoot, resultXml);
			if (xml != null && xml.exists()) {
				final File file_s = new File(exportDir, filename + "__TMP/" + rs.getIdentity().getName() + "_" + rs.getCreationDate() + ".xml");
				// xml.copyTo(file_s);
				FileUtils.copyFileToFile(xml, file_s, false);
			}
		}

		final boolean success = ZipUtil.zipAll(archiveDir, archiveName);
		if (success) {
			for (final File file : archiveDir.listFiles()) {
				file.delete();
			}
			archiveDir.delete();
		}
		return filename;
	}
	
	public void exportResults(List<QTIResultSet> resultSets, ZipOutputStream exportStream, CourseNode currentCourseNode) {
		final String path = createTargetFilename(currentCourseNode.getShortTitle(), "TEST");
		for (final QTIResultSet rs : resultSets) {
			String resultXml = getResultXml(rs.getIdentity().getName(),
					currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString(),
					currentCourseNode.getIdent(), rs.getAssessmentID());

			String filename =  path + "/" + rs.getIdentity().getName() + "_" + rs.getCreationDate() + ".xml";
			try {
				exportStream.putNextEntry(new ZipEntry(filename));
				IOUtils.write(resultXml, exportStream);
				exportStream.closeEntry();
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	/**
	 * Method exports the result.xmls of the given Survey in a zip file without the usernames.
	 * 
	 * @param surveyPath The FolderPath where the survey results are stored.
	 * @param exportDir The directory to store the zip file.
	 * @return The filename of the exported zip file.
	 */
	public String exportResults(final String surveyPath, final File exportDir, final CourseNode currentCourseNode) {
		final String filename = createTargetFilename(currentCourseNode.getShortTitle(), "QUEST");

		final File directory = new File(surveyPath);
		final Set<String> files = new HashSet<String>();
		if (directory.exists()) {
			final String[] allXmls = directory.list(new myFilenameFilter(currentCourseNode.getIdent()));
			if (allXmls != null && allXmls.length > 0) {
				for (final String file : allXmls) {
					files.add(file);
				}
			}
		}

		final File archiveName = new File(exportDir, filename);
		if (files != null && files.size() > 0) {
			ZipUtil.zip(files, directory, archiveName);
		}
		return filename;
	}
	
	public void exportResults(final File surveyPath, final CourseNode courseNode, OutputStream out) {
		ZipOutputStream zout = new ZipOutputStream(out);
		zout.setLevel(9);

		if (surveyPath.exists()) {
			final String[] allXmls = surveyPath.list(new myFilenameFilter(courseNode.getIdent()));
			if (allXmls != null && allXmls.length > 0) {
				for (final String file : allXmls) {
					File xmlFile = new File(surveyPath, file);
					ZipUtil.addFileToZip(file, xmlFile, zout);
				}
			}
		}
	}

	private String createTargetFilename(final String shortTitle, final String type) {
		final StringBuilder tf = new StringBuilder();
		tf.append(type);
		tf.append(Formatter.makeStringFilesystemSave(shortTitle));
		tf.append("_");
		final DateFormat myformat = new SimpleDateFormat("yyyy-MM-dd__hh-mm-ss__SSS");
		final String timestamp = myformat.format(new Date());
		tf.append(timestamp);
		tf.append(".zip");
		return tf.toString();
	}

	/**
	 * Gets the result xml file from the file system.
	 * 
	 * @param username
	 * @param assessmentType
	 * @param nodeId
	 * @return
	 */
	// <OLATBPS-498>
	public String getResultXml(final String username, final String assessmentType, final String nodeId, final long assassmentId) {
		// </OLATBPS-498>
		String filename;
		final String path = OnyxResultManager.getResReporting() + File.separator + username + File.separator + assessmentType + File.separator;
		filename = path + nodeId + "v" + assassmentId + ".xml";

		return filename;
	}

	private String getResultZIP(String username, String assessmentType, String nodeId, long assassmentId) {
		// </OLATBPS-498>
		String filename;
		String path = OnyxResultManager.getResReporting() + File.separator + username + File.separator + assessmentType + File.separator;
		filename = path + nodeId + "v" + assassmentId + ".zip";

		return filename;
	}

	private String getResultSummaryPDF(String username, String assessmentType, String nodeId, long assassmentId) {
		// </OLATBPS-498>
		String filename;
		String path = OnyxResultManager.getResReporting() + File.separator + username + File.separator + assessmentType + File.separator;
		filename = path + "summary_" + nodeId + "v" + assassmentId + File.separator + "summary.pdf";

		return filename;
	}

	private String getResultSummaryHTML(String username, String assessmentType, String nodeId, long assassmentId) {
		// </OLATBPS-498>
		String filename;
		String path = OnyxResultManager.getResReporting() + File.separator + username + File.separator + assessmentType + File.separator;
		filename = path + "summary_" + nodeId + "v" + assassmentId + File.separator + "summary.html";

		return filename;
	}

	/**
	 * Description:<br>
	 * Filters the filenames of the "File.list()" method so that only files witch passes the method "accept" are returned.
	 * <P>
	 * Initial Date: 25.09.2009 <br>
	 * 
	 * @author thomasw@bps-system.de
	 */
	private class myFilenameFilter implements FilenameFilter {

		private final String nodeId;

		public myFilenameFilter(final String nodeId) {
			this.nodeId = nodeId;
		}

		/**
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		@Override
		public boolean accept(final File diretory, final String name) {
			if (name.startsWith(nodeId)) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	// <OLATCE-654>
	/**
	 * Method exports the result.xmls of the given ResultSets in a zip file.
	 * @param resultSets The resultsets to export.
	 * @param exportDir The directory to store the zip file.
	 * @param test The inputstream from referenced test resource (zip archived).
	 * @param currentCourseNode current course node
	 * @param sign sign the zip file
	 * @return The filename of the exported zip file.
	 */
	public String exportAssessmentResults(List<QTIResultSet> resultSets, File exportDir, MediaResource test, CourseNode currentCourseNode, boolean sign,
			File csvFile) {

		String filename = createTargetFilename(currentCourseNode.getShortTitle(), "TEST");
		if (!exportDir.exists()) {
			exportDir.mkdir();
		}

		File archiveDir = new File(exportDir, filename + "__TMP/");
		if (!archiveDir.exists()) {
			archiveDir.mkdir();
		}
		// copy test
		File testFile = new File(archiveDir.getAbsolutePath() + File.separator + "qtitest.zip");
		BufferedOutputStream target = null;
		try {
			target = new BufferedOutputStream(new FileOutputStream(testFile));
			FileUtils.copy(test.getInputStream(), target, test.getSize());
			target.flush();
		} catch (FileNotFoundException e) {
			// do nothing
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (target != null) {
				try {
					target.close();
				} catch (IOException e) {
				}
			}
		}
		
		if (csvFile != null) {
			FileUtils.copyFileToDir(csvFile, archiveDir, "");
		}

		File archiveName = new File(exportDir, filename);
		File fUserdataRoot = new File(WebappHelper.getUserDataRoot());

		String pattern = "yyyy_MMM_dd__HH_mm_SSS";
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

		for (QTIResultSet rs : resultSets) {
			String username = rs.getIdentity().getName();
			String resultXml = getResultXml(username,
					currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString(),
					currentCourseNode.getIdent(), rs.getAssessmentID());
			File xml = new File(fUserdataRoot, resultXml);
			String userFilePart = filename + "__TMP/" + username + "_" + dateFormat.format(rs.getCreationDate());
			if(xml != null && xml.exists()) {
				File file_s = new File(exportDir, userFilePart + ".xml");
				//xml.copyTo(file_s);
				FileUtils.copyFileToFile(xml, file_s, false);
			}
			String summaryPDF = getResultSummaryPDF(username, currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString(),
					currentCourseNode.getIdent(), rs.getAssessmentID());
			File pdf = new File(fUserdataRoot, summaryPDF);
			if (pdf != null && pdf.exists()) {
				File file_s = new File(exportDir, userFilePart + ".pdf");
				//xml.copyTo(file_s);
				FileUtils.copyFileToFile(pdf, file_s, false);
			}
			String summaryHTML = getResultSummaryHTML(username, currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString(),
					currentCourseNode.getIdent(), rs.getAssessmentID());
			File html = new File(fUserdataRoot, summaryHTML);
			if (html != null && html.exists()) {
				File file_s = new File(exportDir, userFilePart + ".html");
				//xml.copyTo(file_s);
				FileUtils.copyFileToFile(html, file_s, false);
			}
			String resultZip = getResultZIP(username, currentCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString(),
					currentCourseNode.getIdent(), rs.getAssessmentID());
			File zip = new File(fUserdataRoot, resultZip);
			if (zip != null && zip.exists()) {
				File file_s = new File(exportDir, userFilePart + ".zip");
				//xml.copyTo(file_s);
				FileUtils.copyFileToFile(zip, file_s, false);
			} else {
				log.debug("no zip-results found at : "+(zip != null ? zip.getAbsolutePath() : " NULL" ));
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
	
	// </OLATCE-654>
}
