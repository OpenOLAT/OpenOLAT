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

package org.olat.course.statistic;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ExportUtil;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.course.statistic.export.ICourseLogExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * The Manager exports the course log files and statistic file as zip archive. 
 * <P>
 * Initial Date:  19.11.2009 <br>
 * @author bja
 */
@Service
public class ExportManager {
	
	private static final Logger log = Tracing.createLoggerFor(ExportManager.class);


	/**
	 * filename used to store courseauthor's activities (personalized)
	 */
	private static final String FILENAME_ADMIN_LOG = "course_admin_log.xlsx";
	/**
	 * filename used to store all user's activities (personalized) in the course
	 * only visible for OLAT-admins
	 */
	private static final String FILENAME_USER_LOG = "course_user_log.xlsx";
	/** 
	 * filename used to store all user's activities (anonymized) in the course
	 */
	private static final String FILENAME_STATISTIC_LOG = "course_statistic_log.xlsx";
	/**
	 * zip filename substring (archive log files)
	 */
	public static final String COURSE_LOG_FILES = "CourseLogFiles";
	/**
	 * zip filename substring (statistic)
	 */
	public static final String COURSE_STATISTIC = "CourseStatistic";
	
	@Autowired
	private ICourseLogExporter courseLogExporter;
	
	/**
	 * Archives the course log files
	 * @param oresID
	 * @param exportDir
	 * @param begin
	 * @param end
	 * @param adminLog
	 * @param userLog
	 * @param statisticLog
	 * @param charset
	 * @param locale
	 * @param email
	 */
	public void archiveCourseLogFiles(Long oresID, String exportDir, Date begin, Date end, boolean adminLog, boolean userLog, boolean statisticLog,
			Locale locale, String email, boolean isAdministrativeUser){
		
		String zipName = ExportUtil.createFileNameWithTimeStamp(ExportManager.COURSE_LOG_FILES, "zip");
		Date date = new Date();
		String tmpDirName = oresID + "-" + date.getTime();
		File tmpDir = new File(WebappHelper.getTmpDir(), tmpDirName);
		if(!tmpDir.exists()) {
			tmpDir.mkdirs();
		}
		
		List<File> logFiles = new ArrayList<>();
		if (adminLog) {
			logFiles.add(createLogFile(oresID, begin, end, tmpDir, FILENAME_ADMIN_LOG, true, false, isAdministrativeUser));
		}
		if (userLog) {
			logFiles.add(createLogFile(oresID, begin, end, tmpDir, FILENAME_USER_LOG, false, false, isAdministrativeUser));
		}
		if (statisticLog) {
			logFiles.add(createLogFile(oresID, begin, end, tmpDir, FILENAME_STATISTIC_LOG, false, true, isAdministrativeUser));
		}
		
		saveFile(exportDir, zipName, tmpDir, logFiles, email, "email.archive", locale);
	}
	
	/**
	 * Create the actual log file.
	 * <p>
	 * Note: vfsContainer and dir must point to the very same directory. This is necessary to allow
	 * converting of the resulting file into a VFSLeaf (which is required by the zip util class) 
	 * and to have the absolute path name of the vfsContainer for the sql export (there's no
	 * getter on the VFSContainer for the absolute path - this is core reason why).
	 * This 'hack' is not very nice but we'll live with it for the moment.
	 * <p>
	 * @param oresID
	 * @param begin
	 * @param end
	 * @param charset
	 * @param vfsContainer
	 * @param dir
	 * @param filename
	 * @param resourceAdminAction
	 * @param anonymize
	 * @return
	 */
	private File createLogFile(Long oresID, Date begin, Date end, File dir, String filename,
			boolean resourceAdminAction, boolean anonymize, boolean isAdministrativeUser) {
		
		File outFile = new File(dir, filename);
		// trigger the course log exporter - it will store the file to outFile
		log.info("createLogFile: start exporting course log file "+outFile.getAbsolutePath());
		courseLogExporter.exportCourseLog(outFile, oresID, begin, end, resourceAdminAction, anonymize, isAdministrativeUser);
		log.info("createLogFile: finished exporting course log file "+outFile.getAbsolutePath());
		return outFile;
	}
	
	private void saveFile(String targetDir, String zipName, File tmpDir, List<File> files, String email, String emailI18nSubkey, Locale locale) {
		File zipFile = new File(targetDir, zipName);
		Set<String> filenames = files.stream().map(File::getName).collect(Collectors.toSet());
		if (ZipUtil.zip(filenames, tmpDir, zipFile)) {
			sendEMail(email, locale, emailI18nSubkey);
		}
		FileUtils.deleteDirsAndFiles(tmpDir, true, true);
	}
	
	private void sendEMail(String email, Locale locale, String emailI18nSubkey) {
		if(email == null || email.length() == 0) {
			return;
		}
		if (locale == null) {
			locale = I18nManager.getInstance().getCurrentThreadLocale();
		}
		Translator translator = Util.createPackageTranslator(ExportManager.class, locale);
		try {
			MailBundle bundle = new MailBundle();
			bundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
			bundle.setTo(email);
			bundle.setContent(translator.translate(emailI18nSubkey + ".subject"),
					translator.translate(emailI18nSubkey+".body"));

			CoreSpringFactory.getImpl(MailManager.class).sendMessage(bundle);
		} catch (Exception e) {
			log.error("Error sending information email to user that file was saved successfully.", e);
		}
	}

	public File getLatestCourseStatisticFile(String targetDir) {
		File courseStatisticsDir = new File(targetDir);
		File[] exportedCourseStatisticZipFiles = courseStatisticsDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(ExportManager.COURSE_STATISTIC) && name.endsWith(".zip");
			}
			
		});
		if (exportedCourseStatisticZipFiles==null || exportedCourseStatisticZipFiles.length==0) {
			return null;
		}
		
		if (exportedCourseStatisticZipFiles.length==1) {
			return exportedCourseStatisticZipFiles[0];
		}
		
		// we have more than one - return the newest
		File newestFile = exportedCourseStatisticZipFiles[0];
		for (int i = 0; i < exportedCourseStatisticZipFiles.length; i++) {
			File file = exportedCourseStatisticZipFiles[i];
			if (file.lastModified()>newestFile.lastModified()) {
				newestFile = file;
			}
		}
		
		return newestFile;
	}	
}
