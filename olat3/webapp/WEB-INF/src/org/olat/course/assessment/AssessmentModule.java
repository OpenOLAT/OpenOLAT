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
* <p>
*/ 

package org.olat.course.assessment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.olat.core.configuration.Destroyable;
import org.olat.core.configuration.Initializable;
import org.olat.core.gui.control.Event;
import org.olat.core.logging.Tracing;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.editor.PublishEvent;

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
public class AssessmentModule implements Initializable, Destroyable, GenericEventListener {
	private static int DEFAULT_POOLSIZE = 3;
	/*
	 * worker pool for updating effciency statements
	 */
	private ExecutorService updateESPool;
	private List upcomingWork;
	private CourseModule courseModule;
	
	/**
	 * [used by spring]
	 */
	private AssessmentModule(CourseModule courseModule) {
		this.courseModule = courseModule;
	}

	/**
	 * @see org.olat.core.configuration.OLATModule#init(com.anthonyeden.lib.config.Configuration)
	 */
	public void init() {
		/*
		 * init Worker pool
		 */
		ThreadFactory4UpdateEfficiencyWorker th4uew = new ThreadFactory4UpdateEfficiencyWorker();
		updateESPool = Executors.newFixedThreadPool(DEFAULT_POOLSIZE, th4uew);
		upcomingWork = new ArrayList();
		/*
		 * always last step, register for course events
		 */
		courseModule.registerForCourseType(this, null);
		/*
		 * no more code after here!
		 */
	}

	/**
	 * @see org.olat.core.configuration.OLATModule#destroy()
	 */
	public void destroy() {
		/*
		 * first step in destroy, deregister for course events
		 */
		//no longer listen to changes
		courseModule.deregisterForCourseType(this);
		/*
		 * no other code before here!
		 */
		//wait for all work being done
		updateESPool.shutdown();
		//check that working queue is empty
		if(upcomingWork.size()>0){
			//hanging work!!
			Tracing.logWarn("still some Efficiency Statement recalculations open!!", AssessmentModule.class);
		}
		
		//
	}

	/**
	 * Called at course publish.
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		if (event instanceof PublishEvent) {
			PublishEvent pe = (PublishEvent) event;
			//FIXME: LD: temporary introduced the (pe.getCommand() == PublishEvent.EVENT_IDENTIFIER) to filter the events from the same VM
			if (pe.getState() == PublishEvent.PRE_PUBLISH && pe.getEventIdentifier() == PublishEvent.EVENT_IDENTIFIER) {
				// PRE PUBLISH -> check node for changes
				addToUpcomingWork(pe);
				return;
			} else if (pe.getState() == PublishEvent.PUBLISH && pe.getEventIdentifier() == PublishEvent.EVENT_IDENTIFIER) {
				// a publish event, check if it matches a previous checked
				boolean recalc = false;
				Long resId = pe.getPublishedCourseResId();
				synchronized (upcomingWork) { //o_clusterOK by:ld synchronized OK - only one cluster node must update the EfficiencyStatements (the course is locked for editing) (same as e.g. file indexer)
					recalc = upcomingWork.contains(resId);
					if (recalc) {
						upcomingWork.remove(resId);
					}
				}
				if (recalc) {
					ICourse pubCourse = CourseFactory.loadCourse(pe.getPublishedCourseResId());
					UpdateEfficiencyStatementsWorker worker = new UpdateEfficiencyStatementsWorker(pubCourse);
					updateESPool.execute(worker);
				}
			}
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
		Set changedNodeIds = pe.getDeletedCourseNodeIds();
		changedNodeIds.addAll(pe.getInsertedCourseNodeIds());
		changedNodeIds.addAll(pe.getModifiedCourseNodeIds());
		//
		boolean courseAssessmentChanged = false;
		Structure courseRun = course.getRunStructure();
		for (Iterator iter = changedNodeIds.iterator(); iter.hasNext();) {
			String nodeId = (String) iter.next();
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
		return;
	}
	
	
	public class ThreadFactory4UpdateEfficiencyWorker implements ThreadFactory{

		/**
		 * @see edu.emory.mathcs.backport.java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
		 */
		public Thread newThread(Runnable r) {
			Thread th = new Thread(r);
			th.setName("UpdateEfficiencyStatements");
			//kill this thread if OLAT is no longer active
			th.setDaemon(true);
			return th;
		}
		
	}

}
