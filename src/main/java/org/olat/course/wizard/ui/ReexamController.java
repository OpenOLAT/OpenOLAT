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
package org.olat.course.wizard.ui;

import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.course.wizard.CourseWizardService;

/**
 * 
 * Initial date: 18 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReexamController extends StepFormBasicController {
	
	private static final String RUN_CONTEXT_KEY = "reexam.switch";
	private static final String KEY_YES = "yes";
	private static final String KEY_NO = "no";
	private static final String[] YES_NO_KEYS = new String[] { KEY_YES, KEY_NO };

	private SingleSelection reexamEl;

	private final StepsRunContext runContext;
	private final String retestContextKey;
	private final ReexamSwitchListener reexamSwitchListener;

	public ReexamController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			String retestContextKey, ReexamSwitchListener reexamSwitchListener) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(CourseWizardService.class, getLocale(), getTranslator()));
		this.runContext = runContext;
		this.retestContextKey = retestContextKey;
		this.reexamSwitchListener = reexamSwitchListener;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		reexamEl = uifactory.addRadiosHorizontal("reexam.switch", formLayout, YES_NO_KEYS,
				translateAll(getTranslator(), YES_NO_KEYS));
		boolean reeexam = runContext.containsKey(RUN_CONTEXT_KEY);
		reexamEl.select(KEY_YES, reeexam);
		reexamEl.select(KEY_NO, !reeexam);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (reexamEl.isOneSelected() && reexamEl.getSelectedKey().equals(KEY_YES)) {
			runContext.put(RUN_CONTEXT_KEY, Boolean.TRUE);
			reexamSwitchListener.onSwitchSelected(ureq, true);
		} else {
			runContext.remove(RUN_CONTEXT_KEY);
			runContext.remove(retestContextKey);
			reexamSwitchListener.onSwitchSelected(ureq, false);
		}
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public interface ReexamSwitchListener {
		
		public void onSwitchSelected(UserRequest ureq, boolean reexam);
		
	}

}
