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

package org.olat.ims.qti.editor.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.memento.Memento;
import org.olat.ims.qti.editor.ItemNodeTabbedFormController;
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.qti.editor.QTIEditorMainController;
import org.olat.ims.qti.editor.QTIEditorPackage;
import org.olat.ims.qti.editor.beecom.objects.Control;
import org.olat.ims.qti.editor.beecom.objects.EssayResponse;
import org.olat.ims.qti.editor.beecom.objects.FIBResponse;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Material;
import org.olat.ims.qti.editor.beecom.objects.QTIObject;
import org.olat.ims.qti.editor.beecom.objects.Question;
import org.olat.ims.qti.editor.beecom.objects.Response;

/**
 * Initial Date: Nov 21, 2004 <br>
 * 
 * @author patrick
 */
public class ItemNode extends GenericQtiNode {

	private Item item;
	private QTIEditorPackage qtiPackage;
	private TabbedPane myTabbedPane;
	private static final Logger log = Tracing.createLoggerFor(ItemNode.class);
	
	/**
	 * 
	 * @param theItem
	 */
	public ItemNode(Item theItem) {
		super(theItem.getIdent().replace(":", ""));
		item = theItem;
		setMenuTitleAndAlt(item.getTitle());
		setUserObject(item.getIdent());
		
	}
	
	/**
	 * @param theItem
	 * @param qtiPackage
	 */
	public ItemNode(Item theItem, QTIEditorPackage qtiPackage) {
		item = theItem;
		this.qtiPackage = qtiPackage;
		setMenuTitleAndAlt(item.getTitle());
		setUserObject(item.getIdent());
	}
	
	@Override
	public String getIconCssClass() {
		if (item.isAlient()) {
			return "o_mi_qtialientitem";
		} else {
			int questionType = item.getQuestion().getType();
			switch (questionType) {
				case Question.TYPE_SC: return "o_mi_qtisc";
				case Question.TYPE_MC: return "o_mi_qtimc";
				case Question.TYPE_KPRIM: return "o_mi_qtikprim";
				case Question.TYPE_FIB: return "o_mi_qtifib";
				case Question.TYPE_ESSAY: return "o_mi_qtiessay";
			}
		}
		return "";
	}

	/**
	 * Set's the node's title and alt text (truncates title)
	 * 
	 * @param title
	 */
	@Override
	public void setMenuTitleAndAlt(String title) {
		super.setMenuTitleAndAlt(title);
		item.setTitle(title);
	}

	@Override
	public TabbedPane createEditTabbedPane(UserRequest ureq, WindowControl wControl, Translator trnsltr,
			QTIEditorMainController editorMainController) {
		if (myTabbedPane == null) {
			try {
				myTabbedPane = new TabbedPane("tabbedPane", ureq.getLocale());
				TabbableController tabbCntrllr = new ItemNodeTabbedFormController(item, qtiPackage, ureq, wControl,
						editorMainController.isRestrictedEdit(), editorMainController.isBlockedEdit());
				tabbCntrllr.addTabs(myTabbedPane);
				tabbCntrllr.addControllerListener(editorMainController);
			} catch (Exception e) {
				myTabbedPane = null;
				log.warn("Cannot create editor for the current item - item.getIdent(): " + item.getIdent());
			}
		}
		return myTabbedPane;
	}

	@Override
	public void childNodeChanges() {
		//
	}

	@Override
	public void insertQTIObjectAt(QTIObject object, int position) {
		throw new AssertException("Can't insert objects on ItemNode.");
	}

	/**
	 * @see org.olat.ims.qti.editor.tree.IQtiNode#removeQTIObjectAt(int)
	 */
	public QTIObject removeQTIObjectAt(int position) {
		throw new AssertException("Can't remove objects on ItemNode.");
	}

	/**
	 * @see org.olat.ims.qti.editor.tree.IQtiNode#getQTIObjectAt(int)
	 */
	public QTIObject getQTIObjectAt(int position) {
		throw new AssertException("Can't get objects from ItemNode.");
	}

	/**
	 * @see org.olat.ims.qti.editor.tree.IQtiNode#getUnderlyingQTIObject()
	 */
	public QTIObject getUnderlyingQTIObject() {
		return item;
	}

	public Memento createMemento() {
		Question question = item.getQuestion();
		// special case switches as question types are encoded into integers!!
		boolean isFIB = question.getType() == Question.TYPE_FIB;
		boolean isESSAY = question.getType() == Question.TYPE_ESSAY;

		// Item metadata
		QtiNodeMemento qnm = new QtiNodeMemento();
		Map<String,Object> qtiState = new HashMap<>();
		qtiState.put("ID", item.getIdent());
		qtiState.put("TITLE", item.getTitle());
		qtiState.put("OBJECTIVES", item.getObjectives());
		// question and responses
		qtiState.put("QUESTION.ID", question.getIdent());
		qtiState.put("QUESTION.HINTTEXT", question.getHintText());
		Material questMaterial = question.getQuestion();
		qtiState.put("QUESTION.MATERIAL.ASTEXT", questMaterial.renderAsText());
		List<String> ids = new ArrayList<>();
		List<String> asTexts = new ArrayList<>();
		List<String> feedbacks = new ArrayList<>();
		List<Response> responses = question.getResponses();
		for (Iterator<Response> iter = responses.iterator(); iter.hasNext();) {
			Response resp = iter.next();
			if (isFIB) {
				if (FIBResponse.TYPE_BLANK.equals(((FIBResponse) resp).getType())) {
					asTexts.add(formatFIBResponseAsText((FIBResponse) resp));
					ids.add(resp.getIdent());
					feedbacks.add(QTIEditHelper.getFeedbackOlatRespText(item, resp.getIdent()));
				}
			} else if (isESSAY) {
				asTexts.add(formatESSAYResponseAsText((EssayResponse) resp));
				ids.add(resp.getIdent());
				feedbacks.add(QTIEditHelper.getFeedbackOlatRespText(item, resp.getIdent()));
			} else {
				// not a FIB or ESSAY response
				asTexts.add(resp.getContent().renderAsText());
				ids.add(resp.getIdent());
				feedbacks.add(QTIEditHelper.getFeedbackOlatRespText(item, resp.getIdent()));
			}
		}
		qtiState.put("QUESTION.RESPONSES.IDS", ids);
		qtiState.put("QUESTION.RESPONSES.ASTEXT", asTexts);
		qtiState.put("QUESTION.RESPONSES.FEEDBACK", feedbacks);
		// feedback
		qtiState.put("FEEDBACK.MASTERY", QTIEditHelper.getFeedbackMasteryText(item));
		qtiState.put("FEEDBACK.FAIL", QTIEditHelper.getFeedbackFailText(item));
		Control control = QTIEditHelper.getControl(item);
		qtiState.put("FEEDBACK.ENABLED", control.getFeedback() == 1 ? Boolean.TRUE : Boolean.FALSE);
		//
		qnm.setQtiState(qtiState);
		//
		return qnm;
	}

	/**
	 * @param resp
	 * @return
	 */
	private String formatFIBResponseAsText(FIBResponse fresp) {
		String asText = "Correct inputs:[" + fresp.getCorrectBlank() + "]";
		asText += " Case sensitive:[" + fresp.getCaseSensitive() + "]";
		asText += " Points:[" + fresp.getPoints() + "]";
		asText += " Blank size:[" + fresp.getSize() + "]";
		asText += " Blank Max size:[" + fresp.getMaxLength() + "]";
		return asText;
	}

	private String formatESSAYResponseAsText(EssayResponse eresp) {
		String asText = "Response field size, columns: [" + eresp.getColumns() + "]";
		asText += " rows: [" + eresp.getRows() + "]";
		asText += " points: [" + eresp.getPoints() + "]";
		return asText;
	}

	public void setMemento(Memento state) {
		throw new UnsupportedOperationException("setting a Memento is not supported yet! \n" + state);
	}

	public String createChangeMessage(Memento mem) {
		String retVal = null;
		if (mem instanceof QtiNodeMemento) {
			QtiNodeMemento qnm = (QtiNodeMemento) mem;
			Map<String,Object> qtiState = qnm.getQtiState();
			//
			String oldTitle = (String) qtiState.get("TITLE");
			String newTitle = item.getTitle();
			String titleChange = null;
			//
			String oldObjectives = (String) qtiState.get("OBJECTIVES");
			String newObjectives = item.getObjectives();
			String objectChange = null;
			//
			Question question = item.getQuestion();
			boolean isFIB = question.getType() == Question.TYPE_FIB;
			boolean isESSAY = question.getType() == Question.TYPE_ESSAY;
			String oldHinttext = (String) qtiState.get("QUESTION.HINTTEXT");
			String newHinttext = question.getHintText();
			String hinttextChange = null;
			//
			String oldQuestion = (String) qtiState.get("QUESTION.MATERIAL.ASTEXT");
			String newQuestion = question.getQuestion().renderAsText();
			String questionChange = null;
			// feedback
			String feedbackChanges = "";
			String oldFeedbackMastery = (String) qtiState.get("FEEDBACK.MASTERY");
			String newFeedbackMastery = QTIEditHelper.getFeedbackMasteryText(item);
			String oldFeedbackFail = (String) qtiState.get("FEEDBACK.FAIL");
			String newFeedbackFail = QTIEditHelper.getFeedbackFailText(item);
			Control control = QTIEditHelper.getControl(item);
			Boolean oldHasFeedback = (Boolean) qtiState.get("FEEDBACK.ENABLED");
			Boolean newHasFeedback = control != null ? new Boolean(control.getFeedback() == 1) : null;
			//
			List asTexts = (List) qtiState.get("QUESTION.RESPONSES.ASTEXT");
			List feedbacks = (List) qtiState.get("QUESTION.RESPONSES.FEEDBACK");
			String oldResp = null;
			String newResp = null;
			String oldFeedback = null;
			String newFeedback = null;
			String responsesChanges = "";
			List<Response> responses = question.getResponses();
			int i = 0;
			boolean nothingToDo = false;
			for (Iterator<Response> iter = responses.iterator(); iter.hasNext();) {
				nothingToDo = false;
				Response resp = iter.next();
				if (isFIB) {
					if (FIBResponse.TYPE_BLANK.equals(((FIBResponse) resp).getType())) {
						newResp = formatFIBResponseAsText((FIBResponse) resp);
					} else {
						// skip
						nothingToDo = true;
					}
				} else if (isESSAY) {
					newResp = formatESSAYResponseAsText((EssayResponse) resp);
				} else {
					newResp = resp.getContent().renderAsText();
				}
				// if NOT nothingToDO
				if (!nothingToDo) {
					oldResp = (String) asTexts.get(i);
					if ((oldResp != null && !oldResp.equals(newResp)) || (newResp != null && !newResp.equals(oldResp))) {
						if (isFIB) {
							responsesChanges += "\nBlank changed:";
							responsesChanges += "\nold blank: \n\t" + formatVariable(oldResp) + "\n\nnew blank: \n\t" + formatVariable(newResp);
						} else {
							responsesChanges += "\nResponse changed:";
							responsesChanges += "\nold response: \n\t" + formatVariable(oldResp) + "\n\nnew response: \n\t" + formatVariable(newResp);
						}
					}
					// feedback to response changed?
					newFeedback = QTIEditHelper.getFeedbackOlatRespText(item, resp.getIdent());
					oldFeedback = (String) feedbacks.get(i);
					if ((oldFeedback != null && !oldFeedback.equals(newFeedback)) || (newFeedback != null && !newFeedback.equals(oldFeedback))) {
						feedbackChanges += "\nFeedback changed:";
						feedbackChanges += "\nold feedback: \n\t" + formatVariable(oldFeedback) + "\n\nnew feedback: \n\t"
								+ formatVariable(newFeedback);
					}
					i++;
				}
			}
			//
			retVal = "\n---+++ Item changes [" + oldTitle + "]:";
			if ((oldTitle != null && !oldTitle.equals(newTitle)) || (newTitle != null && !newTitle.equals(oldTitle))) {
				titleChange = "\n\nold title: \n\t" + formatVariable(oldTitle) + "\n\nnew title: \n\t" + formatVariable(newTitle);
			}
			if ((oldObjectives != null && !oldObjectives.equals(newObjectives))
					|| (newObjectives != null && !newObjectives.equals(oldObjectives))) {
				objectChange = "\n\nold objectives: \n\t" + formatVariable(oldObjectives) + "\n\nnew objectives: \n\t"
						+ formatVariable(newObjectives);
			}
			if (titleChange != null || objectChange != null) {
				retVal += "\nMetadata changed:";
				if (titleChange != null) retVal += titleChange;
				if (objectChange != null) retVal += objectChange;
			}
			//
			if ((oldHinttext != null && !oldHinttext.equals(newHinttext)) || (newHinttext != null && !newHinttext.equals(oldHinttext))) {
				hinttextChange = "\n---+++ old hinttext: \n\t" + formatVariable(oldHinttext) + "\n\nnew hinttext: \n\t"
						+ formatVariable(newHinttext);
				retVal += hinttextChange;
			}
			if ((oldQuestion != null && !oldQuestion.equals(newQuestion)) || (newQuestion != null && !newQuestion.equals(oldQuestion))) {
				questionChange = "\n---+++ old question: \n\t" + formatVariable(oldQuestion) + "\n\nnew question: \n\t"
						+ formatVariable(newQuestion);
				retVal += questionChange;
			}
			if (!responsesChanges.equals("")) {
				retVal += responsesChanges;
			}
			if ((oldFeedbackMastery != null && !oldFeedbackMastery.equals(newFeedbackMastery))
					|| (newFeedbackMastery != null && !newFeedbackMastery.equals(oldFeedbackMastery))) {
				String tmp = "\n---+++ old master feedback: \n\t" + formatVariable(oldFeedbackMastery) + "\n\nnew master feedback: \n\t"
						+ formatVariable(newFeedbackMastery);
				feedbackChanges = tmp + feedbackChanges;
			}
			if ((oldFeedbackFail != null && !oldFeedbackFail.equals(newFeedbackFail))
					|| (newFeedbackFail != null && !newFeedbackFail.equals(oldFeedbackFail))) {
				String tmp = "\n---+++ old fail feedback: \n\t" + formatVariable(oldFeedbackFail) + "\n\nnew fail feedback: \n\t"
						+ formatVariable(newFeedbackFail);
				feedbackChanges = tmp + feedbackChanges;
			}
			if ((oldHasFeedback != null && newHasFeedback != null && oldHasFeedback != newHasFeedback)) {
				String oldF = oldHasFeedback.booleanValue() ? "enabled" : "disabled";
				String newF = newHasFeedback.booleanValue() ? "enabled" : "disabled";
				feedbackChanges = "\n---+++ feedback was : \n\t" + oldF + "\n\n feedback is now: \n\t" + newF + feedbackChanges;
			}
			if (!feedbackChanges.equals("")) {
				retVal += feedbackChanges;
			}
			return retVal;
		}
		return "undefined";
	}

}