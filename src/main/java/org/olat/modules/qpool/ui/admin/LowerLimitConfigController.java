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

import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.manager.review.LowerLimitProvider;

/**
 * 
 * Initial date: 29.11.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LowerLimitConfigController extends FormBasicController implements ReviewDecisionConfigController {

	private TextElement numberOfRatingsEl;
	private TextElement lowerLimitEl;
	
	private Map<String, String> config;

	public LowerLimitConfigController(UserRequest ureq, WindowControl wControl, Map<String, String> config) {
		super(ureq, wControl);
		this.config = config;
		
		initForm(ureq);
	}
	
	@Override
	public Map<String, String> getConfiguration() {
		config.put(LowerLimitProvider.NUMBER_OF_RATINGS, numberOfRatingsEl.getValue());
		config.put(LowerLimitProvider.LOWER_LIMIT, lowerLimitEl.getValue());
		return config;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String numberOfRatings = config.get(LowerLimitProvider.NUMBER_OF_RATINGS);
		numberOfRatingsEl = uifactory.addTextElement("number.of.ratings", 5, numberOfRatings, formLayout);
		numberOfRatingsEl.setMandatory(true);
		numberOfRatingsEl.setDisplaySize(5);

		String averageRating = config.get(LowerLimitProvider.LOWER_LIMIT);
		lowerLimitEl = uifactory.addTextElement("average.rating", 1, averageRating, formLayout);
		lowerLimitEl.setMandatory(true);
		lowerLimitEl.setDisplaySize(1);

//		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
//		buttonLayout.setRootForm(mainForm);
//		formLayout.add("buttons", buttonLayout);
//		uifactory.addFormSubmitButton("save", buttonLayout);
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
//		int numberOfRatingsForFinal = Integer.parseInt(numberOfRatingForFinalEl.getValue());
//		qpoolModule.setNumberOfRatingsForFinal(numberOfRatingsForFinal);
//		int averageRatingForFinal = Integer.parseInt(averageRatingForFinalEl.getValue());
//		qpoolModule.setAverageRatingForFinal(averageRatingForFinal);
	}

	@Override
	protected void doDispose() {
		//
	}

}
