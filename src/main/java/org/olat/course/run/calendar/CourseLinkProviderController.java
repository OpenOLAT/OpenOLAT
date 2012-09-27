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

import java.util.ArrayList;
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
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
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
	private final OLATResourceable ores;
	private final List<ICourse> availableCourses;

	public CourseLinkProviderController(ICourse course, List<ICourse> courses, UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(CalendarManager.class, ureq.getLocale()));

		this.ores = course;
		this.availableCourses = new ArrayList<ICourse>(courses);
		
		setVelocityRoot(Util.getPackageVelocityRoot(CalendarManager.class));	
		clpVC = createVelocityContainer("calCLP");	
		selectionTree = new SelectionTree("clpTree", getTranslator());
		selectionTree.addListener(this);
		selectionTree.setMultiselect(true);
		selectionTree.setAllowEmptySelection(true);
		selectionTree.setShowCancelButton(true);
		selectionTree.setFormButtonKey(CAL_LINKS_SUBMIT);
		selectionTree.setTreeModel(new CourseNodeSelectionTreeModel(courses));
		clpVC.put("tree", selectionTree);
		putInitialPanel(clpVC);
	}

	public Long getCourseId() {
		return ores.getResourceableId();
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

	private void rebuildKalendarEventLinks(INode node, List<String> selectedNodeIDs, List<KalendarEventLink> kalendarEventLinks) {
		if (selectedNodeIDs.contains(node.getIdent()) && node instanceof LinkTreeNode) {
			// assemble link
			LinkTreeNode treeNode = (LinkTreeNode)node;
			OLATResourceable courseOres = treeNode.getCourse();
			if(courseOres != null) {
				RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(courseOres, true);
				List<ContextEntry> ces = new ArrayList<ContextEntry>();
				ces.add(BusinessControlFactory.getInstance().createContextEntry(re));
				if(treeNode.getCourseNode() != null) {
					String courseNodeId = treeNode.getCourseNode().getIdent();
					OLATResourceable oresNode = OresHelper.createOLATResourceableInstance("CourseNode", Long.valueOf(courseNodeId));
					ces.add(BusinessControlFactory.getInstance().createContextEntry(oresNode));
				}
				String extLink = BusinessControlFactory.getInstance().getAsURIString(ces, false);
				KalendarEventLink link = new KalendarEventLink(COURSE_LINK_PROVIDER, node.getIdent(), treeNode.getTitle(), extLink, treeNode.getIconCssClass());
				kalendarEventLinks.add(link);
				treeNode.setSelected(true);
			}
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			rebuildKalendarEventLinks(node.getChildAt(i), selectedNodeIDs, kalendarEventLinks);
		}
	}
	
	protected void doDispose() {
		//
	}

	public CourseLinkProviderController getControler() {
		return this;
	}

	public void setKalendarEvent(KalendarEvent kalendarEvent) {
		this.kalendarEvent = kalendarEvent;
		clearSelection(selectionTree.getTreeModel().getRootNode());
		for (KalendarEventLink link: kalendarEvent.getKalendarEventLinks()) {
			if (!link.getProvider().equals(COURSE_LINK_PROVIDER)) continue;
			String nodeId = link.getId();
			TreeNode node = selectionTree.getTreeModel().getNodeById(nodeId);
			if(node == null) {
				nodeId = availableCourses.get(0).getResourceableId() + "_" + nodeId;
				node = selectionTree.getTreeModel().getNodeById(nodeId);
			}
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
	
	private static class CourseNodeSelectionTreeModel extends GenericTreeModel {
		private static final long serialVersionUID = -7863033366847344767L;

		public CourseNodeSelectionTreeModel(List<ICourse> courses) {
			if(courses.size() == 1) {
				ICourse course = courses.get(0);
				setRootNode(buildTree(course, course.getRunStructure().getRootNode()));
			} else {
				LinkTreeNode rootNode = new LinkTreeNode("", null, null);
				for(ICourse course:courses) {
					LinkTreeNode node = new LinkTreeNode(course.getCourseTitle(), course, null);
					node.setAltText(course.getCourseTitle());
					node.setIdent(course.getResourceableId().toString());
					node.setIconCssClass("o_CourseModule_icon");

					LinkTreeNode childNode = buildTree(course, course.getRunStructure().getRootNode());
					node.addChild(childNode);
					rootNode.addChild(node);
				}
				setRootNode(rootNode);
			}
		}

		private LinkTreeNode buildTree(ICourse course, CourseNode courseNode) {
			LinkTreeNode node = new LinkTreeNode(courseNode.getShortTitle(), course, courseNode);
			node.setAltText(courseNode.getLongTitle());
			node.setIdent(course.getResourceableId() + "_" + courseNode.getIdent());
			node.setIconCssClass("o_" + courseNode.getType() + "_icon");
			node.setUserObject(course);
			for (int i = 0; i < courseNode.getChildCount(); i++) {
				CourseNode childNode = (CourseNode)courseNode.getChildAt(i);
				node.addChild(buildTree(course, childNode));
			}
			return node;
		}
	}
	
	private static class LinkTreeNode extends GenericTreeNode {
		private static final long serialVersionUID = -6043669089871217496L;
		private final ICourse course;
		private final CourseNode courseNode;
		
		public LinkTreeNode(String title, ICourse course, CourseNode courseNode) {
			super(title, null);
			
			this.course = course;
			this.courseNode = courseNode;
		}

		public ICourse getCourse() {
			return course;
		}

		public CourseNode getCourseNode() {
			return courseNode;
		}
	}
}