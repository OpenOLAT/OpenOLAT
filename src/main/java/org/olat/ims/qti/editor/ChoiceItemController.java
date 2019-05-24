/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.ims.qti.editor;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.ims.qti.editor.beecom.objects.ChoiceQuestion;
import org.olat.ims.qti.editor.beecom.objects.ChoiceResponse;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Material;
import org.olat.ims.qti.editor.beecom.objects.Mattext;
import org.olat.ims.qti.editor.beecom.objects.Question;
import org.olat.ims.qti.editor.beecom.objects.Response;

/**
 * Initial Date: Oct 21, 2004 <br>
 * 
 * @author mike
 */
public class ChoiceItemController extends BasicController implements ControllerEventListener {

	private final VelocityContainer main;

	private Item item;
	private QTIEditorPackage qtiPackage;
	private DialogBoxController delYesNoCtrl;
	private final boolean restrictedEdit;
	private final boolean blockedEdit;
	private Material editQuestion;
	private Response editResponse;
	private CloseableModalController dialogCtr;
	private MaterialFormController matFormCtr;

	/**
	 * @param item
	 * @param qtiPackage
	 * @param trnsltr
	 * @param wControl
	 */
	public ChoiceItemController(UserRequest ureq, WindowControl wControl,
			Item item, QTIEditorPackage qtiPackage, Translator trnsltr,
			boolean restrictedEdit, boolean blockedEdit) {
		super(ureq, wControl, trnsltr);

		this.restrictedEdit = restrictedEdit;
		this.blockedEdit = blockedEdit;
		this.item = item;
		this.qtiPackage = qtiPackage;
		
		if (item.getQuestion().getType() == Question.TYPE_MC) {
			main = createVelocityContainer("mcitem", "tab_mcItem");
		} else if (item.getQuestion().getType() == Question.TYPE_KPRIM) {
			main = createVelocityContainer("kprimitem", "tab_kprimItem");
		} else {
			main = createVelocityContainer("scitem", "tab_scItem");
		}
		main.contextPut("question", item.getQuestion());
		main.contextPut("isSurveyMode", qtiPackage.getQTIDocument().isSurvey() ? "true" : "false");
		main.contextPut("isRestrictedEdit", restrictedEdit ? Boolean.TRUE : Boolean.FALSE);
		main.contextPut("isBlockedEdit", Boolean.valueOf(blockedEdit));
		
		String mediaBaseUrl = qtiPackage.getMediaBaseURL();
		if(mediaBaseUrl != null && !mediaBaseUrl.startsWith("http")) {
			mediaBaseUrl = Settings.getServerContextPathURI() + mediaBaseUrl;
		}
		main.contextPut("mediaBaseURL", mediaBaseUrl);
		
		putInitialPanel(main);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == main) {
			// olat::: as: improve easy fix since almost all operations change the main vc.
			main.setDirty(true);
			String cmd = event.getCommand();
			String sPosid = ureq.getParameter("posid");
			int posid = 0;
			if (sPosid != null) posid = Integer.parseInt(sPosid);
			if(cmd == null) {
				//ignore null cmd
			} else if (cmd.equals("up")) {
				if (posid > 0) {
					List<Response> elements = item.getQuestion().getResponses();
					Response obj = elements.remove(posid);
					elements.add(posid - 1, obj);
				}
			} else if (cmd.equals("down")) {
				List<Response> elements = item.getQuestion().getResponses();
				if (posid < elements.size() - 1) {
					Response obj = elements.remove(posid);
					elements.add(posid + 1, obj);
				}
			} else if (cmd.equals("editq")) {
				editQuestion = item.getQuestion().getQuestion();
				displayMaterialFormController(ureq, editQuestion,
						translate("fieldset.legend.question"));
			} else if (cmd.equals("editr")) {
				List<Response> elements = item.getQuestion().getResponses();
				if(posid >= 0 && posid < elements.size()) {
					editResponse = elements.get(posid);
					Material responseMat = editResponse.getContent();
					displayMaterialFormController(ureq, responseMat,
							translate("fieldset.legend.answers"));
				}
			} else if (cmd.equals("addchoice")) {
				ChoiceQuestion question = (ChoiceQuestion) item.getQuestion();
				List<Response> choices = question.getResponses();
				ChoiceResponse newChoice = new ChoiceResponse();
				newChoice.getContent().add(new Mattext(translate("newresponsetext")));
				newChoice.setCorrect(false);
				newChoice.setPoints(-1f); // default value is negative to make sure
				// people understand the meaning of this value
				choices.add(newChoice);
			} else if (cmd.equals("del")) {
				delYesNoCtrl = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(), null, translate("confirm.delete.element"));
				listenTo(delYesNoCtrl);
				delYesNoCtrl.setUserObject(new Integer(posid));
				delYesNoCtrl.activate();
			} else if (cmd.equals("ssc")) { // submit sc
				if(!restrictedEdit && !blockedEdit) {
					ChoiceQuestion question = (ChoiceQuestion) item.getQuestion();
					List<Response> q_choices = question.getResponses();
					String correctChoice = ureq.getParameter("correctChoice");
					for (int i = 0; i < q_choices.size(); i++) {
						ChoiceResponse choice = (ChoiceResponse) q_choices.get(i);
						if (correctChoice != null && correctChoice.equals("value_q" + i)) {
							choice.setCorrect(true);
						} else {
							choice.setCorrect(false);
						}
						choice.setPoints(ureq.getParameter("points_q" + i));
					}
					
					String score = ureq.getParameter("single_score");
					float sc;
					try {
						sc = Float.parseFloat(score);
						if(sc <= 0.0001f) {
							getWindowControl().setWarning(translate("editor.info.mc.zero.points"));
						}
					} catch(Exception e) {
						getWindowControl().setWarning(translate("editor.info.mc.zero.points"));
						sc = 1.0f;
					}
					question.setSingleCorrectScore(sc);
				}
			} else if (cmd.equals("smc")) { // submit mc
				if(!restrictedEdit && !blockedEdit) {
					ChoiceQuestion question = (ChoiceQuestion) item.getQuestion();
					List<Response> choices = question.getResponses();
					boolean hasZeroPointChoice = false;
					for (int i = 0; i < choices.size(); i++) {
						ChoiceResponse choice = (ChoiceResponse) choices.get(i);
						if (ureq.getParameter("value_q" + i) != null && ureq.getParameter("value_q" + i).equalsIgnoreCase("true")) {
							choice.setCorrect(true);
						} else {
							choice.setCorrect(false);
						}
						choice.setPoints(ureq.getParameter("points_q" + i));
						if (choice.getPoints() == 0) hasZeroPointChoice = true;
					}
					if (hasZeroPointChoice && !question.isSingleCorrect()) {
						getWindowControl().setInfo(translate("editor.info.mc.zero.points"));
					}
	
					// set min/max before single_correct score
					// will be corrected by single_correct score afterwards
					question.setMinValue(ureq.getParameter("min_value"));
					question.setMaxValue(ureq.getParameter("max_value"));
					question.setSingleCorrect(ureq.getParameter("valuation_method").equals("single"));
					if (question.isSingleCorrect()) {
						question.setSingleCorrectScore(ureq.getParameter("single_score"));
					} else {
						question.setSingleCorrectScore(0);
					}
				}
			} else if (cmd.equals("skprim")) { // submit kprim
				if(!restrictedEdit && !blockedEdit) {
					float maxValue = 0;
					try {
						maxValue = Float.parseFloat(ureq.getParameter("max_value"));
					} catch (NumberFormatException e) {
						// invalid input, set maxValue 0
					}
					ChoiceQuestion question = (ChoiceQuestion) item.getQuestion();
					List<Response> q_choices = question.getResponses();
					for (int i = 0; i < q_choices.size(); i++) {
						String correctChoice = ureq.getParameter("correctChoice_q" + i);
						ChoiceResponse choice = (ChoiceResponse) q_choices.get(i);
						choice.setPoints(maxValue / 4);
						if ("correct".equals(correctChoice)) {
							choice.setCorrect(true);
						} else {
							choice.setCorrect(false);
						}
					}
					question.setMaxValue(maxValue);
				}
			}
			qtiPackage.serializeQTIDocument();
		}
	}

	@Override
	public void event(UserRequest ureq, Controller controller, Event event) {
		if (controller == matFormCtr) {
			if (event instanceof QTIObjectBeforeChangeEvent) {
				QTIObjectBeforeChangeEvent qobce = (QTIObjectBeforeChangeEvent) event;
				NodeBeforeChangeEvent nce = new NodeBeforeChangeEvent();
				if (editQuestion != null) {
					nce.setNewQuestionMaterial(qobce.getContent());
					nce.setItemIdent(item.getIdent());
					nce.setQuestionIdent(editQuestion.getId());
					nce.setMatIdent(qobce.getId());
					fireEvent(ureq, nce);
				} else if (editResponse != null) {
					nce.setNewResponseMaterial(qobce.getContent());
					nce.setItemIdent(item.getIdent());
					nce.setResponseIdent(editResponse.getIdent());
					nce.setMatIdent(qobce.getId());
					fireEvent(ureq, nce);
				}
			} else if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				if (event == Event.DONE_EVENT) {
					// serialize document
					qtiPackage.serializeQTIDocument();
					// force rerendering of view
					main.setDirty(true);
					editQuestion = null;
					editResponse = null;
				}
				// dispose controllers
				dialogCtr.deactivate();
				dialogCtr.dispose();
				dialogCtr = null;
				matFormCtr.dispose();
				matFormCtr = null;
			}
		} else if (controller == dialogCtr) {
			if (event == Event.CANCELLED_EVENT) {
				dialogCtr.dispose();
				dialogCtr = null;
				matFormCtr.dispose();
				matFormCtr = null;
			}
		} else if (controller == delYesNoCtrl) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				Integer posId = (Integer)delYesNoCtrl.getUserObject();
				item.getQuestion().getResponses().remove(posId.intValue());
				main.setDirty(true);//repaint
			}
		}  
	}

	/**
	 * Displays the MaterialFormController in a closable box.
	 * 
	 * @param ureq
	 * @param mat
	 * @param isRestrictedEditMode
	 */
	private void displayMaterialFormController(UserRequest ureq, Material mat, String title) {
		matFormCtr = new MaterialFormController(ureq, getWindowControl(), mat, qtiPackage, restrictedEdit, blockedEdit);
		matFormCtr.addControllerListener(this);
		dialogCtr = new CloseableModalController(getWindowControl(), "close",
				matFormCtr.getInitialComponent(), true, title);
		matFormCtr.addControllerListener(dialogCtr);
		dialogCtr.activate();
	}

	@Override
	protected void doDispose() {
		item = null;
		if (dialogCtr != null) {
			dialogCtr.dispose();
			dialogCtr = null;
		}
		if (matFormCtr != null) {
			matFormCtr.dispose();
			matFormCtr = null;
		}
	}
}