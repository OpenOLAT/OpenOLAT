
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.ZipUtil;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.QTIResultSet;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.process.ImsRepositoryResolver;
import org.olat.ims.qti.process.Resolver;

/**
 * @author Ingmar Kroll
 */
public class OnyxModule extends AbstractOLATModule implements ConfigOnOff {

	private static String onyxPluginWSLocation;
	public static ArrayList<PlayerTemplate> PLAYERTEMPLATES;
	/*
	 * holds the local config name which is sent to the remote onyxplugin -> onyxplugin must have a config corresponding to this name
	 */
	private static String configName;
	// <OLATCE-713>
	private static String onyxUserViewLocation;
	private static String onyxReporterUserViewLocation;
	// </OLATCE-713>
	private static String onyxExamModeLocation;
	private String assessmentPlugin;
	
	private static Map<Long,Boolean> onyxMap = new ConcurrentHashMap<Long,Boolean>();

	@Override
	public boolean isEnabled() {
		return assessmentPlugin != null && "Onyx".equals(assessmentPlugin);
	}
	
	/**
	 * @return Returns the configName.
	 */
	public static String getConfigName() {
		return configName;
	}

	/**
	 * @param configName The configName to set.
	 */
	public void setConfigName(final String configName) {
		OnyxModule.configName = configName;
	}

	/**
	 * @param pluginWSLocation The pluginWSLocation to set.
	 */
	public void setOnyxPluginWSLocation(final String onyxPluginWSLocation) {
		OnyxModule.onyxPluginWSLocation = onyxPluginWSLocation;
	}

	/**
	 * @param onyxExamModeLocation
	 *            The location of the onyx exam mode
	 */
	public void setOnyxExamModeLocation(String onyxExamModeLocation) {
		OnyxModule.onyxExamModeLocation = onyxExamModeLocation;
	}

	/**
	 * @return Returns the userViewLocation.
	 */
	public static String getUserViewLocation() {
		// <OLATCE-713>
		return onyxUserViewLocation;
		// </OLATCE-713>
	}

	/**
	 * @return Returns the pluginWSLocation.
	 */
	public static String getPluginWSLocation() {
		return onyxPluginWSLocation + "/services";
	}

	/**
	 * @return The location of the Onyx Exam Mode
	 */
	public static String getOnyxExamModeLocation() {
		return onyxExamModeLocation;
	}
	
	/**
	 * [user by Spring]
	 * @param assessmentPlugin
	 */
	public void setAssessmentPlugin(String assessmentPlugin) {
		this.assessmentPlugin = assessmentPlugin;
	}

	/**
	 * [used by spring]
	 */
	private OnyxModule() {
		//
	}

	@Override
	public void init() {
		PLAYERTEMPLATES = new ArrayList<PlayerTemplate>();
		PlayerTemplate pt = new PlayerTemplate("onyxdefault", "templatewithtree");
		PLAYERTEMPLATES.add(pt);
		pt = new PlayerTemplate("onyxwithoutnav", "templatewithouttree");
		PLAYERTEMPLATES.add(pt);
	}

	@Override
	protected void initDefaultProperties() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initFromChangedProperties() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPersistedProperties(final PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	public class PlayerTemplate {
		public String id;
		public String i18nkey;

		/**
		 * @param id
		 * @param i18nkey
		 */
		public PlayerTemplate(final String id, final String i18nkey) {
			this.id = id;
			this.i18nkey = i18nkey;
		}
	}

	public static boolean isOnyxTest(final OLATResourceable res) {
		if (res.getResourceableTypeName().equals(TestFileResource.TYPE_NAME) ||
				res.getResourceableTypeName().equals(SurveyFileResource.TYPE_NAME)) {
			Long resourceId = res.getResourceableId();
			Boolean onyx = onyxMap.get(resourceId);
			if(onyx == null) {
				final Resolver resolver = new ImsRepositoryResolver(res);
				// search for qti.xml, it not exists for qti2
				if (resolver.getQTIDocument() == null) {
					onyx = Boolean.TRUE;
				} else {
					onyx = Boolean.FALSE;
				}
				onyxMap.put(resourceId, onyx);
			}
			return onyx.booleanValue();
		} else {
			return false;
		}
	}

	public static boolean isOnyxTest(File zipfile) {
		// <OLTACE-72>
		File unzippedDir = null;
		if (zipfile.getName().toLowerCase().endsWith(".zip")) {
			unzippedDir = new File(zipfile.getAbsolutePath().substring(0, zipfile.getAbsolutePath().length() - 4) + "__unzipped");
			if (!unzippedDir.exists()) {
				unzippedDir.mkdir();
			}
			ZipUtil.unzip(zipfile, unzippedDir);
			zipfile = unzippedDir;
		}
		// </OLTACE-72>
		BufferedReader br = null;
		try {
			File mani = new File(zipfile.getAbsolutePath() + "/imsmanifest.xml");
			br = new BufferedReader(new FileReader(mani));
			while (br.ready()) {
				String l = br.readLine();
				if (l.indexOf("imsqti_xmlv2p1") != -1 || l.indexOf("imsqti_test_xmlv2p1") != -1 || l.indexOf("imsqti_assessment_xmlv2p1") != -1) {
					br.close();
					// <OLTACE-72>
					if (unzippedDir != null) {
						unzippedDir.delete();
					}
					// </OLTACE-72>
					return true;
				}
			}
			br.close();
		} catch (Exception e) {
			try {
				br.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
			}
		}
		// <OLTACE-72>
		if (unzippedDir != null) {
			unzippedDir.delete();
		}
		// </OLTACE-72>
		return false;

	}

	// <OLATCE-713>
	public static String getOnyxUserViewLocation() {
		return onyxUserViewLocation;
	}

	public void setOnyxUserViewLocation(String onyxUserViewLocation) {
		OnyxModule.onyxUserViewLocation = onyxUserViewLocation;
	}

	public static String getOnyxReporterUserViewLocation() {
		return onyxReporterUserViewLocation;
	}

	public void setOnyxReporterUserViewLocation(String onyxReporterUserViewLocation) {
		OnyxModule.onyxReporterUserViewLocation = onyxReporterUserViewLocation;
	}

	// </OLATCE-713>

	/**
	 * This method looks for the latest qtiResultSet in the DB which belongs to
	 * this course node and user and updates the UserScoreEvaluation.
	 */
	public static ScoreEvaluation getUserScoreEvaluationFromQtiResult(Long courseResourceableId, QTICourseNode courseNode, boolean bestResultConfig,
			Identity identity) {
		QTIResultManager qrm = QTIResultManager.getInstance();
		List<QTIResultSet> resultSets = qrm.getResultSets(courseResourceableId, courseNode.getIdent(), courseNode.getReferencedRepositoryEntry().getKey(),
				identity);
		QTIResultSet currentResultSet = null;

		for (QTIResultSet resultSet : resultSets) {
			if (resultSet.getSuspended()) {
				continue;
			}
			if (currentResultSet == null) {
				currentResultSet = resultSet;
				continue;
			}

			// if a best score is given select the latest resultset with the best score
			if (bestResultConfig) {
				if (resultSet.getScore() > currentResultSet.getScore()) {
					currentResultSet = resultSet;
				} else if ((resultSet.getScore() == currentResultSet.getScore()) && (resultSet.getCreationDate().after(currentResultSet.getCreationDate()))) {
					currentResultSet = resultSet;
				}
			} else if (resultSet.getCreationDate().after(currentResultSet.getCreationDate())) {
				currentResultSet = resultSet;
			}
		}
		if (currentResultSet != null && !currentResultSet.getSuspended()) {
			// <OLATCE-374>
			return new ScoreEvaluation(currentResultSet.getScore(), currentResultSet.getIsPassed(), currentResultSet.getFullyAssessed(), currentResultSet.getAssessmentID());
			// </OLATCE-374>
		}
		return null;
	}
	
	public static boolean existsResultSet(Long courseResourceableId, QTICourseNode courseNode, Identity identity, Long assessmentId) {
		QTIResultManager qrm = QTIResultManager.getInstance();
		List<QTIResultSet> resultSets = qrm.getResultSets(courseResourceableId, courseNode.getIdent(), courseNode.getReferencedRepositoryEntry().getKey(),
				identity);
		for (QTIResultSet resultSet : resultSets) {
			if(resultSet.getAssessmentID() == assessmentId && !resultSet.getSuspended()) {
				return true;
			}
		}
		return false;
	}

}
