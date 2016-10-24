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
package de.bps.onyx.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti.QTIResultSet;
import org.olat.repository.RepositoryEntry;

import de.bps.onyx.plugin.OnyxModule;
import de.bps.onyx.plugin.OnyxResultManager;
import de.bps.onyx.plugin.wsclient.OnyxExamMode;
import de.bps.onyx.plugin.wsclient.OnyxExamModeService;
import de.bps.onyx.plugin.wsserver.MapWrapper;
import de.bps.onyx.plugin.wsserver.StudentIdsWrapper;
import de.bps.onyx.plugin.wsserver.TestState;
import de.bps.onyx.plugin.wsserver.TraineeStatusService;
import de.bps.webservices.clients.onyxreporter.OnyxReporterConnectorFileNameFilter;

public class ExamPool implements Serializable {
	private static final long serialVersionUID = 4771377056688023915L;

	private static final String PARAM_FIRSTNAME = "firstname";
	private static final String PARAM_LASTNAME = "lastname";
	private static final String PARAM_LANGUAGE = "language";
	private static final String PARAM_STATUS = "status";

	private static final String IS_SURVEY = "isSurvey";
	private static final String IS_SYNCHRONIZED = "isSynchronized";
	private static final String SHOW_SOLUTION = "showSolution";
	public static final String CONTINUATION_ALLOWED = "continuationAllowed";
	public static final String SUSPENSION_ALLOWED = "suspendAllowed";
	public static final String TEMPLATE_ID = "templateid";

	private transient final static OLog log = Tracing.createLoggerFor(ExamPool.class);

	//mapping for key : assessmentId value: resultSet
	private final Map<Long, Identity> assessmentIdentityMapping = new ConcurrentHashMap<Long, Identity>();
	//mapping for key: identityId value:assessmentId
	private final Map<Long, Long> identityAssessmentMapping = new ConcurrentHashMap<Long, Long>();
	//mapping for key: identity value: state
	private final Map<Identity, TestState> studentStates = new ConcurrentHashMap<Identity, TestState>();

	private final Long testSessionId;

	//	private final Boolean sessionInitializied;

	private transient final OnyxExamModeService service;
	private transient ICourse referencedCourse;
	private transient CourseNode referencedCourseNode;

	//private String nodeIdent;
	private Long courseId;

	private final File directory = new File(WebappHelper.getUserDataRoot());

	ExamPool(Long testSessionId) {
		super();
		//this.course = course;
		this.testSessionId = testSessionId;
		OnyxExamMode examMode = new OnyxExamMode();
		service = examMode.getOnyxExamModeServicesPort();
	}

	/**
	 * This method registers a new exam at the onyx-exam-service, it should be
	 * only called by the {@link ExamPoolManager} and not the proxies
	 * 
	 * @param course
	 * @param courseNode
	 */
	void initExamPool(ICourse course, CourseNode courseNode) {
		RepositoryEntry entry = courseNode.getReferencedRepositoryEntry();
		this.referencedCourse = course;
		this.referencedCourseNode = courseNode;

		//nodeIdent = courseNode.getIdent();
		courseId = course.getResourceableId();

		if (entry != null) {
			byte[] contentPackage = getContentPackage(entry);
			HashMap<String, String> parameterMap = new HashMap<String, String>();
			// set allowShowSolution either to the configured value (!= null) or to defaultvalue false if test or survey, if selftest then the default is true 
			Boolean allowShowSolution = courseNode.getModuleConfiguration().getBooleanEntry(IQEditController.CONFIG_KEY_ALLOW_SHOW_SOLUTION);
			allowShowSolution = allowShowSolution != null ? allowShowSolution : false;
			parameterMap.put(SHOW_SOLUTION, String.valueOf(allowShowSolution));
//			Boolean showFeedback = (Boolean) courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_ALLOW_SHOW_FEEDBACK);
//			showFeedback = showFeedback != null ? showFeedback : false;
//			parameterMap.put(SHOW_FEEDBACK, String.valueOf(showFeedback));
			parameterMap.put(IS_SURVEY, String.valueOf(referencedCourseNode instanceof IQSURVCourseNode));
			Boolean examControl = courseNode.getModuleConfiguration().getBooleanEntry(ExamPoolManager.CONFIG_KEY_EXAM_CONTROL);
			examControl = examControl != null ? examControl : Boolean.FALSE;
			//couple the synchronizedStart again to the examMode
			Boolean synchronizedStart = (Boolean) courseNode.getModuleConfiguration().get(ExamPoolManager.CONFIG_KEY_EXAM_CONTROL_SYNCHRONIZED_START);
			synchronizedStart = synchronizedStart != null ? synchronizedStart && examControl : false;
			parameterMap.put(IS_SYNCHRONIZED, String.valueOf(synchronizedStart));
			parameterMap.put(CONTINUATION_ALLOWED, Boolean.toString(!examControl.booleanValue()));
			Boolean allowSuspension = courseNode.getModuleConfiguration().getBooleanEntry(IQEditController.CONFIG_KEY_ALLOW_SUSPENSION_ALLOWED);
			allowSuspension = allowSuspension != null ? allowSuspension : false;
			parameterMap.put(SUSPENSION_ALLOWED, String.valueOf(allowSuspension));

			String templateId = courseNode.getModuleConfiguration().getStringValue(IQEditController.CONFIG_KEY_TEMPLATE);
			if (templateId != null) {
				parameterMap.put(TEMPLATE_ID, templateId);
			}

			MapWrapper wrapper = new MapWrapper();
			wrapper.setMap(parameterMap);
			String providerId = CoreSpringFactory.getImpl(OnyxModule.class).getConfigName();
			Long result = service.registerTest(testSessionId, providerId, contentPackage, wrapper);
			log.info("Init result : " + TestState.getState(result) + " for " + testSessionId + " , provider " + providerId + " and parameters " + parameterMap);
		} else {
			log.warn("unable to register new test, no test in course-node");
		}
		
		refreshGroups();
	}
	
	private void refreshGroups() {
		if (referencedCourse == null) {
			referencedCourse = CourseFactory.loadCourse(courseId);
		}
		CourseGroupManager man = referencedCourse.getCourseEnvironment().getCourseGroupManager();
		List<Identity> participants = man.getParticipantsFromBusinessGroups();
		for (Identity participant : participants) {
			if (!studentStates.containsKey(participant)) {
				addStudent(participant, null);
			}
		}
	}

	void addStudent(Identity student, TestState state) {
		studentStates.put(student, state != null ? state : TestState.NOT_ENTERED);
	}

	Long registerStudentTest(Identity student, QTIResultSet resultSet, TestState state) {
		Long result = null;

		addStudent(student, (state != null ? state : TestState.WAITING));

		//map this assessment to it's id
		assessmentIdentityMapping.put(resultSet.getAssessmentID(), student);
		//map this student to his / her assessment
		identityAssessmentMapping.put(student.getKey(), resultSet.getAssessmentID());

		HashMap<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put(PARAM_LANGUAGE, student.getUser().getPreferences().getLanguage());
		parameterMap.put(PARAM_FIRSTNAME, student.getUser().getProperty(UserConstants.FIRSTNAME, null));
		parameterMap.put(PARAM_LASTNAME, student.getUser().getProperty(UserConstants.LASTNAME, null));

		byte[] recommitedFiles = new byte[0];
		if (resultSet.getSuspended()) {
			log.info("Try to recreate for student " + student.getName() + " and suspended assessment : " + resultSet.getAssessmentID());
			//addStudent(student, TestState.SUSPENDED);
			parameterMap.put(PARAM_STATUS, String.valueOf(TestState.RESUME_SUSPENDED.getValue()));
			String assessmentType = referencedCourseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString();
			String path = null;

			Boolean isSurvey = false;
			File xml = null;
			if (referencedCourseNode instanceof IQSURVCourseNode) {
				isSurvey = true;
			}
			if (isSurvey) {
				OlatRootFolderImpl courseRootContainer = referencedCourse.getCourseEnvironment().getCourseBaseContainer();
				path = courseRootContainer.getBasefile() + File.separator + referencedCourseNode.getIdent() + File.separator;
				xml = new File(path);
			} else {
				path = OnyxResultManager.getResReporting() + File.separator + student.getName() + File.separator + assessmentType + File.separator;
				xml = new File(directory, path);
			}

			if (xml != null && xml.exists()) {
				File commitMe = null;
				File[] allXmls = xml.listFiles(new OnyxReporterConnectorFileNameFilter(referencedCourseNode.getIdent(), String.valueOf(resultSet
						.getAssessmentID())));
				if (allXmls != null && allXmls.length > 0) {
					for (File file : allXmls) {
						if (file.isFile()) {
							if(commitMe == null || file.getName().toLowerCase().endsWith(".zip")){
								commitMe = file;
							}
						}
					}
				}
				if (commitMe != null) {
					Long fileLength = commitMe.length();
					recommitedFiles = new byte[fileLength.intValue()];
					java.io.FileInputStream inp = null;
					try {
						inp = new java.io.FileInputStream(commitMe);
						inp.read(recommitedFiles);
						log.info("Found file for suspended assessment for student " + student.getName() + " and suspended assessment : "
								+ resultSet.getAssessmentID() + " # " + commitMe.getAbsolutePath() + "; lenght " + recommitedFiles.length);
					} catch (FileNotFoundException e) {
						log.error("Missing file: " + commitMe.getAbsolutePath(), e);
					} catch (IOException e) {
						log.error("Error copying file: " + commitMe.getAbsolutePath(), e);
					} finally {
						IOUtils.closeQuietly(inp);
					}
				} else {
					log.info("Did not find files for suspended assessment for student " + student.getName() + " and suspended assessment : "
							+ resultSet.getAssessmentID() + " at " + xml.getAbsolutePath() + " an filter-options " + referencedCourseNode.getIdent() + " and "
							+ resultSet.getAssessmentID());
				}
			} else {
				log.info("Did not find resreporting folder for student " + student.getName() + " and suspended assessment : " + resultSet.getAssessmentID()
						+ " at " + xml.getAbsolutePath());
			}
		} else {
			log.info("Assessment not suspended, nothing to restore.");
		}

		MapWrapper parameterWrapper = new MapWrapper();
		parameterWrapper.setMap(parameterMap);

		result = service.registerStudent(testSessionId, resultSet.getAssessmentID(), recommitedFiles, parameterWrapper);
		log.info("Tried to register student with assessmentId" + resultSet.getAssessmentID() + " to test: " + testSessionId + " resulting in : "
				+ TestState.getState(result));
		return result;
	}

	QTIResultSet getAssessmentForStudent(Identity identity) {
		QTIResultSet result = null;
		Long assessmentId = identityAssessmentMapping.get(identity.getKey());
		if (assessmentId != null) {
			result = OnyxResultManager.getResultSet(assessmentId);
		}
		return result;
	}

	/**
	 * Use this method to modify the currently hold states of the students, this
	 * method should be called by the {@link TraineeStatusService} to keep the
	 * shown states in sync with their states in the test
	 * 
	 * @param identities
	 * @param state
	 */
	void changeExamState(List<Identity> identities, TestState state) {
		for (Identity identity : identities) {
			changeExamState(identity, state);
		}
	}

	/**
	 * Use this method to modify the currently hold state of the student, this
	 * method should be called by the {@link TraineeStatusService} to keep the
	 * shown states in sync with their states in the test
	 * 
	 * @param identities
	 * @param state
	 */
	void changeExamState(Identity identity, TestState state) {
		if (studentStates.containsKey(identity)) {
			studentStates.put(identity, state);
		}
	}

	/**
	 * Use this method to send the requested state-changes for the given users
	 * to the test. The effects of this call will then be set by the TestPlayer
	 * (Onyx) with the help of the {@link TraineeStatusService}
	 * 
	 * @param identities
	 * @param state
	 */
	void controllExam(List<Identity> identities, TestState state) {
		StudentIdsWrapper idWrapper = new StudentIdsWrapper();
		ArrayList<Long> assessmentIds = new ArrayList<Long>();
		for (Identity identity : identities) {
			Long assessmentId = identityAssessmentMapping.get(identity.getKey());
			assessmentIds.add(assessmentId);
		}

		idWrapper.setStudentsIds(assessmentIds);

		HashMap<String, String> parameterMap = new HashMap<String, String>();

		parameterMap.put("addTime", TestState.RESUME_ALLOWED == state ? String.valueOf(10) : String.valueOf(0));

		MapWrapper parameterWrapper = new MapWrapper();
		parameterWrapper.setMap(parameterMap);

		Long result = service.testControl(testSessionId, idWrapper, state.getValue(), parameterWrapper);
		log.info("Tried to control exam : " + testSessionId + " for " + assessmentIds + " and state " + state + " and parameters " + parameterMap
				+ " resulting in " + TestState.getState(result));
	}

	TestState getStudentState(Identity student) {
		return studentStates.get(student);
	}

	Map<Identity, TestState> getStudentStates() {
		refreshGroups();
		return studentStates;
	}

	Identity getStudentForAssessment(Long assessmentId) {
		Identity student = assessmentId != null ? assessmentIdentityMapping.get(assessmentId) : null;
		return student;
	}

	Long getTestSessionId() {
		return testSessionId;
	}

	private byte[] getContentPackage(RepositoryEntry repositoryEntry) {
		File cpFile = FileResourceManager.getInstance().getFileResource(repositoryEntry.getOlatResource());

		if (cpFile == null || !cpFile.exists()) {
			cpFile = getCP(repositoryEntry);
		}

		Long fileLength = cpFile.length();
		byte[] contentPackage = new byte[fileLength.intValue()];

		java.io.FileInputStream inp = null;
		try {
			inp = new java.io.FileInputStream(cpFile);
			inp.read(contentPackage);
		} catch (FileNotFoundException e) {
			log.error("Missing file: " + cpFile.getAbsolutePath(), e);
		} catch (IOException e) {
			log.error("Error copying file: " + cpFile.getAbsolutePath(), e);
		} finally {
			IOUtils.closeQuietly(inp);
		}

		return contentPackage;
	}

	/**
	 * Generates a file object for the given re.
	 * 
	 * @param repositoryEntry
	 * @return
	 */
	private File getCP(RepositoryEntry repositoryEntry) {
		//get content-package (= onyx test zip-file)
		OLATResourceable fileResource = repositoryEntry.getOlatResource();
		String unzipedDir = FileResourceManager.getInstance().unzipFileResource(fileResource).getAbsolutePath();
		String zipdirName = FileResourceManager.ZIPDIR;
		String testName = repositoryEntry.getResourcename();
		String pathToFile = unzipedDir.substring(0, unzipedDir.indexOf(zipdirName));
		File onyxTestZip = new File(pathToFile + testName);
		// <OLATCE-499>
		if (!onyxTestZip.exists()) {
			onyxTestZip = new File(pathToFile + "repo.zip");
		}
		// </OLATCE-499>
		return onyxTestZip;
	}

}
/*
history:

$Log: ExamPool.java,v $
Revision 1.19  2012-05-30 09:16:14  blaw
OLATCE-2007
* allow resume of suspended surveys
* hidde suspended test-tries in reporter-overview

Revision 1.18  2012-05-16 13:30:34  blaw
OLATCE-2007
* improved resume of suspended tests

Revision 1.17  2012-05-15 14:11:04  blaw
OLATCE-2021
* catch all exceptions, log them and then throw them back to onyx in returnwsservice
* backup the result-file if errors occurred
* refactored onyx-reporter-requests into a new background-task
* more load/update cycles for qtiresultsets while saving them

Revision 1.16  2012-05-09 16:03:48  blaw
OLATCE-2007
* allow suspend and resume of tests

Revision 1.15  2012-05-07 13:12:51  laeb
OPEN - issue OLATCE-2009: Unterbrechen: Konfiguration "Unterbrechen erlauben" für Onyx-Tests freischalten
https://www.bps-system.de/devel/browse/OLATCE-2009
* new test config "suspensionAllowed"

Revision 1.14  2012-04-25 13:56:53  blaw
OLATCE-1968
* refresh groups, if new groups or users were added to the course after first registration of the exam

Revision 1.13  2012-04-10 15:04:38  laeb
OLATCE-1980 OnyxExamModeService: Um Nutzerdaten erweitern: Vor- und Zuname Student
* added firstname and lastname properties to Onyx registerStudent WS call parameters

Revision 1.12  2012-04-10 13:57:48  blaw
OLATCE-1425
* more logging

Revision 1.11  2012-04-05 13:49:41  blaw
OLATCE-1425
* added history
* better indention
* refactored referencess for ExamPoolManagers to the abstract class
* added yesNoDialog for StartExam-function
* added more gui-warnings and / or fallback-values if student- or exam-values are not available


*/
