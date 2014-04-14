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
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dialog.DialogController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.util.Util;
import org.olat.ims.qti.editor.beecom.objects.FIBQuestion;
import org.olat.ims.qti.editor.beecom.objects.FIBResponse;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Material;
import org.olat.ims.qti.editor.beecom.objects.Mattext;
import org.olat.ims.qti.editor.beecom.objects.Response;

/**
 * Initial Date: Oct 21, 2004 <br>
 * 
 * @author mike
 */
public class FIBItemController extends DefaultController implements ControllerEventListener {
	/*
	 * Logging, Velocity
	 */
	private static final String PACKAGE = Util.getPackageName(FIBItemController.class);
	private static final String VC_ROOT = Util.getPackageVelocityRoot(PACKAGE);

	private VelocityContainer main;
	private Translator trnsltr;

	private Item item;
	private QTIEditorPackage qtiPackage;
	private boolean surveyMode = false;
	private DialogController delYesNoCtrl;
	private boolean restrictedEdit;
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
	public FIBItemController(Item item, QTIEditorPackage qtiPackage, Translator trnsltr, WindowControl wControl, boolean restrictedEdit) {
		super(wControl);

		this.restrictedEdit = restrictedEdit;
		this.item = item;
		this.qtiPackage = qtiPackage;
		this.trnsltr = trnsltr;
		main = new VelocityContainer("fibitem", VC_ROOT + "/tab_fibItem.html", trnsltr, this);
		main.contextPut("question", item.getQuestion());
		surveyMode = qtiPackage.getQTIDocument().isSurvey();
		main.contextPut("isSurveyMode", surveyMode ? "true" : "false");
		main.contextPut("isRestrictedEdit", restrictedEdit ? Boolean.TRUE : Boolean.FALSE);
		
		String mediaBaseUrl = qtiPackage.getMediaBaseURL();
		if(mediaBaseUrl != null && !mediaBaseUrl.startsWith("http")) {
			mediaBaseUrl = Settings.getServerContextPathURI() + mediaBaseUrl;
		}
		main.contextPut("mediaBaseURL", mediaBaseUrl);
		setInitialComponent(main);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == main) {
			// olat::: as: improve easy fix since almost all operations change the main vc.
			main.setDirty(true);
			String cmd = event.getCommand();
			String sPosid = ureq.getParameter("posid");
			int posid = 0;
			if (sPosid != null) posid = Integer.parseInt(sPosid);
			if (cmd.equals("up")) {
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
				displayMaterialFormController(ureq, editQuestion, restrictedEdit);

			} else if (cmd.equals("editr")) {
				editResponse = ((Response) item.getQuestion().getResponses().get(posid));
				Material responseMat = ((Response) item.getQuestion().getResponses().get(posid)).getContent();
				displayMaterialFormController(ureq, responseMat, restrictedEdit);
				
			} else if (cmd.equals("addtext")) {
				FIBQuestion fib = (FIBQuestion) item.getQuestion();
				FIBResponse response = new FIBResponse();
				response.setType(FIBResponse.TYPE_CONTENT);
				Material mat = new Material();
				mat.add(new Mattext(trnsltr.translate("newtextelement")));
				response.setContent(mat);
				fib.getResponses().add(response);
			} else if (cmd.equals("addblank")) {
				FIBQuestion fib = (FIBQuestion) item.getQuestion();
				FIBResponse response = new FIBResponse();
				response.setType(FIBResponse.TYPE_BLANK);
				response.setCorrectBlank("");
				response.setPoints(1f); // default value
				fib.getResponses().add(response);
			} else if (cmd.equals("del")) {
				delYesNoCtrl = DialogController.createYesNoDialogController(getWindowControl(), ureq.getLocale(), trnsltr.translate("confirm.delete.element"), this,
						new Integer(posid));
				getWindowControl().pushAsModalDialog( delYesNoCtrl.getInitialComponent());
			} else if (cmd.equals("sfib")) { // submit fib
				FIBQuestion question = (FIBQuestion) item.getQuestion();
				// Survey specific variables
				if (surveyMode) {
					List<Response> responses = question.getResponses();
					for (int i = 0; i < responses.size(); i++) {
						FIBResponse response = (FIBResponse) responses.get(i);
						if (FIBResponse.TYPE_BLANK.equals(response.getType())) {
							// Set size of input field
							String size = ureq.getParameter("size_q" + i);
							if (size != null) response.setSizeFromString(size);
							String maxLength = ureq.getParameter("maxl_q" + i);
							if (maxLength != null) response.setMaxLengthFromString(maxLength);
						}
					}

				} else {
					// For all other cases, non-surveys
					// set min/max values before single_correct !!
					if (!restrictedEdit) {
						// only in full edit mode the following fields are available:
						// min_value, max_value, valuation_method
						question.setMinValue(ureq.getParameter("min_value"));
						question.setMaxValue(ureq.getParameter("max_value"));
						question.setSingleCorrect("single".equals(ureq.getParameter("valuation_method")));
						if (question.isSingleCorrect()) {
							question.setSingleCorrectScore(ureq.getParameter("single_score"));
						}	else {
							question.setSingleCorrectScore(0.0f);
						}
					}

					NodeBeforeChangeEvent nce = new NodeBeforeChangeEvent();
					nce.setItemIdent(item.getIdent());

					List<Response> responses = question.getResponses();
					for (int i = 0; i < responses.size(); i++) {
						FIBResponse response = (FIBResponse) responses.get(i);
						nce.setResponseIdent(response.getIdent());
						fireEvent(ureq, nce);

						response.setPoints(ureq.getParameter("points_q" + i));
						if (FIBResponse.TYPE_BLANK.equals(response.getType())) {
							response.setCorrectBlank(ureq.getParameter("content_q" + i));
							// Set case sensitiveness
							String caseSensitive = ureq.getParameter("case_q" + i);
							if (caseSensitive == null) caseSensitive = "No";
							response.setCaseSensitive(caseSensitive);
							// Set size of input field
							String size = ureq.getParameter("size_q" + i);
							if (size != null) response.setSizeFromString(size);
							String maxLength = ureq.getParameter("maxl_q" + i);
							if (maxLength != null) response.setMaxLengthFromString(maxLength);

							// find longest correct blank in all synonyms of
							// correct answers, fix max lenght if a longer value
							// is found
							String[] allCorrect = response.getCorrectBlank().split(";");
							int longestCorrect = 0;
							for (int j = 0; j < allCorrect.length; j++) {
								String singleCorrect = allCorrect[j];
								if (singleCorrect.length()  > longestCorrect) {
									longestCorrect = singleCorrect.length();
								}
							}
							if (longestCorrect > response.getMaxLength()) response.setMaxLength(longestCorrect);
						}
					}
				}
			}
			qtiPackage.serializeQTIDocument();
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
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
				// dispose modal dialog controller
				dialogCtr.dispose();
				dialogCtr = null;
				matFormCtr.dispose();
				matFormCtr = null;
			}
		} else if (controller == delYesNoCtrl) {
			getWindowControl().pop();
			if (event == DialogController.EVENT_FIRSTBUTTON) {
				Object position = delYesNoCtrl.getUserObject();
				if(position instanceof Integer) {
					int pos = ((Integer)position).intValue();
					List<Response> responses = item.getQuestion().getResponses();
					if(!responses.isEmpty() && pos < responses.size()) {
						responses.remove(pos);
					}
				}
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
	private void displayMaterialFormController(UserRequest ureq, Material mat, boolean isRestrictedEditMode) {
		matFormCtr = new MaterialFormController(ureq, getWindowControl(), mat, qtiPackage, isRestrictedEditMode);
		matFormCtr.addControllerListener(this);
		dialogCtr = new CloseableModalController(getWindowControl(), "close", matFormCtr.getInitialComponent());
		matFormCtr.addControllerListener(dialogCtr);
		dialogCtr.activate();
	}	
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		main = null;
		item = null;
		trnsltr = null;
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