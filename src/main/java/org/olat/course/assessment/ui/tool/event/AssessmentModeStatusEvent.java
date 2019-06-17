package org.olat.course.assessment.ui.tool.event;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 17 juin 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeStatusEvent extends Event {

	private static final long serialVersionUID = 1365578226635269327L;
	public static final String STATUS_CHANGED = "assessment-mode-status-changed";
	
	public AssessmentModeStatusEvent() {
		super(STATUS_CHANGED);
	}

}
