/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.review;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SelectusReviewService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.ReviewElementDefinition;
import org.olat.modules.selectus.model.review.ReviewElementType;
import org.olat.modules.selectus.model.review.ReviewResponse;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 4 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReviewEditController extends FormBasicController {
	
	private static final int MAX_TEXT_LENGTH = 4000;
	
	private final Position position;
	private final Application application;
	private final List<ReviewElementDefinition> elements;
	private final PositionReviewDefinition reviewDefinition;
	private final Set<ReviewResponse> responseSet = new HashSet<>();
	
	private final List<TextAreaElement> textEls = new ArrayList<>();
	private final List<SliderElement> sliderEls = new ArrayList<>();
	
	@Autowired
	private AuditService auditService;
	@Autowired
	private SelectusReviewService reviewService;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public ReviewEditController(UserRequest ureq, WindowControl wControl,
			Position position, Application application) {
		super(ureq, wControl, "review_edit", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.application = application;
		this.position = position;
		
		Position pos = recruitingService.getPosition(application.getPosition().getKey());
		reviewDefinition = reviewService.getReviewDefinition(pos.getReviewDefinition());
		elements = reviewDefinition.getElements();
		List<ReviewResponse> responses = reviewService.getResponses(application, getIdentity());
		responseSet.addAll(responses);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String applicantName = salutationGenerator.getFullname(application, getLocale());
		setFormDescription("edit.review.text", new String[] { applicantName });
		
		Map<ReviewElementDefinition,ReviewResponse> elementToResponses = new HashMap<>();
		for(ReviewResponse response:responseSet) {
			elementToResponses.put(response.getElement(), response);
		}

		boolean hasLabel = false;
		List<ReviewElement> uiElements = new ArrayList<>();
		for(ReviewElementDefinition element:elements) {
			if(element != null) {
				ReviewResponse response = elementToResponses.get(element);
				if(element.getType() == ReviewElementType.title) {
					uiElements.add(new ReviewElement(null, element, null, null));
					hasLabel = false;
				} else if(element.getType() == ReviewElementType.text) {
					FormItem item = initTextElement(element, response, formLayout);
					uiElements.add(new ReviewElement(item, element, null, null));
					hasLabel = false;
				} else if(element.getType() == ReviewElementType.slider) {
					String leftLabel = null;
					String rightLabel = null;
					FormItem item = initSliderElement(element, response, formLayout);
					if(!hasLabel) {
						leftLabel = reviewDefinition.getDefaultSliderLeftLabel();
						rightLabel = reviewDefinition.getDefaultSliderRightLabel();
					}
					uiElements.add(new ReviewElement(item, element, leftLabel, rightLabel));
					hasLabel = true;
				}
			}
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			((FormLayoutContainer)formLayout).contextPut("uiElements", uiElements);
		}
		
		uifactory.addFormSubmitButton("save", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	private FormItem initTextElement(ReviewElementDefinition element, ReviewResponse response, FormItemContainer formLayout) {
		String comment = response == null ? "" : response.getStringValue();
		TextAreaElement textEl = uifactory.addTextAreaElement("ed_text_" + CodeHelper.getRAMUniqueID(), "label", MAX_TEXT_LENGTH, 4, 60, false, true, false, comment, formLayout);
		textEl.setUserObject(element);
		textEl.setLabel(element.getLabel(), null, false);
		textEls.add(textEl);
		return textEl;
	}
	
	private FormItem initSliderElement(ReviewElementDefinition element, ReviewResponse response, FormItemContainer formLayout) {
		if(reviewDefinition.getDefaultSliderSteps() == null) return null;

		SliderElement sliderEl = uifactory.addSliderElement("ed_slider_" + CodeHelper.getRAMUniqueID(), "slider", formLayout);
		sliderEl.setDomReplacementWrapperRequired(false);
		sliderEl.setMinValue(ReviewElementDefinition.MIN_SLIDER_VALUE);
		sliderEl.setMaxValue(reviewDefinition.getDefaultSliderSteps().doubleValue());
		sliderEl.setStep(1);
		if(response != null && response.getIntegerValue() != null) {
			sliderEl.setValue(response.getIntegerValue().doubleValue());
		}
		sliderEl.setUserObject(element);
		sliderEl.setLabel(element.getLabel(), null, false);
		sliderEls.add(sliderEl);
		return sliderEl;
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		for(TextElement textEl:textEls) {
			textEl.clearError();
			if(textEl.getValue().getBytes().length > MAX_TEXT_LENGTH) {
				textEl.setErrorKey("form.error.toolong", new String[] { Integer.toString(MAX_TEXT_LENGTH) });
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<ReviewResponse> responses = new ArrayList<>();
		for(TextElement textEl:textEls) {
			ReviewElementDefinition element = (ReviewElementDefinition)textEl.getUserObject();
			ReviewResponse response = reviewService.addResponse(element, application, getIdentity(), textEl.getValue());	
			responses.add(response);
		}
		for(SliderElement sliderEl:sliderEls) {
			ReviewElementDefinition element = (ReviewElementDefinition)sliderEl.getUserObject();
			
			boolean hasValue = sliderEl.hasValue();
			if(hasValue) {
				int val = (int)Math.round(sliderEl.getValue());
				if(val < ReviewElementDefinition.MIN_SLIDER_VALUE) {
					val = (int)Math.round(ReviewElementDefinition.MIN_SLIDER_VALUE);
				}
				ReviewResponse response = reviewService.addResponse(element, application, getIdentity(), val);
				responses.add(response);
			} else {
				reviewService.removeResponse(element, application, getIdentity());
			}
		}
		logReview(responses);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void logReview(List<ReviewResponse> responses) {
		Action action;
		String messageI18n;
		if(responseSet.isEmpty()) {
			action = Action.add;
			messageI18n = "audit.log.review.add";
		} else {
			action = Action.update;
			messageI18n = "audit.log.review.update";
		}
		String[] messageArgs = new String[] {
			RecruitingHelper.formatFullNameWithTitle(getIdentity(), getLocale()),
			salutationGenerator.getTitleFullname(application, getLocale()),
			application.getId().toString()
		};
		
		List<ReviewResponse> currentResponses = new ArrayList<>(responseSet);
		Collections.sort(currentResponses, new ResponseComparator());
		String before = auditService.toAuditXml(currentResponses);
		List<ReviewResponse> newResponses = new ArrayList<>(responses);
		Collections.sort(newResponses, new ResponseComparator());
		String after = auditService.toAuditXml(newResponses);
		if(before == null || !before.equals(after)) {
			auditService.auditReviewLog(action, before, after, messageI18n, messageArgs, getTranslator(), position, application, getIdentity());
		}
		responseSet.clear();
		responseSet.addAll(responses);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public static class ResponseComparator implements Comparator<ReviewResponse> {

		@Override
		public int compare(ReviewResponse o1, ReviewResponse o2) {
			int c = 0;
			if(o1 == null && o2 == null) {
				c = 0;
			} else if(o1 == null) {
				c = 1;
			} else if(o2 == null) {
				c = -1;
			} else {
				c = o1.getKey().compareTo(o2.getKey());
			}
			return c;
		}
	}
	
	public class ReviewElement {
		
		private final String leftLabel;
		private final String rightLabel;
		private final FormItem item;
		private final ReviewElementDefinition element;
		
		public ReviewElement(FormItem item, ReviewElementDefinition element, String leftLabel, String rightLabel) {
			this.item = item;
			this.element = element;
			this.leftLabel = leftLabel;
			this.rightLabel = rightLabel;
		}
		
		public String getType() {
			return element.getType() == null ? "" : element.getType().name();
		}
		
		public String getLabel() {
			return element.getLabel();
		}
		
		public FormItem getItem() {
			return item;
		}
		
		public boolean hasLabel() {
			return StringHelper.containsNonWhitespace(leftLabel) || StringHelper.containsNonWhitespace(rightLabel);
		}

		public String getLeftLabel() {
			return leftLabel;
		}

		public String getRightLabel() {
			return rightLabel;
		}
	}
}
