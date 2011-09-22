package org.olat.course.assessment;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable;
import org.olat.core.id.OLATResourceable;
import org.olat.course.ICourse;

public interface AssessmentControllerCreator {

	
	
	public Activateable createAssessmentMainController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, IAssessmentCallback assessmentCallback);
	
	
	public Controller createQTIArchiveWizardController(boolean dummyMode, UserRequest ureq, List nodesTableObjectArrayList, ICourse course,
			WindowControl windowControl);
	
	public Controller createContextualSubscriptionController(UserRequest ureq, WindowControl wControl, ICourse course);
	
}
