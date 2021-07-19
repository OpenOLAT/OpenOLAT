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

import java.util.function.Supplier;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.course.wizard.CourseWizardService;
import org.olat.course.wizard.PublicationContext;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;

/**
 * 
 * Initial date: 18 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PublicationController extends StepFormBasicController {
	
	public static final String RUN_CONTEXT_KEY = "publication";
	private static final String KEY_YES = "yes";
	private static final String KEY_NO = "no";
	private static final String[] YES_NO_KEYS = new String[] { KEY_YES, KEY_NO };
	
	private SingleSelection statusEl;
	private SingleSelection publishEl;

	private final PublicationContext context;

	public PublicationController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(CourseWizardService.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		context = (PublicationContext)getOrCreateFromRunContext(RUN_CONTEXT_KEY, getContextSupplier());
		
		initForm(ureq);
	}
	
	private Supplier<Object> getContextSupplier() {
		return () -> {
			PublicationContext initialContext = new PublicationContext();
			initialContext.setStatus(RepositoryEntryStatusEnum.preparation);
			initialContext.setPublish(false);
			return initialContext;
		};
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("wizard.title.publication");
		
		KeyValues statusKV = new KeyValues();
		statusKV.add(KeyValues.entry(RepositoryEntryStatusEnum.preparation.name(), translate("cif.status.preparation")));
		statusKV.add(KeyValues.entry(RepositoryEntryStatusEnum.review.name(), translate("cif.status.review")));
		statusKV.add(KeyValues.entry(RepositoryEntryStatusEnum.coachpublished.name(), translate("cif.status.coachpublished")));
		statusKV.add(KeyValues.entry(RepositoryEntryStatusEnum.published.name(), translate("cif.status.published")));
		statusEl = uifactory.addDropdownSingleselect("publishedStatus", "details.label.status", formLayout, statusKV.keys(), statusKV.values());
		statusEl.select(context.getStatus().name(), true);
		
		publishEl = uifactory.addRadiosHorizontal("publish.course.elements", formLayout, YES_NO_KEYS,
				translateAll(getTranslator(), YES_NO_KEYS));
		publishEl.select(KEY_YES, context.isPublish());
		publishEl.select(KEY_NO, !context.isPublish());
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		RepositoryEntryStatusEnum status = statusEl.isOneSelected()
				? RepositoryEntryStatusEnum.valueOf(statusEl.getSelectedKey())
				: RepositoryEntryStatusEnum.preparation;
		context.setStatus(status);
		
		boolean publish = publishEl.isOneSelected() && publishEl.getSelectedKey().equals(KEY_YES);
		context.setPublish(publish);
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
