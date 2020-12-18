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
package org.olat.course.editor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.ObjectCloner;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.editor.PublishStepCatalog.CategoryLabel;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.RunMainController;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.course.tree.PublishTreeModel;
import org.olat.properties.Property;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.manager.CatalogManager;
import org.olat.resource.references.Reference;
import org.olat.resource.references.ReferenceManager;
import org.olat.user.UserManager;

/**
 * Description:<br>
 * Manages data manipulation in conjunction with publishing course node changes
 * or changes of general access.
 * 
 * <P>
 * Initial Date:  23.01.2008 <br>
 * @author patrickb
 */
public class PublishProcess {
	
	private static final Logger log = Tracing.createLoggerFor(PublishProcess.class);
	
	private static final String PACKAGE = Util.getPackageName(PublishProcess.class);
	private final Translator translator;
	
	/*
	 * publishing means 
	 */
	private CourseEditorTreeModel editorTreeModel;
	private ICourse course;
	private RepositoryEntry repositoryEntry;
	//to be replaced
	private final PublishTreeModel publishTreeModel;
	
	/*
	 * intermediate structures to calculate next course run
	 */
	private List<CourseEditorTreeNode> editorModelDeletedNodes;
	private List<CourseEditorTreeNode> editorModelInsertedNodes;
	private List<CourseEditorTreeNode> editorModelModifiedNodes;
	private Structure resultingCourseRun;
	private List<String> originalNodeIdsToPublish;

	private final PublishEvents publishEvents = new PublishEvents();

	
	PublishProcess(ICourse course, CourseEditorTreeModel cetm, Locale locale) {
		//o_clusterOK yb guido: it save to hold a reference to the course inside the editor
		this.course = course;
		this.editorTreeModel = cetm;
		publishTreeModel = new PublishTreeModel(editorTreeModel, course.getRunStructure());
		repositoryEntry = RepositoryManager.getInstance().lookupRepositoryEntry(course, false);
		translator = Util.createPackageTranslator(PublishProcess.class, locale);
	}
	
	public static PublishProcess getInstance(ICourse course, CourseEditorTreeModel cetm, Locale locale){
		return new PublishProcess(course, cetm, locale);
	}
	
	public PublishEvents getPublishEvents() {
		return publishEvents;
	}

	/**
	 * first step in publishing course editor nodes.<br>
	 * next step is testPublishSet, see method for more details.
	 * @param nodeIdsToPublish
	 */
	public void createPublishSetFor(List<String> nodeIdsToPublish) {
		this.originalNodeIdsToPublish = nodeIdsToPublish;
		// append new nodes' subnodes
		int selectCount = nodeIdsToPublish.size();
		for (int i = 0; i < selectCount; i++) {
			// avoid using iterator here so we can modify the Collection
			String nodeId = nodeIdsToPublish.get(i);
			CourseEditorTreeNode cetn = editorTreeModel.getCourseEditorNodeById(nodeId);
			if (cetn != null && (cetn.isNewnode() || cetn.isDeleted() || publishTreeModel.isMoved(cetn))) {
				appendPublishableSubnodeIds(cetn, nodeIdsToPublish);
			}
		}

		/*
		 * generatePublishSet, testPublishSet, applyPublishSet
		 */
		/*
		 * several book keeping lists which are also used to modify the course
		 * editor model after the new runstructure is generated into ram.
		 */
		editorModelDeletedNodes = new ArrayList<>();
		editorModelInsertedNodes = new ArrayList<>();
		editorModelModifiedNodes = new ArrayList<>();
		resultingCourseRun = new Structure();
		// has side effect on the above editorModelxxxNodes and the
		// resultingCourseRun;
		calculatePublishSet(nodeIdsToPublish);
	}
	
	

	/**
	 * @param nodesIdsToPublish
	 * @param resultingCourseRun
	 * @param editorModelDeletedNodes
	 * @param editorModelInsertedNodes
	 * @param editorModelModifiedNodes
	 */
	private void calculatePublishSet(List<String> nodesIdsToPublish) {
		/*
		 * START NEW STYLE PUBLISH ................................................. -
		 * visit each node (breadth first) - if node is selected to be published ->
		 * publish and take into account if the node exists already in the
		 * runstructure (keep ident or not). And also if node should get deleted add
		 * it to a list of nodes to be deleted. This is needed for a later clean-up
		 * and archive. ............................. - if node is not selected to
		 * be published, but exists already in the runstructure it must be added to
		 * the tmp-runstructure as it is in the existing runstructure.
		 * ..................................................
		 */
		// start point for node publish visitor
		CourseEditorTreeNode editorRoot = (CourseEditorTreeNode) editorTreeModel.getRootNode();

		// the active runstructure and the new created runstructure
		Structure existingCourseRun = course.getRunStructure();
		// breadth first!
		boolean visitChildrenFirst = false;
		/*
		 * the tree is visited and the book keeping lists are filled. the visitor
		 * itself does not delete or modify neither runstructure nor editor tree
		 * model. The whole complexity of published is encapsulated in the visitor.
		 */
		Visitor nodePublishV = new NodePublishVisitor(editorRoot, nodesIdsToPublish, existingCourseRun);
		TreeVisitor tv = new TreeVisitor(nodePublishV, editorRoot, visitChildrenFirst);
		tv.visitAll();
	}


	/**
	 * starting from each user selected to-publish node all also affected nodes
	 * are added to the list of nodes to be published.
	 * 
	 * @param cetn
	 * @param nodesToPublish
	 */
	private void appendPublishableSubnodeIds(CourseEditorTreeNode cetn, List<String> nodesToPublish) {
		for (int i = 0; i < cetn.getChildCount(); i++) {
			CourseEditorTreeNode child = (CourseEditorTreeNode) cetn.getChildAt(i);
			if (child.hasPublishableChanges()) nodesToPublish.add(child.getIdent());
			appendPublishableSubnodeIds(child, nodesToPublish);
		}
	}
	
	
	/**
	 * can only be called after createPublishSetFor method. The method calculates the
	 * resulting runstructure, and checks them against error and warning messages.
	 * These are returned as a list of StatusDescriptions.<br>
	 * If status ok -> apply the publish set -> this changes the course effectively
	 *  
	 * @param locale
	 * @return
	 */
	public PublishSetInformations testPublishSet(Locale locale) {
		//check for valid references to tests, resource folder, wiki
		List<StatusDescription> damagedRefsInsertedNodes = checkRefs(editorModelInsertedNodes);
		if (!damagedRefsInsertedNodes.isEmpty()) {
			// abort testing as a blocking error found!
			StatusDescription[] status = new StatusDescription[damagedRefsInsertedNodes.size()];
			status = damagedRefsInsertedNodes.toArray(status);
			return new PublishSetInformations(status);
		}
		List<StatusDescription> damagedRefsModifiedNodes = checkRefs(editorModelModifiedNodes);
		if (!damagedRefsModifiedNodes.isEmpty()) {
			// abort testing as a blocking error found
			StatusDescription[] status = new StatusDescription[damagedRefsModifiedNodes.size()];
			status = damagedRefsModifiedNodes.toArray(status);
			return new PublishSetInformations(status);
		}

		CourseNode clonedCourseNode = (CourseNode) ObjectCloner.deepCopy(resultingCourseRun.getRootNode());
		CourseEditorTreeNode clonedRoot = new CourseEditorTreeNode(clonedCourseNode);
		convertInCourseEditorTreeNode(clonedRoot, clonedCourseNode);
		clonedCourseNode.removeAllChildren(); // Do remove all children after convertInCourseEditorTreeNode
		CourseEditorTreeModel cloneCETM = new CourseEditorTreeModel();
		cloneCETM.setRootNode(clonedRoot);

		/*
		 * now we have the cloned editor tree synchronized with the pre-published
		 * runstructure. The cloned editor tree is used within a new
		 * CourseEditorEnvironment which is placed in a new Editor User Course
		 * Session for evaluation only. This is like opening the runstructure in a
		 * virtual course editor for using the validation facilities of the editor
		 * environment.
		 */
		CourseEditorEnv tmpCEV = new CourseEditorEnvImpl(cloneCETM,
				course.getCourseEnvironment().getCourseGroupManager(), locale, NodeAccessType.of(course));
		// the resulting object is not needed, but constructor makes
		// initializations within tmpCEV!! thus important step.
		new EditorUserCourseEnvironmentImpl(tmpCEV, null);
		//
		tmpCEV.setCurrentCourseNodeId(cloneCETM.getRootNode().getIdent());
		tmpCEV.validateCourse();
		StatusDescription[] status = tmpCEV.getCourseStatus();
		// check if the resulting course contains cycles.
		Set<String> nodesInCycle = tmpCEV.listCycles();
		if (!nodesInCycle.isEmpty()) {
			// there are nodes generating cylces -> error! this is not a publishable
			// set!
			StringBuilder sb = new StringBuilder();
			for (String id: nodesInCycle) {
				String title = editorTreeModel.getCourseEditorNodeById(id).getTitle();
				sb.append("<b>").append(title).append("</b> ").append("(id:").append(id).append(")<br>");
			}
			StatusDescription sd = new StatusDescription(ValidationStatus.ERROR, "pbl.error.cycles", "pbl.error.cycles",
					new String[] { sb.toString() }, PACKAGE);
			status = new StatusDescription[] { sd };
		} else {
			/*
			 * these are now status description as they are helpful in the editor
			 * context. The same errors in the publish context have a kind of
			 * different meaning --- Let the nodes explain what it means in publish
			 * mode.
			 */
			for (int i = 0; i < status.length; i++) {
				StatusDescription description = status[i];
				String nodeId = description.getDescriptionForUnit();
				CourseNode cn = cloneCETM.getCourseNode(nodeId);
				status[i] = cn.explainThisDuringPublish(description);
			}
		}
		
		List<StatusDescription> updateNotifications = testUpdateSet(tmpCEV);
		return new PublishSetInformations(status, updateNotifications);
	}
	
	public List<StatusDescription> testUpdateSet(CourseEditorEnv cev) {
		//check for valid references to tests, resource folder, wiki
		List<StatusDescription> notifications = checkUpdates(editorModelInsertedNodes, cev);
		notifications.addAll(checkUpdates(editorModelModifiedNodes, cev));
		return notifications;
	}
	

	/**
	 * Checks references of coursenodes.
	 * 
	 * @param courseEditorTreeNodes
	 * @return boolean
	 */
	private List<StatusDescription> checkRefs(List<CourseEditorTreeNode> courseEditorTreeNodes) {
		// course Editor Nodes With Damaged Reference
		List<StatusDescription> cetnDamaged = new ArrayList<>();
		for (Iterator<CourseEditorTreeNode> iter = courseEditorTreeNodes.iterator(); iter.hasNext();) {
			CourseEditorTreeNode cetn = iter.next();
			CourseNode cn = cetn.getCourseNode();
			/*
			 * for those coursenodes which need a reference to a repository entry to
			 * function properly, check that the reference is valid
			 */
			if (cn.needsReferenceToARepositoryEntry()) {
				RepositoryEntry referencedEntry = cn.getReferencedRepositoryEntry();
				if (referencedEntry == null) {
					cetnDamaged.add(new StatusDescription(ValidationStatus.ERROR, "pbl.error.refs", "pbl.error.refs", new String[] { cetn.getTitle()
							+ "(id:" + cetn.getIdent() + " )" }, PACKAGE));
				}
			}
		}
		return cetnDamaged;
	}
	
	private List<StatusDescription> checkUpdates(List<CourseEditorTreeNode> courseEditorTreeNodes, CourseEditorEnv cev) {
		List<StatusDescription> notifications = new ArrayList<>();
		for (Iterator<CourseEditorTreeNode> iter = courseEditorTreeNodes.iterator(); iter.hasNext();) {
			CourseEditorTreeNode cetn = iter.next();
			CourseNode cn = cetn.getCourseNode();
			List<StatusDescription> nodeNotes = cn.publishUpdatesExplanations(cev);
			if(nodeNotes != null && !nodeNotes.isEmpty()) {
				notifications.addAll(nodeNotes);
			}
		}
		return notifications;
	}

	/**
	 * Convert all CourseNodes into CourseEditorTreeNode
	 * 
	 * @param courseEditorTreeNode  Parent CourseEditorTreeNode
	 * @param node                  Current course node which will be converted
	 */
	private void convertInCourseEditorTreeNode(CourseEditorTreeNode courseEditorTreeNode, CourseNode node) {
		int childCnt = node.getChildCount();
		for (int i = 0; i < childCnt; i++) {
			CourseNode childNode = (CourseNode) node.getChildAt(i);
			CourseEditorTreeNode newEditorNode = new CourseEditorTreeNode(childNode);
			courseEditorTreeNode.addChild(newEditorNode);
			convertInCourseEditorTreeNode(newEditorNode, childNode);
			childNode.removeAllChildren(); // remove all children after calling convertInCourseEditorTreeNode
		}
	}
	
	/**
	 * 
	 * @param identity
	 * @param locale
	 * @param newCourse Optimization for new courses, it doesn't call upddateOnPublish of inserted/updated course nodes
	 */
	public void applyPublishSet(Identity identity, Locale locale, boolean newCourse) {
		// the active runstructure and the new created runstructure
		Structure existingCourseRun = course.getRunStructure();
		EventBus orec = CoordinatorManager.getInstance().getCoordinator().getEventBus();
		/*
		 * use book keeping lists for publish event
		 */
		Set<String> deletedCourseNodeIds = new HashSet<>();
		if (!editorModelDeletedNodes.isEmpty()) {
			for (Iterator<CourseEditorTreeNode> iter = editorModelDeletedNodes.iterator(); iter.hasNext();) {
				CourseEditorTreeNode cetn = iter.next();
				CourseNode cn = cetn.getCourseNode();
				deletedCourseNodeIds.add(cn.getIdent());
			}
		}
		Set<String> insertedCourseNodeIds = new HashSet<>();
		if (!editorModelInsertedNodes.isEmpty()) {
			for (Iterator<CourseEditorTreeNode> iter = editorModelInsertedNodes.iterator(); iter.hasNext();) {
				CourseEditorTreeNode cetn = iter.next();
				CourseNode cn = cetn.getCourseNode();
				insertedCourseNodeIds.add(cn.getIdent());
			}
		}
		Set<String> modifiedCourseNodeIds = new HashSet<>();
		if (!editorModelModifiedNodes.isEmpty()) {
			for (Iterator<CourseEditorTreeNode> iter = editorModelModifiedNodes.iterator(); iter.hasNext();) {
				CourseEditorTreeNode cetn = iter.next();
				CourseNode cn = cetn.getCourseNode();
				modifiedCourseNodeIds.add(cn.getIdent());
			}
		}
		/*
		 * broadcast PRE PUBLISH event that a publish will take place
		 */
		PublishEvent beforePublish = new PublishEvent(course, identity);
		beforePublish.setDeletedCourseNodeIds(deletedCourseNodeIds);
		beforePublish.setInsertedCourseNodeIds(insertedCourseNodeIds);
		beforePublish.setModifiedCourseNodeIds(modifiedCourseNodeIds);
		beforePublish.setState(PublishEvent.PRE_PUBLISH);
		// old course structure accessible
		orec.fireEventToListenersOf(beforePublish, course);
		// remove new nodes which were marked as delete and deletion is published.	
		UserManager um = UserManager.getInstance();
		String charset = um.getUserCharset(identity);
		if (!editorModelDeletedNodes.isEmpty()) {
			for (Iterator<CourseEditorTreeNode> iter = editorModelDeletedNodes.iterator(); iter.hasNext();) {
				CourseEditorTreeNode cetn = iter.next();
				CourseNode cn = cetn.getCourseNode();
				CourseNode oldCn = existingCourseRun.getNode(cetn.getIdent());
				// moved node with a parent deleted (deletion published) -> oldCn ==
				// null
				if (oldCn != null) {
					if (!(cn.getIdent().equals(oldCn.getIdent()))) { throw new AssertException("deleted cn.getIdent != oldCn.getIdent"); }
				}
				cetn.removeFromParent();
				if (!cetn.isNewnode() && oldCn != null) {
					// only clean up and archive of nodes which were already in run
					// save data, remove references
					deleteRefs(oldCn);
					archiveDeletedNode(identity, cn, oldCn, locale, charset);
					// 2) delete all user data
					oldCn.cleanupOnDelete(course);
				}
			}
		}
		/*
		 * mark modified ones as no longer dirty
		 */
		if (!editorModelModifiedNodes.isEmpty()) {
			for (Iterator<CourseEditorTreeNode> iter = editorModelModifiedNodes.iterator(); iter.hasNext();) {
				CourseEditorTreeNode cetn = iter.next();
				CourseNode cn = cetn.getCourseNode();
				CourseNode oldCn = existingCourseRun.getNode(cetn.getIdent());
				// moved node with a parent deleted (deletion published) -> oldCn ==
				// null
				if (oldCn != null) {
					if (!(cn.getIdent().equals(oldCn.getIdent()))) { throw new AssertException("deleted cn.getIdent != oldCn.getIdent"); }
				}
				cetn.setDirty(false);
				//
				updateRefs(cn, oldCn);
			}
		}
		/*
		 * mark newly published ones is no longer new and dirty
		 */
		if (!editorModelInsertedNodes.isEmpty()) {
			for (Iterator<CourseEditorTreeNode> iter = editorModelInsertedNodes.iterator(); iter.hasNext();) {
				CourseEditorTreeNode cetn = iter.next();
				CourseNode cn = cetn.getCourseNode();
				CourseNode oldCn = existingCourseRun.getNode(cetn.getIdent());
				if (oldCn != null) { throw new AssertException("new node has an oldCN??"); }
				cetn.setDirty(false);
				cetn.setNewnode(false);
				//
				updateRefs(cn, null);
			}
		}
		/*
		 * saving
		 */
		long pubtimestamp = System.currentTimeMillis();
		editorTreeModel.setLatestPublishTimestamp(pubtimestamp);
		// set the new runstructure and save it.
		existingCourseRun.setRootNode(resultingCourseRun.getRootNode());
		CourseFactory.saveCourse(course.getResourceableId());
		
		//on old course, apply update to published nodes
		if(!newCourse) {
			for (CourseEditorTreeNode cetn:editorModelInsertedNodes) {
				cetn.getCourseNode().updateOnPublish(locale, course, identity, publishEvents);
			}
			for (CourseEditorTreeNode cetn:editorModelModifiedNodes) {
				cetn.getCourseNode().updateOnPublish(locale, course, identity, publishEvents);
			}
		}
		
		//commit all changes before sending an event
		DBFactory.getInstance().commitAndCloseSession();
		
		NodeAccessService nodeAccessService = CoreSpringFactory.getImpl(NodeAccessService.class);
		nodeAccessService.onCoursePublished(course);
		
		/*
		 * broadcast event
		 */
		PublishEvent publishEvent = new PublishEvent(course, identity);
		publishEvent.setDeletedCourseNodeIds(deletedCourseNodeIds);
		publishEvent.setInsertedCourseNodeIds(insertedCourseNodeIds);
		publishEvent.setModifiedCourseNodeIds(modifiedCourseNodeIds);
		// new course structure accessible
		// CourseFactory is one listener, which removes the course from the
		// cache.
		orec.fireEventToListenersOf(publishEvent, course);
		/*
		 * END NEW STYLE PUBLISH
		 */
	}
	
	private void archiveDeletedNode(Identity identity, CourseNode cn, CourseNode oldCn, Locale locale, String charset) {
		File exportDirectory = CourseFactory.getOrCreateDataExportDirectory(identity, course.getCourseTitle());
		String archiveName = cn.getType() + "_"
				+ StringHelper.transformDisplayNameToFileSystemName(cn.getShortName())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis())) + ".zip";

		File exportFile = new File(exportDirectory, archiveName);
		try(FileOutputStream fileStream = new FileOutputStream(exportFile);
				ZipOutputStream exportStream = new ZipOutputStream(fileStream)) {
			oldCn.archiveNodeData(locale, course, null, exportStream, "", charset);
		} catch (IOException e) {
			log.error("", e);
		}
	}

	/**
	 * Update references of a course node in the run. If the course node is new,
	 * simply insert new references. Otherwise, delete old references and insert
	 * the new references.
	 * 
	 * @param updatedNode The updated course node
	 * @param oldNode The old course node corresponding to the new node. May be
	 *          null if updatedNode is a new node.
	 */
	private void updateRefs(CourseNode updatedNode, CourseNode oldNode) {
		if (oldNode != null) deleteRefs(oldNode);
		if (updatedNode.needsReferenceToARepositoryEntry()) {
			RepositoryEntry referencedEntry = updatedNode.getReferencedRepositoryEntry();
			if (referencedEntry == null) throw new AssertException("Could not fetch referenced entry where an entry is expected.");
			// if there is an entry, add the reference
			addRef(updatedNode, referencedEntry.getOlatResource());
		}
	}

	/**
	 * Add reference to resourceable held by courseNode.
	 * 
	 * @param courseNode
	 * @param resourceable
	 */
	private void addRef(CourseNode courseNode, OLATResourceable resourceable) {
		CoreSpringFactory.getImpl(ReferenceManager.class).addReference(course, resourceable, courseNode.getIdent());
	}

	/**
	 * Delete references to resourceables of node with ident courseNodeIdent.
	 * 
	 * @param courseNode
	 */
	private void deleteRefs(CourseNode courseNode) {
		ReferenceManager refM = CoreSpringFactory.getImpl(ReferenceManager.class);
		List<Reference> courseRefs = refM.getReferences(course);
		for (Iterator<Reference> iter = courseRefs.iterator(); iter.hasNext();) {
			Reference ref = iter.next();
			if (!ref.getUserdata().equals(courseNode.getIdent())) continue;
			refM.delete(ref);
			break;
		}
	}

	protected void publishToCatalog(String choiceValue, List<CategoryLabel> labels) {
		
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		CourseNode rootNode = course.getRunStructure().getRootNode();
		Property prop = cpm.findCourseNodeProperty(rootNode, null, null, "catalog-choice");
		if(prop == null) {
			prop = cpm.createCourseNodePropertyInstance(rootNode, null, null, "catalog-choice", null, null, choiceValue, null);
			cpm.saveProperty(prop);
		} else {
			prop.setStringValue(choiceValue);
			cpm.updateProperty(prop);
		}

		CatalogManager cm = CoreSpringFactory.getImpl(CatalogManager.class);
		List<CatalogEntry> refParentCategories = cm.getCatalogCategoriesFor(repositoryEntry);
		
		a_a:
		for(CategoryLabel label:labels) {
			CatalogEntry category = label.getCategory();
			CatalogEntry parentCategory = label.getParentCategory();
			if(label.isDeleted()) {
				//test
				if(category.getKey() != null) {
					List<CatalogEntry> children = cm.getChildrenOf(category);
					for(CatalogEntry child:children) {
						if(child.getRepositoryEntry() != null && child.getRepositoryEntry().equalsByPersistableKey(repositoryEntry)) {
							cm.deleteCatalogEntry(child);
						}
					}
				}
			} else if(category.getKey() == null) {
				//it's a new entry -> check if not already in catalog at this position
				for(Iterator<CatalogEntry> refIt=refParentCategories.iterator(); refIt.hasNext(); ) {
					CatalogEntry refParentCategory = refIt.next();
					if(refParentCategory.equalsByPersistableKey(parentCategory)) {
						refIt.remove();
						break a_a;
					}
				}
				cm.addCatalogEntry(parentCategory, category);
			} else {
				for(Iterator<CatalogEntry> refIt=refParentCategories.iterator(); refIt.hasNext(); ) {
					CatalogEntry refParentCategory = refIt.next();
					if(refParentCategory.equalsByPersistableKey(category)) {
						refIt.remove();
					}
				}
			}
		}
	}

	void clearPublishSet() {
		resultingCourseRun = null;
		// clear publis datastructures
		editorModelDeletedNodes = null;
		editorModelInsertedNodes = null;
		editorModelModifiedNodes = null;	
	}

	

	String assemblePublishConfirmation() {
		List<String> nodeIdsToPublish = originalNodeIdsToPublish;
		StringBuilder msg = new StringBuilder();

		OLATResourceable courseRunOres = OresHelper.createOLATResourceableInstance(RunMainController.ORES_TYPE_COURSE_RUN, repositoryEntry.getOlatResource().getResourceableId());
		int cnt = CoordinatorManager.getInstance().getCoordinator().getEventBus().getListeningIdentityCntFor(courseRunOres) -1; // -1: Remove myself from list
		if (cnt > 0) {		
			msg.append(translate("pbl.confirm.users", String.valueOf(cnt)));			
		} else {
			msg.append(translator.translate("pbl.confirm"));
		}
		
		if(nodeIdsToPublish != null && !nodeIdsToPublish.isEmpty()) {
			msg.append("<ul class='list-unstyled'>");

			CourseEditorTreeModel cetm = course.getEditorTreeModel();
			for (int i = 0; i < nodeIdsToPublish.size(); i++) {
				msg.append("<li>");
				String nodeId = nodeIdsToPublish.get(i);
				CourseEditorTreeNode cetn = (CourseEditorTreeNode) cetm.getNodeById(nodeId);
				CourseNode cn = cetm.getCourseNode(nodeId);
				if (cetn.isDeleted() && !cetn.isNewnode()) {
					msg.append("<i class='o_icon o_icon_delete_item'> </i> ");
				} else {
					CourseNodeConfiguration nodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(cn.getType());
					if(nodeConfig != null) {
						msg.append("<i class='o_icon ").append(nodeConfig.getIconCSSClass()).append("'> </i> ");
					}
				}
				msg.append(cn.getShortTitle()).append("</li>");
			}
			msg.append("</ul>");
		}
		
		return msg.toString();
	}

	boolean hasPublishableChanges() {
		return publishTreeModel.hasPublishableChanges();
	}


	public PublishTreeModel getPublishTreeModel() {
		return publishTreeModel;
	}

	public void changeGeneralAccess(Identity author, RepositoryEntryStatusEnum access, boolean allUsers, boolean guests) {
		RepositoryManager.getInstance().setAccess(repositoryEntry, access, allUsers, guests);
		MultiUserEvent modifiedEvent = new EntryChangedEvent(repositoryEntry, author, Change.modifiedAtPublish, "publish");
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, repositoryEntry);
	}
	
	private String translate(String i18n, String arg) {
		return translator.translate(i18n, new String[]{arg});
	}

	
	CourseEditorTreeModel getCourseEditorTreeModel() {
		return editorTreeModel;
	}


	/**
	 * Description:<br>
	 * Visitor for traversing the editor model tree structure and generate a run
	 * structure.<br>
	 * The visitor is non-destructive on the editor model tree and also on the
	 * existing runstructure. It only generates a new run structure. If this new
	 * run structure is persisted one must take in account the provided book
	 * keeping lists of nodes which should be deleted and/or lists with nodes
	 * which should have cleared the dirty and new status.<br>
	 * <P>
	 * Initial Date: 09.08.2006 <br>
	 * 
	 * @author patrickb
	 */
	private final class NodePublishVisitor implements Visitor {
		private CourseEditorTreeNode root;
		private Structure existingRun;
		private List<String> publishNodeIds;
		private List<CourseEditorTreeNode> skippableNodes;

		private NodePublishVisitor(CourseEditorTreeNode root, List<String> userSelectedNodeIdsToPublish, Structure existingRun) {
			/*
			 * editorRoot, resultingCourseRun, editorModelDeletedNodes,
			 * editorModelPublishedNodes, existingCourseRun, nodesIdsToPublish,
			 * editorModelSkipableNodes
			 */

			super();
			this.root = root;
			this.existingRun = existingRun;
			this.publishNodeIds = userSelectedNodeIdsToPublish;
			// internal use
			this.skippableNodes = new ArrayList<>();
		}

		@Override
		public void visit(INode node) {
			/*
			 * DO NOT add or delete nodes via editorTreeModel, .....................
			 * or the CourseEditorTreeNode cetn !! .................................
			 */
			CourseEditorTreeNode cetn = (CourseEditorTreeNode) node;
      if (cetn == root && publishNodeIds.contains(cetn.getIdent()) ) {
        // root node changed and published
        CourseNode clone = (CourseNode)XStreamHelper.xstreamClone(cetn.getCourseNode());
        resultingCourseRun.setRootNode(clone);
        editorModelModifiedNodes.add(cetn);
        return;
			}
      if (cetn == root) {
      	// root node
        CourseNode clone = (CourseNode)XStreamHelper.xstreamClone(cetn.getCourseNode());
        resultingCourseRun.setRootNode(clone);
        return;
      }
			/*
			 * check that root exist in newRunStruct
			 */
			if (resultingCourseRun.getRootNode() == null) { throw new AssertException("No Root node??"); }
			// node is selected by user to be published
			boolean publishNode = publishNodeIds.contains(cetn.getIdent());
			// this node is already in the runstructure -> keep ident!
			boolean alreadyInRun = null != existingRun.getNode(cetn.getCourseNode().getIdent());
			// node has changed and exists in the runstructure
			boolean dirtyOnly = cetn.isDirty() && !cetn.isDeleted() && !cetn.isNewnode();
			boolean unchanged = !cetn.isDirty() && !cetn.isDeleted() && !cetn.isNewnode();

			// check if this node is not already somehow no longer interesting
			if (skippableNodes.contains(cetn) || editorModelDeletedNodes.contains(cetn)) {
				// skip
				return;
			}
			if (!publishNode && alreadyInRun) {
				if (unchanged) {
					// already published, add it as it is. Silent "re-publish"
					addNodeTo(resultingCourseRun, cetn);
				} else {
					// changed in edit but not published => take old from existingRun
					addNodeTo(resultingCourseRun, existingRun.getNode(cetn.getIdent()), cetn);
				}
			} else if (publishNode && !alreadyInRun) {
				if (dirtyOnly) {
					// publish modified node which was in the run once. Then moved under a
					// new node and its former parent is deleted.
					addNodeTo(resultingCourseRun, cetn);
					editorModelModifiedNodes.add(cetn);
				} else if (cetn.isDeleted()) {
					// publish deletion of a new node
					editorModelDeletedNodes.add(cetn);
					List<CourseEditorTreeNode> getsAlsoDeleted = new ArrayList<>();
					collectSubTreeNodesStartingFrom(cetn, getsAlsoDeleted);
					// whole subtree added, marked as being deleted
					editorModelDeletedNodes.addAll(getsAlsoDeleted);
					if(!cetn.isNewnode()) {
						log.warn("try to publish node [" + cetn.getTitle() + " " + cetn.getIdent() + "]which says it is not new (but deleted )but also not in the runstructure (OO-249 delete it).");
					}
				} else if (!cetn.isDeleted()) {
					// publish new node
					addNodeTo(resultingCourseRun, cetn);
					editorModelInsertedNodes.add(cetn);
					if(!cetn.isNewnode()) {
						log.warn("try to publish node [" + cetn.getTitle() + " " + cetn.getIdent() + "] which says it is not new but also not in the runstructure (OO-249 publish it).");
					}
				} else {
					// this state is really not possible
					throw new AssertException("try to publish node [" + cetn.getTitle() + " " + cetn.getIdent()
							+ "]which says it is not new but also not in the runstructure.");
				}
			} else if (publishNode && alreadyInRun) {
				if (dirtyOnly) {
					// publish modified node
					addNodeTo(resultingCourseRun, cetn);
					editorModelModifiedNodes.add(cetn);
				} else if (cetn.isDeleted()) {
					// publish deletion of a node
					editorModelDeletedNodes.add(cetn);
					List<CourseEditorTreeNode> getsAlsoDeleted = new ArrayList<>();
					collectSubTreeNodesStartingFrom(cetn, getsAlsoDeleted);
					// whole subtree added, marked as being deleted
					editorModelDeletedNodes.addAll(getsAlsoDeleted);
					if(cetn.isNewnode()) {
						log.warn(cetn.getTitle() + " - try to publish node which says it is new and deleted but also exists in the runstructure (OO-249 delete it).");
					}
				} else if (cetn.isNewnode() && !cetn.isDeleted()) {
					// this state is not possible but appears somewhat
					addNodeTo(resultingCourseRun, cetn);
					editorModelModifiedNodes.add(cetn);
					log.warn(cetn.getTitle() + " - try to publish node which says it is new but also exists in the runstructure (OO-249: publish the node).");
				} else if (!cetn.isNewnode() && !cetn.isDeleted()) {
					//not new, not deleted, not changed
					log.warn(cetn.getTitle() + " - try to publish node which says it is not new, not deleted, not changed (OO-249: publish do nothing).");
				} else {
					log.error(cetn.getTitle() + " - try to publish node which is in an unkown state (OO-249: publish do nothing).");
				}
			} else {
				// !publishNode && !alreadyInRun
				// check condition, and add all subnodes to be skipped
				if (!cetn.isNewnode()) { 
					log.warn(cetn.getTitle()+" - node is not to publish and not in run -> hence it should be isNewnode() == true, but it is not (OO-249: ignore it until explicitly published)!!");
				}
				List<CourseEditorTreeNode> skippable = new ArrayList<>();
				collectSubTreeNodesStartingFrom(cetn, skippable);
				// remember this new node with its subtree as not being published
				// there may float a dirty node in the subtree, which got there by
				// moving
				// this node would get published if we do not keep book here.
				skippableNodes.addAll(skippable);
			}
		}

		/**
		 * @param newRunStruct
		 * @param cetn
		 */
		private void addNodeTo(final Structure newRunStruct, CourseEditorTreeNode cetn) {
			CourseNode clone = (CourseNode) XStreamHelper.xstreamClone(cetn.getCourseNode());
			clone.removeAllChildren();// children get also visited
			// parent in the course editor model
			CourseEditorTreeNode parentCetn = (CourseEditorTreeNode) cetn.getParent();
			CourseNode parent = parentCetn.getCourseNode();
			CourseNode parentClone = newRunStruct.getNode(parent.getIdent());
			parentClone.addChild(clone);
		}

		/**
		 * @param newRunStruct
		 * @param cetn
		 */
		private void addNodeTo(final Structure newRunStruct, CourseNode courseNode, CourseEditorTreeNode cetn) {
			CourseNode clone = (CourseNode) XStreamHelper.xstreamClone(courseNode);
			clone.removeAllChildren();// children get also visited
			// parent in the course editor model
			CourseEditorTreeNode parentCetn = (CourseEditorTreeNode) cetn.getParent();
			CourseNode parent = parentCetn.getCourseNode();
			CourseNode parentClone = newRunStruct.getNode(parent.getIdent());
			parentClone.addChild(clone);
		}

		/**
		 * flat list of all CourseEditorTreeNodes starting from root
		 * 
		 * @param treeNode
		 * @param rootNodeWithSubtree
		 */
		private void collectSubTreeNodesStartingFrom(CourseEditorTreeNode treeNode, List<CourseEditorTreeNode> rootNodeWithSubtree) {
			for (int i = 0; i < treeNode.getChildCount(); i++) {
				CourseEditorTreeNode node = (CourseEditorTreeNode)treeNode.getChildAt(i);
				rootNodeWithSubtree.add(node);
				collectSubTreeNodesStartingFrom(node, rootNodeWithSubtree);
			}
		}
	}// end nested class
}
