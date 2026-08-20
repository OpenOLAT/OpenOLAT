/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.repository.ui.list;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.rating.RatingFormEvent;
import org.olat.core.gui.components.rating.RatingWithAverageFormItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryStatistics;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 19 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class InfoPageRatingController extends FormBasicController {

	private RatingWithAverageFormItem ratingEl;

	private final RepositoryEntry entry;
	private final boolean guestOnly;

	@Autowired
	private CommentAndRatingService commentAndRatingService;

	public InfoPageRatingController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean guestOnly) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.guestOnly = guestOnly;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		RepositoryEntryStatistics statistics = entry.getStatistics();
		Integer myRating = commentAndRatingService.getRatingValue(getIdentity(), entry, null);
		Double averageRating = statistics.getRating();
		long numOfRatings = statistics.getNumOfRatings();
		float ratingValue = myRating == null ? 0f : myRating.floatValue();
		float averageRatingValue = averageRating == null ? 0f : averageRating.floatValue();

		ratingEl = new RatingWithAverageFormItem("rating", ratingValue, averageRatingValue, 5, numOfRatings);
		ratingEl.setEnabled(!guestOnly);
		formLayout.add("rating", ratingEl);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (ratingEl == source && event instanceof RatingFormEvent ratingEvent) {
			doRating(ratingEvent.getRating());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doRating(float rating) {
		int ratingValue = Math.round(rating);
		UserRating userRating = commentAndRatingService.getRating(getIdentity(), entry, null);
		if (userRating == null) {
			commentAndRatingService.createRating(getIdentity(), entry, null, ratingValue);
		} else {
			commentAndRatingService.updateRating(userRating, ratingValue);
		}
	}

}
