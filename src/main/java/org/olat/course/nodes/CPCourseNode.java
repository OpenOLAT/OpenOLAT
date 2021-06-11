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
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.cp.CPEditController;
import org.olat.course.nodes.cp.CPRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.types.ImsCPFileResource;
import org.olat.ims.cp.CPManager;
import org.olat.ims.cp.ui.CPPackageConfig;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class CPCourseNode extends AbstractAccessableCourseNode {
	
	private static final Logger log = Tracing.createLoggerFor(CPCourseNode.class);

	private static final long serialVersionUID = -4317662219173515498L;
	public static final String TYPE = "cp";

	public CPCourseNode() {
		this(null);
	}

	public CPCourseNode(INode parent) {
		super(TYPE, parent);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		CPEditController childTabCntrllr = new CPEditController(ureq, wControl, stackPanel, this);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ICourse.class, userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		CPRunController cprunC = new CPRunController(getModuleConfiguration(), ureq, wControl, this, nodecmd, ores, false, userCourseEnv);
		return cprunC.createNodeRunConstructionResult(ureq, null);
	}

	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ICourse.class, userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		return new CPRunController(getModuleConfiguration(), ureq, wControl, this, null, ores, true, userCourseEnv);
	}
	
	@Override
	protected String getDefaultTitleOption() {
		return CourseNode.DISPLAY_OPTS_CONTENT;
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) {
			return oneClickStatusCache[0];
		}

		StatusDescription sd = StatusDescription.NOERROR;
		boolean isValid = CPEditController.isModuleConfigValid(getModuleConfiguration());
		if (!isValid) {
			// FIXME: refine statusdescriptions
			String shortKey = "error.noreference.short";
			String longKey = "error.noreference.long";
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(CPEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(CPEditController.PANE_TAB_CPCONFIG);
		}
		return sd;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		// only here we know which translator to take for translating condition
		// error messages
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		// ",false" because we do not want to be strict, but just indicate whether
		// the reference still exists or not
		return CPEditController.getCPReference(getModuleConfiguration(), false);
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
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent) {
		int CURRENTVERSION = 7;
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// use defaults for new course building blocks
			config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, Boolean.FALSE.booleanValue());
			config.setBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU, Boolean.TRUE.booleanValue());
			// how to render files (include jquery etc)
			DeliveryOptions nodeDeliveryOptions = DeliveryOptions.defaultWithGlossary();
			nodeDeliveryOptions.setInherit(Boolean.TRUE);
			config.set(CPEditController.CONFIG_DELIVERYOPTIONS, nodeDeliveryOptions);
			config.setConfigurationVersion(CURRENTVERSION);
		} else {
			config.remove(NodeEditController.CONFIG_INTEGRATION);
			if (config.getConfigurationVersion() < 2) {
				// update new configuration options using default values for existing
				// nodes
				config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, Boolean.TRUE.booleanValue());
				Boolean componentMenu = config.getBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU);
				if (componentMenu == null) {
					config.setBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU, Boolean.TRUE.booleanValue());
				}
				config.setConfigurationVersion(2);
			}
			
			if(config.getConfigurationVersion() < 3) {
				config.set(NodeEditController.CONFIG_CONTENT_ENCODING, NodeEditController.CONFIG_CONTENT_ENCODING_AUTO);
				config.set(NodeEditController.CONFIG_JS_ENCODING, NodeEditController.CONFIG_JS_ENCODING_AUTO);
				config.setConfigurationVersion(3);
			}
			// Version 5 was ineffective since the delivery options were not set. We have to redo this and
			// save it as version 6
			if(config.getConfigurationVersion() < 7) {
				String contentEncoding = (String)config.get(NodeEditController.CONFIG_CONTENT_ENCODING);
				if (contentEncoding != null && contentEncoding.equals("auto")) {
					contentEncoding = null; // new style for auto
				}
				String jsEncoding = (String)config.get(NodeEditController.CONFIG_JS_ENCODING);
				if (jsEncoding != null && jsEncoding.equals("auto")) {
					jsEncoding = null; // new style for auto
				}

				
				CPPackageConfig reConfig = null;
				DeliveryOptions nodeDeliveryOptions = (DeliveryOptions)config.get(CPEditController.CONFIG_DELIVERYOPTIONS);
				if (nodeDeliveryOptions == null) {
					// Update missing delivery options now, inherit from repo by default
					nodeDeliveryOptions = DeliveryOptions.defaultWithGlossary();
					nodeDeliveryOptions.setInherit(Boolean.TRUE);
					
					RepositoryEntry re = getReferencedRepositoryEntry();
					// Check if delivery options are set for repo entry, if not create default
					if(re != null) {
						CPManager cpManager = CoreSpringFactory.getImpl(CPManager.class);
						reConfig = cpManager.getCPPackageConfig(re.getOlatResource());						
						if(reConfig == null) {
							reConfig = new CPPackageConfig();
						}
						DeliveryOptions repoDeliveryOptions = reConfig.getDeliveryOptions();
						if (repoDeliveryOptions == null) {
							// migrate existing config back to repo entry using the default as a base
							repoDeliveryOptions = DeliveryOptions.defaultWithGlossary();
							reConfig.setDeliveryOptions(repoDeliveryOptions);
							repoDeliveryOptions.setContentEncoding(contentEncoding);
							repoDeliveryOptions.setJavascriptEncoding(jsEncoding);						
							cpManager.setCPPackageConfig(re.getOlatResource(), reConfig);
						} else {
							// see if we have any different settings than the repo. if so, don't use inherit mode
							if(contentEncoding != repoDeliveryOptions.getContentEncoding() || jsEncoding != repoDeliveryOptions.getJavascriptEncoding()) {
								nodeDeliveryOptions.setInherit(Boolean.FALSE);	
								nodeDeliveryOptions.setContentEncoding(contentEncoding);
								nodeDeliveryOptions.setJavascriptEncoding(jsEncoding);
							}
						}
					}
					// remove old config parameters
					config.remove(NodeEditController.CONFIG_CONTENT_ENCODING);
					config.remove(NodeEditController.CONFIG_JS_ENCODING);
					// replace with new delivery options
					config.set(CPEditController.CONFIG_DELIVERYOPTIONS, nodeDeliveryOptions);
				}
				config.setConfigurationVersion(7);
			}
			
			// else node is up-to-date - nothing to do
		}
		if (config.getConfigurationVersion() != CURRENTVERSION) {
			log.error("CP course node version not updated to lastest version::" + CURRENTVERSION + ", was::" + config.getConfigurationVersion() + ". Check the code, programming error.");
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
			RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(ImsCPFileResource.TYPE_NAME);
			RepositoryEntry re = handler.importResource(owner, rie.getInitialAuthor(), rie.getDisplayName(),
					rie.getDescription(), false, organisation, locale, rie.importGetExportedFile(), null);
			CPEditController.setCPReference(re, getModuleConfiguration());
		} else {
			CPEditController.removeCPReference(getModuleConfiguration());
		}
	}
}