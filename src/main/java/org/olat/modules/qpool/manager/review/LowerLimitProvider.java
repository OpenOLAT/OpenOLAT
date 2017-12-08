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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.qpool.manager.review;

import java.util.Locale;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.ReviewDecisionProvider;
import org.olat.modules.qpool.model.ReviewDecision;
import org.olat.modules.qpool.ui.admin.ReviewProcessAdminController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 06.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class LowerLimitProvider implements ReviewDecisionProvider {

	public static final String TYPE = "lowerLimitProvider";
	
	@Autowired
	private QuestionPoolModule qpoolModule;
	@Autowired
	private CommentAndRatingService commentAndRatingService;

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getName(Locale locale) {
		Translator translator = Util.createPackageTranslator(ReviewProcessAdminController.class, locale);
		return translator.translate("lower.limit.provider.name");
	}
	
	@Override
	public boolean isSelectable() {
		return true;
	}

	@Override
	public boolean hasRatingController() {
		return true;
	}

	@Override
	public ReviewDecision decideStatus(QuestionItemShort item, Float rating) {
		ReviewDecision decision = new ReviewDecision(false, item.getQuestionStatus());
		if (ratingToLow(rating)) {
			decision = new ReviewDecision(true, QuestionStatus.revised);
		} else {
			Long numberOfRatings = commentAndRatingService.countRatings(item, null);
			if (hasEnoughRatings(numberOfRatings)) {
				decision = new ReviewDecision(true, QuestionStatus.finalVersion);
			}
		}
		return decision;
	}

	private boolean ratingToLow(Float rating) {
		return rating < qpoolModule.getReviewDecisionLowerLimit();
	}

	private boolean hasEnoughRatings(Long numberOfRatings) {
		return numberOfRatings != null && numberOfRatings >= qpoolModule.getReviewDecisionNumberOfRatings();
	}

}
