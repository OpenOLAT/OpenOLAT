
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

import java.io.File;
import java.io.FileFilter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.olat.commons.lifecycle.LifeCycleManager;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti.QTIResultSet;

import de.bps.webservices.clients.onyxreporter.OnyxReporterConnector;
import de.bps.webservices.clients.onyxreporter.OnyxReporterException;

/**
 * @author Ingmar Kroll
 */
public class OnyxResultManager {
	//<ONYX-705>
	private static final String PASS = "pass";
	private static final String SCORE = "score";
	//</ONYX-705>
	
	private static final String RES_REPORTING = "resreporting";
	
	private static final String REPORTER_NOT_FINISHED = "reporter_not_finshed";

	public static String getResReporting() {
		return RES_REPORTING;
	}
	
	public static OLog LOGGER = Tracing.createLoggerFor(OnyxResultManager.class);

	public static void persistOnyxResults(QTIResultSet qtiResultSet, String resultfile) {

		//if onyx was started from learningressources or bookmark no results are persisted
		if (qtiResultSet == null) {
			LOGGER.info("persit onyx result: qtiResultSet is null!!!");
			return;
		}

		//Get course and course node
		ICourse course = CourseFactory.loadCourse(qtiResultSet.getOlatResource());
		CourseNode courseNode = course.getRunStructure().getNode(qtiResultSet.getOlatResourceDetail());

		Boolean isSurvey = false;
		// <OLATBPS-363>
		if (!courseNode.getClass().equals(IQTESTCourseNode.class) && !courseNode.getClass().equals(IQSELFCourseNode.class)) {
		// </OLATBPS-363>
			isSurvey = true;
		}
		LOGGER.info("persit onyx result: identiyname=" + qtiResultSet.getIdentity().getName() + "  nodeident=" + courseNode.getIdent() + "  resultfile="
				+ resultfile);
		String path = null;
		if (isSurvey) {
			OlatRootFolderImpl courseRootContainer = course.getCourseEnvironment().getCourseBaseContainer();
			path = courseRootContainer.getBasefile() + File.separator + courseNode.getIdent() + File.separator;
		} else {
			path = WebappHelper.getUserDataRoot() + File.separator + RES_REPORTING + File.separator + qtiResultSet.getIdentity().getName()
				+ File.separator + courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString() + File.separator;
		}
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File resultfileF = new File(resultfile);
		File resultfileUnzippedDir = new File(resultfileF.getAbsolutePath().substring(0, resultfileF.getAbsolutePath().length() - 4) + "__unzipped");
		if (!resultfileUnzippedDir.exists()) {
			resultfileUnzippedDir.mkdir();
		}
		ZipUtil.unzip(resultfileF, resultfileUnzippedDir);
		File[] results = resultfileUnzippedDir.listFiles(new FileFilter() {
			public boolean accept(java.io.File result) {
				return result.getName().toLowerCase().endsWith("xml")&&result.getName().toLowerCase().startsWith("result");
			}
		});

		if (results==null || results.length != 1) { throw new UnsupportedOperationException("onyx result zip contains not exactly 1 result file"); }
		File result = results[0];
		//add onyx session id (assessment id in qtiresultset table) to identify the different test attempts
		File file_s = new File(path + courseNode.getIdent() + "v" + qtiResultSet.getAssessmentID() + ".xml");
		//result.copyTo(file_s);
		FileUtils.copyFileToFile(result, file_s, false);
		result.delete();
		resultfileF.delete();
		resultfileUnzippedDir.delete();

		//if this is a onyx survey we are done here
		if (isSurvey) {
			return;
		}

		//before asking onyxReporter for resultsets, save the QTIResultSet with the flag "reporterFinsished = false"
		qtiResultSet = (QTIResultSet) DBFactory.getInstance().loadObject(qtiResultSet);
		qtiResultSet.setLastModified(new Date());
		try {
			DBFactory.getInstance().saveObject(qtiResultSet);
		} catch (Exception e) {
			LOGGER.error("Unable to initialy save the QTIResultSet after finishing Onyx Test.", e);
		}

		performOnyxReport(qtiResultSet, file_s);
	}

	public static String getUniqueId(Identity identity, CourseNode node,
			UserCourseEnvironment userCourseEnv) {
		String uId = String.valueOf(CodeHelper.getGlobalForeverUniqueID().hashCode());

		QTIResultSet qtiResultSet = new QTIResultSet();
		qtiResultSet.setAssessmentID(Long.parseLong(uId));
		qtiResultSet.setOlatResource(userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		qtiResultSet.setOlatResourceDetail(node.getIdent());
		qtiResultSet.setRepositoryRef(node.getReferencedRepositoryEntry().getKey().longValue());
		qtiResultSet.setIdentity(identity);
		qtiResultSet.setQtiType(1);
		qtiResultSet.setLastModified(new Date());
		DBFactory.getInstance().saveObject(qtiResultSet);
		DBFactory.getInstance().commit();

		return uId;
	}

	public static String getUniqueIdForShowOnly() {
		String uId = String.valueOf(CodeHelper.getGlobalForeverUniqueID().hashCode());
		return uId;
	}

	public static QTIResultSet getResultSet(long uniqueId)	{
		List<QTIResultSet> liste = getResultSetByAssassmentId(uniqueId);
		QTIResultSet qtiResultSet = null;
		if (liste != null && liste.size() > 0) {
			qtiResultSet = liste.get(0);
		}
		return qtiResultSet;
	}

	private static List<QTIResultSet> getResultSetByAssassmentId(Long assessmentID) {
		DB db = DBFactory.getInstance();

		StringBuilder slct = new StringBuilder();
		slct.append("select rset from ");
		slct.append("org.olat.ims.qti.QTIResultSet rset ");
		slct.append("where ");
		slct.append("rset.assessmentID=? ");

		return db.find(slct.toString(), new Object[] { assessmentID }, new Type[] { StandardBasicTypes.LONG });
	}

	/**
	 * Ask the Onyx Reporter for a result.xml that has allready been saved but the reporter was not finished.
	 * This is called by a nightly job.
	 * @param qtiResultSet
	 */
	private static boolean performOnyxReport(QTIResultSet qtiResultSet) {
		return performOnyxReport(qtiResultSet, null);
	}

	/**
	 * Ask the Onyx Reporter with a given file and save the results to db.
	 * @param qtiResultSet
	 * @param file_s
	 */
	private static boolean performOnyxReport(QTIResultSet qtiResultSet, File file_s) {
		
		boolean reporterFinsished = true;
		
		//Get course and course node
		ICourse course = CourseFactory.loadCourse(qtiResultSet.getOlatResource());
		CourseNode courseNode = course.getRunStructure().getNode(qtiResultSet.getOlatResourceDetail());

		//<OLATCE-1048> SelfTests and Surveys are not AssessableCourseNode --> no assessments saved --> Switched to AbstractAssessableCourseNode
		AbstractAccessableCourseNode node = null;
		if (courseNode instanceof AbstractAccessableCourseNode) {
			node = (AbstractAccessableCourseNode) courseNode;
		} else {
			LOGGER.warn("Tried to perform an OnyxReport with a non-assessable course node! "+(courseNode!=null?(courseNode.getShortName()+" Class: "+courseNode.getClass()):"NULL"));
		}
		//</OLATCE-1048>
		//<ONYX-705>
		Map<String, String> results = null;
		//</ONYX-705>
		//if no file was given use the qtiresultset to get the file
		if (file_s == null) {
			String path = WebappHelper.getUserDataRoot() + File.separator + RES_REPORTING + File.separator + qtiResultSet.getIdentity().getName()
			+ File.separator + courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString() + File.separator;
			file_s = new File(path + courseNode.getIdent() + "v" + qtiResultSet.getAssessmentID() + ".xml");
			if (!file_s.exists()) {
				LOGGER.error("performOnyxReport was called but no result.xml exists with path: " + file_s.getAbsolutePath());
				return false;
			}
		}

		try {
		//<ONYX-705>
			OnyxReporterConnector onyxReporter = new OnyxReporterConnector();
			
			// <OLATBPS-363>
				results = onyxReporter.getResults(file_s, node, qtiResultSet.getIdentity());
			// </OLATBPS-363>
		} catch (OnyxReporterException e) {
		//</ONYX-705>
			LifeCycleManager.createInstanceFor(qtiResultSet).markTimestampFor(REPORTER_NOT_FINISHED);
			reporterFinsished = false;
			LOGGER.warn("OnyxReporter was unreachable during get the results. An entry in Lifecyclemanager is done and the report will be finshed with a job.");
		}

		String score = null, passed = null;
		
		//<ONYX-705>
		for (String vars : results.keySet()) {
			// only testoutcomes "score" and "passed" are stored at olat db
			if (SCORE.equalsIgnoreCase(vars)) {
				score = results.get(vars);
			}	else if (PASS.equalsIgnoreCase(vars)) {
					passed = results.get(vars);
			} else {
				LOGGER.info("TestOutCome "+results.get(vars)+ " is not stored in OLAT DB");
			}
		}
		//</ONYX-705>
		if (score != null || passed != null) {
			if (score != null) {
				qtiResultSet.setScore(Float.valueOf(score));
				//if own cutvalue for passed is configured use this instead of the PASS variable from onyx test.
				if (courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_CUTVALUE) != null) {
					// <OLATBPS-508>
					if (Float.valueOf(score) >= ((Float) courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_CUTVALUE))) {
					// </OLATBPS-508>
						passed = "true";
					}
				}
			}
			if (passed != null) {
				qtiResultSet.setIsPassed(Boolean.valueOf(passed));
			}
		}
		qtiResultSet.setLastModified(new Date());
		DBFactory.getInstance().saveObject(qtiResultSet);
		DBFactory.getInstance().commit();
		return reporterFinsished;
	}

	/**
	 * This is called by a nightly job: update all resultsets where the Onyx Reporter has not finished yet.
	 * (maybe because the reporter was not available).
	 */
	public static void updateOnyxResults() {
		List<QTIResultSet> liste = findResultSets();
		for (QTIResultSet qTIResultSet : liste) {
			LifeCycleManager lcm =  null;
			if (qTIResultSet != null) {
				lcm = LifeCycleManager.createInstanceFor(qTIResultSet);
			}
			if (lcm != null && lcm.lookupLifeCycleEntry(REPORTER_NOT_FINISHED) != null) {
				if (performOnyxReport(qTIResultSet)) {
					lcm.deleteAllEntriesForPersistentObject();
				}
			}
		}
 }
	
	public static List findResultSets() {
		DB db = DBFactory.getInstance();

		StringBuilder slct = new StringBuilder();
		slct.append("select rset from ");
		slct.append("org.olat.ims.qti.QTIResultSet rset ");
		
		return db.find(slct.toString());
	}
}