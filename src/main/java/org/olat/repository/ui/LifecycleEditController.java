/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui;

import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
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
 * Initial date: 18.03.2013<br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class LifecycleEditController extends FormBasicController {

	private FormToggle defaultCourseExecEl;
	private TextElement labelEl;
	private TextElement softKeyEl;
	private DateChooser validFromEl;
	private DateChooser validToEl;

	private RepositoryEntryLifecycle lifecycle;
	private final RepositoryEntryLifecycleDAO reLifecycleDao;

	public LifecycleEditController(UserRequest ureq, WindowControl wControl, RepositoryEntryLifecycle lifecycle) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));

		// lifecycle may be null for creating a new entry
		this.lifecycle = lifecycle;
		reLifecycleDao = CoreSpringFactory.getImpl(RepositoryEntryLifecycleDAO.class);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String softKey = lifecycle == null ? "" : lifecycle.getSoftKey();
		softKeyEl = uifactory.addTextElement("lifecycle.softkey", "lifecycle.softkey", 128, softKey, formLayout);
		softKeyEl.setMandatory(true);

		String label = lifecycle == null ? "" : lifecycle.getLabel();
		labelEl = uifactory.addTextElement("lifecycle.label", "lifecycle.label", 128, label, formLayout);

		Date from = lifecycle == null ? null : lifecycle.getValidFrom();
		validFromEl = uifactory.addDateChooser("lifecycle.validFrom", "lifecycle.validFrom", from, formLayout);

		Date to = lifecycle == null ? null : lifecycle.getValidTo();
		validToEl = uifactory.addDateChooser("lifecycle.validTo", "lifecycle.validTo", to, formLayout);

		defaultCourseExecEl = uifactory.addToggleButton("lifecycle.course.exec.default", "lifecycle.course.exec.default", translate("on"), translate("off"), formLayout);
		defaultCourseExecEl.toggle(lifecycle != null && lifecycle.isDefaultPublicCycle());

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		String submitBtnI18nKey;
		if (lifecycle != null) {
			submitBtnI18nKey = "save";
		} else {
			submitBtnI18nKey = "add";
		}
		uifactory.addFormSubmitButton("ok", submitBtnI18nKey, buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		softKeyEl.clearError();
		if (!StringHelper.containsNonWhitespace(softKeyEl.getValue())) {
			softKeyEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		} else if (softKeyEl.getValue() != null && softKeyEl.getValue().length() > 64) {
			softKeyEl.setErrorKey("form.error.toolong", "64");
			allOk &= false;
		}
		
		labelEl.clearError();
		if (StringHelper.containsNonWhitespace(labelEl.getValue()) && labelEl.getValue().length() > 250) {
			labelEl.setErrorKey("form.error.toolong", "250");
			allOk &= false;
		}

		return allOk;
	}

	private void setDefaultCourseExecPeriod() {
		boolean currentLifecycleIsDefault = defaultCourseExecEl.isOn();
		List<RepositoryEntryLifecycle> rePublicLifecycles = reLifecycleDao.loadPublicLifecycle();

		// Check if any existing lifecycle is default and if the new one should be default
		if (currentLifecycleIsDefault) {
			for (RepositoryEntryLifecycle reLifecycle : rePublicLifecycles) {
				if (reLifecycle.isDefaultPublicCycle()) {
					reLifecycle.setDefaultPublicCycle(false);
					reLifecycleDao.updateLifecycle(reLifecycle);
				}
			}
		}

		lifecycle.setDefaultPublicCycle(currentLifecycleIsDefault);
		reLifecycleDao.updateLifecycle(lifecycle);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (lifecycle == null) {
			String label = labelEl.getValue();
			String softKey = softKeyEl.getValue();
			Date from = validFromEl.getDate();
			Date to = validToEl.getDate();
			lifecycle = reLifecycleDao.create(label, softKey, false, from, to);
		} else {
			lifecycle.setLabel(labelEl.getValue());
			lifecycle.setSoftKey(softKeyEl.getValue());
			lifecycle.setValidFrom(validFromEl.getDate());
			lifecycle.setValidTo(validToEl.getDate());
			reLifecycleDao.updateLifecycle(lifecycle);
		}
		setDefaultCourseExecPeriod();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
