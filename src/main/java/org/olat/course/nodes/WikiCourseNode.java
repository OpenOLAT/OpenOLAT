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
import java.util.zip.ZipOutputStream;

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
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.wiki.WikiEditController;
import org.olat.course.nodes.wiki.WikiRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.types.WikiResource;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.wiki.Wiki;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiToZipUtils;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class WikiCourseNode extends AbstractAccessableCourseNode {
	private static final long serialVersionUID = -5800975339569440113L;

	private static final Logger log = Tracing.createLoggerFor(WikiCourseNode.class);

	public static final String TYPE = "wiki";
	private Condition preConditionEdit;

	/**
	 * Default constructor for course node of type single page
	 */
	public WikiCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}

	@Override
	public void updateModuleConfigDefaults(boolean isNewNode) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// use defaults for new course building blocks
			config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, false);
			config.setConfigurationVersion(1);
		}
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
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course,UserCourseEnvironment euce) {
		WikiEditController childTabCntrllr = new WikiEditController(getModuleConfiguration(), ureq, wControl, this, course,euce);
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, euce, childTabCntrllr);

	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		if(ne.isCapabilityAccessible("access")) {
			WikiRunController wikiController = new WikiRunController(wControl, ureq, this, userCourseEnv, ne);
			return wikiController.createNodeRunConstructionResult();
		}
		Controller controller = MessageUIFactory.createInfoMessage(ureq, wControl, null, this.getNoAccessExplanation());
		return new NodeRunConstructionResult(controller);
	}

	@Override
	public StatusDescription isConfigValid() {
		/*
		 * first check the one click cache
		 */
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

	public Condition getPreConditionEdit() {
		if (preConditionEdit == null) {
			preConditionEdit = new Condition();
		}
		preConditionEdit.setConditionId("editarticle");
		return preConditionEdit;
	}

	/**
	 * 
	 * @param preConditionEdit
	 */
	public void setPreConditionEdit(Condition preConditionEdit) {
		if (preConditionEdit == null) {
			preConditionEdit = getPreConditionEdit();
		}
		preConditionEdit.setConditionId("editarticle");
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
	protected void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval) {
	  super.calcAccessAndVisibility(ci, nodeEval);
		
	  boolean editor = (getPreConditionEdit().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionEdit()));
		nodeEval.putAccessStatus("editarticle", editor);		
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		// mark the subscription to this node as deleted
		SubscriptionContext subsContext = WikiManager.createTechnicalSubscriptionContextForCourse(course.getCourseEnvironment(), this);
		NotificationsManager.getInstance().delete(subsContext);
	
	}
}