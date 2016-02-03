package org.olat.ims.qti21.ui.statistics;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.StatisticsComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.QTI21StatisticsManager;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;

/**
 * 
 * Initial date: 03.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentItemStatisticsController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private final int numOfParticipants;
	private final AssessmentItem item;
	private final QTI21StatisticSearchParams searchParams;
	private final QTI21StatisticResourceResult resourceResult;
	
	@Autowired
	private QTI21StatisticsManager qtiStatisticsManager;
	
	public QTI21AssessmentItemStatisticsController(UserRequest ureq, WindowControl wControl,
			AssessmentItem item, String sectionTitle, QTI21StatisticResourceResult resourceResult, boolean printMode) {
		super(ureq, wControl);
		
		this.item = item;
		
		this.resourceResult = resourceResult;
		searchParams = resourceResult.getSearchParams();
		numOfParticipants = resourceResult.getQTIStatisticAssessment().getNumOfParticipants();

		mainVC = createVelocityContainer("statistics_item");
		mainVC.put("d3loader", new StatisticsComponent("d3loader"));
		mainVC.contextPut("title", item.getTitle());
		if(StringHelper.containsNonWhitespace(sectionTitle)) {
			mainVC.contextPut("sectionTitle", sectionTitle);
		}
		mainVC.contextPut("numOfParticipants", resourceResult.getQTIStatisticAssessment().getNumOfParticipants());
		mainVC.contextPut("printMode", new Boolean(printMode));

		List<Interaction> interactions = item.getItemBody().findInteractions();
		System.out.println(interactions.size());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	

}
