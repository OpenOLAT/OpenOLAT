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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.iq;

import org.olat.core.commons.controllers.filechooser.FileRemoveEvent;
import org.olat.core.commons.controllers.filechooser.LinkFileCombiCalloutController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.ICourse;
import org.olat.course.editor.CourseEditorHelper;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.tree.CourseInternalLinkTreeModel;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IQLayoutConfigurationController extends BasicController {

	private VelocityContainer myContent;

	private QTI21EditLayoutForm mod21ConfigForm;
	private SecuritySettingsForm pageSecurityCtrl;
	private LinkFileCombiCalloutController combiLinkCtr;
	
	private VFSContainer courseFolderBaseContainer;
	private ModuleConfiguration moduleConfiguration;
	private AbstractAccessableCourseNode courseNode;

	@Autowired
	private QTI21Service qti21service;
	@Autowired
	private RepositoryManager repositoryManager;

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param stackPanel
	 * @param course
	 * @param courseNode
	 * @param euce
	 * @param type
	 */
	public IQLayoutConfigurationController(UserRequest ureq, WindowControl wControl, ICourse course,
			AbstractAccessableCourseNode courseNode, String type) {
		super(ureq, wControl);
		this.moduleConfiguration = courseNode.getModuleConfiguration();
		//o_clusterOk by guido: save to hold reference to course inside editor
		this.courseNode = courseNode;
		
		myContent = createVelocityContainer("edit_layout");
		courseFolderBaseContainer = course.getCourseFolderContainer();	

		String disclaimer = (String) moduleConfiguration.get(IQEditController.CONFIG_KEY_DISCLAIMER);
		boolean allowRelativeLinks = moduleConfiguration.getBooleanSafe(IQEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS);

		boolean relFilPathIsProposal = false;
		if(disclaimer == null) {
			// Use calculated file and folder name as default when not yet configured
			disclaimer = CourseEditorHelper.createUniqueRelFilePathFromShortTitle(courseNode, courseFolderBaseContainer);
			relFilPathIsProposal = true;
		}
		combiLinkCtr = new LinkFileCombiCalloutController(ureq, wControl, courseFolderBaseContainer,
				disclaimer, relFilPathIsProposal, allowRelativeLinks, true,
				new CourseInternalLinkTreeModel(course.getEditorTreeModel()), null, null);
		listenTo(combiLinkCtr);
		myContent.put("combiCtr", combiLinkCtr.getInitialComponent());
		myContent.contextPut("editorEnabled", combiLinkCtr.isEditorEnabled());
		myContent.contextPut("type", type);
		
		// Security configuration form
		pageSecurityCtrl = new SecuritySettingsForm(ureq, wControl, allowRelativeLinks);
		listenTo(pageSecurityCtrl);
		myContent.put("allowRelativeLinksForm", pageSecurityCtrl.getInitialComponent());
		
		putInitialPanel(myContent);	
		updateEditController(ureq);
		
		switch(type) {
			case QTI21Constants.QMD_ENTRY_TYPE_ASSESS:
				myContent.contextPut("repEntryTitle", translate("choosenfile.test"));
				break;
			case QTI21Constants.QMD_ENTRY_TYPE_SELF:
				myContent.contextPut("repEntryTitle", translate("choosenfile.self"));
				break;
			case QTI21Constants.QMD_ENTRY_TYPE_SURVEY:
				myContent.contextPut("repEntryTitle", translate("choosenfile.surv"));
				break;
		}
	}
	
	protected void updateEditController(UserRequest ureq) {
		removeAsListenerAndDispose(mod21ConfigForm);
		mod21ConfigForm = null;
		
		RepositoryEntry re = getIQReference();
		if(re != null && ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
			QTI21DeliveryOptions deliveryOptions =  qti21service.getDeliveryOptions(re);
			mod21ConfigForm = new QTI21EditLayoutForm(ureq, getWindowControl(), moduleConfiguration, re, deliveryOptions);
			listenTo(mod21ConfigForm);
			myContent.put("iqeditform", mod21ConfigForm.getInitialComponent());
		} else {
			myContent.remove("iqeditform");
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == combiLinkCtr) {
			if (event == Event.DONE_EVENT) {
				String chosenFile = VFSManager.getRelativeItemPath(combiLinkCtr.getFile(), courseFolderBaseContainer, null);
			    if (chosenFile != null){
			        moduleConfiguration.set(IQEditController.CONFIG_KEY_DISCLAIMER, chosenFile);
			    }  else {
			        moduleConfiguration.remove(IQEditController.CONFIG_KEY_DISCLAIMER);
			    }
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);	
			} else if(event instanceof FileRemoveEvent) {
				moduleConfiguration.remove(IQEditController.CONFIG_KEY_DISCLAIMER);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
			myContent.contextPut("editorEnabled", combiLinkCtr.isEditorEnabled());
		} else if(source == pageSecurityCtrl){
			if(event == Event.DONE_EVENT){
				boolean allowRelativeLinks = pageSecurityCtrl.getAllowRelativeLinksConfig();
				courseNode.getModuleConfiguration().setBooleanEntry(IQEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS, allowRelativeLinks);
				combiLinkCtr.setAllowEditorRelativeLinks(allowRelativeLinks);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == mod21ConfigForm) {
			if (event == Event.DONE_EVENT) {
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}

	/**
	 * Ge the qti file soft key repository reference 
	 * @param config
	 * @param strict
	 * @return RepositoryEntry
	 */
	private RepositoryEntry getIQReference() {
		if (moduleConfiguration == null) return null;
		String repoSoftkey = (String)moduleConfiguration.get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (repoSoftkey == null) return null;
		return repositoryManager.lookupRepositoryEntryBySoftkey(repoSoftkey, false);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public class SecuritySettingsForm extends FormBasicController {

		private boolean allow;
		private SelectionElement allowRelativeLinksEl;


		/**
		 * @param ureq
		 * @param wControl
		 * @param allowRelativeLinksConfig
		 *            true: page is link relative to course root folder; false: page
		 *            is relative to base directory
		 */
		public SecuritySettingsForm(UserRequest ureq, WindowControl wControl, boolean allowRelativeLinks) {
				super(ureq, wControl);
				this.allow = allowRelativeLinks;
				initForm (ureq);
		}

		/**
		 * @return Boolean new configuration
		 */
		public boolean getAllowRelativeLinksConfig(){
			return allowRelativeLinksEl.isSelected(0);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			fireEvent (ureq, Event.DONE_EVENT);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			// no explicit submit button, DONE event fired every time the checkbox is clicked
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormTitle("fieldset.allowRelativeLinksForm");
			allowRelativeLinksEl = uifactory.addCheckboxesHorizontal("allowRelativeLinks", "allowRelativeLinks", formLayout, new String[] {"xx"}, new String[] {null});
			if(allow) {
				allowRelativeLinksEl.select("xx", true);
			}
			allowRelativeLinksEl.addActionListener(FormEvent.ONCLICK);
		}

		@Override
		protected void doDispose() {
			//	
		}
	}
}