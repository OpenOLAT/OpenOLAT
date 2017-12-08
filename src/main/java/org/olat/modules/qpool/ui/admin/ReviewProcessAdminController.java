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

	private SingleSelection providerEl;
	private TextElement numberOfRatingsEl;
	private TextElement lowerLimitEl;
	
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
		providerEl = uifactory.addDropdownSingleselect("admin.review.process.decision.type", formLayout, providerKeys, providerValues, null);
		providerEl.addActionListener(FormEvent.ONCHANGE);
		if(StringHelper.containsNonWhitespace(providerType)) {
			for(String providerKey:providerKeys) {
				if(providerKey.equals(providerType)) {
					providerEl.select(providerKey, true);
				}
			}
		}
		
		// lower limit provider
		String numberOfRatings = Integer.toString(qpoolModule.getReviewDecisionNumberOfRatings());
		numberOfRatingsEl = uifactory.addTextElement("number.of.ratings", 5, numberOfRatings, formLayout);
		numberOfRatingsEl.setMandatory(true);
		numberOfRatingsEl.setDisplaySize(5);

		String lowerLimit = Integer.toString(qpoolModule.getReviewDecisionLowerLimit());
		lowerLimitEl = uifactory.addTextElement("lower.limit", 1, lowerLimit, formLayout);
		lowerLimitEl.setMandatory(true);
		lowerLimitEl.setDisplaySize(1);

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
		boolean allOk = true;
		
		allOk &= validateInt(numberOfRatingsEl);
		allOk &= validateInt(lowerLimitEl);
		
		return allOk & super.validateFormLogic(ureq);
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

	@Override
	protected void formOK(UserRequest ureq) {
		
		String selectedProviderType = providerEl.getSelectedKey();
		qpoolModule.setReviewDecisionType(selectedProviderType);
		boolean lowerLimitProvider = LowerLimitProvider.TYPE.equals(selectedProviderType)? true: false;
		if (lowerLimitProvider) {
			int numberOfRatings = Integer.parseInt(numberOfRatingsEl.getValue());
			qpoolModule.setReviewDecisionNumberOfRatings(numberOfRatings);
			int lowerLimit = Integer.parseInt(lowerLimitEl.getValue());
			qpoolModule.setReviewDecisionLowerLimit(lowerLimit);
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	

}
