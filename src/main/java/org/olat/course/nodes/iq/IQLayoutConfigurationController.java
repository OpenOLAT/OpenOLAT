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

import org.olat.commons.file.filechooser.FileChooseCreateEditController;
import org.olat.commons.file.filechooser.LinkChooseCreateEditController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseInternalLinkTreeModel;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

import de.bps.onyx.plugin.OnyxModule;

/**
 * 
 * Initial date: 23 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IQLayoutConfigurationController extends BasicController {

	private VelocityContainer myContent;

	private IQ12LayoutEditForm mod12ConfigForm;
	private QTI21EditLayoutForm mod21ConfigForm;
	private FileChooseCreateEditController fccecontr;
	
	private Boolean allowRelativeLinks;
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
			AbstractAccessableCourseNode courseNode, UserCourseEnvironment euce, String type) {
		super(ureq, wControl);
		this.moduleConfiguration = courseNode.getModuleConfiguration();
		//o_clusterOk by guido: save to hold reference to course inside editor
		this.courseNode = courseNode;
		
		myContent = createVelocityContainer("edit_layout");		

		String disclaimer = (String) moduleConfiguration.get(IQEditController.CONFIG_KEY_DISCLAIMER);
		String legend = translate("fieldset.chosecreateeditfile");
	
		allowRelativeLinks = moduleConfiguration.getBooleanEntry(IQEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS);
		if(allowRelativeLinks == null) {
			allowRelativeLinks=Boolean.FALSE;
		}
		fccecontr = new LinkChooseCreateEditController(ureq, wControl, disclaimer, allowRelativeLinks, course.getCourseFolderContainer(),
				type, legend, new CourseInternalLinkTreeModel(course.getEditorTreeModel()));		
		listenTo(fccecontr);
		
		Component fcContent = fccecontr.getInitialComponent();
		myContent.put("filechoosecreateedit", fcContent);
		myContent.contextPut("type", type);
		
		putInitialPanel(myContent);	
		updateEditController(ureq);
		
		switch(type) {
			case AssessmentInstance.QMD_ENTRY_TYPE_ASSESS:
				myContent.contextPut("repEntryTitle", translate("choosenfile.test"));
				break;
			case AssessmentInstance.QMD_ENTRY_TYPE_SELF:
				myContent.contextPut("repEntryTitle", translate("choosenfile.self"));
				break;
			case AssessmentInstance.QMD_ENTRY_TYPE_SURVEY:
				myContent.contextPut("repEntryTitle", translate("choosenfile.surv"));
				break;
		}
	}
	
	protected void updateEditController(UserRequest ureq) {
		removeAsListenerAndDispose(mod12ConfigForm);
		removeAsListenerAndDispose(mod21ConfigForm);
		mod12ConfigForm = null;
		mod21ConfigForm = null;
		
		RepositoryEntry re = getIQReference();
		if(re == null) {
			myContent.remove("iqeditform");
		} else if(ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
			QTI21DeliveryOptions deliveryOptions =  qti21service.getDeliveryOptions(re);
			mod21ConfigForm = new QTI21EditLayoutForm(ureq, getWindowControl(), moduleConfiguration, deliveryOptions);
			listenTo(mod21ConfigForm);
			myContent.put("iqeditform", mod21ConfigForm.getInitialComponent());
		} else if(OnyxModule.isOnyxTest(re.getOlatResource())) {
			myContent.remove("iqeditform");
		} else {
			mod12ConfigForm = new IQ12LayoutEditForm(ureq, getWindowControl(), moduleConfiguration);
			listenTo(mod12ConfigForm);
			myContent.put("iqeditform", mod12ConfigForm.getInitialComponent());
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == fccecontr) {
			if (event == FileChooseCreateEditController.FILE_CHANGED_EVENT) {
			    String chosenFile = fccecontr.getChosenFile();
			    if (chosenFile != null){
			        moduleConfiguration.set(IQEditController.CONFIG_KEY_DISCLAIMER, fccecontr.getChosenFile());
			    }  else {
			        moduleConfiguration.remove(IQEditController.CONFIG_KEY_DISCLAIMER);
			    }
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			} else if (event == FileChooseCreateEditController.ALLOW_RELATIVE_LINKS_CHANGED_EVENT) {
				allowRelativeLinks = fccecontr.getAllowRelativeLinks();
				courseNode.getModuleConfiguration().setBooleanEntry(IQEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS, allowRelativeLinks.booleanValue());
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == mod12ConfigForm || source == mod21ConfigForm) {
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
}