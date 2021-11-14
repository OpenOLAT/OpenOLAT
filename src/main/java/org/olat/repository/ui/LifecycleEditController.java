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
package org.olat.repository.ui;

import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * 
 * Initial date: 18.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LifecycleEditController extends FormBasicController {

	private TextElement labelEl;
	private TextElement softKeyEl;
	private DateChooser validFromEl;
	private DateChooser validToEl;
	
	private final RepositoryEntryLifecycle lifecycle;
	private final RepositoryEntryLifecycleDAO reLifecycleDao;
	
	public LifecycleEditController(UserRequest ureq, WindowControl wControl, RepositoryEntryLifecycle lifecycle) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		this.lifecycle = lifecycle;
		reLifecycleDao = CoreSpringFactory.getImpl(RepositoryEntryLifecycleDAO.class);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String label = lifecycle == null ? "" : lifecycle.getLabel();
		labelEl = uifactory.addTextElement("lifecycle.label", "lifecycle.label", 128, label, formLayout);
		
		String softKey = lifecycle == null ? "" : lifecycle.getSoftKey();
		softKeyEl = uifactory.addTextElement("lifecycle.softkey", "lifecycle.softkey", 128, softKey, formLayout);
		
		Date from = lifecycle == null ? null : lifecycle.getValidFrom();
		validFromEl = uifactory.addDateChooser("lifecycle.validFrom", "lifecycle.validFrom", from, formLayout);
		
		Date to = lifecycle == null ? null : lifecycle.getValidTo();
		validToEl = uifactory.addDateChooser("lifecycle.validTo", "lifecycle.validTo", to, formLayout);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		labelEl.clearError();
		if(!StringHelper.containsNonWhitespace(labelEl.getValue())) {
			labelEl.setErrorKey("form.mandatory.hover", null);
			allOk = false;
		}

		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(lifecycle == null) {
			String label = labelEl.getValue();
			String softKey = softKeyEl.getValue();
			Date from = validFromEl.getDate();
			Date to = validToEl.getDate();
			reLifecycleDao.create(label, softKey, false, from, to);
		} else {
			lifecycle.setLabel(labelEl.getValue());
			lifecycle.setSoftKey(softKeyEl.getValue());
			lifecycle.setValidFrom(validFromEl.getDate());
			lifecycle.setValidTo(validToEl.getDate());
			reLifecycleDao.updateLifecycle(lifecycle);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
