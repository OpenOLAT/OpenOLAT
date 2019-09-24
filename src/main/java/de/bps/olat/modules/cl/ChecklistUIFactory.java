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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.ICourse;

/**
 * Description:<br>
 * This Factory provides a set of specific checklist views.
 * 
 * <P>
 * Initial Date:  22.07.2009 <br>
 * @author bja <bja@bps-system.de>
 */
public class ChecklistUIFactory {
	
	
	public static final CheckpointComparator comparatorTitleAsc = new CheckpointComparator(1, true);
	public static final CheckpointComparator comparatorTitleDesc = new CheckpointComparator(1, false);
	public static final CheckpointComparator comparatorDescriptionAsc = new CheckpointComparator(2, true);
	public static final CheckpointComparator comparatorDescriptionDesc = new CheckpointComparator(2, false);
	public static final CheckpointComparator comparatorModeAsc = new CheckpointComparator(3, true);
	public static final CheckpointComparator comparatorModeDesc = new CheckpointComparator(3, false);
	
	/** singleton */
	private static ChecklistUIFactory INSTANCE = new ChecklistUIFactory();
	
	private ChecklistUIFactory() {
		// constructor
	}
	
	public static ChecklistUIFactory getInstance() {
		return INSTANCE;
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
	public Controller createManageCheckpointsController(UserRequest ureq, WindowControl wControl, Checklist checklist, ICourse course, boolean readOnly) {
		return new ChecklistManageCheckpointsController(ureq, wControl, checklist, course, readOnly);
	}

}
