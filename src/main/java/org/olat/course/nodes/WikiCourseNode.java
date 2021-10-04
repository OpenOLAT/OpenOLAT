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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.noderight.NodeRight;
import org.olat.course.noderight.NodeRightGrant.NodeRightRole;
import org.olat.course.noderight.NodeRightService;
import org.olat.course.noderight.NodeRightType;
import org.olat.course.noderight.NodeRightTypeBuilder;
import org.olat.course.nodes.wiki.WikiEditController;
import org.olat.course.nodes.wiki.WikiRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.types.WikiResource;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.wiki.Wiki;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiToZipUtils;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryEntryToOrganisation;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.CopyCourseOverviewRow;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class WikiCourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID = -5800975339569440113L;

	private static final Logger log = Tracing.createLoggerFor(WikiCourseNode.class);

	public static final String TYPE = "wiki";

	private static final int CURRENT_VERSION = 4;
	public static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "reporef";
	
	private static final String LEGACY_KEY_EDIT_BY_COACH = "edit.by.coach";
	private static final String LEGACY_KEY_EDIT_BY_PARTICIPANT = "edit.by.participant";
	
	public static final NodeRightType EDIT = NodeRightTypeBuilder.ofIdentifier("edit")
			.setLabel(WikiEditController.class, "config.edit")
			.addRole(NodeRightRole.coach, true)
			.addRole(NodeRightRole.participant, true)
			.build();
	public static final List<NodeRightType> NODE_RIGHT_TYPES = Collections.singletonList(EDIT);
	
	public static final String EDIT_CONDITION = "editarticle";
	private Condition preConditionEdit;

	public WikiCourseNode() {
		super(TYPE);
	}

	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType);
		
		ModuleConfiguration config = getModuleConfiguration();
		int version = config.getConfigurationVersion();
		
		if (isNewNode) {
			// use defaults for new course building blocks
			config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, false);
			config.setConfigurationVersion(1);
		}
		if (version < 2) {
			removeDefaultPreconditions();
		}
		if (version < 3 && config.has(LEGACY_KEY_EDIT_BY_COACH)) {
			NodeRightService nodeRightService = CoreSpringFactory.getImpl(NodeRightService.class);
			NodeRight right = nodeRightService.getRight(config, EDIT);
			Collection<NodeRightRole> roles = new ArrayList<>(2);
			if (config.getBooleanSafe(LEGACY_KEY_EDIT_BY_COACH)) {
				roles.add(NodeRightRole.coach);
			}
			if (config.getBooleanSafe(LEGACY_KEY_EDIT_BY_PARTICIPANT)) {
				roles.add(NodeRightRole.participant);
			}
			nodeRightService.setRoleGrants(right, roles);
			nodeRightService.setRight(config, right);
			// Remove legacy
			config.remove(LEGACY_KEY_EDIT_BY_COACH);
			config.remove(LEGACY_KEY_EDIT_BY_PARTICIPANT);
		}
		if (version < 4) {
			NodeRightService nodeRightService = CoreSpringFactory.getImpl(NodeRightService.class);
			nodeRightService.initDefaults(config, NODE_RIGHT_TYPES);
		}
		
		config.setConfigurationVersion(CURRENT_VERSION);
	}
	
	private void removeDefaultPreconditions() {
		if (hasCustomPreConditions()) {
			boolean defaultPreconditions =
					!preConditionEdit.isExpertMode()
				&& !preConditionEdit.isEasyModeCoachesAndAdmins()
				&& !preConditionEdit.isEasyModeAlwaysAllowCoachesAndAdmins()
				&& !preConditionEdit.isAssessmentMode()
				&& !preConditionEdit.isAssessmentModeViewResults();
			if (defaultPreconditions) {
				removeCustomPreconditions();
			}
		}
	}
	
	public void removeCustomPreconditions() {
		preConditionEdit = null;
	}
	
	@Override
	protected void postImportCopyConditions(CourseEnvironmentMapper envMapper) {
		super.postImportCopyConditions(envMapper);
		postImportCondition(preConditionEdit, envMapper);
	}

	@Override
	public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		super.postExport(envMapper, backwardsCompatible);
		postExportCondition(preConditionEdit, envMapper, backwardsCompatible);
	}
	
	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCrourse, CopyCourseContext context) {
		if (context != null) {
			CopyType resourceCopyType = null;
			
			if (context.isCustomConfigsLoaded()) {
				CopyCourseOverviewRow nodeSettings = context.getCourseNodesMap().get(getIdent());
				
				if (nodeSettings != null) {
					resourceCopyType = nodeSettings.getResourceCopyType();
				}
			} else if (context.getBlogCopyType() != null) {
				resourceCopyType = context.getWikiCopyType();				
			}
			
			if (resourceCopyType != null) {
				switch (resourceCopyType) {
				case reference:
					// Nothing to do here, this is the default behavior
					break;
				case createNew:
					// Create a new empty wiki with the same name
					RepositoryEntry wiki = getReferencedRepositoryEntry();
					
					if (wiki != null) {
						RepositoryHandlerFactory handlerFactory = RepositoryHandlerFactory.getInstance();
						
						Set<RepositoryEntryToOrganisation> organisations = wiki.getOrganisations();
						Organisation organisation = null;
						if (organisations != null && organisations.size() > 1) {
							organisation = organisations.stream().filter(RepositoryEntryToOrganisation::isMaster).map(RepositoryEntryToOrganisation::getOrganisation).findFirst().orElse(null);
						} else if (organisations != null) {
							organisation = organisations.stream().map(RepositoryEntryToOrganisation::getOrganisation).findFirst().orElse(null);
						}
						
						RepositoryEntry newWiki = handlerFactory.getRepositoryHandler(wiki).createResource(context.getExecutingIdentity(), wiki.getDisplayname(), wiki.getDescription(), null, organisation, null);
						
						if (newWiki != null) {
							AbstractFeedCourseNode.setReference(getModuleConfiguration(), newWiki);
						}
					}
					break;
				case ignore:
					// Remove the config, must be configured later
					AbstractFeedCourseNode.removeReference(getModuleConfiguration());
					break;
				default:
					break;
				}
			}
		}
		
		super.postCopy(envMapper, processType, course, sourceCrourse, context);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course,UserCourseEnvironment euce) {
		WikiEditController childTabCntrllr = new WikiEditController(ureq, wControl, stackPanel, this, course,euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
	}
	
	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return hasCustomPreConditions()
				? ConditionAccessEditConfig.custom()
				: ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		if(nodeSecCallback.isAccessible()) {
			WikiRunController wikiController = new WikiRunController(wControl, ureq, this, userCourseEnv, nodeSecCallback.getNodeEvaluation());
			return wikiController.createNodeRunConstructionResult();
		}
		Controller controller = MessageUIFactory.createInfoMessage(ureq, wControl, null, this.getNoAccessExplanation());
		return new NodeRunConstructionResult(controller);
	}

	@Override
	public StatusDescription isConfigValid() {
		if(oneClickStatusCache!=null) {
			return oneClickStatusCache[0];
		}
		
		StatusDescription sd =  StatusDescription.NOERROR;
		boolean isValid = WikiEditController.isModuleConfigValid(getModuleConfiguration());
		if (!isValid) {
			String shortKey = "error.noreference.short";
			String longKey = "error.noreference.long";
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(WikiEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(WikiEditController.PANE_TAB_WIKICONFIG);
		}
		return sd;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		//only here we know which translator to take for translating condition error messages
		String translatorStr = Util.getPackageName(WikiEditController.class);
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, translatorStr,getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		//"false" because we do not want to be strict, but just indicate whether
		// the reference still exists or not
		return WikiEditController.getWikiReference(getModuleConfiguration(), false);
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		//wiki is a repo entry
		return true;
	}

	@Override
	public void exportNode(File exportDirectory, ICourse course) {
		RepositoryEntry re = WikiEditController.getWikiReference(getModuleConfiguration(), false);
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
			RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(WikiResource.TYPE_NAME);
			RepositoryEntry re = handler.importResource(owner, rie.getInitialAuthor(), rie.getDisplayName(),
				rie.getDescription(), false, organisation, locale, rie.importGetExportedFile(), null);
			WikiEditController.setWikiRepoReference(re, getModuleConfiguration());
		} else {
			WikiEditController.removeWikiReference(getModuleConfiguration());
		}
	}

	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String archivePath, String charset) {
		String repoRef = (String)getModuleConfiguration().get("reporef");
		OLATResourceable ores = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repoRef, true).getOlatResource();
		
		Wiki wiki = WikiManager.getInstance().getOrLoadWiki(ores);
		if(wiki.getAllPagesWithContent().isEmpty()) {
			return false;
		}
		 
		//OK, there is something to archive 
		String currentPath;
		if(StringHelper.containsNonWhitespace(archivePath)) {
			currentPath = archivePath;
		} else {
			currentPath = "wiki_"
					+ StringHelper.transformDisplayNameToFileSystemName(getShortName())
					+ "_" + getIdent()
					+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()));
		}
		
		VFSContainer container = WikiManager.getInstance().getWikiContainer(ores, WikiManager.WIKI_RESOURCE_FOLDER_NAME);
		if(container != null) { //the container could be null if the wiki is an old empty one - so nothing to archive
		    try {
				VFSContainer parent = container.getParentContainer();
				WikiToZipUtils.wikiToZip(parent, currentPath, exportStream);
			} catch (IOException e) {
				log.error("", e);
			} 
		}
		return true;
	}
	
	@Override
	public List<ConditionExpression> getConditionExpressions() {
		if (hasCustomPreConditions()) {
			List<ConditionExpression> parentConditions = super.getConditionExpressions();
			List<ConditionExpression> conditions = new ArrayList<>();
			if(parentConditions != null && parentConditions.size() > 0) {
				conditions.addAll(parentConditions);
			}
			Condition editCondition = getPreConditionEdit();
			if(editCondition != null && StringHelper.containsNonWhitespace(editCondition.getConditionExpression())) {
				ConditionExpression ce = new ConditionExpression(editCondition.getConditionId());
				ce.setExpressionString(editCondition.getConditionExpression());
				conditions.add(ce);
			}
			return conditions;
		}
		return super.getConditionExpressions();
	}
	
	public boolean hasCustomPreConditions() {
		return preConditionEdit != null;
	}

	public Condition getPreConditionEdit() {
		if (preConditionEdit == null) {
			preConditionEdit = new Condition();
		}
		preConditionEdit.setConditionId(EDIT_CONDITION);
		return preConditionEdit;
	}

	public void setPreConditionEdit(Condition preConditionEdit) {
		if (preConditionEdit == null) {
			preConditionEdit = getPreConditionEdit();
		}
		preConditionEdit.setConditionId(EDIT_CONDITION);
		this.preConditionEdit = preConditionEdit;
	}
	
	/**
	 * The access condition for wiki is composed of 2 dimensions: readonly (or access) and read&write (or editarticle). <br/>
	 * If the access is readonly, the read&write dimension is no more relevant.<br/>
	 * If the access is not readonly, read&write condition should be evaluated. <br/>
	 * 
	 * @see org.olat.course.nodes.GenericCourseNode#calcAccessAndVisibility(org.olat.course.condition.interpreter.ConditionInterpreter, org.olat.course.run.userview.NodeEvaluation)
	 */
	@Override
	public void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval) {
		super.calcAccessAndVisibility(ci, nodeEval);
	
		if (hasCustomPreConditions()) {
			boolean editor = (getPreConditionEdit().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionEdit()));
			nodeEval.putAccessStatus(EDIT_CONDITION, editor);
		}
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		// mark the subscription to this node as deleted
		SubscriptionContext subsContext = WikiManager.createTechnicalSubscriptionContextForCourse(course.getCourseEnvironment(), this);
		CoreSpringFactory.getImpl(NotificationsManager.class).delete(subsContext);
	
	}
	
	@Override
	public List<NodeRightType> getNodeRightTypes() {
		return NODE_RIGHT_TYPES;
	}
}