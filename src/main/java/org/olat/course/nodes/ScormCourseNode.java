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

package org.olat.course.nodes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.learningpath.ui.TabbableLeaningPathNodeConfigController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.cp.CPEditController;
import org.olat.course.nodes.scorm.ScormAssessmentConfig;
import org.olat.course.nodes.scorm.ScormEditController;
import org.olat.course.nodes.scorm.ScormLearningPathNodeHandler;
import org.olat.course.nodes.scorm.ScormRunController;
import org.olat.course.nodes.scorm.ScormRunSegmentController;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.reminder.AssessmentReminderProvider;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.fileresource.types.ScormCPFileResource;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.scorm.ScormMainManager;
import org.olat.modules.scorm.ScormPackageConfig;
import org.olat.modules.scorm.archiver.ScormExportManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

/**
 * Description:<br>
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class ScormCourseNode extends AbstractAccessableCourseNode {
	
	private static final Logger log = Tracing.createLoggerFor(ScormCourseNode.class);
	private static final long serialVersionUID = 2970594874787761801L;
	@SuppressWarnings("deprecation")
	private static final String TRANSLATOR_PACKAGE = Util.getPackageName(ScormEditController.class);
	
	public static final String TYPE = "scorm";
	private static final int CURRENT_CONFIG_VERSION = 5;
	
	private static final String CONFIG_RAW_CONTENT = "rawcontent";
	private static final String CONFIG_HEIGHT = "height";	
	private final static String CONFIG_HEIGHT_AUTO = "auto";
	
	public ScormCourseNode() {
		super(TYPE);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		ScormEditController childTabCntrllr = new ScormEditController(this, ureq, wControl, course);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd, VisibilityFilter visibilityFilter) {
		Controller cuntCtrl = new ScormRunSegmentController(ureq, wControl, userCourseEnv, this);
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, cuntCtrl, userCourseEnv, this, "o_scorm_icon");
		// no inline-in-olat-menu integration possible: no display configuration option
		return new NodeRunConstructionResult(ctrl);
	}

	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		ScormRunController cprunC = new ScormRunController(getModuleConfiguration(), ureq, userCourseEnv, wControl, this, true);
		return new NodeRunConstructionResult(cprunC).getRunController();
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null && oneClickStatusCache.length > 0) {
			return oneClickStatusCache[0];
		}
		
		List<StatusDescription> statusDescs = validateInternalConfiguration();
		if(statusDescs.isEmpty()) {
			statusDescs.add(StatusDescription.NOERROR);
		}
		oneClickStatusCache = StatusDescriptionHelper.sort(statusDescs);
		return oneClickStatusCache[0];
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, TRANSLATOR_PACKAGE, getConditionExpressions());
		if(oneClickStatusCache != null && oneClickStatusCache.length > 0) {
			//isConfigValidWithTranslator add first
			sds.remove(oneClickStatusCache[0]);
		}
		sds.addAll(validateInternalConfiguration());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}
	
	private List<StatusDescription> validateInternalConfiguration() {
		List<StatusDescription> sdList = new ArrayList<>(2);

		boolean hasScormReference = ScormEditController.hasScormReference(getModuleConfiguration());
		if (!hasScormReference) {
			addStatusErrorDescription("error.noreference.short", "error.noreference.long", ScormEditController.PANE_TAB_CPCONFIG, sdList);
		}
		
		if (isFullyAssessedScoreConfigError()) {
			addStatusErrorDescription("error.fully.assessed.score", "error.fully.assessed.score",
					TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
		}
		if (isFullyAssessedPassedConfigError()) {
			addStatusErrorDescription("error.fully.assessed.passed", "error.fully.assessed.passed",
					TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
		}
		
		return sdList;
	}
	
	private boolean isFullyAssessedScoreConfigError() {
		boolean hasScore = Mode.none != new ScormAssessmentConfig(getModuleConfiguration()).getScoreMode();
		boolean isScoreTrigger = CoreSpringFactory.getImpl(ScormLearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnScore(null, null)
				.isEnabled();
		return isScoreTrigger && !hasScore;
	}
	
	private boolean isFullyAssessedPassedConfigError() {
		boolean hasPassed = new ScormAssessmentConfig(getModuleConfiguration()).getPassedMode() != Mode.none;
		boolean isPassedTrigger = CoreSpringFactory.getImpl(ScormLearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnPassed(null, null)
				.isEnabled();
		return isPassedTrigger && !hasPassed;
	}

	private void addStatusErrorDescription(String shortDescKey, String longDescKey, String pane,
			List<StatusDescription> status) {
		String[] params = new String[] { getShortTitle() };
		StatusDescription sd = new StatusDescription(StatusDescription.ERROR, shortDescKey, longDescKey, params,
				TRANSLATOR_PACKAGE);
		sd.setDescriptionForUnit(getIdent());
		sd.setActivateableViewIdentifier(pane);
		status.add(sd);
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		// ",false" because we do not want to be strict, but just indicate whether
		// the reference still exists or not
		return CPEditController.getCPReference(getModuleConfiguration(), false);
	}
	
	public String getReferencedRepositoryEntrySoftkey() {
		return (String)getModuleConfiguration().get(CPEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return true;
	}

	/**
	 * Update the module configuration to have all mandatory configuration flags
	 * set to usefull default values
	 * @param isNewNode true: an initial configuration is set; false: upgrading
	 *          from previous node configuration version, set default to maintain
	 *          previous behaviour
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType);
		
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// use defaults for new course building blocks
			config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, Boolean.TRUE.booleanValue());
			config.setBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU, Boolean.TRUE.booleanValue());
			config.setBooleanEntry(ScormEditController.CONFIG_SHOWNAVBUTTONS, Boolean.TRUE.booleanValue());
			config.set(CONFIG_HEIGHT, "680");
			config.set(NodeEditController.CONFIG_CONTENT_ENCODING, NodeEditController.CONFIG_CONTENT_ENCODING_AUTO);	
			config.set(NodeEditController.CONFIG_JS_ENCODING, NodeEditController.CONFIG_JS_ENCODING_AUTO);	
			config.setBooleanEntry(ScormEditController.CONFIG_FULLWINDOW, true);
			config.setBooleanEntry(ScormEditController.CONFIG_CLOSE_ON_FINISH, false);
			config.setBooleanEntry(ScormEditController.CONFIG_ADVANCESCORE, true);
			config.setBooleanEntry(ScormEditController.CONFIG_ATTEMPTSDEPENDONSCORE, false);
			config.setIntValue(ScormEditController.CONFIG_MAXATTEMPTS, 0);
			config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
			
			DeliveryOptions deliveryOptions = new DeliveryOptions();
			deliveryOptions.setInherit(Boolean.TRUE);
			config.set(ScormEditController.CONFIG_DELIVERY_OPTIONS, deliveryOptions);
		} else {
			int version = config.getConfigurationVersion();
			if (version < CURRENT_CONFIG_VERSION) {
				// Loaded config is older than current config version => migrate
				if (version == 1) {
					version = 2;
					// remove old config from previous versions
					config.remove(NodeEditController.CONFIG_INTEGRATION);
					// add new parameter 'shownavbuttons' and 'height'
					config.setBooleanEntry(ScormEditController.CONFIG_SHOWNAVBUTTONS, Boolean.TRUE.booleanValue());
					config.set(CONFIG_HEIGHT, CONFIG_HEIGHT_AUTO);					
				}
				
				if (version == 2) {
					version = 3;
					config.set(NodeEditController.CONFIG_CONTENT_ENCODING, NodeEditController.CONFIG_CONTENT_ENCODING_AUTO);	
					config.set(NodeEditController.CONFIG_JS_ENCODING, NodeEditController.CONFIG_JS_ENCODING_AUTO);	
				}
				
				if (version == 3) {
					version = 4;
					config.setBooleanEntry(ScormEditController.CONFIG_FULLWINDOW, false);
					config.setBooleanEntry(ScormEditController.CONFIG_CLOSE_ON_FINISH, false);
					config.setBooleanEntry(ScormEditController.CONFIG_ADVANCESCORE, false);
					config.setBooleanEntry(ScormEditController.CONFIG_ATTEMPTSDEPENDONSCORE, false);
					config.setIntValue(ScormEditController.CONFIG_MAXATTEMPTS, 0);
				}
				
				if (version == 4) {
					boolean rawContent = config.getBooleanSafe(CONFIG_RAW_CONTENT, true);
					
					String height = (String)config.get(CONFIG_HEIGHT);
					String contentEncoding = (String)config.get(NodeEditController.CONFIG_CONTENT_ENCODING);
					String jsEncoding = (String)config.get(NodeEditController.CONFIG_JS_ENCODING);

					ScormPackageConfig reConfig = null;
					DeliveryOptions nodeDeliveryOptions = new DeliveryOptions();
					RepositoryEntry re = getReferencedRepositoryEntry();
					if(re != null) {
						ScormMainManager scormMainManager = CoreSpringFactory.getImpl(ScormMainManager.class);
						reConfig = scormMainManager.getScormPackageConfig(re.getOlatResource());

						//move the settings from the node to the repo
						if(reConfig == null || reConfig.getDeliveryOptions() == null) {
							if(reConfig == null) {
								reConfig = new ScormPackageConfig();
							}
							reConfig.setDeliveryOptions(new DeliveryOptions());
							nodeDeliveryOptions.setInherit(Boolean.TRUE);
							if(rawContent) {
								nodeDeliveryOptions.setStandardMode(Boolean.TRUE);
							} else {	
								nodeDeliveryOptions.setStandardMode(Boolean.FALSE);
								reConfig.getDeliveryOptions().setOpenolatCss(Boolean.TRUE);
								reConfig.getDeliveryOptions().setPrototypeEnabled(Boolean.TRUE);
								reConfig.getDeliveryOptions().setHeight(height);
							}
							reConfig.getDeliveryOptions().setContentEncoding(contentEncoding);
							reConfig.getDeliveryOptions().setJavascriptEncoding(jsEncoding);
							scormMainManager.setScormPackageConfig(re.getOlatResource(), reConfig);
						} else {
							DeliveryOptions repoDeliveryOptions = reConfig.getDeliveryOptions();
							boolean reRawContent = repoDeliveryOptions.getStandardMode() == null ? true : repoDeliveryOptions.getStandardMode().booleanValue();
							if(((height == null && repoDeliveryOptions.getHeight() == null) || (height != null && height.equals(repoDeliveryOptions.getHeight())))
									&& ((contentEncoding == null && repoDeliveryOptions.getContentEncoding() == null) || (contentEncoding != null && contentEncoding.equals(repoDeliveryOptions.getContentEncoding())))
									&& ((jsEncoding == null && repoDeliveryOptions.getJavascriptEncoding() == null) || (jsEncoding != null && jsEncoding.equals(repoDeliveryOptions.getJavascriptEncoding())))
									&& rawContent == reRawContent) {
								nodeDeliveryOptions.setInherit(Boolean.TRUE);	
							} else {
								nodeDeliveryOptions.setInherit(Boolean.FALSE);	
								nodeDeliveryOptions.setContentEncoding(contentEncoding);
								nodeDeliveryOptions.setJavascriptEncoding(jsEncoding);
								nodeDeliveryOptions.setHeight(height);
								if(rawContent) {
									nodeDeliveryOptions.setStandardMode(Boolean.TRUE);
								} else {
									nodeDeliveryOptions.setStandardMode(Boolean.FALSE);
									nodeDeliveryOptions.setOpenolatCss(Boolean.TRUE);
									nodeDeliveryOptions.setPrototypeEnabled(Boolean.TRUE);
									nodeDeliveryOptions.setHeight(height);
								}
							}
						}
					}

					config.set(ScormEditController.CONFIG_DELIVERY_OPTIONS, nodeDeliveryOptions);
					version = 5;
				}
				
				//version is now set to current version
				config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
			}
		}
	}

	@Override
	public void exportNode(File exportDirectory, ICourse course) {
		RepositoryEntry re = CPEditController.getCPReference(getModuleConfiguration(), false);
		if (re == null) return;
		File fExportDirectory = new File(exportDirectory, getIdent());
		fExportDirectory.mkdirs();
		RepositoryEntryImportExport reie = new RepositoryEntryImportExport(re, fExportDirectory);
		reie.exportDoExport();
	}

	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, boolean withReferences) {
		RepositoryEntryImportExport rie = new RepositoryEntryImportExport(importDirectory, getIdent());
		if(withReferences && rie.anyExportedPropertiesAvailable()) {
			RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(ScormCPFileResource.TYPE_NAME);
			RepositoryEntry re = handler.importResource(owner, rie.getInitialAuthor(), rie.getDisplayName(),
				rie.getDescription(), false, organisation, locale, rie.importGetExportedFile(), null);
			ScormEditController.setScormCPReference(re, getModuleConfiguration());
		} else {
			CPEditController.removeCPReference(getModuleConfiguration());
		}
	}

	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String archivePath, String charset) {
		String fileName = "scorm_"
				+ StringHelper.transformDisplayNameToFileSystemName(getShortName())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xls";
		fileName = ZipUtil.concat(archivePath, fileName);

		Translator trans = Util.createPackageTranslator(ScormExportManager.class, locale);
		String results = ScormExportManager.getInstance().getResults(course.getCourseEnvironment(), this, trans);
		try {
			exportStream.putNextEntry(new ZipEntry(fileName));
			IOUtils.write(results, exportStream, "UTF-8");
			exportStream.closeEntry();
		} catch (IOException e) {
			log.error("", e);
		}
		return true;
	}
	
	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		
		CoursePropertyManager pm = course.getCourseEnvironment().getCoursePropertyManager();
		// 1) Delete all properties: score, passed, log, comment, coach_comment,
		// attempts
		pm.deleteNodeProperties(this, null);
		// 2) Delete all user files for this scorm node
		// FIXME gs
		// it is problematic that the data is stored using username/courseid-scormid/
		// much better would be /courseid-scormid/username/
		// I would consider refatoring this and setting up an upgrade task that moves the
		// folders accordingly
	}
	
	@Override
	public CourseNodeReminderProvider getReminderProvider(boolean rootNode) {
		return new AssessmentReminderProvider(getIdent(), new ScormAssessmentConfig(getModuleConfiguration()));
	}
	
}