/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui.peerreview;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.rating.RatingComponent;
import org.olat.core.gui.components.rating.RatingType;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.ui.GTAParticipantController;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.ui.multireport.MultiEvaluationsFormController;

/**
 * 
 * Initial date: 5 août 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAMultiEvaluationsFormController extends BasicController {

	private final Link closeButton;
	private final VelocityContainer mainVC;
	private final GTAPeerReviewLegendNameGenerator legendNameGenerator;
	
	private int count = 0;
	
	public GTAMultiEvaluationsFormController(UserRequest ureq, WindowControl wControl,
			Form form, DataStorage storage, SessionFilter filter, GTAPeerReviewLegendNameGenerator legendNameGenerator,
			GTACourseNode gtaNode, List<TaskReviewAssignment> assignments) {
		super(ureq, wControl, Util.createPackageTranslator(GTAParticipantController.class, ureq.getLocale()));
		this.legendNameGenerator = legendNameGenerator;
		
		mainVC = createVelocityContainer("evaluations_overview");
		
		MultiEvaluationsFormController multiEvaluationFormCtrl = new MultiEvaluationsFormController(ureq, getWindowControl(),
				form, storage, filter, legendNameGenerator);
		listenTo(multiEvaluationFormCtrl);
		mainVC.put("evaluations", multiEvaluationFormCtrl.getInitialComponent());
		
		closeButton = LinkFactory.createButton("close", "close", mainVC, this);
		closeButton.setElementCssClass("btn-primary");
		initRatings(gtaNode, assignments);

		putInitialPanel(mainVC);
	}
	
	private void initRatings(GTACourseNode gtaNode, List<TaskReviewAssignment> assignments) {
		List<Rating> ratings = new ArrayList<>();

		boolean qualityFeedback = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_PEER_REVIEW_QUALITY_FEEDBACK, false);
		String qualityFeedbackType = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_QUALITY_FEEDBACK_TYPE,
					GTACourseNode.GTASK_VALUE_PEER_REVIEW_QUALITY_FEEDBACK_YES_NO);
		if(qualityFeedback) {
			boolean withYesNoRating = GTACourseNode.GTASK_VALUE_PEER_REVIEW_QUALITY_FEEDBACK_YES_NO.equals(qualityFeedbackType);
			boolean withStarsRating = GTACourseNode.GTASK_VALUE_PEER_REVIEW_QUALITY_FEEDBACK_STARS.equals(qualityFeedbackType);
			for(TaskReviewAssignment assignment:assignments) {
				if(assignment.getRating() != null) {	
					float rating = assignment.getRating().floatValue();
					RatingComponent ratingCmp;
					String ratingId = "reviewRating_" + (++count);
					if(withYesNoRating) {
						ratingCmp = new RatingComponent(ratingId, RatingType.yesNo, 0, 5, false);
					} else if(withStarsRating) {
						ratingCmp = new RatingComponent(ratingId, RatingType.stars, 0, 5, false);
					} else {
						continue;
					}
					ratingCmp.setCurrentRating(rating);
					ratingCmp.setEnabled(false);
					
					String user = legendNameGenerator.getName(assignment.getParticipation());
					ratings.add(new Rating(ratingId, user, ratingCmp));
					mainVC.put(ratingId, ratingCmp);
				}
			}
		}

		mainVC.contextPut("ratings", ratings);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(closeButton == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
		}
	}
	
	public record Rating(String ratingId, String user, RatingComponent ratingCmp) {
		//
	}
}
