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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tree.MenuTreeEvent;
import org.olat.core.gui.components.tree.MenuTreeItem;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.StatusDescriptionHelper;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.PublishTreeModel;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.CatalogManager;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date: 18.01.2008 <br>
 * 
 * @author patrickb
 */
class PublishStep00 extends BasicStep {

	private PublishProcess publishProcess;
	private OLATResourceable ores;
	
	private final boolean hasCatalog;
	private final boolean hasPublishableChanges;

	public PublishStep00(UserRequest ureq, CourseEditorTreeModel cetm, ICourse course) {
		super(ureq);
		this.ores = course;
		publishProcess = PublishProcess.getInstance(course, cetm, ureq.getLocale());
		
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		CourseNode rootNode = course.getRunStructure().getRootNode();
		
		Property prop = cpm.findCourseNodeProperty(rootNode, null, null, "catalog-choice");
		if(prop == null) {
			hasCatalog = false;
		} else if("no".equals(prop.getStringValue())) {
			hasCatalog = true;
		} else {
			RepositoryEntry repositoryEntry = RepositoryManager.getInstance().lookupRepositoryEntry(ores, true);
			hasCatalog = !CoreSpringFactory.getImpl(CatalogManager.class).getCatalogCategoriesFor(repositoryEntry).isEmpty();
		}
		
		hasPublishableChanges = publishProcess.hasPublishableChanges();
		
		setI18nTitleAndDescr("publish.header", null);
		// proceed with direct access as next step.
		setNextStep(new PublishStep01(ureq, course, hasPublishableChanges, hasCatalog));
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.Step#getInitialPrevNextFinishConfig()
	 */
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		// in any case allow next or finish
		//VCRP-3: add catalog entry in publish wizard
		if(hasPublishableChanges || !hasCatalog){
			//this means we have possible steps 00a (error messages) and 00b (warning messages)
			return PrevNextFinishConfig.NEXT;
		}else{
			// proceed with direct access as next step.
			return PrevNextFinishConfig.NEXT_FINISH;
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.Step#getStepController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.core.gui.control.generic.wizard.StepsRunContext,
	 *      org.olat.core.gui.components.form.flexible.impl.Form)
	 */
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {

		RepositoryEntry repoEntry = RepositoryManager.getInstance().lookupRepositoryEntry(ores, false);
		/*
		 * first step is to show selection tree for selecting
		 * prepares all data needed for next step(s)
		 */
		runContext.put("repoEntry", repoEntry);
		runContext.put("publishProcess", publishProcess);

		return new PublishStep00Form(ureq, wControl, form, publishProcess, runContext);
	}

	/**
	 * Initial Date: 18.01.2008 <br>
	 * 
	 * @author patrickb
	 */
	class PublishStep00Form extends StepFormBasicController {

		private PublishProcess publishManager2;
		private MenuTreeItem multiSelectTree;
		private StatusDescription[] sds;
		private List<StatusDescription> updateNotes;
		private FormLayoutContainer fic;
		private StaticTextElement errorElement;
		private FormLink selectAllLink;
		private FormLink uncheckallLink;

		public PublishStep00Form(UserRequest ureq, WindowControl wControl, Form form, PublishProcess publishManager2, StepsRunContext runContext) {
			super(ureq, wControl, form, runContext, LAYOUT_VERTICAL, null);			
			this.publishManager2 = publishManager2;
			initForm(ureq);
		}
		
		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			//
			// create publish set
			// test for errors
			// errors are shown as error text for the form
			//
			boolean createPublishSet = true;
			if(containsRunContextKey("publishSetCreatedFor")){
				createPublishSet = getFromRunContext("publishSetCreated") != multiSelectTree.getSelectedKeys();
			}
			if(createPublishSet && publishManager2.hasPublishableChanges()){
				//only add selection if changes were possible
				List<String> selectedKeys = new ArrayList<>(multiSelectTree.getSelectedKeys());
				for(Iterator<String> selectionIt=selectedKeys.iterator(); selectionIt.hasNext(); ) {
					String ident = selectionIt.next();
					TreeNode node = publishManager2.getPublishTreeModel().getNodeById(ident);
					if(!publishManager2.getPublishTreeModel().isSelectable(node)) {
						selectionIt.remove();
					}
				}
				
				List<String> asList = new ArrayList<>(selectedKeys);
				publishManager2.createPublishSetFor(asList);
				addToRunContext("publishSetCreatedFor", selectedKeys);
				//
				PublishSetInformations set = publishProcess.testPublishSet(getLocale());
				sds = set.getWarnings();
				updateNotes = set.getUpdateInfos();
				addToRunContext("updateNotes", updateNotes);
				
				//
				boolean isValid = sds.length == 0;
				if (isValid) {
					// no error and no warnings -> return immediate
					return true;
				}
				//sort status -> first are errors, followed by warnings
				sds = StatusDescriptionHelper.sort(sds);

				// assemble warnings and errors
				String generalErrorTxt = null;
				String errorTxt = getTranslator().translate("publish.notpossible.setincomplete");
				String warningTxt = "";

				String errors = "<ul class='list-unstyled'>";
				int errCnt = 0;
				String warnings = "<ul class='list-unstyled'>";
				for (int i = 0; i < sds.length; i++) {
					StatusDescription description = sds[i];
					String nodeId = sds[i].getDescriptionForUnit();
					if (nodeId == null) {
						// a general error
						generalErrorTxt = sds[i].getShortDescription(getLocale());
						break;
					}
					String nodeName = publishProcess.getCourseEditorTreeModel().getCourseNode(nodeId).getShortName();
					String isFor = "<h5>" + nodeName + "</h5>";
					if (description.isError()) {
						errors += "<li>" + isFor + description.getShortDescription(getLocale()) + "</li>";
						errCnt++;
					} else if (description.isWarning()) {
						warnings += "<li>" + isFor + description.getShortDescription(getLocale()) + "</li>";
					}
				}
				warnings += "</ul>";
				errors += "</ul>";
				//
				errorTxt += errors;
				warningTxt += warnings;
				
				if (generalErrorTxt != null) {
					addToRunContext("STEP00.generalErrorText", generalErrorTxt);
					errorElement.setValue(generalErrorTxt);
					errorElement.setVisible(true);
					return false;
				} else if (errCnt > 0) {
					addToRunContext("STEP00.errorMessage", errorTxt);
					errorElement.setValue(errorTxt);
					errorElement.setVisible(true);
					return false;
				} else /*must be true then if (warCnt > 0)*/ {
					addToRunContext("STEP00.warningMessage", warningTxt);
					return true;
				}
			} else {
				// no new publish set to be calculated
				// check if some error was detected before.
				boolean retVal = !containsRunContextKey("STEP00.generalErrorText");
				retVal = retVal && !containsRunContextKey("STEP00.erroMessage");
				return retVal;
			}
		}
		
		@Override
		protected void formOK(UserRequest ureq) {
			addToRunContext("validPublish",Boolean.valueOf(publishManager2.hasPublishableChanges()));
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
		@Override
		protected void formNOK(UserRequest ureq) {
			addToRunContext("validPublish",Boolean.FALSE);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			if (publishManager2.hasPublishableChanges()) {
				//
				fic = FormLayoutContainer.createCustomFormLayout("publish", getTranslator(), this.velocity_root	+ "/publish.html");
				formLayout.add(fic);
				errorElement = uifactory.addStaticTextElement("errorElement", null, null, fic);//null > no label, null > no value
				errorElement.setVisible(false);
				//publish treemodel is tree model and INodeFilter at the same time
				multiSelectTree = uifactory.addTreeMultiselect("seltree", null, fic, publishManager2.getPublishTreeModel(), this);
				multiSelectTree.setFilter(publishManager2.getPublishTreeModel());
				multiSelectTree.setRootVisible(false);
				multiSelectTree.setMultiSelect(true);
				selectableAll();
				
				selectAllLink = uifactory.addFormLink("checkall", fic);
				selectAllLink.setElementCssClass("o_sel_course_publish_selectall_cbb");
				selectAllLink.addActionListener(FormEvent.ONCLICK);
				uncheckallLink = uifactory.addFormLink("uncheckall", fic);
				uncheckallLink.setElementCssClass("o_sel_course_publish_deselectall_cbb");
				uncheckallLink.addActionListener(FormEvent.ONCLICK);
			} else {
				// set message container - telling nothing to publish.
				formLayout.add(FormLayoutContainer.createCustomFormLayout("nothingtopublish", getTranslator(), this.velocity_root
						+ "/nothingtopublish.html"));
			}
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == selectAllLink) {
				selectableAll();
			} else if (source == uncheckallLink) {
				deselectRec(publishManager2.getPublishTreeModel().getRootNode());
			} else if (source == multiSelectTree) {
				if(event instanceof MenuTreeEvent) {
					MenuTreeEvent mte = (MenuTreeEvent)event;
					TreeNode selectedNode = publishManager2.getPublishTreeModel()
							.getNodeById(mte.getIdent());
					if(MenuTreeEvent.SELECT.equals(mte.getCommand())) {	
						selectRec(selectedNode, publishManager2.getPublishTreeModel());
					} else if(MenuTreeEvent.DESELECT.equals(mte.getCommand())) {
						deselectRec(selectedNode);
					}
				}
			} else {
				super.formInnerEvent(ureq, source, event);
			}
		}
		
		private void selectableAll() {
			selectableRec(publishManager2.getPublishTreeModel().getRootNode(), publishManager2.getPublishTreeModel());
		}
		
		private void selectableRec(TreeNode node, PublishTreeModel model) {
			if(model.isSelectable(node)) {
				multiSelectTree.select(node.getIdent(), true);
				multiSelectTree.open(node);
			}
			
			for(int i=node.getChildCount(); i-->0; ) {
				selectableRec((TreeNode)node.getChildAt(i), model);
			}
		}
		
		private void selectRec(INode node, PublishTreeModel model) {
			if(model.isVisible(node)) {
				multiSelectTree.select(node.getIdent(), true);
			}
			for(int i=node.getChildCount(); i-->0; ) {
				selectRec(node.getChildAt(i), model);
			}
		}
		
		private void deselectRec(INode node) {
			multiSelectTree.select(node.getIdent(), false);
			for(int i=node.getChildCount(); i-->0; ) {
				deselectRec(node.getChildAt(i));
			}
		}
	}
}