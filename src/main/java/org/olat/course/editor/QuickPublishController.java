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
package org.olat.course.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.INodeFilter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.PublishTreeModel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuickPublishController extends BasicController {
	
	private final Link noLink;
	private final Link autoLink;
	private final Link manualLink;
	private final OLATResourceable courseOres;
	
	@Autowired
	private RepositoryManager repositoryManager;
	
	public QuickPublishController(UserRequest ureq, WindowControl wControl, ICourse course) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryModule.class, ureq.getLocale()));
		this.courseOres = OresHelper.clone(course);

		VelocityContainer mainVC = createVelocityContainer("quick_publish");
		
		String accessI18n = "";
		String accessI18CssClass = "o_success";
		OLATResource courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(courseResource, false);

		switch (entry.getEntryStatus()) {
			case preparation:
				accessI18n = translate("cif.status.preparation");
				accessI18CssClass = "o_warning";
				break;
			case review:
				accessI18n = translate("cif.status.review");
				accessI18CssClass = "o_warning";			
				break;
			case coachpublished:
				accessI18n = translate("cif.status.coachpublished");
				accessI18CssClass = "o_warning";			
				break;
				
			case published:
				accessI18n = translate("cif.status.published");
				if(!entry.isPublicVisible()) {
					accessI18CssClass = "o_warning";
				}
				break;
			default:
				accessI18n = "ERROR";
				accessI18CssClass = "o_error";			
				break;		
		}

		mainVC.contextPut("accessI18n", accessI18n);
		mainVC.contextPut("accessI18CssClass", accessI18CssClass);
		
		noLink = LinkFactory.createButton("pbl.quick.no", mainVC, this);
		noLink.setElementCssClass("o_sel_course_quickpublish_no");
		manualLink = LinkFactory.createButton("pbl.quick.manual", mainVC, this);
		manualLink.setElementCssClass("o_sel_course_quickpublish_manual");
		autoLink = LinkFactory.createButton("pbl.quick.auto", mainVC, this);
		autoLink.setCustomEnabledLinkCSS("btn btn-primary");
		autoLink.setElementCssClass("o_sel_course_quickpublish_auto");
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(noLink == source) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if(manualLink == source) {
			fireEvent(ureq, EditorMainController.MANUAL_PUBLISH);
		} else if(autoLink == source) {
			if(doAutoPublish()) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
	}
	
	private boolean doAutoPublish() {
		ICourse course = CourseFactory.getCourseEditSession(courseOres.getResourceableId());
		CourseEditorTreeModel cetm = course.getEditorTreeModel();
		PublishProcess publishProcess = PublishProcess.getInstance(course, cetm, getLocale());
		PublishTreeModel publishTreeModel = publishProcess.getPublishTreeModel();
 
		if (publishTreeModel.hasPublishableChanges()) {
			List<String> nodeToPublish = new ArrayList<>();
			visitPublishModel(publishTreeModel.getRootNode(), publishTreeModel, nodeToPublish);

			//only add selection if changes were possible
			for(Iterator<String> selectionIt=nodeToPublish.iterator(); selectionIt.hasNext(); ) {
				String ident = selectionIt.next();
				TreeNode node = publishProcess.getPublishTreeModel().getNodeById(ident);
				if(!publishTreeModel.isSelectable(node)) {
					selectionIt.remove();
				}
			}

			publishProcess.createPublishSetFor(nodeToPublish);
			
			PublishSetInformations set = publishProcess.testPublishSet(getLocale());
			StatusDescription[] status = set.getWarnings();
			//publish not possible when there are errors
			StringBuilder errMsg = new StringBuilder();
			for(int i = 0; i < status.length; i++) {
				if(status[i].isError()) {
					errMsg.append(status[i].getLongDescription(getLocale()));
					logError("Status error by publish: " + status[i].getLongDescription(getLocale()), null);
				}
			}
			
			if(errMsg.length() > 0) {
				getWindowControl().setWarning(errMsg.toString());
				return false;
			}
			
			PublishEvents publishEvents = publishProcess.getPublishEvents();
			try {
				publishProcess.applyPublishSet(getIdentity(), getLocale(), false);
			} catch(Exception e) {
				logError("",  e);
			}
			
			if(!publishEvents.getPostPublishingEvents().isEmpty()) {
				for(MultiUserEvent event:publishEvents.getPostPublishingEvents()) {
					CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, courseOres);
				}
			}
		}
		
		return true;
	}
	
	private static void visitPublishModel(TreeNode node, INodeFilter filter, Collection<String> nodeToPublish) {
		int numOfChildren = node.getChildCount();
		for (int i = 0; i < numOfChildren; i++) {
			INode child = node.getChildAt(i);
			if (child instanceof TreeNode && filter.isVisible(child)) {
				nodeToPublish.add(child.getIdent());
				visitPublishModel((TreeNode)child, filter, nodeToPublish);
			}
		}
	}
}