
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.PathUtils;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.QTIResultSet;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.process.ImsRepositoryResolver;
import org.olat.ims.qti.process.Resolver;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Ingmar Kroll
 */
@Service("onyxModule")
public class OnyxModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final OLog log = Tracing.createLoggerFor(OnyxModule.class);

	@Value("${onyx.plugin.wslocation}")
	private String onyxPluginWSLocation;
	public static ArrayList<PlayerTemplate> PLAYERTEMPLATES;
	/*
	 * holds the local config name which is sent to the remote onyxplugin -> onyxplugin must have a config corresponding to this name
	 */
	@Value("${onyx.plugin.configname}")
	private String configName;
	// <OLATCE-713>
	@Value("${onyx.plugin.userviewlocation}")
	private String onyxUserViewLocation;
	@Value("${onyx.reporter.userviewlocation}")
	private String onyxReporterUserViewLocation;
	// </OLATCE-713>
	@Value("${onyx.plugin.exammodelocation}")
	private String onyxExamModeLocation;
	@Value("${assessmentplugin.activate}")
	private String assessmentPlugin;
	
	private static Map<Long,Boolean> onyxMap = new ConcurrentHashMap<Long,Boolean>();
	

	@Autowired
	public OnyxModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public boolean isEnabled() {
		return assessmentPlugin != null && "Onyx".equals(assessmentPlugin);
	}
	
	/**
	 * @return Returns the configName.
	 */
	public String getConfigName() {
		return configName;
	}

	/**
	 * @param configName The configName to set.
	 */
	public void setConfigName(final String configName) {
		this.configName = configName;
	}

	/**
	 * @param pluginWSLocation The pluginWSLocation to set.
	 */
	public void setOnyxPluginWSLocation(final String onyxPluginWSLocation) {
		this.onyxPluginWSLocation = onyxPluginWSLocation;
	}

	/**
	 * @param onyxExamModeLocation
	 *            The location of the onyx exam mode
	 */
	public void setOnyxExamModeLocation(String onyxExamModeLocation) {
		this.onyxExamModeLocation = onyxExamModeLocation;
	}

	/**
	 * @return Returns the userViewLocation.
	 */
	public String getUserViewLocation() {
		// <OLATCE-713>
		return onyxUserViewLocation;
		// </OLATCE-713>
	}

	/**
	 * @return Returns the pluginWSLocation.
	 */
	public String getPluginWSLocation() {
		return onyxPluginWSLocation + "/services";
	}

	/**
	 * @return The location of the Onyx Exam Mode
	 */
	public String getOnyxExamModeLocation() {
		return onyxExamModeLocation;
	}
	
	/**
	 * [user by Spring]
	 * @param assessmentPlugin
	 */
	public void setAssessmentPlugin(String assessmentPlugin) {
		this.assessmentPlugin = assessmentPlugin;
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
	protected void initFromChangedProperties() {
		//
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
				onyx = Boolean.FALSE;
				try {
					final Resolver resolver = new ImsRepositoryResolver(res);
					// search for qti.xml, it not exists for qti2
					if (resolver.getQTIDocument() == null) {
						onyx = Boolean.TRUE;
					} else {
						onyx = Boolean.FALSE;
					}
				} catch(OLATRuntimeException e) {
					log.error("", e);
				}
				onyxMap.put(resourceId, onyx);
			}
			return onyx.booleanValue();
		} else {
			return false;
		}
	}
	
	public static ResourceEvaluation isOnyxTest(File file, String filename) {
		ResourceEvaluation eval = new ResourceEvaluation();
		BufferedReader reader = null;
		try {
			ImsManifestFileFilter visitor = new ImsManifestFileFilter();
			Path fPath = PathUtils.visit(file, filename, visitor);
			if(visitor.isValid()) {
				Path qtiPath = fPath.resolve("imsmanifest.xml");
				reader = Files.newBufferedReader(qtiPath, StandardCharsets.UTF_8);
				while (reader.ready()) {
					String l = reader.readLine();
					if (l.indexOf("imsqti_xmlv2p1") != -1 || l.indexOf("imsqti_test_xmlv2p1") != -1 || l.indexOf("imsqti_assessment_xmlv2p1") != -1) {
						eval.setValid(true);
						break;
					}
				}
			} else {
				eval.setValid(false);
			}
		} catch(NoSuchFileException nsfe) {
			eval.setValid(false);
		} catch (IOException | IllegalArgumentException e) {
			log.error("", e);
			eval.setValid(false);
		} finally {
			IOUtils.closeQuietly(reader);
		}
		return eval;
	}
	
	private static class ImsManifestFileFilter extends SimpleFileVisitor<Path> {
		private boolean imsManifestFile;

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		throws IOException {
			String filename = file.getFileName().toString();
			if("imsmanifest.xml".equals(filename)) {
				imsManifestFile = true;
			}
			return imsManifestFile ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
		}
		
		public boolean isValid() {
			return imsManifestFile;
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
			IOUtils.closeQuietly(br);
		}
		// <OLTACE-72>
		if (unzippedDir != null) {
			unzippedDir.delete();
		}
		// </OLTACE-72>
		return false;

	}

	// <OLATCE-713>
	public String getOnyxUserViewLocation() {
		return onyxUserViewLocation;
	}

	public void setOnyxUserViewLocation(String onyxUserViewLocation) {
		this.onyxUserViewLocation = onyxUserViewLocation;
	}

	public String getOnyxReporterUserViewLocation() {
		return onyxReporterUserViewLocation;
	}

	public void setOnyxReporterUserViewLocation(String onyxReporterUserViewLocation) {
		this.onyxReporterUserViewLocation = onyxReporterUserViewLocation;
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
			AssessmentEntryStatus status = null;
			if(currentResultSet.getFullyAssessed() != null && currentResultSet.getFullyAssessed().booleanValue()) {
				status = AssessmentEntryStatus.done;
			}
			return new ScoreEvaluation(currentResultSet.getScore(), currentResultSet.getIsPassed(),
					status, currentResultSet.getFullyAssessed(), currentResultSet.getAssessmentID());
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
