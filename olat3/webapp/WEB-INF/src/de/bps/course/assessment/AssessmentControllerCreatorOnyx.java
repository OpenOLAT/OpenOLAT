package de.bps.course.assessment;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable;
import org.olat.core.id.OLATResourceable;

import de.bps.course.assessment.AssessmentMainController;

import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentControllerCreatorOlat;
import org.olat.course.assessment.IAssessmentCallback;
import de.bps.ims.qti.export.QTIArchiveWizardController;

public class AssessmentControllerCreatorOnyx extends AssessmentControllerCreatorOlat {

	/**
	 * @see org.olat.course.assessment.AssessmentMainControllerCreator#create(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl, org.olat.core.id.OLATResourceable, org.olat.course.assessment.IAssessmentCallback)
	 */
	@Override
	public Activateable createAssessmentMainController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, IAssessmentCallback assessmentCallback) {
		return new AssessmentMainController(ureq, wControl, ores, assessmentCallback);
	}
	
	/**
	 * @see org.olat.course.assessment.AssessmentControllerCreator#createQTIArchiveWizardController(boolean, org.olat.core.gui.UserRequest, java.util.List, org.olat.course.ICourse, org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller createQTIArchiveWizardController(boolean dummyMode, UserRequest ureq, List nodesTableObjectArrayList, ICourse course,
			WindowControl wControl) {
		return new QTIArchiveWizardController(dummyMode, ureq, nodesTableObjectArrayList, course, wControl);
	}
}
