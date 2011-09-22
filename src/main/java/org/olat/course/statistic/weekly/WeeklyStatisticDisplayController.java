package org.olat.course.statistic.weekly;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.statistic.DateChooserForm;
import org.olat.course.statistic.IStatisticManager;
import org.olat.course.statistic.StatisticDisplayController;
import org.olat.course.statistic.StatisticResult;

public class WeeklyStatisticDisplayController extends StatisticDisplayController {

	/** the logging object used in this class **/
	private static final OLog log_ = Tracing.createLoggerFor(WeeklyStatisticDisplayController.class);

	private VelocityContainer weeklyStatisticFormVc_;
	private VelocityContainer weeklyStatisticVc_;
	private DateChooserForm form_;

	public WeeklyStatisticDisplayController(UserRequest ureq, WindowControl windowControl, ICourse course, IStatisticManager statisticManager) {
		super(ureq, windowControl, course, statisticManager);
	}
	
	@Override
	protected Component createInitialComponent(UserRequest ureq) {
		setVelocityRoot(Util.getPackageVelocityRoot(getClass()));

		weeklyStatisticVc_ = this.createVelocityContainer("weeklystatisticparent");
		
		weeklyStatisticFormVc_ = this.createVelocityContainer("weeklystatisticform");
		form_ = new DateChooserForm(ureq, getWindowControl(), 8*7);
		listenTo(form_);
		weeklyStatisticFormVc_.put("statisticForm", form_.getInitialComponent());
		weeklyStatisticFormVc_.contextPut("statsSince", getStatsSinceStr(ureq));

		weeklyStatisticVc_.put("weeklystatisticform", weeklyStatisticFormVc_);

		Component parentInitialComponent = super.createInitialComponent(ureq);
		weeklyStatisticVc_.put("statistic", parentInitialComponent);
		
		return weeklyStatisticVc_;
	}

	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == form_ && event == Event.DONE_EVENT) {
			// need to regenerate the statisticResult
			// and now recreate the table controller
			recreateTableController(ureq);
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected StatisticResult recalculateStatisticResult(UserRequest ureq) {
		// recalculate the statistic result based on the from and to dates.
		// do this by going via sql (see WeeklyStatisticManager)
		IStatisticManager weeklyStatisticManager = getStatisticManager();			
		StatisticResult statisticResult = 
			weeklyStatisticManager.generateStatisticResult(ureq, getCourse(), getCourseRepositoryEntryKey(), form_.getFromDate(), form_.getToDate());
		return statisticResult;
	}
}
