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
* <p>
*/ 

package org.olat.ims.qti.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.ims.qti.editor.beecom.objects.EssayQuestion;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Material;

/**
 * Initial Date: Oct 21, 2004 <br>
 * 
 * @author mike
 */
public class EssayItemController extends DefaultController implements ControllerEventListener {
	/*
	 * Logging, Velocity
	 */
	private static final String PACKAGE = Util.getPackageName(EssayItemController.class);
	private static final String VC_ROOT = Util.getPackageVelocityRoot(PACKAGE);

	private VelocityContainer main;
	private Translator trnsltr;

	private Item item;
	private EssayQuestion essayQuestion;
	private QTIEditorPackage qtiPackage;
	private boolean restrictedEdit;
	private CloseableModalController dialogCtr;
	private MaterialFormController matFormCtr;

	/**
	 * @param item
	 * @param qtiPackage
	 * @param trnsltr
	 * @param wControl
	 */
	public EssayItemController(Item item, QTIEditorPackage qtiPackage, Translator trnsltr, WindowControl wControl, boolean restrictedEdit) {
		super(wControl);

		this.restrictedEdit = restrictedEdit;
		this.item = item;
		this.qtiPackage = qtiPackage;
		this.trnsltr = trnsltr;
		main = new VelocityContainer("essayitem", VC_ROOT + "/tab_essayItem.html", trnsltr, this);
		essayQuestion = (EssayQuestion) item.getQuestion();
		main.contextPut("question", essayQuestion);
		main.contextPut("response", essayQuestion.getEssayResponse());
		main.contextPut("mediaBaseURL", qtiPackage.getMediaBaseURL());
		main.contextPut("isRestrictedEdit", restrictedEdit ? Boolean.TRUE : Boolean.FALSE);
		setInitialComponent(main);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == main) {
			String cmd = event.getCommand();
			if (cmd.equals("editq")) {
				displayMaterialFormController(ureq, item.getQuestion().getQuestion(), restrictedEdit);

			} else if (cmd.equals("sessay")) { // submit essay
				// fetch columns
				String sColumns = ureq.getParameter("columns_q");
				int iColumns;
				try {
					iColumns = Integer.parseInt(sColumns);
				} catch (NumberFormatException nfe) {
					iColumns = 50;
					getWindowControl().setWarning(trnsltr.translate("error.columns"));
				}

				// fetch rows
				String sRows = ureq.getParameter("rows_q");
				int iRows;
				try {
					iRows = Integer.parseInt(sRows);
				} catch (NumberFormatException nfe) {
					iRows = 5;
					getWindowControl().setWarning(trnsltr.translate("error.rows"));
				}

				if (restrictedEdit) {
					boolean hasChange = false;
					hasChange = iColumns != essayQuestion.getEssayResponse().getColumns();
					hasChange = hasChange || (iRows != essayQuestion.getEssayResponse().getRows());
					if (hasChange) {
						NodeBeforeChangeEvent nce = new NodeBeforeChangeEvent();
						nce.setItemIdent(item.getIdent());
						nce.setResponseIdent(essayQuestion.getEssayResponse().getIdent());
						fireEvent(ureq, nce);
					}
				}

				essayQuestion.getEssayResponse().setColumns(iColumns);
				essayQuestion.getEssayResponse().setRows(iRows);

			}
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
				nce.setNewQuestionMaterial(qobce.getContent());
				nce.setItemIdent(item.getIdent());
				nce.setQuestionIdent(item.getQuestion().getQuestion().getId());
				nce.setMatIdent(qobce.getId());
				fireEvent(ureq, nce);
			} else if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				if (event == Event.DONE_EVENT) {
					qtiPackage.serializeQTIDocument();
					// force rerendering of view
					main.setDirty(true);
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
		}
	}

	/**
	 * Displays the MaterialFormController in a closable box.
	 * 
	 * @param ureq
	 * @param mat
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