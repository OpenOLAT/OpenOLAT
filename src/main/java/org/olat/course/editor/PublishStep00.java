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
import java.util.List;

import org.olat.catalog.CatalogManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.OLATResourceable;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.StatusDescriptionHelper;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

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
		
		//VCRP-3: add catalog entry in publish wizard
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		CourseNode rootNode = course.getRunStructure().getRootNode();
		
		Property prop = cpm.findCourseNodeProperty(rootNode, null, null, "catalog-choice");
		if(prop == null) {
			hasCatalog = false;
		} else if("no".equals(prop.getStringValue())) {
			hasCatalog = true;
		} else {
			RepositoryEntry repositoryEntry = RepositoryManager.getInstance().lookupRepositoryEntry(ores, true);
			hasCatalog = !CatalogManager.getInstance().getCatalogCategoriesFor(repositoryEntry).isEmpty();
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
		runContext.put("publishProcess", publishProcess);
		//fxdiff VCRP-1,2: access control of resources
		if(repoEntry.isMembersOnly()) {
			runContext.put("selectedCourseAccess", RepositoryEntry.MEMBERS_ONLY);
		} else {
			runContext.put("selectedCourseAccess",String.valueOf(repoEntry.getAccess()));
		}
		return new PublishStep00Form(ureq, wControl, form, publishProcess, runContext);
	}

	/**
	 * Description:<br>
	 * TODO: patrickb Class Description for PublishStep00Form
	 * 
	 * <P>
	 * Initial Date: 18.01.2008 <br>
	 * 
	 * @author patrickb
	 */
	class PublishStep00Form extends StepFormBasicController {

		private PublishProcess publishManager2;
		private MultipleSelectionElement multiSelectTree;
		private StatusDescription[] sds;
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
		protected void doDispose() {
		// nothing to dispose

		}

		
		@SuppressWarnings("synthetic-access")
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
				List<String> asList = new ArrayList<String>(multiSelectTree.getSelectedKeys());
				publishManager2.createPublishSetFor(asList);
				addToRunContext("publishSetCreatedFor", multiSelectTree.getSelectedKeys());
				//
				sds = publishProcess.testPublishSet(ureq.getLocale());
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
				String warningTxt = getTranslator().translate("publish.withwarnings");

				String errors = "<UL>";
				int errCnt = 0;
				String warnings = "<UL>";
				for (int i = 0; i < sds.length; i++) {
					StatusDescription description = sds[i];
					String nodeId = sds[i].getDescriptionForUnit();
					if (nodeId == null) {
						// a general error
						generalErrorTxt = sds[i].getShortDescription(ureq.getLocale());
						break;
					}
					String nodeName = publishProcess.getCourseEditorTreeModel().getCourseNode(nodeId).getShortName();
					String isFor = "<b>" + nodeName + "</b><br/>";
					if (description.isError()) {
						errors += "<LI>" + isFor + description.getShortDescription(ureq.getLocale()) + "</LI>";
						errCnt++;
					} else if (description.isWarning()) {
						warnings += "<LI>" + isFor + description.getShortDescription(ureq.getLocale()) + "</LI>";
					}
				}
				warnings += "</UL>";
				errors += "</UL>";
				//
				errorTxt += "<P/>" + errors;
				warningTxt += "<P/>" + warnings;

				if(errCnt > 0){
					//if an error found
					//normally this should already be prevented by offering only correct
					//tree nodes in the selection tree.
					
					return false;
				}
				
				
				if (generalErrorTxt != null) {
					addToRunContext("STEP00.generalErrorText", generalErrorTxt);					
					//TODO: PB: errorElement.setErrorComponent doesn't work, used setValue as workaround
					/*FormItem errorFormItem = uifactory.createSimpleErrorText("errorElement", generalErrorTxt);
					errorElement.setErrorComponent(errorFormItem, fic);*/
					errorElement.setValue(generalErrorTxt);
					errorElement.setVisible(true);
					return false;
				} else if (errCnt > 0) {
					addToRunContext("STEP00.errorMessage", errorTxt);					
					/*FormItem errorFormItem = uifactory.createSimpleErrorText("errorElement", errorTxt);
					errorElement.setErrorComponent(errorFormItem, this.flc);*/
					errorElement.setValue(errorTxt);
					errorElement.setVisible(true);
					return false;
				} else /*must be true then if (warCnt > 0)*/ {
					addToRunContext("STEP00.warningMessage", warningTxt);
					return true;
				}
			}else{
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
				multiSelectTree = uifactory.addTreeMultiselect("seltree", null, fic, publishManager2.getPublishTreeModel(), publishManager2.getPublishTreeModel());
				multiSelectTree.selectAll();
				selectAllLink = uifactory.addFormLink("checkall", fic);
				selectAllLink.setElementCssClass("o_sel_course_publish_selectall_cbb");
				selectAllLink.addActionListener(this, FormEvent.ONCLICK);
				uncheckallLink = uifactory.addFormLink("uncheckall", fic);
				uncheckallLink.setElementCssClass("o_sel_course_publish_deselectall_cbb");
				uncheckallLink.addActionListener(this, FormEvent.ONCLICK);
			} else {
				// set message container - telling nothing to publish.
				formLayout.add(FormLayoutContainer.createCustomFormLayout("nothingtopublish", getTranslator(), this.velocity_root
						+ "/nothingtopublish.html"));
			}
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == selectAllLink) {
				multiSelectTree.selectAll();
			} else if (source == uncheckallLink) {
				multiSelectTree.uncheckAll();
			}
		}
		
	}// endclass

}// endclass
