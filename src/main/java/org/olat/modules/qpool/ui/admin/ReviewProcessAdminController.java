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
package org.olat.modules.qpool.ui.admin;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.rating.RatingFormItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.ReviewDecisionProvider;
import org.olat.modules.qpool.ReviewService;
import org.olat.modules.qpool.manager.review.LowerLimitProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReviewProcessAdminController extends FormBasicController {

	private static final String FINAL_VISIBILITY_MANAGER = "final.visibility.manager";
	private static final String FINAL_VISIBILITY_MANAGER_TEACHER = "final.visibility.manager.teacher";

	private SingleSelection providerEl;
	private TextElement numberOfRatingsEl;
	private RatingFormItem lowerLimitEl;
	private SingleSelection finalVisibilityEl;
	
	@Autowired
	private QuestionPoolModule qpoolModule;
	@Autowired
	private ReviewService reviewService;
	
	public ReviewProcessAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_qpool_review_process_admin");
		setFormTitle("admin.review.process.title");
		
		// decision provider
		List<ReviewDecisionProvider> providers = reviewService.getSelectableReviewDecisionProviders();
		String[] providerKeys = new String[providers.size()];
		String[] providerValues = new String[providers.size()];
		for(int i=providers.size(); i-->0; ) {
			ReviewDecisionProvider provider = providers.get(i);
			providerKeys[i] = provider.getType();
			providerValues[i] = provider.getName(getLocale());
		}
		
		String providerType = qpoolModule.getReviewDecisionProviderType();
		providerEl = uifactory.addDropdownSingleselect("admin.review.process.decision.type", formLayout, providerKeys, providerValues);
		providerEl.addActionListener(FormEvent.ONCHANGE);
		if(StringHelper.containsNonWhitespace(providerType)) {
			for(String providerKey:providerKeys) {
				if(providerKey.equals(providerType)) {
					providerEl.select(providerKey, true);
				}
			}
			if (!providerEl.isOneSelected()) {
				providerEl.select(providerKeys[0], true);
			}
		}
		
		// lower limit provider
		providerEl.setExampleKey("lower.limit.desc", null);
		
		String numberOfRatings = Integer.toString(qpoolModule.getReviewDecisionNumberOfRatings());
		numberOfRatingsEl = uifactory.addTextElement("number.of.ratings", 5, numberOfRatings, formLayout);
		numberOfRatingsEl.setElementCssClass("o_sel_qpool_num_of_reviews");
		numberOfRatingsEl.setMandatory(true);
		numberOfRatingsEl.setDisplaySize(5);

		lowerLimitEl = uifactory.addRatingItem("lower.limit", "lower.limit", qpoolModule.getReviewDecisionLowerLimit(), 5, true, formLayout);
		lowerLimitEl.setMandatory(true);
		
		uifactory.addSpacerElement("spacer", formLayout, false);
		
		// final visibility
		String[] finalVisibilityKeys = new String[] {FINAL_VISIBILITY_MANAGER, FINAL_VISIBILITY_MANAGER_TEACHER};
		String[] finalVisibilityValues= new String[] {
				translate(FINAL_VISIBILITY_MANAGER),
				translate(FINAL_VISIBILITY_MANAGER_TEACHER)};
		finalVisibilityEl = uifactory.addDropdownSingleselect("final.visibility", formLayout, finalVisibilityKeys, finalVisibilityValues);
		finalVisibilityEl.setElementCssClass("o_sel_qpool_final_visibility");
		String selectedKey = qpoolModule.isFinalVisibleTeach()? FINAL_VISIBILITY_MANAGER_TEACHER: FINAL_VISIBILITY_MANAGER;
		finalVisibilityEl.select(selectedKey, true);

		//buttons
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		
		updateUI();
	}
	
	private void updateUI() {
		String selectedProviderType = providerEl.isOneSelected()? providerEl.getSelectedKey(): null;
		boolean lowerLimitProvider = LowerLimitProvider.TYPE.equals(selectedProviderType)? true: false;
		numberOfRatingsEl.setVisible(lowerLimitProvider);
		lowerLimitEl.setVisible(lowerLimitProvider);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (providerEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateInt(numberOfRatingsEl);
		allOk &= validateInt(lowerLimitEl);
		
		return allOk;
	}
	
	private boolean validateInt(TextElement el) {
		boolean allOk = true;
		
		el.clearError();
		if(StringHelper.containsNonWhitespace(el.getValue())) {
			try {
				int val = Integer.parseInt(el.getValue());
				if(val <= 0) {
					el.setErrorKey("error.integer.positive", null);
					allOk &= false;
				}
			} catch (Exception e) {
				el.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		} else {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateInt(RatingFormItem el) {
		boolean allOk = true;
		
		el.clearError();
		Integer lowerLimit = Float.valueOf(el.getCurrentRating()).intValue();
		if(lowerLimit <= 0) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String selectedProviderType = providerEl.getSelectedKey();
		qpoolModule.setReviewDecisionType(selectedProviderType);
		boolean lowerLimitProvider = LowerLimitProvider.TYPE.equals(selectedProviderType)? true: false;
		if (lowerLimitProvider) {
			int numberOfRatings = Integer.parseInt(numberOfRatingsEl.getValue());
			qpoolModule.setReviewDecisionNumberOfRatings(numberOfRatings);
			int lowerLimit = Float.valueOf(lowerLimitEl.getCurrentRating()).intValue();
			qpoolModule.setReviewDecisionLowerLimit(lowerLimit);
		}
		
		String selectedFinalVisibility = finalVisibilityEl.getSelectedKey();
		if (FINAL_VISIBILITY_MANAGER_TEACHER.equals(selectedFinalVisibility)) {
			qpoolModule.setFinalVisibleTeach(true);
		} else {
			qpoolModule.setFinalVisibleTeach(false);
		}
	}
}
