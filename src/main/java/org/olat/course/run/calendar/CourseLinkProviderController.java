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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.commons.calendar.ui.LinkProvider;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTreeItem;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

public class CourseLinkProviderController extends FormBasicController implements LinkProvider {

	private static final String COURSE_LINK_PROVIDER = "COURSE";
	private KalendarEvent kalendarEvent;

	private FormSubmit saveButton;
	private final Long courseId;
	private final List<OLATResourceable> availableCourses;
	private MenuTreeItem multiSelectTree;
	private final CourseNodeSelectionTreeModel courseNodeTreeModel;
	
	@Autowired
	private CalendarManager calendarManager;

	protected CourseLinkProviderController(Long courseId, List<OLATResourceable> courses, UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "course_elements");
		setTranslator(Util.createPackageTranslator(CalendarManager.class, ureq.getLocale(), getTranslator()));
		this.courseId = courseId;
		this.availableCourses = courses;
		courseNodeTreeModel = new CourseNodeSelectionTreeModel(courses);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		multiSelectTree = uifactory.addTreeMultiselect("seltree", null, formLayout, courseNodeTreeModel, this);
		multiSelectTree.setRootVisible(availableCourses.size() == 1);
		multiSelectTree.setMultiSelect(true);

		saveButton = uifactory.addFormSubmitButton("ok", "cal.links.submit", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<KalendarEventLink> kalendarEventLinks = kalendarEvent.getKalendarEventLinks();
		TreeNode rootNode = courseNodeTreeModel.getRootNode();
		for(Iterator<KalendarEventLink> linkIt = kalendarEventLinks.iterator(); linkIt.hasNext(); ) {
			KalendarEventLink link = linkIt.next();
			if(COURSE_LINK_PROVIDER.equals(link.getProvider())) {
				linkIt.remove();
			}
		}
		
		clearSelection(rootNode);
		Collection<String> nodeIds = multiSelectTree.getSelectedKeys();
		rebuildKalendarEventLinks(rootNode, nodeIds, kalendarEventLinks);
		// if the calendarevent is already associated with a calendar, save the modifications.
		// otherwise, the modifications will be saver, when the user saves
		// the calendar event.
		if (kalendarEvent.getCalendar() != null) {
			calendarManager.updateEventFrom(kalendarEvent.getCalendar(), kalendarEvent);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	public Long getCourseId() {
		return courseId;
	}

	private void rebuildKalendarEventLinks(INode node, Collection<String> selectedNodeIDs, List<KalendarEventLink> kalendarEventLinks) {
		if (selectedNodeIDs.contains(node.getIdent()) && node instanceof LinkTreeNode) {
			// assemble link
			LinkTreeNode treeNode = (LinkTreeNode)node;
			OLATResourceable courseOres = treeNode.getCourse();
			if(courseOres != null) {
				RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(courseOres, true);
				List<ContextEntry> ces = new ArrayList<>();
				ces.add(BusinessControlFactory.getInstance().createContextEntry(re));
				if(treeNode.getCourseNodeIdent() != null) {
					String courseNodeId = treeNode.getCourseNodeIdent();
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

	@Override
	public void setKalendarEvent(KalendarEvent kalendarEvent) {
		this.kalendarEvent = kalendarEvent;
		//clear all selections
		clearSelection(courseNodeTreeModel.getRootNode());
		multiSelectTree.deselectAll();
		
		for (KalendarEventLink link: kalendarEvent.getKalendarEventLinks()) {
			if (link.getProvider().equals(COURSE_LINK_PROVIDER)) {
				String nodeId = link.getId();
				TreeNode node = courseNodeTreeModel.getNodeById(nodeId);
				if(node == null) {
					String fallBackNodeId = availableCourses.get(0).getResourceableId() + "_" + nodeId;
					node = courseNodeTreeModel.getNodeById(fallBackNodeId);
				}
				if(node == null && nodeId.indexOf("_") < 0) {
					//course selected -> map to root node
					for(OLATResourceable courseOres: availableCourses) {
						if(nodeId.equals(courseOres.getResourceableId().toString())) {
							try {
								ICourse course = CourseFactory.loadCourse(courseOres);
								String fallBackNodeId = course.getResourceableId() + "_" + course.getRunStructure().getRootNode().getIdent();
								node = courseNodeTreeModel.getNodeById(fallBackNodeId);
							} catch (CorruptedCourseException e) {
								logError("", e);
							}
						}
					}
				}
				if (node != null) {
					node.setSelected(true);
					multiSelectTree.select(node.getIdent(), true);
					multiSelectTree.open(node);
				}
			}
		}
	}
	
	@Override
	public void setDisplayOnly(boolean displayOnly) {
		multiSelectTree.setEnabled(!displayOnly);
		multiSelectTree.reset();
		saveButton.setVisible(!displayOnly);
	}
	
	private void clearSelection(TreeNode node) {
		node.setSelected(false);
		for (int i = 0; i < node.getChildCount(); i++) {
			TreeNode childNode = (TreeNode)node.getChildAt(i);
			clearSelection(childNode);
		}
	}
	
	private static class CourseNodeSelectionTreeModel extends GenericTreeModel {
		private static final long serialVersionUID = -7863033366847344767L;
		private static final Logger log = Tracing.createLoggerFor(CourseNodeSelectionTreeModel.class);

		public CourseNodeSelectionTreeModel(List<OLATResourceable> courses) {
			try {
				if(courses.size() == 1) {
					ICourse course = CourseFactory.loadCourse(courses.get(0));
					setRootNode(buildCourseTree(course));
				} else {
					LinkTreeNode rootNode = new LinkTreeNode("", null, null);
					for(OLATResourceable courseOres:courses) {
						ICourse course = CourseFactory.loadCourse(courseOres);
						rootNode.addChild(buildCourseTree(course));
					}
					setRootNode(rootNode);
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
		
		private LinkTreeNode buildCourseTree(ICourse course) {
			return buildTree(course, course.getRunStructure().getRootNode());
		}

		private LinkTreeNode buildTree(ICourse course, CourseNode courseNode) {
			LinkTreeNode node = new LinkTreeNode(courseNode.getShortTitle(), course, courseNode);
			node.setAltText(courseNode.getLongTitle());
			node.setIdent(course.getResourceableId() + "_".concat(courseNode.getIdent()));
			if(courseNode == course.getRunStructure().getRootNode()) {
				node.setIconCssClass("o_CourseModule_icon");
			} else {
				CourseNodeConfiguration config = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType());
				if(config != null) {
					node.setIconCssClass(("o_icon ".concat(config.getIconCSSClass())).intern());
				}
			}
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
		
		private final OLATResourceable courseOres;
		private final String courseNodeIdent;
		
		public LinkTreeNode(String title, ICourse course, CourseNode courseNode) {
			super(title, null);
			courseOres = course == null ? null : OresHelper.clone(course);
			courseNodeIdent = courseNode == null ? null : courseNode.getIdent();
		}

		public OLATResourceable getCourse() {
			return courseOres;
		}

		public String getCourseNodeIdent() {
			return courseNodeIdent;
		}
	}
}