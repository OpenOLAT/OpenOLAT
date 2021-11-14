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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.editor.events.AssessmentTestEvent;

import uk.ac.ed.ph.jqtiplus.node.test.AbstractPart;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
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
	private SingleSelection limitAttemptsEl;
	private FormLink inheritMaxAttemptsButton;
	private FormLayoutContainer maxAttemptsWarningLayout;
	
	private SingleSelection allowReviewEl;
	private SingleSelection showSolutionEl;
	private SingleSelection allowSkippingEl;
	private SingleSelection allowCommentEl;
	
	private DialogBoxController attemptsWarningCtrl;
	
	private final AbstractPart part;
	protected final boolean editable;
	private final boolean allowInherit;
	protected final boolean restrictedEdit;
	
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
		limitAttemptsEl.setHelpTextKey("item.session.control.attempts.hint", null);
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
		
		String warningPage = velocity_root + "/max_attempts_warning.html";
		maxAttemptsWarningLayout = FormLayoutContainer.createCustomFormLayout("maxAttemptsWarning", getTranslator(), warningPage);
		formLayout.add(maxAttemptsWarningLayout);
		inheritMaxAttemptsButton = uifactory.addFormLink("force.inherited.max.attempts", maxAttemptsWarningLayout, Link.BUTTON_XSMALL);
		inheritMaxAttemptsButton.setForceOwnDirtyFormWarning(false);
		inheritMaxAttemptsButton.setDomReplacementWrapperRequired(false);
		maxAttemptsWarningLayout.add("force.inherited.max.attempts", inheritMaxAttemptsButton);
		updateWarningMaxAttempts();
		
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
			showSolutionEl.select(key, true);
		}
	}
	
	private void updateWarningMaxAttempts() {
		boolean needWarning = false;
		
		MaxAttemptsStatistics statistics = MaxAttemptsStatistics.calculate(part);
		if(statistics.getNumOfItems() != 0 && statistics.getNumOfItems() == statistics.getNumOfItemsWithoutInherited()) {
			String itemMsg = translate("warning.item.session.control.attempts.all.items.defined");
			maxAttemptsWarningLayout.contextPut("itemMsg", itemMsg);
			needWarning = true;
		} else if(statistics.getNumOfItemsWithoutInherited() > 0) {
			String itemMsg = translate("warning.item.session.control.attempts.items.defined");
			maxAttemptsWarningLayout.contextPut("itemMsg", itemMsg);
			needWarning = true;
		} else {
			maxAttemptsWarningLayout.contextRemove("itemMsg");
		}

		if(statistics.getNumOfSubSections() != 0 && statistics.getNumOfSubSections() == statistics.getNumOfSectionsWithoutInherited()) {
			String sectionMsg = translate("warning.item.session.control.attempts.all.sections.defined");
			maxAttemptsWarningLayout.contextPut("sectionMsg", sectionMsg);
			needWarning = true;
		} else if(statistics.getNumOfSectionsWithoutInherited() > 0) {
			String sectionMsg = translate("warning.item.session.control.attempts.sections.defined");
			maxAttemptsWarningLayout.contextPut("sectionMsg", sectionMsg);
			needWarning = true;
		} else {
			maxAttemptsWarningLayout.contextRemove("sectionMsg");
		}

		boolean warningVisible = limitAttemptsEl.isSelected(0) && needWarning;
		inheritMaxAttemptsButton.setVisible(warningVisible);
		maxAttemptsWarningLayout.setVisible(warningVisible);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
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

		return allOk;
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
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(attemptsWarningCtrl == source) {
			if(!DialogBoxUIFactory.isOkEvent(event)) {
				limitAttemptsEl.select(NO, true);
				limitAttemptsEl.getComponent().setDirty(true);
				maxAttemptsEl.setVisible(false);
			}
			updateWarningMaxAttempts();
			removeAsListenerAndDispose(attemptsWarningCtrl);
			attemptsWarningCtrl = null;
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == limitAttemptsEl) {
			boolean limitAttemptsEnabled = limitAttemptsEl.isOneSelected() && limitAttemptsEl.isSelected(0);
			maxAttemptsEl.setVisible(limitAttemptsEnabled);
			if(limitAttemptsEnabled && attemptsWarningCtrl == null) {
				if(!StringHelper.containsNonWhitespace(maxAttemptsEl.getValue()) || "0".equals(maxAttemptsEl.getValue())) {
					maxAttemptsEl.setValue("1");
				}
				String text = translate("warning.item.session.control.attempts");
				attemptsWarningCtrl = activateOkCancelDialog(ureq, null, text, attemptsWarningCtrl);
			}
			updateWarningMaxAttempts();
		} else if(inheritMaxAttemptsButton == source) {
			doInheritMaxAttempts(ureq);
			updateWarningMaxAttempts();
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
			checkNotNull(itemSessionControl).setMaxAttempts(Integer.valueOf(maxAttempts));
		} else if(limitAttemptsEl.isSelected(1)) {
			checkNotNull(itemSessionControl).setMaxAttempts(0);
		} else if(itemSessionControl != null) {
			itemSessionControl.setMaxAttempts(null);
		}
	}
	
	private ItemSessionControl checkNotNull(ItemSessionControl itemSessionControl) {
		if(itemSessionControl == null) {
			itemSessionControl = new ItemSessionControl(part);
			part.setItemSessionControl(itemSessionControl);
		}
		return itemSessionControl;
	}
	
	private void doInheritMaxAttempts(UserRequest ureq) {
		doChildrenInheritMaxAttempts(part);
		fireEvent(ureq, AssessmentTestEvent.ASSESSMENT_TEST_CHANGED_EVENT);
	}
	
	private void doChildrenInheritMaxAttempts(AbstractPart aPart) {
		List<? extends AbstractPart> parts = aPart.getChildAbstractParts();
		for(AbstractPart subPart:parts) {
			ItemSessionControl itemSessionControl = subPart.getItemSessionControl();
			if(itemSessionControl != null) {
				itemSessionControl.setMaxAttempts(null);
			}
			
			if(subPart instanceof AssessmentSection) {
				doChildrenInheritMaxAttempts(subPart);
			}
		}
	}

	private static class MaxAttemptsStatistics {
		private int numOfSubSections = 0;
		private int numOfItems = 0;
		
		private int numOfSectionsWithoutInherited = 0;
		private int numOfItemsWithoutInherited = 0;
		
		public static final MaxAttemptsStatistics calculate(AbstractPart aPart) {
			MaxAttemptsStatistics statistics = new MaxAttemptsStatistics();
			warningMaxAttempts(aPart, statistics);
			return statistics;
		}
		
		private static void warningMaxAttempts(AbstractPart aPart, MaxAttemptsStatistics statistics) {
			List<? extends AbstractPart> parts = aPart.getChildAbstractParts();
			for(AbstractPart subPart:parts) {
				if(subPart instanceof AssessmentItemRef) {
					statistics.incrementNumOfItems();
					if(!statistics.isMaxAttemptsInherited(subPart)) {
						statistics.incrementNumOfItemsWithoutInherited();
					}
				} else if(subPart instanceof AssessmentSection) {
					statistics.incrementNumOfSubSections();
					if(!statistics.isMaxAttemptsInherited(subPart)) {
						statistics.incrementNumOfSectionsWithoutInherited();
					}
					warningMaxAttempts(subPart, statistics);
				}
			}
		}
		
		public int getNumOfSubSections() {
			return numOfSubSections;
		}
		
		public void incrementNumOfSubSections() {
			numOfSubSections++;
		}
		
		public int getNumOfItems() {
			return numOfItems;
		}
		
		public void incrementNumOfItems() {
			numOfItems++;
		}
		
		public int getNumOfSectionsWithoutInherited() {
			return numOfSectionsWithoutInherited;
		}
		
		public void incrementNumOfSectionsWithoutInherited() {
			numOfSectionsWithoutInherited++;
		}
		
		public int getNumOfItemsWithoutInherited() {
			return numOfItemsWithoutInherited;
		}
		
		public void incrementNumOfItemsWithoutInherited() {
			numOfItemsWithoutInherited++;
		}
		
		public boolean isMaxAttemptsInherited(AbstractPart aPart) {
			ItemSessionControl itemSessionControl = aPart.getItemSessionControl();//can be null
			return itemSessionControl == null || itemSessionControl.getMaxAttempts() == null;
		}
	}
}
