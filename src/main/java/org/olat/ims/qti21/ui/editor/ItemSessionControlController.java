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
package org.olat.ims.qti21.ui.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;

import uk.ac.ed.ph.jqtiplus.node.test.AbstractPart;
import uk.ac.ed.ph.jqtiplus.node.test.ItemSessionControl;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;

/**
 * 
 * Initial date: 03.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class ItemSessionControlController extends FormBasicController {

	private static final String[] yesnoKeys = new String[] { "y", "n" };
	private static final String[] attemtpsKeys = new String[] { "y", "n", "inherit" };

	private TextElement maxAttemptsEl /*, maxTimeEl */;
	private SingleSelection limitAttemptsEl, allowSkippingEl, allowCommentEl, allowReviewEl, showSolutionEl;
	
	protected final boolean editable;
	protected final boolean restrictedEdit;
	private final AbstractPart part;
	
	public ItemSessionControlController(UserRequest ureq, WindowControl wControl,
			AbstractPart part, boolean restrictedEdit, boolean editable) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentTestDisplayController.class, ureq.getLocale()));
		this.part = part;
		this.editable = editable;
		this.restrictedEdit = restrictedEdit;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		/*
		TimeLimits timeLimits = part.getTimeLimits();
		String timeMax = "";
		if(timeLimits != null && timeLimits.getMaximumMillis() != null && timeLimits.getMaximumMillis().longValue() > 0) {
			long maxInMinute = timeLimits.getMaximumMillis().longValue() / (60 * 1000);
			timeMax = Long.toString(maxInMinute);
		}
		maxTimeEl = uifactory.addTextElement("time.limit", "time.limit.max", 4, timeMax, formLayout);
		maxTimeEl.setDisplaySize(4);
		maxTimeEl.setEnabled(!restrictedEdit);
		*/

		ItemSessionControl itemSessionControl = part.getItemSessionControl();//can be null
		Integer maxAttempts = null;
		if(itemSessionControl != null) {
			maxAttempts = itemSessionControl.getMaxAttempts();
		}
		String[] aKeys = part instanceof TestPart ? yesnoKeys : attemtpsKeys;
		String[] yesnoValues = new String[] { translate("yes"), translate("no") };
		String[] attemptsValues = new String[] { translate("yes"), translate("no"), translate("inherit") };
		String[] aValues = part instanceof TestPart ? yesnoValues : attemptsValues;
		limitAttemptsEl = uifactory.addRadiosHorizontal("form.imd.limittries", formLayout, aKeys, aValues);
		limitAttemptsEl.addActionListener(FormEvent.ONCLICK);

		String maxAttemptsStr = maxAttempts == null ? "" : maxAttempts.toString();
		maxAttemptsEl = uifactory.addTextElement("attempts", "item.session.control.attempts", 4, maxAttemptsStr, formLayout);
		if(maxAttempts == null) {
			if(aKeys.length == 2) {
				limitAttemptsEl.select(attemtpsKeys[0], true);
				maxAttemptsEl.setValue("1");
			} else {
				limitAttemptsEl.select(attemtpsKeys[2], true);
			}
		} else if(maxAttempts.intValue() == 0) {
			limitAttemptsEl.select(attemtpsKeys[1], true);
		} else {
			limitAttemptsEl.select(attemtpsKeys[0], true);
		}
		limitAttemptsEl.setEnabled(!restrictedEdit && editable);
		maxAttemptsEl.setVisible(limitAttemptsEl.isSelected(0));
		maxAttemptsEl.setEnabled(!restrictedEdit && editable);
		
		allowSkippingEl = uifactory.addRadiosHorizontal("item.session.control.allow.skipping", formLayout, yesnoKeys, yesnoValues);
		allowSkippingEl.addActionListener(FormEvent.ONCHANGE);
		allowSkippingEl.setEnabled(!restrictedEdit && editable);
		// the default value is allowSkipping=true
		if(itemSessionControl != null && itemSessionControl.getAllowSkipping() != null && !itemSessionControl.getAllowSkipping().booleanValue()) {
			allowSkippingEl.select(yesnoKeys[1], true);
		} else {
			allowSkippingEl.select(yesnoKeys[0], true);
		}
		
		allowCommentEl = uifactory.addRadiosHorizontal("item.session.control.allow.comment", formLayout, yesnoKeys, yesnoValues);
		allowCommentEl.addActionListener(FormEvent.ONCHANGE);
		allowCommentEl.setEnabled(!restrictedEdit && editable);
		if(itemSessionControl != null && itemSessionControl.getAllowComment() != null && itemSessionControl.getAllowComment().booleanValue()) {
			allowCommentEl.select(yesnoKeys[0], true);
		} else {
			allowCommentEl.select(yesnoKeys[1], false);
		}
		
		allowReviewEl = uifactory.addRadiosHorizontal("item.session.control.allow.review", formLayout, yesnoKeys, yesnoValues);
		allowReviewEl.addActionListener(FormEvent.ONCHANGE);
		allowReviewEl.setEnabled(!restrictedEdit && editable);
		if(itemSessionControl != null && itemSessionControl.getAllowReview() != null && itemSessionControl.getAllowReview().booleanValue()) {
			allowReviewEl.select(yesnoKeys[0], true);
		} else {
			allowReviewEl.select(yesnoKeys[1], false);
		}
	
		showSolutionEl = uifactory.addRadiosHorizontal("item.session.control.show.solution", formLayout, yesnoKeys, yesnoValues);
		showSolutionEl.addActionListener(FormEvent.ONCHANGE);
		showSolutionEl.setEnabled(!restrictedEdit && editable);
		if(itemSessionControl != null && itemSessionControl.getShowSolution() != null && itemSessionControl.getShowSolution().booleanValue()) {
			showSolutionEl.select(yesnoKeys[0], true);
		} else {
			showSolutionEl.select(yesnoKeys[1], false);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		allowCommentEl.clearError();
		if(!allowCommentEl.isOneSelected()) {
			allowCommentEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		allowReviewEl.clearError();
		if(!allowReviewEl.isOneSelected()) {
			allowReviewEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		showSolutionEl.clearError();
		if(!showSolutionEl.isOneSelected()) {
			showSolutionEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		maxAttemptsEl.clearError();
		if(limitAttemptsEl.isOneSelected() && limitAttemptsEl.isSelected(0) &&
				StringHelper.containsNonWhitespace(maxAttemptsEl.getValue())) {
			try {
				int maxAttempts = Integer.parseInt(maxAttemptsEl.getValue());
				if(maxAttempts < 0) {
					maxAttemptsEl.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				maxAttemptsEl.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		}
		
		/*
		maxTimeEl.clearError();
		if(StringHelper.containsNonWhitespace(maxTimeEl.getValue())) {
			try {
				int timeLimite = Integer.parseInt(maxTimeEl.getValue());
				if(timeLimite < 0) {
					maxTimeEl.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				maxTimeEl.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		}
		*/

		return allOk & super.validateFormLogic(ureq);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == limitAttemptsEl) {
			maxAttemptsEl.setVisible(limitAttemptsEl.isOneSelected() && limitAttemptsEl.isSelected(0));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		ItemSessionControl itemSessionControl = part.getItemSessionControl();//can be null
		
		// need to be first! 
		if(allowSkippingEl.isOneSelected() && allowSkippingEl.isSelected(0)) {
			checkNotNull(itemSessionControl).setAllowSkipping(Boolean.TRUE);
		} else if(itemSessionControl != null) {
			itemSessionControl.setAllowSkipping(Boolean.FALSE);
		}
		
		if(allowCommentEl.isOneSelected() && allowCommentEl.isSelected(0)) {
			checkNotNull(itemSessionControl).setAllowComment(Boolean.TRUE);
		} else if(itemSessionControl != null) {
			itemSessionControl.setAllowComment(Boolean.FALSE);
		}
		
		if(allowReviewEl.isOneSelected() && allowReviewEl.isSelected(0)) {
			checkNotNull(itemSessionControl).setAllowReview(Boolean.TRUE);
		} else if(itemSessionControl != null) {
			itemSessionControl.setAllowReview(Boolean.FALSE);
		}
		
		if(showSolutionEl.isOneSelected() && showSolutionEl.isSelected(0)) {
			checkNotNull(itemSessionControl).setShowSolution(Boolean.TRUE);
		} else if(itemSessionControl != null) {
			itemSessionControl.setShowSolution(Boolean.FALSE);
		}
		
		if(limitAttemptsEl.isSelected(0) && maxAttemptsEl != null && maxAttemptsEl.isVisible()
				&& StringHelper.isLong(maxAttemptsEl.getValue())) {
			int maxAttempts = Integer.parseInt(maxAttemptsEl.getValue());
			checkNotNull(itemSessionControl).setMaxAttempts(new Integer(maxAttempts));
		} else if(limitAttemptsEl.isSelected(1)) {
			checkNotNull(itemSessionControl).setMaxAttempts(0);
		} else if(itemSessionControl != null) {
			itemSessionControl.setMaxAttempts(null);
		}
		
		/*
		TimeLimits timeLimits = part.getTimeLimits();
		if(StringHelper.containsNonWhitespace(maxTimeEl.getValue())) {
			int valInMinute = Integer.parseInt(maxTimeEl.getValue());
			checkNotNull(timeLimits).setMaximum(valInMinute * 60d);
		} else if(timeLimits != null) {
			timeLimits.setMaximum(null);
		}
		*/
	}
	
	private ItemSessionControl checkNotNull(ItemSessionControl itemSessionControl) {
		if(itemSessionControl == null) {
			itemSessionControl = new ItemSessionControl(part);
			part.setItemSessionControl(itemSessionControl);
		}
		return itemSessionControl;
	}
	
	/*
	private TimeLimits checkNotNull(TimeLimits timeLimits) {
		if(timeLimits == null) {
			timeLimits = new TimeLimits(part);
			part.setTimeLimits(timeLimits);
		}
		return timeLimits;
	}
	*/
}
