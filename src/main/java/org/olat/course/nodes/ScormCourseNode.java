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
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.cp.CPEditController;
import org.olat.course.nodes.scorm.ScormEditController;
import org.olat.course.nodes.scorm.ScormRunController;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.types.ScormCPFileResource;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
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
public class ScormCourseNode extends AbstractAccessableCourseNode implements PersistentAssessableCourseNode {
	
	private static final Logger log = Tracing.createLoggerFor(ScormCourseNode.class);
	private static final long serialVersionUID = 2970594874787761801L;
	public static final String TYPE = "scorm";
	private static final int CURRENT_CONFIG_VERSION = 5;
	

	private static final String CONFIG_RAW_CONTENT = "rawcontent";
	private static final String CONFIG_HEIGHT = "height";	
	private final static String CONFIG_HEIGHT_AUTO = "auto";
	

	/**
	 * Constructor for a course building block of the type IMS CP learning content
	 */
	public ScormCourseNode() {
		super(TYPE);
		// init default values
		updateModuleConfigDefaults(true);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createEditController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl, org.olat.course.ICourse)
	 */
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		updateModuleConfigDefaults(false);
		ScormEditController childTabCntrllr = new ScormEditController(this, ureq, wControl, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, euce, childTabCntrllr);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createNodeRunConstructionResult(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation)
	 */
	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		updateModuleConfigDefaults(false);
		ScormRunController cprunC = new ScormRunController(getModuleConfiguration(), ureq, userCourseEnv, wControl, this, false);
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, cprunC, this, "o_scorm_icon");
		// no inline-in-olat-menu integration possible: no display configuration option
		return new NodeRunConstructionResult(ctrl);
	}

	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		updateModuleConfigDefaults(false);
		ScormRunController cprunC = new ScormRunController(getModuleConfiguration(), ureq, userCourseEnv, wControl, this, true);
		return new NodeRunConstructionResult(cprunC).getRunController();
	}

	@Override
	public StatusDescription isConfigValid() {
		/*
		 * first check the one click cache
		 */
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		StatusDescription sd = StatusDescription.NOERROR;
		boolean isValid = ScormEditController.isModuleConfigValid(getModuleConfiguration());
		if (!isValid) {
			String shortKey = "error.noreference.short";
			String longKey = "error.noreference.long";
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(ScormEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(ScormEditController.PANE_TAB_CPCONFIG);
		}
		return sd;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		// only here we know which translator to take for translating condition
		// error messages
		String translatorStr = Util.getPackageName(ScormEditController.class);
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
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
	 * 
	 * @param isNewNode true: an initial configuration is set; false: upgrading
	 *          from previous node configuration version, set default to maintain
	 *          previous behaviour
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// use defaults for new course building blocks
			config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, Boolean.TRUE.booleanValue());
			config.setBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU, Boolean.TRUE.booleanValue());
			config.setBooleanEntry(ScormEditController.CONFIG_SHOWNAVBUTTONS, Boolean.TRUE.booleanValue());
			config.set(CONFIG_HEIGHT, "680");
			config.set(NodeEditController.CONFIG_CONTENT_ENCODING, NodeEditController.CONFIG_CONTENT_ENCODING_AUTO);	
			config.set(NodeEditController.CONFIG_JS_ENCODING, NodeEditController.CONFIG_JS_ENCODING_AUTO);	
			//fxdiff FXOLAT-116: SCORM improvements
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
					//fxdiff FXOLAT-116: SCORM improvements
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
	public AssessmentEvaluation getUserScoreEvaluation(UserCourseEnvironment userCourseEnv) {
		return getUserScoreEvaluation(getUserAssessmentEntry(userCourseEnv));
	}
	
	@Override
	public AssessmentEvaluation getUserScoreEvaluation(AssessmentEntry entry) {
		return AssessmentEvaluation.toAssessmentEvalutation(entry, this);
	}

	@Override
	public AssessmentEntry getUserAssessmentEntry(UserCourseEnvironment userCourseEnv) {
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnv.getIdentityEnvironment().getIdentity();
		return am.getAssessmentEntry(this, mySelf);//we want t
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
}