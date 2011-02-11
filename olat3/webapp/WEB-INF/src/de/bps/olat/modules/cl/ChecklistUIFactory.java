/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
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
