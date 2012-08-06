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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.wiki.WikiEditController;
import org.olat.course.nodes.wiki.WikiRunController;
import org.olat.course.repository.ImportReferencesController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiToZipUtils;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class WikiCourseNode extends AbstractAccessableCourseNode {

	public static final String TYPE = "wiki";
	private Condition preConditionEdit;

	/**
	 * Default constructor for course node of type single page
	 */
	public WikiCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}

	public void updateModuleConfigDefaults(boolean isNewNode) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// use defaults for new course building blocks
			config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, false);
			config.setConfigurationVersion(1);
		}
	}
	
	@Override
	public void postImport(CourseEnvironmentMapper envMapper) {
		super.postImport(envMapper);
		postImportCondition(preConditionEdit, envMapper);
	}

	@Override
	public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		super.postExport(envMapper, backwardsCompatible);
		postExportCondition(preConditionEdit, envMapper, backwardsCompatible);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createEditController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl, org.olat.course.ICourse)
	 */
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, ICourse course,UserCourseEnvironment euce) {
		WikiEditController childTabCntrllr = new WikiEditController(getModuleConfiguration(), ureq, wControl, this, course,euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, course.getCourseEnvironment()
				.getCourseGroupManager(), euce, childTabCntrllr);

	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createNodeRunConstructionResult(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation)
	 */
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		if(ne.isCapabilityAccessible("access")) {
			WikiRunController wikiController = new WikiRunController(wControl, ureq, this, userCourseEnv.getCourseEnvironment(), ne);
			return new NodeRunConstructionResult(wikiController);
		}
		Controller controller = MessageUIFactory.createInfoMessage(ureq, wControl, null, this.getNoAccessExplanation());
		return new NodeRunConstructionResult(controller, null, null, null);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid()
	 */
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


	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		//only here we know which translator to take for translating condition error messages
		String translatorStr = Util.getPackageName(WikiEditController.class);
		List sds = isConfigValidWithTranslator(cev, translatorStr,getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}
	
	
	/**
	 * @see org.olat.course.nodes.CourseNode#getReferencedRepositoryEntry()
	 */
	public RepositoryEntry getReferencedRepositoryEntry() {
		//"false" because we do not want to be strict, but just indicate whether
		// the reference still exists or not
		RepositoryEntry entry = WikiEditController.getWikiReference(getModuleConfiguration(), false);
		return entry;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#needsReferenceToARepositoryEntry()
	 */
	public boolean needsReferenceToARepositoryEntry() {
		//wiki is a repo entry
		return true;
	}
	
	/**
	 * @see org.olat.course.nodes.GenericCourseNode#exportNode(File, ICourse)
	 */
	public void exportNode(File exportDirectory, ICourse course) {
		RepositoryEntry re = WikiEditController.getWikiReference(getModuleConfiguration(), false);
		if (re == null) return;
		File fExportDirectory = new File(exportDirectory, getIdent());
		fExportDirectory.mkdirs();
		RepositoryEntryImportExport reie = new RepositoryEntryImportExport(re, fExportDirectory);
		reie.exportDoExport();
	}
	
	

	/**
	 * @see org.olat.course.nodes.GenericCourseNode#importNode(File, ICourse, boolean, UserRequest, WindowControl)
	 */
	public Controller importNode(File importDirectory, ICourse course, boolean unattendedImport, UserRequest ureq, WindowControl wControl) {
		File importSubdir = new File(importDirectory, getIdent());
		RepositoryEntryImportExport rie = new RepositoryEntryImportExport(importSubdir);
		if (!rie.anyExportedPropertiesAvailable()) return null;

		// do import referenced repository entries
		if (unattendedImport) {
			Identity admin = BaseSecurityManager.getInstance().findIdentityByName("administrator");
			ImportReferencesController.doImport(rie, this, ImportReferencesController.IMPORT_WIKI ,true, admin);
			return null;
		} else {
			return new ImportReferencesController(ureq, wControl, this, ImportReferencesController.IMPORT_WIKI,rie);
		}
	}
	


	/**
	 * @see org.olat.course.nodes.GenericCourseNode#archiveNodeData(java.util.Locale, org.olat.course.ICourse, java.io.File, java.lang.String)
	 */
	public boolean archiveNodeData(Locale locale, ICourse course, File exportDirectory, String charset) {
		String repoRef = (String)this.getModuleConfiguration().get("reporef");
		OLATResourceable ores = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repoRef, true).getOlatResource();
		
		if(WikiManager.getInstance().getOrLoadWiki(ores).getAllPagesWithContent().size()>0) {
		  //OK, there is somthing to archive 
		  VFSContainer exportContainer = new LocalFolderImpl(exportDirectory);
		  VFSContainer wikiExportContainer = (VFSContainer)exportContainer.resolve(WikiManager.WIKI_RESOURCE_FOLDER_NAME);
		  if(wikiExportContainer == null){
			  wikiExportContainer = exportContainer.createChildContainer(WikiManager.WIKI_RESOURCE_FOLDER_NAME);
		  }
		  String exportDirName = getShortTitle()+"_"+Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()));
		  VFSContainer destination = wikiExportContainer.createChildContainer(exportDirName);
			if (destination==null) {
				exportDirName = VFSManager.rename(wikiExportContainer, exportDirName);
				destination = wikiExportContainer.createChildContainer(exportDirName);
			}
			if (destination==null) {
				Tracing.logError("archiveNodeData: Could not create destination directory: wikiExportContainer="+wikiExportContainer+", exportDirName="+exportDirName, getClass());
				return false;
			}
			
		  VFSContainer container = WikiManager.getInstance().getWikiContainer(ores, WikiManager.WIKI_RESOURCE_FOLDER_NAME);
		  if(container!=null) { //the container could be null if the wiki is an old empty one - so nothing to archive
		    VFSContainer parent = container.getParentContainer();
		    VFSLeaf wikiZip = WikiToZipUtils.getWikiAsZip(parent);
		    destination.copyFrom(wikiZip);
		  }
		  return true;
		}		
	  //empty wiki, no need to archive
  	return false;
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
	protected void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval) {
	  super.calcAccessAndVisibility(ci, nodeEval);
		
	  boolean editor = (getPreConditionEdit().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionEdit()));
		nodeEval.putAccessStatus("editarticle", editor);		
	}
		
	
	public void cleanupOnDelete(ICourse course) {
		// mark the subscription to this node as deleted
		SubscriptionContext subsContext = WikiManager.createTechnicalSubscriptionContextForCourse(course.getCourseEnvironment(), this);
		NotificationsManager.getInstance().delete(subsContext);
	
	}

}