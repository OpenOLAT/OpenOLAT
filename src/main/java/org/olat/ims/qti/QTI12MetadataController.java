package org.olat.ims.qti;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.ims.qti.editor.ItemNodeTabbedFormController;
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.qti.editor.beecom.objects.ChoiceQuestion;
import org.olat.ims.qti.editor.beecom.objects.ChoiceResponse;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Material;
import org.olat.ims.qti.editor.beecom.objects.Question;

/**
 * 
 * Initial date: 20.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12MetadataController extends FormBasicController  {
	
	private final Item item;

	public QTI12MetadataController(UserRequest ureq, WindowControl wControl, Item item) {
		super(ureq, wControl, "qti_metadatas");
		setTranslator(Util.createPackageTranslator(ItemNodeTabbedFormController.class, getLocale(), getTranslator()));
		this.item = item;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Question question = item.getQuestion();
		
		if(question != null) {
			//settings
			FormLayoutContainer leftSettingsCont = FormLayoutContainer.createDefaultFormLayout("left_settings", getTranslator());
			leftSettingsCont.setRootForm(mainForm);
			formLayout.add("leftSettings", leftSettingsCont);
			
			String shuffleStr = Boolean.toString(question.isShuffle());
			uifactory.addStaticTextElement("form.imd.shuffle", shuffleStr, leftSettingsCont);
			
			String duration = "";
			if(item.getDuration() != null) {
				duration = item.getDuration().getMin() + ":" + item.getDuration().getSec();
			}
			uifactory.addStaticTextElement("form.metadata.duration", duration, leftSettingsCont);
			
			FormLayoutContainer rightSettingsCont = FormLayoutContainer.createDefaultFormLayout("right_settings", getTranslator());
			rightSettingsCont.setRootForm(mainForm);
			formLayout.add("rightSettings", rightSettingsCont);
			if(question instanceof ChoiceQuestion) {
				ChoiceQuestion choice = (ChoiceQuestion)question;
				if(item.getQuestion().getType() == Question.TYPE_SC) {
					String score = Float.toString(question.getSingleCorrectScore());
					uifactory.addStaticTextElement("score", score, rightSettingsCont);
					
				} else if(item.getQuestion().getType() == Question.TYPE_MC) {
					String minVal = Float.toString(choice.getMinValue());
					String maxVal = Float.toString(choice.getMaxValue());
					uifactory.addStaticTextElement("score.min", minVal, rightSettingsCont);
					uifactory.addStaticTextElement("score.max", maxVal, rightSettingsCont);
				} 
			}
		}
		
		//feedbacks
		boolean hasFeedbacks = false;
		
		FormLayoutContainer leftFeedbackCont = FormLayoutContainer.createDefaultFormLayout("left_feedback", getTranslator());
		leftFeedbackCont.setRootForm(mainForm);
		
		Material masteryMat = QTIEditHelper.getFeedbackMasteryMaterial(item);
		if(masteryMat != null) {
			String text = masteryMat.renderAsText();
			uifactory.addStaticTextElement("item_feedback_mastery", text, leftFeedbackCont);
			hasFeedbacks = true;
		}
		
		FormLayoutContainer rightFeedbackCont = FormLayoutContainer.createDefaultFormLayout("right_feedback", getTranslator());
		rightFeedbackCont.setRootForm(mainForm);
		
		Material failureMat = QTIEditHelper.getFeedbackFailMaterial(item);
		if(failureMat != null) {
			String text = failureMat.renderAsText();
			uifactory.addStaticTextElement("item_feedback_fail", text, rightFeedbackCont);
			hasFeedbacks = true;
		}
		
		if (question != null && question.getType() <= Question.TYPE_MC) {
			int count = 0;
			for (Object obj : question.getResponses()) {
				ChoiceResponse response = (ChoiceResponse) obj;
				Material responseFeedbackMat = QTIEditHelper.getFeedbackOlatRespMaterial(item, response.getIdent());
				if(responseFeedbackMat != null) {
					boolean left = (count++ % 2 == 0);
					String text = responseFeedbackMat.renderAsText();
					StaticTextElement el = uifactory.addStaticTextElement("item_feedback_" + count, text, left ? leftFeedbackCont : rightFeedbackCont);
					el.setLabel(null, null);
				}
			}
			hasFeedbacks = count > 0;
		}
		if(hasFeedbacks) {
			formLayout.add("leftFeedback", leftFeedbackCont);
			formLayout.add("rightFeedback", rightFeedbackCont);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}