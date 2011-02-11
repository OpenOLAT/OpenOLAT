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
* <p>
*/ 

package org.olat.upgrade;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.hibernate.FlushMode;
import org.hibernate.ObjectDeletedException;
import org.olat.core.CoreBeanTypes;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.commons.services.text.TextService;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.NotificationsUpgrade;
import org.olat.core.util.notifications.Publisher;
import org.olat.core.util.notifications.Subscriber;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.PersistingCourseImpl;
import org.olat.course.statistic.LoggingVersionManagerImpl;
import org.olat.modules.fo.ForumManager;
import org.olat.modules.fo.Message;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.delete.service.DeletionModule;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Description:<br>
 * Upgrade to OLAT 6.2:
 * - Migration of old wiki-fields to flexiform 
 * 
 * Code is already here for every update. 
 * Method calls will be commented out step by step when corresponding new controllers are ready.
 * As long as there will be other things to migrate Upgrade won't be set to DONE!
 * 
 * <P>
 * Initial Date: 20.06.09 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class OLATUpgrade_6_3_0 extends OLATUpgrade {
	private static final String VERSION = "OLAT_6.3";
	private static final String TASK_CLEANUP_TMP_UPLOAD_FILES_KEY = "cleanupTmpUploadFiles";
	private static final String TASK_MIGRATE_FORUMS_MESSAGES = "Migrate forums messages to add word and character count";
	private static final String TASK_MIGRATE_NOTIFICATIONS = "Migrate notifications publishers";
	private static final String TASK_MIGRATE_COURSE_LOG_FILES = "Migrate course log files to course folders and deleted dir";
	
	/** filename used to store courseauthor's activities (personalized) - relict from PersistingAuditManager **/
	public static final String FILENAME_ADMIN_LOG = "course_admin_log.txt";
	
	/** readme filename used by old CourseLogsArchiveManager - relict from CourseLogsArchiveManager **/
	private static final String README = "README.txt";
	
	/**
	 * filename used to store all user's activities (personalized) in the course
	 * only visible for OLAT-admins
	 *  - relict from PersistingAuditManager 
	 */
	public static final String FILENAME_USER_LOG = "course_user_log.txt";
	/** filename used to store all user's activities (anonymised) in the course - relict from PersistingAuditManager **/
	public static final String FILENAME_STATISTIC_LOG = "course_statistic_log.txt";

	/** from PersistingCourseImpl which has this as private unfortunatelly **/
	private static final String COURSEFOLDER = "coursefolder";
	
	private static final String LOGS_DIRNAME = "logs";
	
	private static final String OLD_COURSE_LOGS_DIRNAME="old_course_logs";
	private static final String OLD_COURSE_LOGS_ZIPFILENAME="old_course_logs.zip";
	private static final String OLD_COURSE_LOGS_IN_APACHE_FORMAT_ZIPFILENAME = "old_course_logs_apache_format.zip";
	private static final String TASK_CLEANUP_BROKEN_COURSES = "cleanup_broken_courses";
	
	private Map<String, NotificationsUpgrade> notificationUpgrades;
	
	/** counter for statistics about what went wrong during apache course log migration **/
	private int filesWithApacheConversionErrors_ = 0;
	private DeletionModule deletionModule;
	private CourseModule courseModule;
	private String nodeId;
	private Object lockObject = new Object();
	@Autowired
	TextService languageService;
	
	/**
	 * [used by spring]
	 */
	public OLATUpgrade_6_3_0(DeletionModule deletionModule, CourseModule courseModule, String nodeId) {
		this.deletionModule = deletionModule;
		this.courseModule = courseModule;
		this.nodeId = nodeId;
	}
	
	/**
	 * @see org.olat.upgrade.OLATUpgrade#doPreSystemInitUpgrade(org.olat.upgrade.UpgradeManager)
	 */
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
	}

	/**
	 * @see org.olat.upgrade.OLATUpgrade#doPostSystemInitUpgrade(org.olat.upgrade.UpgradeManager)
	 */
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else {
			if (uhd.isInstallationComplete()) return false;
		}
		
		// Cleanup temp upload files that are not deleted properly
		cleanupTmpUploadFiles(upgradeManager, uhd);
		//Migrate forums messages
		migrateMessages(upgradeManager, uhd);
		//Migrate notifications
		migrateNotifications(upgradeManager, uhd);
		//Migrate course log files
		migrateCourseLogFiles(upgradeManager, uhd);
		//check and fix broken courses on the filesystem and database
		searchForBrokenCourses(upgradeManager, uhd);
		
		// set the logging version to 1 starting NOW
		new LoggingVersionManagerImpl().setLoggingVersionStartingNow(1);
		
//    // now pre and post code was ok, finish installation
		uhd.setInstallationComplete(true);
//		// persist infos
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		return true;
	}
	
	private void searchForBrokenCourses(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_CLEANUP_BROKEN_COURSES)) {
			
			String bcRoot = FolderConfig.getCanonicalRoot();
			File courseFolder = new File(bcRoot+"/course");
			String[] courseFolderNames = courseFolder.list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					try {
						Long.parseLong(name);
					} catch (NumberFormatException e) {
						return false;
					}
					return true;
				}
			});
			List<String> courseList = Arrays.asList(courseFolderNames);
			int counter = 0;
			for (String string : courseList) {
				Long courseResId = Long.parseLong(string);
				RepositoryEntry repoEntry = RepositoryManager.getInstance().lookupRepositoryEntry(OresHelper.createOLATResourceableInstance(CourseModule.class, courseResId), false);
				if (repoEntry != null) {
					//try to load course...
					try {
						//CourseFactory.loadCourse(courseResId);
						//check whether the runstructure is there (faster then loading the whole course)
						File runstructure = new File(bcRoot+"/course/"+courseResId+"/runstructure.xml");
						if (! runstructure.exists()) log.warn("Missing course structure file: "+runstructure.getAbsolutePath());
						File editstructure = new File(bcRoot+"/course/"+courseResId+"/editortreemodel.xml");
						if (! editstructure.exists()) log.warn("Missing course structure file: "+editstructure.getAbsolutePath());
						File courseconfig = new File(bcRoot+"/course/"+courseResId+"/CourseConfig.xml");
						if (! courseconfig.exists()) log.warn("Missing course structure file: "+courseconfig.getAbsolutePath());
					} catch (Exception e) {
						log.warn("Could not load course for resId: "+courseResId, e);
					}
				} else {
					log.warn("No repositoryEntry found for: "+courseResId);
				}
				
				if (counter > 0 && counter % 100 == 0) {
					log.audit("Another 100 courses done");
					DBFactory.getInstance().intermediateCommit();
				}
				counter++;
			}
			
			//now by database
			counter = 0;
			List<RepositoryEntry> entries =  RepositoryManager.getInstance().queryByType(CourseModule.ORES_TYPE_COURSE);
			for (RepositoryEntry repositoryEntry : entries) {
				Long courseResId = repositoryEntry.getOlatResource().getResourceableId();
				
				File runstructure = new File(bcRoot+"/course/"+courseResId+"/runstructure.xml");
				if (! runstructure.exists()) log.warn("Course is in DB but not on Filesystem: Missing course structure file: "+runstructure.getAbsolutePath());
				File editstructure = new File(bcRoot+"/course/"+courseResId+"/editortreemodel.xml");
				if (! editstructure.exists()) log.warn("Course is in DB but not on Filesystem: Missing course structure file: "+editstructure.getAbsolutePath());
				File courseconfig = new File(bcRoot+"/course/"+courseResId+"/CourseConfig.xml");
				if (! courseconfig.exists()) log.warn("Course is in DB but not on Filesystem: Missing course structure file: "+courseconfig.getAbsolutePath());
				
				if (counter > 0 && counter % 100 == 0) {
					log.audit("Another 100 courses done");
					DBFactory.getInstance().intermediateCommit();
				}
				counter++;
			}
			
			uhd.setBooleanDataValue(TASK_CLEANUP_BROKEN_COURSES, false);  //FIXME: set to true when done
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		
	}

	private void migrateNotifications(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_MIGRATE_NOTIFICATIONS)) {
			log.audit("+-----------------------------------------------------------------------------+");
			log.audit("+... Calculate the businesspath for the publishers (notifications)         ...+");
			log.audit("+-----------------------------------------------------------------------------+");

			int counter = 0;
			NotificationsManager notificationMgr = NotificationsManager.getInstance();
			List<Publisher> allPublishers = notificationMgr.getAllPublisher();
			if (log.isDebug()) log.info("Found " + allPublishers.size() + " publishers to migrate.");

			getNotificationUpgrades();
			
			for(Publisher publisher:allPublishers) {
				Publisher publisherToSave = upgrade(publisher);
				if(publisherToSave != null) {
					try {
						DBFactory.getInstance().updateObject(publisherToSave);
					} catch (ObjectDeletedException e) {
						log.warn("Publisher was already deleted, no update possible! Publisher key: "+publisherToSave.getKey() );
					} catch (Exception e) {
						log.warn("Publisher was already deleted, no update possible! Publisher key: "+publisherToSave.getKey() );
					}
					counter++;
				}
				if (counter > 0 && counter % 100 == 0) {
					log.audit("Another 100 publishers done");
					DBFactory.getInstance().intermediateCommit();
				}
			}

			DBFactory.getInstance().intermediateCommit();
			log.audit("**** Migrated " + counter + " publishers. ****");
			
			log.audit("+-----------------------------------------------------------------------------+");
			log.audit("+... Update the latest emailed date for all subscribers                       +");
			log.audit("+-----------------------------------------------------------------------------+");
			DBQuery query = DBFactory.getInstance().createQuery("update " + Subscriber.class.getName() + " subscriber set subscriber.latestEmailed=:latestDate");
			Calendar cal = Calendar.getInstance();
			//
			// use the day of installing the release, 
			// and set the time back to midnight instead of 
			// going back one day, e.g. cal.add(Calendar.DAY_OF_MONTH, -1);
			//
			// 1) before release day, sending notifications the old way at 02:00:00 a.m.
			// 2) at release day, sending notifications the old way at 02:00:00 a.m.
			// .. Install the Release -> Upgrader sets latestEmail sent on subscribers to release day at 00:00:00
			// 3) day after release, sending notifications the new way at 02:00:00 a.m.
			//
			// with this procedure only the news are sent twice which were created between 00:00:00 and 02:00:00 of release day.
			// 
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			query.setTimestamp("latestDate", cal.getTime());
			int subCounter = query.executeUpdate(FlushMode.AUTO);
			
			DBFactory.getInstance().intermediateCommit();
			log.audit("**** Migrated " + subCounter + " subscribers. ****");

			uhd.setBooleanDataValue(TASK_MIGRATE_NOTIFICATIONS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}
	
	private Publisher upgrade(Publisher publisher) {
		if(publisher == null) return null;
		if(publisher.getData() != null && publisher.getData().startsWith("[")) return null;
		
		String type = publisher.getType();
		if (notificationUpgrades == null) {
			log.error("No upgrader");
		}
		
		NotificationsUpgrade upgrade = notificationUpgrades.get(type);
		if(upgrade == null) {
			log.error("No upgrader for publisher: " + publisher.getType());
			return null;
		}
		log.audit("upgrading..."+upgrade.getClass().getName());
		return upgrade.ugrade(publisher);
	}
	
	private void migrateMessages(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_MIGRATE_FORUMS_MESSAGES)) {
			log.audit("+-----------------------------------------------------------------------------+");
			log.audit("+... Calcualating word and character count in existing forum posts         ...+");
			log.audit("+-----------------------------------------------------------------------------+");
			
			int counter = 0;
			ForumManager fMgr = ForumManager.getInstance();
			List<Long> allForumKeys = fMgr.getAllForumKeys();
			if (log.isDebug()) log.info("Found " + allForumKeys.size() + " forums to migrate.");

			for(Long forumKey:allForumKeys) {
				List<Message> allMessages = fMgr.getMessagesByForumID(forumKey);
				for (Message message : allMessages) {
					try{
						String body = message.getBody();
						Locale locale = languageService.detectLocale(body);
						int characters = languageService.characterCount(body, locale);
						message.setNumOfCharacters(characters);
						int words = languageService.wordCount(body, locale);
						message.setNumOfWords(words);
						counter++;
						
						DBFactory.getInstance().updateObject(message);
						
						if (counter > 0 && counter % 100 == 0) {
							log.audit("Another 100 messages done");
							DBFactory.getInstance().intermediateCommit();
						}
					} catch (Exception e) {
						log.error("Error during Migration: "+e, e);
						DBFactory.getInstance().rollback();
					}
				}
			}
			
			DBFactory.getInstance().intermediateCommit();
			log.audit("**** Migrated " + counter + " messages. ****");
			uhd.setBooleanDataValue(TASK_MIGRATE_FORUMS_MESSAGES, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}


	private void cleanupTmpUploadFiles(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_CLEANUP_TMP_UPLOAD_FILES_KEY)) {
			log.audit("+-----------------------------------------------------------------------------+");
			log.audit("+... Cleaning up old temporary upload files                                ...+");
			log.audit("+-----------------------------------------------------------------------------+");
			
			File tempUploadDir = new File(WebappHelper.getUserDataRoot() + "/tmp/");
			long counter = 0;
			long mem = 0;
			if (tempUploadDir.exists() && tempUploadDir.isDirectory()) {
				// get all files that start with instanceID_NodeID_ followed by a number
				FileFilter tmpUploadFileFilter = new RegexFileFilter(WebappHelper.getInstanceId()+ "_" + nodeId + "_[0-9]*");
				File[] tmpUploadFiles = tempUploadDir.listFiles(tmpUploadFileFilter);
				for (File file : tmpUploadFiles) {
					if (file.isFile() && file.exists()) {
						mem += file.length();
						file.delete();
						counter++;
					}
				}				
			}
			// ok, all done, commit done task to upgrade manager
			uhd.setBooleanDataValue(TASK_CLEANUP_TMP_UPLOAD_FILES_KEY, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
			// some info output
			log.info("Deleted #" + counter + " temporary upload files that consumed a total of " + StringHelper.formatMemory(mem) + " expensive diskspace. Pure happyness for your sysadmin.");
		}
	}

	private void migrateCourseLogFiles(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_MIGRATE_COURSE_LOG_FILES)) {
			log.audit("+-----------------------------------------------------------------------------+");
			log.audit("+... Migrate the Course Log Files                                          ...+");
			log.audit("+-----------------------------------------------------------------------------+");

			// doing an intermediate commit at the start to make sure we don't have any transaction open
			// further below we don't do anything with the database, it's raw file operations, hence
			// no further intermediatecommit is needed afterwards
			DBFactory.getInstance().intermediateCommit();

			File globalOldCourseLogsDir = new File(deletionModule.getArchiveRootPath(), OLD_COURSE_LOGS_DIRNAME);
			if (globalOldCourseLogsDir.exists() && !globalOldCourseLogsDir.isDirectory()) {
				log.error("**** !!!! Resource exists but is not a directory - cannot move course log files: "+globalOldCourseLogsDir.getAbsolutePath());
				throw new IllegalStateException("Resource exists but is not a directory - cannot move course log files - owner/permission issue?: "+globalOldCourseLogsDir.getAbsolutePath());
//				globalOldCourseLogsDir = null;
			} else if (!globalOldCourseLogsDir.exists() && !globalOldCourseLogsDir.mkdirs()) {
				log.error("**** !!!! Cannot create directory - cannot move course log files: "+globalOldCourseLogsDir.getAbsolutePath());
				throw new IllegalStateException("Cannot create directory - cannot move course log files - owner/permission issue?: "+globalOldCourseLogsDir.getAbsolutePath());
//				globalOldCourseLogsDir = null;
			}
			
			
			File courseRootDir = new File(FolderConfig.getCanonicalRoot() + File.separator + PersistingCourseImpl.COURSE_ROOT_DIR_NAME);
			
			File[] dirs = courseRootDir.listFiles();
			int nonCourseDirs = 0;
			int migratedCourses = 0;
			int zipErrors = 0;
			int moveErrors = 0;
			int coursesWithoutLogsDir = 0;
			for (int i = 0; i < dirs.length; i++) {
				File aDir = dirs[i];
				nonCourseDirs++;
				if (!aDir.isDirectory()) {
					continue;
				}
				if (!aDir.getName().matches("[0123456789]*")) {
					continue;
				}
				if (!aDir.getName().toLowerCase().equals(aDir.getName().toUpperCase())) {
					// kind of superfluous check, but still...
					continue;
				}
				nonCourseDirs--;
				File courseLogDir = new File(aDir, LOGS_DIRNAME);
				if (!courseLogDir.isDirectory() || !courseLogDir.exists()) {
					coursesWithoutLogsDir++;
					continue;
				}
				
				boolean zipSuccess = zipCourseLogFiles(courseLogDir);
				boolean moveSuccess = false;
				if (zipSuccess && globalOldCourseLogsDir!=null) {
					moveSuccess = moveInvisibleCourseLogFiles(courseLogDir, globalOldCourseLogsDir);
				}
				
				if (!zipSuccess) zipErrors++;
				if (!moveSuccess) moveErrors++;
				migratedCourses++;

				if (migratedCourses > 0 && migratedCourses % 100 == 0) {
					log.audit("Another 100 course log files migrated, "+migratedCourses+" done. Total-Dirs: "+dirs.length+", Non-Course-Dirs: "+nonCourseDirs+". Courses without logs dir: "+coursesWithoutLogsDir+". Errors: "+zipErrors+" zip errors, "+moveErrors+" move errors, "+filesWithApacheConversionErrors_+" apache-conversion errors. ****");
				}
			}
			
			log.audit("**** Migrated " + migratedCourses + " courses. Total-Dirs: "+dirs.length+", Non-Course-Dirs: "+nonCourseDirs+". Courses without logs dir: "+coursesWithoutLogsDir+". Errors: "+zipErrors+" zip errors, "+moveErrors+" move errors, "+filesWithApacheConversionErrors_+" apache-conversion errors ****");
			uhd.setBooleanDataValue(TASK_MIGRATE_COURSE_LOG_FILES, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}

	public File createTempDirectory() {
	  File temp = null;
	
	  try{
	  	temp = File.createTempFile("temp_olat_migrate", Long.toString(System.nanoTime()));
	  } catch (IOException ioe) {
	  	log.error("**** !!!! Could not get temporary file");
	  }
	
	  if(!(temp.delete()))
	  {
	      log.error("**** !!!! Could not delete temp file: " + temp.getAbsolutePath());
	  }
	
	  if(!(temp.mkdir()))
	  {
	  	log.error("**** !!!! Could not create temp directory: " + temp.getAbsolutePath());
	  }
	
	  return (temp);
	}

	private boolean zipCourseLogFiles(File courseLogDir) {
		File[] logFiles = courseLogDir.listFiles();
		Set<String> toBeZippedFiles = new HashSet<String>();
		Set<File> toBeDeletedFiles = new HashSet<File>();
		File readMe = null;
		for (int i = 0; i < logFiles.length; i++) {
			File logFile = logFiles[i];
			String logFileName = logFile.getName();
			if (logFileName.equals(FILENAME_ADMIN_LOG) && courseModule.isAdminLogVisibleForMigrationOnly()) {
				toBeZippedFiles.add(logFile.getName());
				toBeDeletedFiles.add(logFile);
			} else if (logFileName.equals(FILENAME_USER_LOG) && courseModule.isUserLogVisibleForMigrationOnly()) {
				toBeZippedFiles.add(logFile.getName());
				toBeDeletedFiles.add(logFile);
			} else if (logFileName.equals(FILENAME_STATISTIC_LOG) && courseModule.isStatisticLogVisibleForMigrationOnly()) {
				toBeZippedFiles.add(logFile.getName());
				toBeDeletedFiles.add(logFile);
			} else if (logFileName.equals(README)) {
				readMe = logFile;
			}
		}
		
		if (readMe!=null && toBeZippedFiles.size()>0) {
			toBeZippedFiles.add(readMe.getName());
			toBeDeletedFiles.add(readMe);
		}
		
		File courseFolder = new File(courseLogDir.getParentFile(), COURSEFOLDER);
		if (courseFolder.exists() && !courseFolder.isDirectory()) {
			log.error("**** !!!! Could not migrate course log files for "+courseLogDir.getParentFile().getName()+" as there is a file called '"+COURSEFOLDER+" which was expected to be a directory: "+courseFolder.getAbsolutePath());
			return false;
		}
		
		if (!courseFolder.exists()) {
			if (!courseFolder.mkdirs()) {
				log.error("**** !!!! Could not create directory "+courseFolder.getAbsolutePath());
				return false;
			}
		}
		
		File oldCourseLogsDir = new File(courseFolder, OLD_COURSE_LOGS_DIRNAME);
		if (oldCourseLogsDir.exists()) {
			log.error("**** !!!! "+OLD_COURSE_LOGS_DIRNAME+" alreday existed! Not migrating course. Dir= "+oldCourseLogsDir.getAbsolutePath());
			return false;
		}
		if (!oldCourseLogsDir.mkdirs()) {
			log.error("**** !!!! Could not create directory "+oldCourseLogsDir.getAbsolutePath());
			return false;
		}
		
		File oldCourseLogsZip = new File(oldCourseLogsDir, OLD_COURSE_LOGS_ZIPFILENAME);
		if (!ZipUtil.zip(toBeZippedFiles, courseLogDir, oldCourseLogsZip, true)) {
			log.error("**** !!!! Could not zip course log files from "+courseLogDir+", into "+oldCourseLogsZip.getAbsolutePath());
			return false;
		}
		
		// now convert those files into apache log format
		File tempDir = createTempDirectory();
		Set<String> toBeApacheLoggedFiles = new HashSet<String>();
		for (Iterator<File> it = toBeDeletedFiles.iterator(); it.hasNext();) {
			File toBeApacheLoggedFile = it.next();
			if (toBeApacheLoggedFile.getName().equals(README)) {
				// ignore the readme file
				continue;
			}
			File apacheLogFile = readSequence(toBeApacheLoggedFile, tempDir);
			toBeApacheLoggedFiles.add(apacheLogFile.getName());
		}
		File oldCourseLogsInApacheFormatZip = new File(oldCourseLogsDir, OLD_COURSE_LOGS_IN_APACHE_FORMAT_ZIPFILENAME);
		if (!ZipUtil.zip(toBeApacheLoggedFiles, tempDir, oldCourseLogsInApacheFormatZip, true)) {
			log.error("**** !!!! Could not zip course log files (those in apache format) from "+tempDir+", into "+oldCourseLogsInApacheFormatZip.getAbsolutePath());
			if (!FileUtils.deleteDirsAndFiles(tempDir, true, true)) {
				tempDir.deleteOnExit();
			}
			return false;
		}
		if (!FileUtils.deleteDirsAndFiles(tempDir, true, true)) {
			tempDir.deleteOnExit();
		}
		
		// now delete those files
		for (Iterator<File> it = toBeDeletedFiles.iterator(); it.hasNext();) {
			File toBeDeletedFile = it.next();
			if (!toBeDeletedFile.delete()) {
				log.error("**** !!!! Could not delete file "+toBeDeletedFile.getAbsolutePath());
				return false;
			}
		}
		
		return true;
	}
	
	private boolean moveInvisibleCourseLogFiles(File courseLogDir, File globalOldCourseLogsDir) {
		File[] logFiles = courseLogDir.listFiles();
		Set<File> toBeMovedFiles = new HashSet<File>();
		for (int i = 0; i < logFiles.length; i++) {
			File logFile = logFiles[i];
			String logFileName = logFile.getName();
			if (logFileName.equals(FILENAME_ADMIN_LOG) && !courseModule.isAdminLogVisibleForMigrationOnly()) {
				toBeMovedFiles.add(logFile);
			} else if (logFileName.equals(FILENAME_USER_LOG) && !courseModule.isUserLogVisibleForMigrationOnly()) {
				toBeMovedFiles.add(logFile);
			} else if (logFileName.equals(FILENAME_STATISTIC_LOG) && !courseModule.isStatisticLogVisibleForMigrationOnly()) {
				toBeMovedFiles.add(logFile);
			}
		}
		
		File concreteOldCourseLogsDir = new File(globalOldCourseLogsDir, courseLogDir.getParentFile().getName());
		if (concreteOldCourseLogsDir.exists() && !concreteOldCourseLogsDir.isDirectory()) {
			log.error("**** !!!! Resource exists but is not a directory: "+concreteOldCourseLogsDir.getAbsolutePath());
			return false;
		} else if (!concreteOldCourseLogsDir.exists() && !concreteOldCourseLogsDir.mkdirs()) {
			log.error("**** !!!! Could not create directory:: "+concreteOldCourseLogsDir.getAbsolutePath());
			return false;
		}
		
		for (Iterator<File> it = toBeMovedFiles.iterator(); it.hasNext();) {
			File toBeMovedFile = it.next();
			File targetFile = new File(concreteOldCourseLogsDir, toBeMovedFile.getName());
			if (!toBeMovedFile.renameTo(targetFile)) {
				log.error("**** !!!! Could not move file "+toBeMovedFile.getAbsolutePath()+" to "+targetFile.getAbsolutePath());
				return false;
			}
		}
		
		logFiles = courseLogDir.listFiles();
		if (logFiles!=null && logFiles.length>0) {
			log.warn("**** !!!! Directory is not empty: "+courseLogDir.getAbsolutePath());
			return false;
		}
		
		if (!courseLogDir.delete()) {
			log.error("**** !!!! Could not delete directory: "+courseLogDir.getAbsolutePath());
			return false;
		}
		
		return true;
	}

	/** copied from 6.2.x version of CourseLogsArchiveManager and modified file handling to avoid going via VFS **/
	private File readSequence(File leaf, File outDir){
		
		String line;
		File resultingFile = new File(outDir, leaf.getName());

		BufferedReader br = null;
		FileOutputStream fos = null;
		BufferedWriter writer = null;
		boolean zeroErrors = true;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(leaf)));
			fos = new FileOutputStream(resultingFile);
			writer = new BufferedWriter(new OutputStreamWriter(fos));
			while(null != (line = br.readLine())){
					line = convertLine(line);
					// <MODIFIED FOR SAFETY>
					if (line.length()==0) {
						log.warn("**** !!!! Conversion failed with file: "+leaf);
						zeroErrors = false;
					}
					// </MODIFIED FOR SAFETY>
					writer.append(line);
					writer.append("\r\n");
			}
		} catch (IOException e) {
			log.error("**** !!!! Could not convert file to apache format: "+leaf);
			return null;
		} finally {
			if (!zeroErrors) {
				filesWithApacheConversionErrors_++;
			}
			if (br!=null) {
				try{ br.close(); } catch(Exception e) {
					// this empty catch is ok
				}
			}
			if (writer!=null) {
				try{ writer.close(); } catch(Exception e) {
					// this empty catch is ok
				}
			}
		}
		return resultingFile;
	}
	
	/** copied 1:1 from 6.2.x version of CourseLogsArchiveManager **/
	private String convertLine(String line){
		StringBuilder sb = new StringBuilder();
		String[] splitters = line.split("\t");
		
		// <MODIFIED FOR SAFETY>
		if (splitters.length<5) {
			log.error("**** !!!! Could not convert line - fewer than 5 fields: "+line);
			return "";
		}
		// </MODIFIED FOR SAFETY>

		sb.append(splitters[2]);
		sb.append(" - ");
		sb.append(splitters[2]);

		String timeStamp = splitters[0];
		sb.append(" [");
		sb.append(timeStamp.substring(8,10)); // day
		sb.append("/");
		sb.append(getMonth(timeStamp.substring(5,7))); // month
		sb.append("/");
		sb.append(timeStamp.substring(0,4)); // year
		sb.append(":");
		sb.append(timeStamp.substring(11,16));
		sb.append(" +0000] \"GET /");
		sb.append(splitters[3].trim());
		sb.append("_");
		sb.append(splitters[4].replaceAll(" ", "_"));
		if(splitters.length > 5){
			sb.append("_");
			sb.append(splitters[5].trim());
		}
		if(splitters.length > 6){
			sb.append("_");
			sb.append(splitters[6].trim());
		}
		
		sb.append(" HTTP/1.0\" 200 100");
		return sb.toString();
	}
	
	/** copied 1:1 from 6.2.x version of CourseLogsArchiveManager **/
	private String getMonth(String num){
		if(num.startsWith("0")) num = num.substring(1);
		int i = Integer.parseInt(num);
		String[] months = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
		return months[i-1];
	}
	

	public String getVersion() {
		return VERSION;
	}

	public Map<String, NotificationsUpgrade> getNotificationUpgrades() {
		if (notificationUpgrades == null) {
			synchronized(lockObject) {
				if (notificationUpgrades == null) { // check again in synchronized-block, only one may create list
					notificationUpgrades = new HashMap<String,NotificationsUpgrade>();
					Map<String, Object> notificationUpgradeMap = CoreSpringFactory.getBeansOfType(CoreBeanTypes.notificationsUpgrade);
					Collection<Object> notificationUpgradeValues = notificationUpgradeMap.values();
					for (Object object : notificationUpgradeValues) {
						NotificationsUpgrade notificationsUpgrade = (NotificationsUpgrade) object;
						log.debug("initNotificationUpgrades notificationsUpgrade=" + notificationsUpgrade);
						notificationUpgrades.put(notificationsUpgrade.getType(), notificationsUpgrade);
					}	
				}
			}
		}
		return notificationUpgrades;
	}

}
