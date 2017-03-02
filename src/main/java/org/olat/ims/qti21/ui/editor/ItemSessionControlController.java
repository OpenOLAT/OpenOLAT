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
 * To set the options for the timeLimits and itemSessionControl
 * 
 * 
 * Initial date: 03.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class ItemSessionControlController extends FormBasicController {

	private static final String YES = "y";
	private static final String NO = "n";
	private static final String INHERIT = "inherit";
	private static final String[] yesnoKeys = new String[] { YES, NO };
	private static final String[] yesNoInheritKeys = new String[] { YES, NO, INHERIT };

	private TextElement maxAttemptsEl;
	protected SingleSelection limitAttemptsEl, allowSkippingEl, allowCommentEl, allowReviewEl, showSolutionEl;
	
	private final boolean allowInherit;
	protected final boolean editable;
	protected final boolean restrictedEdit;
	private final AbstractPart part;
	
	public ItemSessionControlController(UserRequest ureq, WindowControl wControl,
			AbstractPart part, boolean restrictedEdit, boolean editable) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentTestDisplayController.class, ureq.getLocale()));
		this.part = part;
		allowInherit = !(part instanceof TestPart);
		this.editable = editable;
		this.restrictedEdit = restrictedEdit;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] yesNoValues = new String[] { translate("yes"), translate("no") };
		String[] yesNoInheritValues = new String[] { translate("yes"), translate("no"), translate("inherit") };
		// inherit is allowed?
		String[] aKeys = allowInherit ?  yesNoInheritKeys : yesnoKeys;
		String[] aValues = allowInherit ? yesNoInheritValues : yesNoValues;

		ItemSessionControl itemSessionControl = part.getItemSessionControl();//can be null
		Integer maxAttempts = null;
		if(itemSessionControl != null) {
			maxAttempts = itemSessionControl.getMaxAttempts();
		}
		limitAttemptsEl = uifactory.addRadiosHorizontal("form.imd.limittries", formLayout, aKeys, aValues);
		limitAttemptsEl.addActionListener(FormEvent.ONCLICK);
		String maxAttemptsStr = maxAttempts == null ? "" : maxAttempts.toString();
		maxAttemptsEl = uifactory.addTextElement("attempts", "item.session.control.attempts", 4, maxAttemptsStr, formLayout);
		if(maxAttempts == null) {
			if(aKeys.length == 2) {
				limitAttemptsEl.select(YES, true);
				maxAttemptsEl.setValue("1");
			} else {
				limitAttemptsEl.select(INHERIT, true);
			}
		} else if(maxAttempts.intValue() == 0) {
			limitAttemptsEl.select(NO, true);
		} else {
			limitAttemptsEl.select(YES, true);
		}
		limitAttemptsEl.setEnabled(!restrictedEdit && editable);
		maxAttemptsEl.setVisible(limitAttemptsEl.isSelected(0));
		maxAttemptsEl.setEnabled(!restrictedEdit && editable);
		
		allowSkippingEl = uifactory.addRadiosHorizontal("item.session.control.allow.skipping", formLayout, aKeys, aValues);
		allowSkippingEl.addActionListener(FormEvent.ONCHANGE);
		allowSkippingEl.setEnabled(!restrictedEdit && editable);
		allowSkippingEl.setHelpText(translate("item.session.control.allow.skipping.hint"));
		// the default value is allowSkipping=true
		if(itemSessionControl != null && itemSessionControl.getAllowSkipping() != null) {
			String key = itemSessionControl.getAllowSkipping().booleanValue() ? YES : NO;
			allowSkippingEl.select(key, true);
		} else {
			String key = allowInherit ? INHERIT : YES;
			allowSkippingEl.select(key, true);
		}
		
		allowCommentEl = uifactory.addRadiosHorizontal("item.session.control.allow.comment", formLayout, aKeys, aValues);
		allowCommentEl.addActionListener(FormEvent.ONCHANGE);
		allowCommentEl.setEnabled(!restrictedEdit && editable);
		allowCommentEl.setHelpText(translate("item.session.control.allow.comment.hint"));
		if(itemSessionControl != null && itemSessionControl.getAllowComment() != null) {
			String key = itemSessionControl.getAllowComment().booleanValue() ? YES : NO;
			allowCommentEl.select(key, true);
		} else {
			String key = allowInherit ? INHERIT : YES;
			allowCommentEl.select(key, true);
		}
		
		allowReviewEl = uifactory.addRadiosHorizontal("item.session.control.allow.review", formLayout, aKeys, aValues);
		allowReviewEl.addActionListener(FormEvent.ONCHANGE);
		allowReviewEl.setEnabled(!restrictedEdit && editable);
		allowReviewEl.setHelpText(translate("item.session.control.allow.review.hint"));
		if(itemSessionControl != null && itemSessionControl.getAllowReview() != null) {
			String key = itemSessionControl.getAllowReview().booleanValue() ? YES : NO;
			allowReviewEl.select(key, true);
		} else {
			String key = allowInherit ? INHERIT : NO;
			allowReviewEl.select(key, true);
		}
	
		showSolutionEl = uifactory.addRadiosHorizontal("item.session.control.show.solution", formLayout, aKeys, aValues);
		showSolutionEl.addActionListener(FormEvent.ONCHANGE);
		showSolutionEl.setEnabled(!restrictedEdit && editable);
		showSolutionEl.setHelpText(translate("item.session.control.show.solution.hint"));
		if(itemSessionControl != null && itemSessionControl.getShowSolution() != null) {
			String key = itemSessionControl.getShowSolution().booleanValue() ? YES : NO;
			showSolutionEl.select(key, true);
		} else {
			String key = allowInherit ? INHERIT : NO;
			showSolutionEl.select(key, false);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		allOk &= validateSingleSelection(allowCommentEl);
		allOk &= validateSingleSelection(allowReviewEl);
		allOk &= validateSingleSelection(showSolutionEl);
		allOk &= validateSingleSelection(allowSkippingEl);

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
	
	private boolean validateSingleSelection(SingleSelection selectionEl) {
		boolean allOk = true;
		selectionEl.clearError();
		if(!selectionEl.isOneSelected()) {
			selectionEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		return allOk;
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
		if(allowSkippingEl.isOneSelected()) {
			if(allowSkippingEl.isSelected(0)) {//yes
				checkNotNull(itemSessionControl).setAllowSkipping(Boolean.TRUE);
			} else if(allowSkippingEl.isSelected(1)) {//no
				checkNotNull(itemSessionControl).setAllowSkipping(Boolean.FALSE);
			} else if(itemSessionControl != null) {//inherit
				itemSessionControl.setAllowSkipping(null);
			}
		}
		
		if(allowCommentEl.isOneSelected()) {
			if(allowCommentEl.isSelected(0)) {
				checkNotNull(itemSessionControl).setAllowComment(Boolean.TRUE);
			} else if(allowCommentEl.isSelected(1)) {
				checkNotNull(itemSessionControl).setAllowComment(Boolean.FALSE);
			} else if(itemSessionControl != null) {
				itemSessionControl.setAllowComment(null);
			}
		}
		
		if(allowReviewEl.isOneSelected()) {
			if(allowReviewEl.isSelected(0)) {
				checkNotNull(itemSessionControl).setAllowReview(Boolean.TRUE);
			} else if(allowReviewEl.isSelected(1)) {
				checkNotNull(itemSessionControl).setAllowReview(Boolean.FALSE);
			} else if(itemSessionControl != null) {
				itemSessionControl.setAllowReview(null);
			}
		}
		
		if(showSolutionEl.isOneSelected()) {
			if(showSolutionEl.isSelected(0)) {
				checkNotNull(itemSessionControl).setShowSolution(Boolean.TRUE);
			} else if(showSolutionEl.isSelected(1)) {
				checkNotNull(itemSessionControl).setShowSolution(Boolean.FALSE);
			} else if(itemSessionControl != null) {
				itemSessionControl.setShowSolution(null);
			}
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
