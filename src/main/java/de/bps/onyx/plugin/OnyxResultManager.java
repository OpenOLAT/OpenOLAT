
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
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.ims.qti.QTIResultSet;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;

import de.bps.webservices.clients.onyxreporter.OnyxReporterConnector;
import de.bps.webservices.clients.onyxreporter.OnyxReporterException;

/**
 * @author Ingmar Kroll
 */
public class OnyxResultManager {
	//<ONYX-705>
	public static final String PASS = "pass";
	public static final String SCORE = "score";
	//</ONYX-705>
	
	public static final String SUFFIX_ZIP = ".zip";
	public static final String SUFFIX_XML = ".xml";
	private static final String RES_REPORTING = "resreporting";

	private static final String REPORTER_NOT_FINISHED = "reporter_not_finshed";
	
	public final static long IGNORE_PREVIEW_CASE = -1l;

	public static String getResReporting() {
		return RES_REPORTING;
	}
	
	public static OLog LOGGER = Tracing.createLoggerFor(OnyxResultManager.class);

	public static void persistOnyxResults(QTIResultSet qtiResultSet, final String resultfile) {

		//if onyx was started from learningressources or bookmark no results are persisted
		if (qtiResultSet == null) {
			LOGGER.info("persit onyx result: qtiResultSet is null!!!");
			return;
		}

		// Get course and course node
		final ICourse course = CourseFactory.loadCourse(qtiResultSet.getOlatResource());
		final CourseNode courseNode = course.getRunStructure().getNode(qtiResultSet.getOlatResourceDetail());

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
			final OlatRootFolderImpl courseRootContainer = course.getCourseEnvironment().getCourseBaseContainer();
			path = courseRootContainer.getBasefile() + File.separator + courseNode.getIdent() + File.separator;
		} else {
			path = WebappHelper.getUserDataRoot() + File.separator + RES_REPORTING + File.separator + qtiResultSet.getIdentity().getName() + File.separator
					+ courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString() + File.separator;
		}
		final File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		final File resultfileF = new File(resultfile);
		final File resultfileUnzippedDir = new File(resultfileF.getAbsolutePath().substring(0, resultfileF.getAbsolutePath().length() - 4) + "__unzipped");
		if (!resultfileUnzippedDir.exists()) {
			resultfileUnzippedDir.mkdir();
		}
		ZipUtil.unzip(resultfileF, resultfileUnzippedDir);
		final File[] results = resultfileUnzippedDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(java.io.File result) {
				return result.getName().toLowerCase().startsWith("result");
			}
		});

		if (results == null || results.length < 1) {
			throw new UnsupportedOperationException("Onyx result zip does not contain exactly 1 result file");
		}
		File file_s = null;
		for (File result : results) {
			final String name = result.getName();
			LOGGER.debug("Found file: " + name);
			String suffix = SUFFIX_XML;
			if (name != null) {
				int i = name.lastIndexOf('.');
				if (i >= 0) {
					suffix = name.substring(i);
				}
				LOGGER.debug("Using suffix: " + suffix);
			}

			//add onyx session id (assessment id in qtiresultset table) to identify the different test attempts
			final String prefix = getResultsFilenamePrefix(path, courseNode, qtiResultSet.getAssessmentID());
			final File file = new File(prefix + suffix);
			if (SUFFIX_ZIP.equals(suffix)) {
				// the result file to use with result set
				file_s = file;
			} else if (file_s == null) {
				// the xml file to use with result set
				// only take XML instead of ZIP file, if no ZIP file found already
				file_s = file;
			}
			//result.copyTo(file_s);
			FileUtils.copyFileToFile(result, file, false);
			result.delete();
		}
		
		resultfileUnzippedDir.delete();

		//if this is a onyx survey we are done here
		if (isSurvey) {
			return;
		} else {
			// before asking onyxReporter for resultsets, save the QTIResultSet
			// with the flag "reporterFinsished = false"
			qtiResultSet = (QTIResultSet) DBFactory.getInstance().loadObject(qtiResultSet);
			qtiResultSet.setLastModified(new Date());
			try {
				DBFactory.getInstance().updateObject(qtiResultSet);
			} catch (Exception e) {
				LOGGER.error("Unable to initialy save the QTIResultSet after finishing Onyx Test.", e);
			}

			// create an identenv with no roles, no attributes, no locale
			IdentityEnvironment ienv = new IdentityEnvironment();
			ienv.setIdentity(qtiResultSet.getIdentity());
			UserCourseEnvironment userCourseEnvironment = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());

			boolean reporterFinished = false;
			try {
				reporterFinished = OnyxResultManager.performOnyxReport(qtiResultSet);
			} catch (Exception e) {
				LOGGER.error("unable to to finish ReporterTask", e);
			}

			qtiResultSet = (QTIResultSet) DBFactory.getInstance().loadObject(qtiResultSet);
			QTICourseNode node = (QTICourseNode) (courseNode instanceof QTICourseNode ? courseNode : null);
			if (reporterFinished && !qtiResultSet.getSuspended() && node != null) {
				boolean bestResultConfigured = false;
				ScoreEvaluation sc = OnyxModule.getUserScoreEvaluationFromQtiResult(userCourseEnvironment.getCourseEnvironment().getCourseResourceableId(), node,
						bestResultConfigured, qtiResultSet.getIdentity());
				if(node instanceof AssessableCourseNode){
					((AssessableCourseNode) node).updateUserScoreEvaluation(sc, userCourseEnvironment, qtiResultSet.getIdentity(), false, Role.coach);
				}
				
			} else {
				LOGGER.info("Won't update ScoreEvaluation for user for resultKey: " + qtiResultSet.getKey() + " assessmentId: " + qtiResultSet.getAssessmentID()
						+ "; reporterFinished: " + reporterFinished + "; suspended: " + qtiResultSet.getSuspended());
			}
			qtiResultSet = null;
			DBFactory.getInstance().commitAndCloseSession();
		}
	}

	public static QTIResultSet createQTIResultSet(Identity identity, CourseNode node, Long olatResourceId, Long assessmentId) {
		QTIResultSet qtiResultSet = new QTIResultSet();
		qtiResultSet.setAssessmentID(assessmentId);
		qtiResultSet.setOlatResource(olatResourceId);
		qtiResultSet.setOlatResourceDetail(node.getIdent());
		qtiResultSet.setRepositoryRef(node.getReferencedRepositoryEntry().getKey().longValue());
		qtiResultSet.setIdentity(identity);
		qtiResultSet.setQtiType(1);
		qtiResultSet.setLastModified(new Date());
		DBFactory.getInstance().saveObject(qtiResultSet);
		DBFactory.getInstance().commitAndCloseSession();
		qtiResultSet = (QTIResultSet) DBFactory.getInstance().loadObject(qtiResultSet, true);

		return qtiResultSet;
	}

	public static String getUniqueIdForShowOnly(Identity identity, RepositoryEntry entry) {
		final String uId = String.valueOf(CodeHelper.getGlobalForeverUniqueID().hashCode());

		QTIResultSet qtiResultSet = new QTIResultSet();
		qtiResultSet.setAssessmentID(Long.valueOf(uId));
		qtiResultSet.setOlatResource(IGNORE_PREVIEW_CASE);
		qtiResultSet.setOlatResourceDetail(uId);
		qtiResultSet.setRepositoryRef(entry.getKey());
		qtiResultSet.setIdentity(identity);
		qtiResultSet.setQtiType(1);
		qtiResultSet.setLastModified(new Date());
		DBFactory.getInstance().saveObject(qtiResultSet);
		DBFactory.getInstance().commitAndCloseSession();
		qtiResultSet = (QTIResultSet) DBFactory.getInstance().loadObject(qtiResultSet, true);

		return uId;
	}

	public static QTIResultSet getResultSet(final long uniqueId) {
		final List<Long> liste = getResultSetByAssassmentId(uniqueId);
		QTIResultSet qtiResultSet = null;
		if (liste != null && liste.size() > 0) {
			Long key = liste.get(0);
			qtiResultSet = DBFactory.getInstance().loadObject(QTIResultSet.class, key);
			DBFactory.getInstance().intermediateCommit();
		}
		return qtiResultSet;
	}

	public static Boolean isLastTestTry(QTIResultSet testTry) {
		Boolean isLast = true;

		String query = "select rset.key from org.olat.ims.qti.QTIResultSet rset where rset.identity=? and rset.olatResourceDetail=? and rset.creationDate >= ?";
		@SuppressWarnings("unchecked")
		List<Long> results = DBFactory.getInstance().find(query,
				new Object[] { testTry.getIdentity().getKey(), testTry.getOlatResourceDetail(), testTry.getCreationDate() },
				new Type[] { StandardBasicTypes.LONG, StandardBasicTypes.STRING, StandardBasicTypes.DATE });
		for (Long result : results) {
			if (!(testTry.getKey().equals(result)) && testTry.getKey() < result) {
				isLast = false;
				break;
			}
		}

		return isLast;
	}

	public static QTIResultSet getLastSuspendedQTIResultSet(Identity identity, CourseNode node) {
		List<Long> suspendedResults = getSuspendedQTIResultSet(identity, node);
		QTIResultSet lastResultSet = null;

		for (Long resultSet : suspendedResults) {
			QTIResultSet res = (DBFactory.getInstance().loadObject(QTIResultSet.class, resultSet));
			if (lastResultSet != null) {
				if (lastResultSet.getCreationDate().before(res.getCreationDate())) {
					lastResultSet = res;
				}
			} else {
				lastResultSet = res;
			}
		}

		return lastResultSet;
	}

	private static List<Long> getSuspendedQTIResultSet(Identity identity, CourseNode node) {
		String query = "select rset.key from org.olat.ims.qti.QTIResultSet rset where rset.suspended = ? and rset.identity=? and rset.olatResourceDetail=?";
		List<Long> results = DBFactory.getInstance().find(query, new Object[] { Boolean.TRUE, identity.getKey(), node.getIdent() },
				new Type[] { StandardBasicTypes.BOOLEAN, StandardBasicTypes.LONG, StandardBasicTypes.STRING });
		DBFactory.getInstance().intermediateCommit();
		return results;
	}

	private static List<Long> getResultSetByAssassmentId(Long assessmentID) {
		DB db = DBFactory.getInstance();
		db.commitAndCloseSession();
		StringBuilder slct = new StringBuilder();
		slct.append("select rset.key from ");
		slct.append("org.olat.ims.qti.QTIResultSet rset ");
		slct.append("where ");
		slct.append("rset.assessmentID=? ");
		List<Long> results = db.find(slct.toString(), new Object[] { assessmentID }, new Type[] { StandardBasicTypes.LONG });
		db.intermediateCommit();
		return results;
	}

	/**
	 * Ask the Onyx Reporter with a given file and save the results to db.
	 * 
	 * @param qtiResultSet
	 * @param file_s
	 */
	static boolean performOnyxReport(QTIResultSet qtiResultSet) {
		boolean reporterFinsished = true;
		LOGGER.info("PerfomReport Begin for " + qtiResultSet.getAssessmentID() + " # " + qtiResultSet.getKey());
		//Get course and course node
		ICourse course = CourseFactory.loadCourse(qtiResultSet.getOlatResource());
		CourseNode courseNode = course.getRunStructure().getNode(qtiResultSet.getOlatResourceDetail());

		//<OLATCE-1048> SelfTests and Surveys are not AssessableCourseNode --> no assessments saved --> Switched to AbstractAssessableCourseNode
		AbstractAccessableCourseNode node = null;
		if (courseNode instanceof AbstractAccessableCourseNode) {
			node = (AbstractAccessableCourseNode) courseNode;
		} else {
			LOGGER.warn("Tried to perform an OnyxReport with a non-assessable course node! "
					+ (courseNode != null ? (courseNode.getShortName()
							+ " Class: " + courseNode.getClass()) : "NULL"));
		}
		// </OLATCE-1048>
		//<ONYX-705>
		Map<String, String> results = null;
		//</ONYX-705>

		String path = WebappHelper.getUserDataRoot() + File.separator + RES_REPORTING + File.separator + qtiResultSet.getIdentity().getName() + File.separator
				+ courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString() + File.separator;
		File file_s = getResultsFile(path, courseNode, qtiResultSet.getAssessmentID());
		if (!file_s.exists()) {
			LOGGER.error("performOnyxReport was called but no result.xml exists with path: " + file_s.getAbsolutePath());
			reporterFinsished = false;
		}

		if(reporterFinsished){
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
		}

		if(reporterFinsished) {
			String score = null, passed = null;
		
			//<ONYX-705>
			for (String vars : results.keySet()) {
				// only testoutcomes "score" and "passed" are stored at olat db
				if (SCORE.equalsIgnoreCase(vars)) {
					score = results.get(vars);
				}	else if (PASS.equalsIgnoreCase(vars)) {
					passed = results.get(vars);
				} else {
					LOGGER.debug("TestOutCome "+results.get(vars)+ " is not stored in OLAT DB");
				}
			}
			qtiResultSet = (QTIResultSet) DBFactory.getInstance().loadObject(qtiResultSet);
			synchronized (qtiResultSet) {
				if (score != null || passed != null) {
					Float scoreValue = null;
					try {
						if (score != null) {
							scoreValue = Float.valueOf(score);
							qtiResultSet.setScore(scoreValue);
						}

						//if own cutvalue for passed is configured use this instead of the PASS variable from onyx test.
						if (courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_CUTVALUE) != null) {
							Float cutValue = ((Float) courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_CUTVALUE));
							if (scoreValue >= cutValue) {
								passed = "true";
							} else {
								passed = "false";
							}
						}
					} catch (NumberFormatException nfe) {
						LOGGER.error("Unable to parse score: " + score + "to float", nfe);
					} catch (ClassCastException cce) {
						LOGGER.error("Unable to cast cut-value to float", cce);
					}

					if (passed != null) {
						qtiResultSet.setIsPassed(Boolean.valueOf(passed));
					}
				}
				qtiResultSet.setLastModified(new Date());
				DBFactory.getInstance().updateObject(qtiResultSet);
				DBFactory.getInstance().commitAndCloseSession();
			}
		}
		LOGGER.info("PerfomReport Finished for " + qtiResultSet.getAssessmentID() + " # " + qtiResultSet.getKey());
		return reporterFinsished;
	}

	/**
	 * This is called by a nightly job: update all resultsets where the Onyx Reporter has not finished yet. (maybe because the reporter was not available).
	 */
	public static void updateOnyxResults() {
		final List<QTIResultSet> liste = findResultSets();
		for (final QTIResultSet qTIResultSet : liste) {
			LifeCycleManager lcm = null;
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

	public static List<QTIResultSet> findResultSets() {
		final DB db = DBFactory.getInstance();

		final StringBuilder slct = new StringBuilder();
		slct.append("select rset from ");
		slct.append("org.olat.ims.qti.QTIResultSet rset ");

		return db.find(slct.toString());
	}
	
	public static final String getResultsFilenamePrefix(final String path, final CourseNode courseNode, final long assessmentId) {
		final String prefix = path + courseNode.getIdent() + "v" + assessmentId;
		return prefix;
	}

	/**
	 * Retrieves the results file.
	 * 
	 * @param path
	 * @param courseNode
	 * @param assessmentId
	 * @return Delivers the result.zip, if found, the result.xml otherwise.
	 *         Returns null if not found.
	 */
	public static final File getResultsFile(final String path, final CourseNode courseNode, final long assessmentId) {
		final String prefix = getResultsFilenamePrefix(path, courseNode, assessmentId);
		File file = new File(prefix + SUFFIX_ZIP);
		if (file.exists()) {
			return file;
		}
		file = new File(prefix + SUFFIX_XML);
		if (file.exists()) {
			return file;
		}
		return null;
	}
}