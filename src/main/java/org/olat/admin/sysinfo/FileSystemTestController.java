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

package org.olat.admin.sysinfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.taskExecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;

/**
 *  @author Christian Guretzki
 */

public class FileSystemTestController extends BasicController implements GenericEventListener {
	
	OLog log = Tracing.createLoggerFor(this.getClass());

	private VelocityContainer myContent;
	private Link startTestButton;
	private Link cleanupTestDirButton;
	private Link fileSystemTestOnButton;
	private Link reloadButton;
	
	private final static String STATUS_STOPPED      = "Stopped"; 
	private final static String STATUS_WRITING      = "Write test dirs/files...";
	private final static String STATUS_FILE_WRITTEN = "Written test dirs/files";
	private final static String STATUS_CHECKING     = "Checking test dirs/files...";
	private final static String STATUS_FILE_CHECKED = "File checked";
	private String testStatus = STATUS_STOPPED;
	
	private int loops = 1;
	private int maxNbrDirs = 100;
	private int maxNbrFiles = 100;
	private int nbrCharInFile = 100;

	private final static String TEST_BASEDIR_NAME = "filesystem_test";
	private final static String TEST_DIRNAME_PREFIX = "testdir_";
	private final static String TEST_FILENAME_PREFIX = "testfile_";
	private String testBaseDir;

	private StringBuffer testResults = new StringBuffer();

	private FileSystemTestForm fileSystemTestForm;
	
	public static final OLATResourceable ORES_FILESYSTEMTEST = OresHelper.createOLATResourceableType(FileSystemTestController.class);

	private final TaskExecutorManager taskExecutorManager;
	
	/**
	 * Controlls user session in admin view.
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public FileSystemTestController(UserRequest ureq, WindowControl wControl) { 
		super(ureq, wControl);
		
		testBaseDir = FolderConfig.getCanonicalTmpDir() + File.separator + TEST_BASEDIR_NAME;
		taskExecutorManager = CoreSpringFactory.getImpl(TaskExecutorManager.class);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, ORES_FILESYSTEMTEST);
		
		myContent = createVelocityContainer("filesystemtest");
		fileSystemTestForm  = new FileSystemTestForm(ureq, wControl, getTranslator());
		listenTo(fileSystemTestForm);
		myContent.put("file.system.test.form", fileSystemTestForm.getInitialComponent());
		startTestButton = LinkFactory.createButton("filesystemtest.start", myContent, this);
		cleanupTestDirButton = LinkFactory.createButton("filesystemtest.cleanup.testdir", myContent, this);
		fileSystemTestOnButton = LinkFactory.createButton("filesystemtest.on", myContent, this);
		reloadButton = LinkFactory.createButton("filesystemtest.reload", myContent, this);
		myContent.contextPut("isfiletest_on", false);
		setStatus(STATUS_STOPPED);
		resetTestResults();
		putInitialPanel(myContent);
		
	}


	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == startTestButton) {
			startTest();
			log.info("start FileSystemTest...");
		} else if (source == fileSystemTestOnButton) {
			myContent.contextPut("isfiletest_on", true);
		} else if (source == cleanupTestDirButton) {
			cleanupTestDir();
		} else if (source == reloadButton) {
			getWindowControl().getWindowBackOffice().getWindow().setDirty(true);
		}
	}

	private void startTest() {
		cleanupTestDir();
		resetTestResults();
		setStatus(STATUS_WRITING);
		startThreadWriter(loops,maxNbrDirs,maxNbrFiles);		
	}


	private void cleanupTestDir() {
		log.info("cleanup testdir..."); 
		FileUtils.deleteDirsAndFiles(new File(testBaseDir) , true, true);
	}


	private void resetTestResults() {
		testResults = new StringBuffer();
		myContent.contextPut("testresults", testResults);
	}


	private void startThreadWriter(final int loops, final int maxNbrDirs, final int maxNbrFiles) {
		Runnable fileWritterThread = new Runnable() {
			public void run() {
				try {
					for (int loop=0; loop<loops; loop++) {
						long startTime = System.currentTimeMillis();
					  // loop over dir
						for(int dirNr=1; dirNr <= maxNbrDirs; dirNr++ ) {
							String currentFullDirPathName = testBaseDir + File.separator + TEST_DIRNAME_PREFIX + loop + "-" + dirNr;
							File testDir = new File(currentFullDirPathName);
							if (!testDir.exists()) {
								log.info("make dir=" + currentFullDirPathName);
								testDir.mkdirs();
							} else {
								log.warn("Dir=" + currentFullDirPathName + " already exists" );
							}
							// loop over file
							for(int fileNr=1; fileNr <= maxNbrFiles; fileNr++ ) {
								String currentFileName = TEST_FILENAME_PREFIX + dirNr + "-" + fileNr + ".txt";
								String filePath = currentFullDirPathName + File.separator + currentFileName;
								log.debug("create file=" + filePath);
								if (nbrCharInFile == 0) {
									FileUtils.createEmptyFile(new File(filePath));
								} else {
									// create file with content								
									try {
										File testFile = new File(filePath);
										testFile.createNewFile();
										FileOutputStream fos = new FileOutputStream(testFile);
								    for (int i=0; i<nbrCharInFile;i=i+15) {
								    	fos.write("testfilesystem\n".getBytes());
								    }
								    if (fileSystemTestForm.isFsSyncEnabled()) { 
								    	log.debug("call sync before close");
								    	fos.getFD().sync();
								    }
								    fos.close();
									} catch (IOException e) {
										log.error("Could not write file=" + filePath, e);
									}
								}
							}
						}
						long durationInSec = (System.currentTimeMillis() - startTime)/1000;
						log.info("FileWritter finished in " + durationInSec + "sec");
						setStatus(STATUS_FILE_WRITTEN);
						appendTestResult("WRITTER: loop=" + loop + ", file written from node " + CoordinatorManager.getInstance().getCoordinator().getNodeId() + " in " + durationInSec + "sec" );
						CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new FileSystemTestEvent(FileSystemTestEvent.COMMAND_FILE_WRITTEN, loop, maxNbrDirs, maxNbrFiles, durationInSec, fileSystemTestForm.isCheckWithRetriesEnabled()), ORES_FILESYSTEMTEST);
					}
				} catch (RuntimeException e) {
					log.error("Error while writter-thread was working", e);
				}
			}
		};
		taskExecutorManager.execute(fileWritterThread);
	}


	private void setStatus(String status) {
		testStatus = status;
		myContent.contextPut("teststatus", testStatus);
	}


	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == fileSystemTestForm) {
			loops       = fileSystemTestForm.getLoops();
			maxNbrDirs  = fileSystemTestForm.getMaxNbrDirs();
			maxNbrFiles = fileSystemTestForm.getMaxNbrFiles();
			nbrCharInFile = fileSystemTestForm.getNbrCharInFile();
		}
	}

	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, ORES_FILESYSTEMTEST);
	}


	public void event(Event event) {
		log.debug("Event=" + event);
		if (event instanceof FileSystemTestEvent) {
			FileSystemTestEvent fileSystemTestEvent = (FileSystemTestEvent)event;
			if (fileSystemTestEvent.getCommand().equalsIgnoreCase(FileSystemTestEvent.COMMAND_FILE_WRITTEN)) {
				startCheckerThread(fileSystemTestEvent.getLoop(), fileSystemTestEvent.getMaxNbrDirs(), fileSystemTestEvent.getMaxNbrFiles(), fileSystemTestEvent.isCheckWithRetriesEnabled());
			} else if (fileSystemTestEvent.getCommand().equalsIgnoreCase(FileSystemTestEvent.COMMAND_FILE_CHECKED)) {
				if (fileSystemTestEvent.isFileCheckOk() ) {
					appendTestResult("CHECKER: Test OK     loop=" + fileSystemTestEvent.getLoop() + " on node=" + fileSystemTestEvent.getNodeId() + " (found all dirs and files on this node in " + fileSystemTestEvent.getDuration() + "sec)");
				} else {
					appendTestResult("CHECKER: Test FAILED loop=" + fileSystemTestEvent.getLoop() + " on node=" + fileSystemTestEvent.getNodeId() + " (missing dirs or files on this node " + fileSystemTestEvent.getDuration() + "sec)");
				}
			}
		} else {
			log.warn("Receive Unkonwn event=" + event);
		}
		
	}


	private void appendTestResult(String result) {
		testResults.append(result);
		testResults.append("<br>");
		myContent.contextPut("testresults", testResults);
	}


	private void startCheckerThread(final int loop, final int maxNbrDirs, final int maxNbrFiles, final boolean isCheckWithRetriesEnabled) {
		Runnable fileWritterThread = new Runnable() {
			public void run() {
				long startTime = System.currentTimeMillis();
				setStatus(STATUS_CHECKING);
				boolean fileCheckOk = true;
				try {
				  // loop over dir
					for(int dirNr=1; dirNr <= maxNbrDirs; dirNr++ ) {
						String currentFullDirPathName = testBaseDir + File.separator + TEST_DIRNAME_PREFIX + loop + "-" + dirNr;
						File testDir = new File(currentFullDirPathName);
						if (!checkFileExists(testDir, isCheckWithRetriesEnabled)) {
							log.error("FileCheck ERROR: Dir=" + currentFullDirPathName + " does NOT exists" );
							fileCheckOk = false;
						} else {
							// ok dir exist, check all files
							// loop over file
							for(int fileNr=1; fileNr <= maxNbrFiles; fileNr++ ) {
								String currentFileName = TEST_FILENAME_PREFIX + dirNr + "-" + fileNr + ".txt";
								String filePath = currentFullDirPathName + File.separator + currentFileName;
								File testFile = new File(filePath);
								if (!checkFileExists(testFile, isCheckWithRetriesEnabled)) {
									log.error("FileCheck ERROR: File=" + filePath + " does NOT exists" );
									fileCheckOk = false;
								}
							}
						}
					}
					long durationInSec = (System.currentTimeMillis() - startTime)/1000;
					log.info("FileCheck loop=" + loop + " finished in " + durationInSec + "sec for #dir=" + maxNbrDirs + " #files pro dir=" + maxNbrFiles);
					setStatus(STATUS_FILE_CHECKED);
					CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new FileSystemTestEvent(FileSystemTestEvent.COMMAND_FILE_CHECKED, CoordinatorManager.getInstance().getCoordinator().getNodeId(), loop, fileCheckOk, durationInSec), ORES_FILESYSTEMTEST);
				} catch (RuntimeException e) {
					log.error("Error while trying to close instant messaging connection", e);
				}
			}
		};
		taskExecutorManager.execute(fileWritterThread);
	}


	protected boolean checkFileExists(File testFile, boolean checkWithRetriesEnabled) {
		if (testFile.exists()) {
			return true;
		}
		log.info("check with retries");
		long startTime = System.currentTimeMillis();
		int maxRetries = 20;
		int loopCounter = 1;
		while (checkWithRetriesEnabled && loopCounter++ < maxRetries) {
			try {
				Thread.sleep(loopCounter * 100);
			} catch (InterruptedException e) {
				log.error("Exception in checkFileExists, " + e);
			}
			if (testFile.exists()) {
				long duration = System.currentTimeMillis() - startTime;
				log.warn("Found file=" + testFile + " after reties, #retries=" + loopCounter + " , " + duration + "msec");
				return true;
			}
		}
		if (loopCounter >= maxRetries) {
			long duration = System.currentTimeMillis() - startTime;
			log.error("Did not found file=" + testFile + " after max #retries=" + loopCounter + " , " + duration + "msec");
		}
		return false;
	}
	
}