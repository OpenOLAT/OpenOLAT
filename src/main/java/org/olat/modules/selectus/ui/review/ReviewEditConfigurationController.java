/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.review;

import java.util.Collection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.ReviewFillEnum;
import org.olat.modules.selectus.model.review.ReviewVisibilityEnum;
import org.olat.modules.selectus.model.review.ReviewerNameVisibilityEnum;
import org.olat.modules.selectus.ui.position.PositionEditableController;

/**
 * 
 * Initial date: 3 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReviewEditConfigurationController extends FormBasicController implements PositionEditableController {
	
	private static String[] onKeys = new String[] { "on" };
	private static String[] reviewerKeys = new String[] {
			ReviewerNameVisibilityEnum.visible.name(), ReviewerNameVisibilityEnum.anonymous.name()
		};
	private static String[] visiblityKeys = new String[] {
			ReviewVisibilityEnum.afterSubmission.name(), ReviewVisibilityEnum.afterRating.name(),
			ReviewVisibilityEnum.always.name()
		};
	private static String[] enableReviewKeys = new String[] { ReviewFillEnum.fill.name() , "view" };
	
	private static final String[] statisticsVisibilityKeys = new String[] { "statistics", "radarChart" };

	private MultipleSelectionElement enableEl;
	private MultipleSelectionElement enableReviewCommentEl;
	private MultipleSelectionElement enableReviewCommitteeEl;
	private MultipleSelectionElement enableReviewHeadEl;
	private MultipleSelectionElement enableReviewSecretaryEl;
	private MultipleSelectionElement enableReviewExofficioEl;
	private SingleSelection reviewerNameEl;
	private SingleSelection reviewVisibilityCommitteeEl;
	private SingleSelection reviewVisibilityHeadEl;
	private SingleSelection reviewVisibilitySecretaryEl;
	private SingleSelection reviewVisibilityExofficioEl;
	private TextElement sliderStepsEl;
	private TextElement sliderLabelLeftEl;
	private TextElement sliderLabelRightEl;
	private SpacerElement spacer;
	private MultipleSelectionElement statisticsEl;
	
	private Position position;
	private final boolean readOnly;
	private PositionReviewDefinition positionReviewDefinition;

	@Autowired
	private RecruitingModule recruitingModule;

	public ReviewEditConfigurationController(UserRequest ureq, WindowControl wControl,
			Position position, PositionReviewDefinition positionReviewDefinition, boolean readOnly) {
		super(ureq, wControl);
		this.position = position;
		this.readOnly = readOnly;
		this.positionReviewDefinition = positionReviewDefinition;
		
		initForm(ureq);
		setReviewVisibility();
		setStatisticsVisibility();
		updateUI();
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		position = updatedPosition;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("reviews.configuration");
		setFormDescription("reviews.configuration.explanation");

		String[] onValues = new String[] { translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("enable.review", formLayout, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		enableEl.setEnabled(!readOnly);
		if(position != null && position.isReviewEnabled()) {
			enableEl.select("on", true);
		}
		
		String[] reviewerValues = new String[] { translate("review.show.name"), translate("review.anonymous") };
		reviewerNameEl = uifactory.addRadiosVertical("reviewer.name", formLayout, reviewerKeys, reviewerValues);
		reviewerNameEl.setEnabled(!readOnly);
		if(positionReviewDefinition == null) {
			reviewerNameEl.select(ReviewerNameVisibilityEnum.visible.name(), true);
		} else {
			reviewerNameEl.select(positionReviewDefinition.getReviewNameVisibility().name(), true);
		}
		
		enableReviewCommentEl = uifactory.addCheckboxesHorizontal("enable.review.comment", formLayout, onKeys, onValues);
		enableReviewCommentEl.setEnabled(!readOnly);
		if(positionReviewDefinition != null && positionReviewDefinition.isReviewCommentEnabled()) {
			enableReviewCommentEl.select("on", true);
		}
		
		initReviewVisibilityForm(formLayout);
		initSliderConfigurationForm(formLayout);
		
		String[] statisticsVisibilityValues = new String[] { translate("enable.statistics.statistics"), translate("enable.statistics.radarChart") };
		statisticsEl = uifactory.addCheckboxesVertical("enable.statistics", formLayout, statisticsVisibilityKeys, statisticsVisibilityValues, 1);
		statisticsEl.setEnabled(!readOnly);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setVisible(!readOnly);
		FormSubmit submitButton = uifactory.addFormSubmitButton("save", buttonsCont);
		submitButton.setVisible(!readOnly);
		FormCancel cancelButton = uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		cancelButton.setVisible(!readOnly);
	}
	

	private void initSliderConfigurationForm(FormItemContainer formLayout) {
		spacer = uifactory.addSpacerElement("slider.configuration", formLayout, true);
		spacer.setLabel("slider.configuration", null);
		
		String steps = positionReviewDefinition == null || positionReviewDefinition.getDefaultSliderSteps() == null
				? null : positionReviewDefinition.getDefaultSliderSteps().toString();
		if (steps == null) {
			steps = recruitingModule.getReviewDefaultSliderSteps().toString();
		}
		sliderStepsEl = uifactory.addTextElement("slider.steps", "slider.steps", 32, steps, formLayout);
		sliderStepsEl.setEnabled(!readOnly);
		String leftLabel = positionReviewDefinition == null ? null : positionReviewDefinition.getDefaultSliderLeftLabel();
		if (leftLabel == null) {
			leftLabel = translate("review.default.label.left"); // default
		}
		sliderLabelLeftEl = uifactory.addTextElement("slider.left.label", "slider.left.label", 32, leftLabel, formLayout);
		sliderLabelLeftEl.setEnabled(!readOnly);
		String rightLabel = positionReviewDefinition == null ? null : positionReviewDefinition.getDefaultSliderRightLabel();
		if (rightLabel == null) {
			rightLabel = translate("review.default.label.right"); // default
		}
		sliderLabelRightEl = uifactory.addTextElement("slider.right.label", "slider.right.label", 32, rightLabel, formLayout);	
		sliderLabelRightEl.setEnabled(!readOnly);
	}
	
	private void initReviewVisibilityForm(FormItemContainer formLayout) {
		String[] enableReviewValues = new String[] { translate("review.fill"), translate("review.view") };
		String[] visiblityValues = new String[] { translate("review.visible.after"), translate("review.visible.rating"), translate("review.visible.always") };
		// head
		enableReviewHeadEl = uifactory.addCheckboxesVertical("enable.review.head", formLayout, enableReviewKeys, enableReviewValues, 1);
		enableReviewHeadEl.addActionListener(FormEvent.ONCHANGE);
		enableReviewHeadEl.setEnabled(!readOnly);

		reviewVisibilityHeadEl = uifactory.addRadiosVertical("review.visibility.head", formLayout, visiblityKeys, visiblityValues);
		reviewVisibilityHeadEl.setAllowNoSelection(true);
		reviewVisibilityHeadEl.setLabel(null, null);
		reviewVisibilityHeadEl.setEnabled(!readOnly);
		
		// secretary
		enableReviewSecretaryEl = uifactory.addCheckboxesVertical("enable.review.secretary", formLayout, enableReviewKeys, enableReviewValues, 1);
		enableReviewSecretaryEl.addActionListener(FormEvent.ONCHANGE);
		enableReviewSecretaryEl.setEnabled(!readOnly);

		reviewVisibilitySecretaryEl = uifactory.addRadiosVertical("review.visibility.secretary", formLayout, visiblityKeys, visiblityValues);
		reviewVisibilitySecretaryEl.setAllowNoSelection(true);
		reviewVisibilitySecretaryEl.setLabel(null, null);
		reviewVisibilitySecretaryEl.setEnabled(!readOnly);

		// committee
		enableReviewCommitteeEl = uifactory.addCheckboxesVertical("enable.review.committee", formLayout, enableReviewKeys, enableReviewValues, 1);
		enableReviewCommitteeEl.addActionListener(FormEvent.ONCHANGE);
		enableReviewCommitteeEl.setEnabled(!readOnly);

		reviewVisibilityCommitteeEl = uifactory.addRadiosVertical("review.visibility.committee", formLayout, visiblityKeys, visiblityValues);
		reviewVisibilityCommitteeEl.setAllowNoSelection(true);
		reviewVisibilityCommitteeEl.setLabel(null, null);
		reviewVisibilityCommitteeEl.setEnabled(!readOnly);
		
		// ex-officio
		enableReviewExofficioEl = uifactory.addCheckboxesVertical("enable.review.exofficio", formLayout, enableReviewKeys, enableReviewValues, 1);
		enableReviewExofficioEl.addActionListener(FormEvent.ONCHANGE);
		enableReviewExofficioEl.setEnabled(!readOnly);

		reviewVisibilityExofficioEl = uifactory.addRadiosVertical("review.visibility.exofficio", formLayout, visiblityKeys, visiblityValues);
		reviewVisibilityExofficioEl.setAllowNoSelection(true);
		reviewVisibilityExofficioEl.setLabel(null, null);
		reviewVisibilityExofficioEl.setEnabled(!readOnly);
	}
	
	private void setReviewVisibility() {
		if(positionReviewDefinition != null && positionReviewDefinition.getReviewFillHead() == ReviewFillEnum.fill) {
			enableReviewHeadEl.select(ReviewFillEnum.fill.name(), true);
		}
		if(positionReviewDefinition != null && positionReviewDefinition.getReviewVisibilityHead() != ReviewVisibilityEnum.staffOnly) {
			enableReviewHeadEl.select("view", true);
		}

		if(positionReviewDefinition == null) {
			reviewVisibilityHeadEl.select(ReviewVisibilityEnum.always.name(), true);
		} else if(positionReviewDefinition.getReviewVisibilityHead() != ReviewVisibilityEnum.staffOnly) {
			reviewVisibilityHeadEl.select(positionReviewDefinition.getReviewVisibilityHead().name(), true);
		}
		
		// secretary
		if(positionReviewDefinition != null && positionReviewDefinition.getReviewFillSecretary() == ReviewFillEnum.fill) {
			enableReviewSecretaryEl.select(ReviewFillEnum.fill.name(), true);
		}
		if(positionReviewDefinition != null && positionReviewDefinition.getReviewVisibilitySecretary() != ReviewVisibilityEnum.staffOnly) {
			enableReviewSecretaryEl.select("view", true);
		}

		if(positionReviewDefinition == null) {
			reviewVisibilitySecretaryEl.select(ReviewVisibilityEnum.always.name(), true);
		} else if(positionReviewDefinition.getReviewVisibilitySecretary() != ReviewVisibilityEnum.staffOnly) {
			reviewVisibilitySecretaryEl.select(positionReviewDefinition.getReviewVisibilitySecretary().name(), true);
		}
		
		// committee
		if(positionReviewDefinition == null || (positionReviewDefinition != null && positionReviewDefinition.getReviewFillCommittee() == ReviewFillEnum.fill)) {
			enableReviewCommitteeEl.select(ReviewFillEnum.fill.name(), true);
		}
		if(positionReviewDefinition != null && positionReviewDefinition.getReviewVisibilityCommittee() != ReviewVisibilityEnum.staffOnly) {
			enableReviewCommitteeEl.select("view", true);
		}

		if(positionReviewDefinition == null) {
			reviewVisibilityCommitteeEl.select(ReviewVisibilityEnum.always.name(), true);
		} else if(positionReviewDefinition.getReviewVisibilityCommittee() != ReviewVisibilityEnum.staffOnly) {
			reviewVisibilityCommitteeEl.select(positionReviewDefinition.getReviewVisibilityCommittee().name(), true);
		}
		
		// ex-officio
		if(positionReviewDefinition != null && positionReviewDefinition.getReviewFillExofficio() == ReviewFillEnum.fill) {
			enableReviewExofficioEl.select(ReviewFillEnum.fill.name(), true);
		}
		if(positionReviewDefinition != null && positionReviewDefinition.getReviewVisibilityExofficio() != ReviewVisibilityEnum.staffOnly) {
			enableReviewExofficioEl.select("view", true);
		}

		if(positionReviewDefinition == null) {
			reviewVisibilityExofficioEl.select(ReviewVisibilityEnum.always.name(), true);
		} else if(positionReviewDefinition.getReviewVisibilityExofficio() != ReviewVisibilityEnum.staffOnly) {
			reviewVisibilityExofficioEl.select(positionReviewDefinition.getReviewVisibilityExofficio().name(), true);
		}
	}
	
	private void setStatisticsVisibility() {
		if(positionReviewDefinition == null || positionReviewDefinition.getReviewStatisticsEnabled() == null) {
			if(recruitingModule.isReviewStatisticsEnabled()) {
				statisticsEl.select("statistics", true);
			}
		} else if(positionReviewDefinition.getReviewStatisticsEnabled().booleanValue()) {
			statisticsEl.select("statistics", true);
		}
		
		if(positionReviewDefinition == null || positionReviewDefinition.getReviewRadarChartEnabled() == null) {
			if(recruitingModule.isReviewStatisticsChartEnabled()) {
				statisticsEl.select("radarChart", true);
			}
		} else if(positionReviewDefinition.getReviewRadarChartEnabled().booleanValue()) {
			statisticsEl.select("radarChart", true);
		}
	}
	
	private void updateUI() {
		boolean enabled = enableEl.isAtLeastSelected(1);
		spacer.setVisible(enabled);
		enableReviewCommentEl.setVisible(enabled);
		reviewerNameEl.setVisible(enabled);
		reviewVisibilityCommitteeEl.setVisible(enabled);
		sliderStepsEl.setVisible(enabled);
		sliderLabelLeftEl.setVisible(enabled);
		sliderLabelRightEl.setVisible(enabled);
		
		enableReviewCommitteeEl.setVisible(enabled);
		enableReviewHeadEl.setVisible(enabled);
		enableReviewSecretaryEl.setVisible(enabled);
		enableReviewExofficioEl.setVisible(enabled && recruitingModule.isRoleExOfficioEnabled());
		
		boolean enableViewCommittee = enableReviewCommitteeEl.isSelected(1);
		reviewVisibilityCommitteeEl.setVisible(enabled && enableViewCommittee);
		boolean enableViewHead = enableReviewHeadEl.isSelected(1);
		reviewVisibilityHeadEl.setVisible(enabled && enableViewHead);
		boolean enableViewSecretary = enableReviewSecretaryEl.isSelected(1);
		reviewVisibilitySecretaryEl.setVisible(enabled && enableViewSecretary);
		boolean enableViewExofficio = enableReviewExofficioEl.isSelected(1);
		reviewVisibilityExofficioEl.setVisible(enabled && enableViewExofficio && recruitingModule.isRoleExOfficioEnabled());
		
		statisticsEl.setVisible(enabled);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateSingleSelection(reviewerNameEl);
		allOk &= validateIntegerGreaterThanOne(sliderStepsEl);
		return allOk;
	}
	
	private boolean validateIntegerGreaterThanOne(TextElement el) {
		boolean allOk = true;
		
		el.clearError();
		if(StringHelper.containsNonWhitespace(el.getValue())) {
			try {
				if(StringHelper.isLong(el.getValue()) && Integer.parseInt(el.getValue()) > 1) {
					// ok
				} else {
					el.setErrorKey("review.step.form.error");
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				el.setErrorKey("review.step.form.error");
				allOk &= false;
			}
		} else {
			el.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateSingleSelection(SingleSelection el) {
		boolean allOk = true;
		el.clearError();
		if(!el.isOneSelected()) {
			el.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			if(enableEl.isAtLeastSelected(1)) {
				setReviewVisibility();
				setStatisticsVisibility();
			}
			updateUI();
		} else if(enableReviewCommitteeEl == source || enableReviewHeadEl == source
				|| enableReviewSecretaryEl == source || enableReviewExofficioEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	protected void commit(Position position, PositionReviewDefinition positionReviewDefinition) {
		boolean enabled = enableEl.isAtLeastSelected(1);
		position.setReviewEnabled(enabled);
		
		if(enabled) {
			positionReviewDefinition.setReviewCommentEnabled(enableReviewCommentEl.isAtLeastSelected(1));
			
			String reviewerNameKey = reviewerNameEl.getSelectedKey();
			positionReviewDefinition.setReviewNameVisibility(ReviewerNameVisibilityEnum.valueOf(reviewerNameKey));
			
			positionReviewDefinition.setReviewFillCommittee(getReviewFillEnum(enableReviewCommitteeEl));
			positionReviewDefinition.setReviewFillHead(getReviewFillEnum(enableReviewHeadEl));
			positionReviewDefinition.setReviewFillSecretary(getReviewFillEnum(enableReviewSecretaryEl));
			positionReviewDefinition.setReviewFillExofficio(getReviewFillEnum(enableReviewExofficioEl));

			positionReviewDefinition.setReviewVisibilityCommittee(getReviewFillEnum(enableReviewCommitteeEl, reviewVisibilityCommitteeEl));
			positionReviewDefinition.setReviewVisibilityHead(getReviewFillEnum(enableReviewHeadEl, reviewVisibilityHeadEl));
			positionReviewDefinition.setReviewVisibilitySecretary(getReviewFillEnum(enableReviewSecretaryEl, reviewVisibilitySecretaryEl));
			positionReviewDefinition.setReviewVisibilityExofficio(getReviewFillEnum(enableReviewExofficioEl, reviewVisibilityExofficioEl));

			if(StringHelper.containsNonWhitespace(sliderStepsEl.getValue())) {
				Integer steps = Integer.parseInt(sliderStepsEl.getValue());
				positionReviewDefinition.setDefaultSliderSteps(steps);
			} else {
				positionReviewDefinition.setDefaultSliderSteps(null);
			}
			
			positionReviewDefinition.setDefaultSliderLeftLabel(sliderLabelLeftEl.getValue());
			positionReviewDefinition.setDefaultSliderRightLabel(sliderLabelRightEl.getValue());
			
			Collection<String> selectedKeys = statisticsEl.getSelectedKeys();
			positionReviewDefinition.setReviewStatisticsEnabled(selectedKeys.contains("statistics"));
			positionReviewDefinition.setReviewRadarChartEnabled(selectedKeys.contains("radarChart"));
		} else {
			positionReviewDefinition.setReviewCommentEnabled(false);
			positionReviewDefinition.setReviewNameVisibility(null);
			positionReviewDefinition.setReviewFillCommittee(null);
			positionReviewDefinition.setReviewFillHead(null);
			positionReviewDefinition.setReviewFillSecretary(null);
			positionReviewDefinition.setReviewFillExofficio(null);
			positionReviewDefinition.setReviewVisibilityCommittee(null);
			positionReviewDefinition.setReviewVisibilityHead(null);
			positionReviewDefinition.setReviewVisibilitySecretary(null);
			positionReviewDefinition.setReviewVisibilityExofficio(null);
			positionReviewDefinition.setDefaultSliderSteps(null);
			positionReviewDefinition.setDefaultSliderLeftLabel(null);
			positionReviewDefinition.setDefaultSliderRightLabel(null);
			positionReviewDefinition.setReviewStatisticsEnabled(null);
			positionReviewDefinition.setReviewRadarChartEnabled(null);
		}
	}
	
	private ReviewFillEnum getReviewFillEnum(MultipleSelectionElement enableReviewEl) {
		Collection<String> selectedKeys = enableReviewEl.getSelectedKeys();
		return selectedKeys.contains(ReviewFillEnum.fill.name()) ? ReviewFillEnum.fill : ReviewFillEnum.no;
	}
	
	private ReviewVisibilityEnum getReviewFillEnum(MultipleSelectionElement enableReviewEl, SingleSelection reviewVisibilityEl) {
		Collection<String> selectedKeys = enableReviewEl.getSelectedKeys();
		if(selectedKeys.contains("view") && reviewVisibilityEl.isOneSelected()) {
			return ReviewVisibilityEnum.valueOf(reviewVisibilityEl.getSelectedKey());
		}
		return ReviewVisibilityEnum.staffOnly;
	}
}