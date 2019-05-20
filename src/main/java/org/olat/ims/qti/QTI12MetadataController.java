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
package org.olat.ims.qti;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.HtmlFilter;
import org.olat.ims.qti.editor.ItemNodeTabbedFormController;
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.qti.editor.beecom.objects.ChoiceQuestion;
import org.olat.ims.qti.editor.beecom.objects.ChoiceResponse;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Material;
import org.olat.ims.qti.editor.beecom.objects.Question;
import org.olat.ims.qti.editor.beecom.objects.Response;

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
		
		FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
		
		if(question != null) {
			layoutCont.contextPut("hasQuestion", Boolean.TRUE);
			
			//settings
			String shuffleStr = translate(question.isShuffle() ? "editor.true" : "editor.false");
			uifactory.addStaticTextElement("form.imd.shuffle", shuffleStr, formLayout);
			
			String duration = "";
			if(item.getDuration() != null) {
				duration = item.getDuration().getMin() + ":" + item.getDuration().getSec();
			}
			uifactory.addStaticTextElement("form.metadata.duration", duration, formLayout);
			
			if(question instanceof ChoiceQuestion) {
				ChoiceQuestion choice = (ChoiceQuestion)question;
				if(item.getQuestion().getType() == Question.TYPE_SC) {
					String score = Float.toString(question.getSingleCorrectScore());
					uifactory.addStaticTextElement("score", score, formLayout);
				} else if(item.getQuestion().getType() == Question.TYPE_MC) {
					String minVal = Float.toString(choice.getMinValue());
					String maxVal = Float.toString(choice.getMaxValue());
					uifactory.addStaticTextElement("score.min", minVal, formLayout);
					uifactory.addStaticTextElement("score.max", maxVal, formLayout);
				} 
			}
			
			//correct responses
			List<Response> responses = question.getResponses();
			if(question.getType() == Question.TYPE_MC || question.getType() == Question.TYPE_SC) {
				setMCAndSCCorrectResponses(question, responses, layoutCont);
			} else if(question.getType() == Question.TYPE_KPRIM) {
				setKPrimCorrectResponses(responses, layoutCont);
			}
		}
		
		//feedbacks
		boolean hasFeedbacks = false;
		Material masteryMat = QTIEditHelper.getFeedbackMasteryMaterial(item);
		if(masteryMat != null) {
			layoutCont.contextPut("item_feedback_mastery", masteryMat.renderAsText());
			hasFeedbacks = true;
		}

		Material failureMat = QTIEditHelper.getFeedbackFailMaterial(item);
		if(failureMat != null) {
			layoutCont.contextPut("item_feedback_fail", failureMat.renderAsText());
			hasFeedbacks = true;
		}
		
		List<String> responsesFeedback = new ArrayList<>();
		if (question != null && question.getType() <= Question.TYPE_MC) {
			for (Object obj : question.getResponses()) {
				ChoiceResponse response = (ChoiceResponse) obj;
				Material responseFeedbackMat = QTIEditHelper.getFeedbackOlatRespMaterial(item, response.getIdent());
				if(responseFeedbackMat != null) {
					responsesFeedback.add(responseFeedbackMat.renderAsText());
				}
			}
			hasFeedbacks |= responsesFeedback.size() > 0;
		}

		layoutCont.contextPut("responsesFeedback", responsesFeedback);
		layoutCont.contextPut("hasFeedbacks", new Boolean(hasFeedbacks));
	}
	
	private void setKPrimCorrectResponses(List<Response> responses, FormLayoutContainer layoutCont) {
		List<ResponseAndPoints> responsesPoints = new ArrayList<>();
		if(responses != null && responses.size() > 0) {
			for(Response response:responses) {
				String responseSummary = getResponseSummary(response);
				if(responseSummary != null) {
					
					boolean correct =  response.isCorrect();
					String points = Float.toString(response.getPoints());
					ResponseAndPoints responseInfos = new ResponseAndPoints(Formatter.formatLatexFormulas(responseSummary), points, correct);
					responsesPoints.add(responseInfos);
				}
			}
		}
		layoutCont.contextPut("kprimResponsesAndPoints", responsesPoints);
	}
	
	private void setMCAndSCCorrectResponses(Question question, List<Response> responses, FormLayoutContainer layoutCont) {
		List<String> correctResponseNames = new ArrayList<>();
		List<ResponseAndPoints> responsesPoints = new ArrayList<>();
		if(responses != null && responses.size() > 0) {

			if(question.isSingleCorrect()) {
				for(Response response:responses) {
					if(response.isCorrect()) {
						String responseSummary = getResponseSummary(response);
						if(responseSummary != null) {
							correctResponseNames.add(Formatter.formatLatexFormulas(responseSummary));
						}
					}
				}
			} else {
				boolean hasNegative = false;
				for(Response response:responses) {
					if(response.getPoints() < 0.0f) {
						hasNegative = true;
					}
				}
				
				for(Response response:responses) {
					String responseSummary = getResponseSummary(response);
					if(responseSummary != null &&
							((hasNegative && response.getPoints() >= 0.0f) 
							 || (!hasNegative && response.getPoints() > 0.0f))) {
						boolean correct =  response.getPoints() > 0.0f;
						String points = Float.toString(response.getPoints());
						ResponseAndPoints responseInfos = new ResponseAndPoints(responseSummary, points, correct);
						responsesPoints.add(responseInfos);
					}
				}
			}
		}
		layoutCont.contextPut("responses", correctResponseNames);
		layoutCont.contextPut("responsesAndPoints", responsesPoints);
	}
	
	private String getResponseSummary(Response response) {
		String responseSummary;
		if(response.getContent() == null) {
			responseSummary = null;
		} else {
			responseSummary = response.getContent().renderAsText();
			if(responseSummary.length() > 128) {
				responseSummary = new HtmlFilter().filter(responseSummary);
				if(responseSummary.length() > 128) {
					responseSummary = responseSummary.substring(0, 125) + "...";
				}
			}
		}
		return StringHelper.containsNonWhitespace(responseSummary) ? responseSummary : null;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public static class ResponseAndPoints {
		
		private final String point;
		private final boolean correct;
		private final String responseSummary;
		
		public ResponseAndPoints(String responseSummary, String point, boolean correct) {
			this.point = point;
			this.correct = correct;
			this.responseSummary = responseSummary;
		}

		public String getPoints() {
			return point;
		}

		public boolean isCorrect() {
			return correct;
		}

		public String getResponseSummary() {
			return responseSummary;
		}
	}
}