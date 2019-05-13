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

package org.olat.course.assessment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.gui.control.Event;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.assessment.manager.UpdateEfficiencyStatementsWorker;
import org.olat.course.editor.PublishEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * This is a PublishEvent listener, and triggers the update of the EfficiencyStatements 
 * for the published course. It only considers the events from the same JVM.
 * 
 * <P>
 * Initial Date: 11.08.2006 <br>
 * 
 * @author patrickb
 */
@Service("assessmentModule")
public class AssessmentModule extends AbstractSpringModule implements GenericEventListener {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentModule.class);

	private List<Long> upcomingWork;
	
	@Value("${assessment.mode:enabled}")
	private String assessmentModeEnabled;
	
	@Autowired
	private CourseModule courseModule;
	@Autowired
	private TaskExecutorManager taskExecutorManager;
	
	@Autowired
	public AssessmentModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	@Override
	public void init() {
		updateProperties();
		upcomingWork = new ArrayList<>();
		/*
		 * always last step, register for course events
		 */
		courseModule.registerForCourseType(this, null);
		/*
		 * no more code after here!
		 */
	}
	
	private void updateProperties() {
		String enabledObj = getStringPropertyValue("assessment.mode", true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			assessmentModeEnabled = enabledObj;
		}
	}

	@Override
	public void destroy() {
		/*
		 * first step in destroy, deregister for course events
		 */
		//no longer listen to changes
		courseModule.deregisterForCourseType(this);
		/*
		 * no other code before here!
		 */
		//check that working queue is empty
		if(!upcomingWork.isEmpty()) {
			//hanging work!!
			log.warn("still some Efficiency Statement recalculations open!!");
		}
	}

	/**
	 * Called at course publish.
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(Event event) {
		if (event instanceof PublishEvent) {
			PublishEvent pe = (PublishEvent) event;
			if (pe.getState() == PublishEvent.PRE_PUBLISH && pe.isEventOnThisNode()) {
				// PRE PUBLISH -> check node for changes
				addToUpcomingWork(pe);
			} else if (pe.getState() == PublishEvent.PUBLISH && pe.isEventOnThisNode()) {
				// a publish event, check if it matches a previous checked
				prepareUpdate(pe.getPublishedCourseResId());
			}
		} else if (event instanceof EfficiencyStatementEvent) {
			if(EfficiencyStatementEvent.CMD_RECALCULATE.equals(event.getCommand())) {
				EfficiencyStatementEvent esEvent = (EfficiencyStatementEvent)event;
				//force recalculate
				upcomingWork.add(esEvent.getCourseResourceId());
				prepareUpdate(esEvent.getCourseResourceId());
			}
		}
	}
	
	private void prepareUpdate(Long resId) {
		boolean recalc = false;
		synchronized (upcomingWork) { //o_clusterOK by:ld synchronized OK - only one cluster node must update the EfficiencyStatements (the course is locked for editing) (same as e.g. file indexer)
			recalc = upcomingWork.contains(resId);
			if (recalc) {
				for(; upcomingWork.remove(resId); ) {
					//remove all with the same res id
				}
			}
		}
		if (recalc) {
			ICourse pubCourse = CourseFactory.loadCourse(resId);
			UpdateEfficiencyStatementsWorker worker = new UpdateEfficiencyStatementsWorker(pubCourse);
			taskExecutorManager.execute(worker);
		}
	}

	/**
	 * @param pe
	 */
	private void addToUpcomingWork(PublishEvent pe) {
		ICourse course = CourseFactory.loadCourse(pe.getPublishedCourseResId());
		boolean courseEfficiencyEnabled = course.getCourseEnvironment().getCourseConfig().isEfficencyStatementEnabled();
		if (!courseEfficiencyEnabled) {
			// no efficiency enabled, stop here.
			return;
		}
		// deleted + inserted + modified node ids -> changedNodeIds
		Set<String> changedNodeIds = pe.getDeletedCourseNodeIds();
		changedNodeIds.addAll(pe.getInsertedCourseNodeIds());
		changedNodeIds.addAll(pe.getModifiedCourseNodeIds());
		//
		boolean courseAssessmentChanged = false;
		Structure courseRun = course.getRunStructure();
		for (Iterator<String> iter = changedNodeIds.iterator(); iter.hasNext();) {
			String nodeId = iter.next();
			boolean wasNodeAsessable = AssessmentHelper.checkIfNodeIsAssessable(courseRun.getNode(nodeId));
			boolean isNodeAssessable = AssessmentHelper.checkIfNodeIsAssessable(course.getEditorTreeModel().getCourseNode(nodeId));
			//if node was or became assessable
			if (wasNodeAsessable || isNodeAssessable) {				
				courseAssessmentChanged = true;
				break;
			}
		}
		if (!courseAssessmentChanged) {
			// assessment changes detected, stop here
			return;
		}
		synchronized (upcomingWork) { //o_clusterOK by:ld synchronized OK - only one cluster node must update the EfficiencyStatements (the course is locked for editing)
			upcomingWork.add(course.getResourceableId());
		}
	}

	public boolean isAssessmentModeEnabled() {
		return "enabled".equals(assessmentModeEnabled);
	}

	public void setAssessmentModeEnabled(boolean enabled) {
		assessmentModeEnabled = enabled ? "enabled" : "disabled";
		setStringProperty("assessment.mode", assessmentModeEnabled, true);
	}
}
