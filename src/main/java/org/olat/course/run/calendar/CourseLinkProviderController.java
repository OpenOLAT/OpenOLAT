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

package org.olat.course.run.calendar;

import java.util.Iterator;
import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.commons.calendar.ui.LinkProvider;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.SelectionTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

public class CourseLinkProviderController extends BasicController implements LinkProvider {

	private static final String COURSE_LINK_PROVIDER = "COURSE";
	private static final String CAL_LINKS_SUBMIT = "cal.links.submit";
	private VelocityContainer clpVC;
	private KalendarEvent kalendarEvent;
	private SelectionTree selectionTree;
	private OLATResourceable ores;

	public CourseLinkProviderController(ICourse course, UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, new PackageTranslator(Util.getPackageName(CalendarManager.class), ureq.getLocale()));
		this.ores = course;
		setVelocityRoot(Util.getPackageVelocityRoot(CalendarManager.class));	
		clpVC = createVelocityContainer("calCLP");	
		selectionTree = new SelectionTree("clpTree", getTranslator());
		selectionTree.addListener(this);
		selectionTree.setMultiselect(true);
		selectionTree.setAllowEmptySelection(true);
		selectionTree.setShowCancelButton(true);
		selectionTree.setFormButtonKey(CAL_LINKS_SUBMIT);
		selectionTree.setTreeModel(new CourseNodeSelectionTreeModel(course));
		clpVC.put("tree", selectionTree);
		putInitialPanel(clpVC);
	}

	public void event(UserRequest ureq, Component source, Event event) {
		if (source == selectionTree) {
			TreeEvent te = (TreeEvent)event;
			if (event.getCommand().equals(TreeEvent.COMMAND_TREENODES_SELECTED)) {
				// rebuild kalendar event links
				// we do not use the tree event's getSelectedNodeIDs, instead
				// we walk through the model and fetch the children in order
				// to keep the sorting.
				//fxdiff
				List<KalendarEventLink> kalendarEventLinks = kalendarEvent.getKalendarEventLinks();
				TreeNode rootNode = selectionTree.getTreeModel().getRootNode();
				for(Iterator<KalendarEventLink> linkIt = kalendarEventLinks.iterator(); linkIt.hasNext(); ) {
					KalendarEventLink link = linkIt.next();
					if(COURSE_LINK_PROVIDER.equals(link.getProvider())) {
						linkIt.remove();
					}
				}
				
				clearSelection(rootNode);
				rebuildKalendarEventLinks(rootNode, te.getNodeIds(), kalendarEventLinks);
				// if the calendarevent is already associated with a calendar, save the modifications.
				// otherwise, the modifications will be saver, when the user saves
				// the calendar event.
				if (kalendarEvent.getCalendar() != null)
					CalendarManagerFactory.getInstance().getCalendarManager().addEventTo(kalendarEvent.getCalendar(), kalendarEvent);
				fireEvent(ureq, Event.DONE_EVENT);
			} else if (event.getCommand().equals(TreeEvent.CANCELLED_TREEEVENT.getCommand())) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
	}

	private void rebuildKalendarEventLinks(TreeNode node, List selectedNodeIDs, List kalendarEventLinks) {
		if (selectedNodeIDs.contains(node.getIdent())) {
			// assemble link
			StringBuilder extLink = new StringBuilder();
			extLink.append(Settings.getServerContextPathURI()).append("/auth/repo/go?rid=");
			ICourse course = CourseFactory.loadCourse(ores);
			RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
			extLink.append(re.getKey()).append("&amp;par=").append(node.getIdent());
			KalendarEventLink link = new KalendarEventLink(COURSE_LINK_PROVIDER, node.getIdent(), node.getTitle(), extLink.toString(), node.getIconCssClass());
			kalendarEventLinks.add(link);
			node.setSelected(true);
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			rebuildKalendarEventLinks((TreeNode)node.getChildAt(i), selectedNodeIDs, kalendarEventLinks);
		}
	}
	
	protected void doDispose() {
		// TODO Auto-generated method stub
	}

	public CourseLinkProviderController getControler() {
		return this;
	}

	public Long getCourseID() {
	  return ores.getResourceableId();
	}

	public void setKalendarEvent(KalendarEvent kalendarEvent) {
		this.kalendarEvent = kalendarEvent;
		clearSelection(selectionTree.getTreeModel().getRootNode());
		for (Iterator iter = kalendarEvent.getKalendarEventLinks().iterator(); iter.hasNext();) {
			KalendarEventLink link = (KalendarEventLink) iter.next();
			if (!link.getProvider().equals(COURSE_LINK_PROVIDER)) continue;
			String nodeId = link.getId();
			TreeNode node = selectionTree.getTreeModel().getNodeById(nodeId);
			if (node != null) {
				node.setSelected(true);
			}
		}
	}
	
	public void setDisplayOnly(boolean displayOnly) {
		if (displayOnly) {
			clpVC.contextPut("displayOnly", Boolean.TRUE);
			selectionTree.setVisible(false);
			clpVC.contextPut("links", kalendarEvent.getKalendarEventLinks());
		} else {
			clpVC.contextPut("displayOnly", Boolean.FALSE);
			selectionTree.setVisible(true);
			clpVC.contextRemove("links");
		}
	}
	
	private void clearSelection(TreeNode node) {
		node.setSelected(false);
		for (int i = 0; i < node.getChildCount(); i++) {
			TreeNode childNode = (TreeNode)node.getChildAt(i);
			clearSelection(childNode);
		}
	}

	public void addControllerListener(ControllerEventListener controller) {
		super.addControllerListener(controller);
	}
	
}

class CourseNodeSelectionTreeModel extends GenericTreeModel {
	
	public CourseNodeSelectionTreeModel(ICourse course) {
		setRootNode(buildTree(course.getRunStructure().getRootNode()));
	}

	private GenericTreeNode buildTree(CourseNode courseNode) {
		GenericTreeNode node = new GenericTreeNode(courseNode.getShortTitle(), null);
		node.setAltText(courseNode.getLongTitle());
		node.setIdent(courseNode.getIdent());
		node.setIconCssClass("o_" + courseNode.getType() + "_icon");
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			CourseNode childNode = (CourseNode)courseNode.getChildAt(i);
			node.addChild(buildTree(childNode));
		}
		return node;
	}
	
}