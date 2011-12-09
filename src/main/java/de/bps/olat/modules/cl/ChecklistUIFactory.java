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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.olat.modules.cl;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * This Factory provides a set of specific checklist views.
 * 
 * <P>
 * Initial Date:  22.07.2009 <br>
 * @author bja <bja@bps-system.de>
 */
public class ChecklistUIFactory {
	
	
	public static CheckpointComparator comparatorTitleAsc = new CheckpointComparator(1, true);
	public static CheckpointComparator comparatorTitleDesc = new CheckpointComparator(1, false);
	public static CheckpointComparator comparatorDescriptionAsc = new CheckpointComparator(2, true);
	public static CheckpointComparator comparatorDescriptionDesc = new CheckpointComparator(2, false);
	public static CheckpointComparator comparatorModeAsc = new CheckpointComparator(3, true);
	public static CheckpointComparator comparatorModeDesc = new CheckpointComparator(3, false);
	
	/** singleton */
	private static ChecklistUIFactory INSTANCE = new ChecklistUIFactory();
	
	private ChecklistUIFactory() {
		// constructor
	}
	
	public static ChecklistUIFactory getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Create run view of this checklist for course context.
	 * @param ureq
	 * @param wControl
	 * @param checklist
	 * @param filter
	 * @param canEdit
	 * @param canManage
	 * @param course
	 * @return controller
	 */
	public Controller createDisplayController(UserRequest ureq, WindowControl wControl, Checklist checklist, List<ChecklistFilter> filter, boolean canEdit, boolean canManage, ICourse course, CourseNode coursenode) {
		DefaultController checklistController = new ChecklistDisplayController(ureq, wControl, checklist, filter, canEdit, canManage, course);
		checklistController.addLoggingResourceable(LoggingResourceable.wrap(coursenode));
		return checklistController;
	}
	
	/**
	 * Create run view of this checklist.
	 * @param ureq
	 * @param wControl
	 * @param checklist
	 * @param filter
	 * @return controller
	 */
	public Controller createDisplayController(UserRequest ureq, WindowControl wControl, Checklist checklist, List<ChecklistFilter> filter) {
		return new ChecklistDisplayController(ureq, wControl, checklist, filter, false, false, null);
	}
	
	/**
	 * Create edit view of this checklist.
	 * @param ureq
	 * @param wControl
	 * @param checklist
	 * @param filter
	 * @param extendedAccess
	 * @return controller
	 */
	public Controller createEditCheckpointsController(UserRequest ureq, WindowControl wControl, Checklist checklist, String submitKey, CheckpointComparator checkpointComparator) {
		return new ChecklistEditCheckpointsController(ureq, wControl, checklist, submitKey, checkpointComparator);
	}
	
	/**
	 * Create manage view of this checklist.
	 * @param ureq
	 * @param wControl
	 * @param checklist
	 * @param course
	 * @return controller
	 */
	public Controller createManageCheckpointsController(UserRequest ureq, WindowControl wControl, Checklist checklist, ICourse course) {
		return new ChecklistManageCheckpointsController(ureq, wControl, checklist, course);
	}

}
