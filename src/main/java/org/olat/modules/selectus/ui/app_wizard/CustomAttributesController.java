/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.app_wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingMainController;

/**
 * 
 * Initial date: 13 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CustomAttributesController extends FormBasicController {
	
	private final Tab tab;
	private final boolean admin;
	private final boolean editable;
	private final boolean segmented;
	private Position position;
	private final Application application;
	private final TabConfiguration tabConfiguration;
	private List<FormItem> additionalAttributesEl = new ArrayList<>();
	private final ApplicationAttributesDelegate attributesDelegate;
	
	public CustomAttributesController(UserRequest ureq, WindowControl wControl, Form rootForm, Application application,
			TabConfiguration tabConfiguration, boolean admin, boolean segmented, boolean editable) {
		super(ureq, wControl, null, Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale()));
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		
		this.admin = admin;
		this.editable = editable;
		this.segmented = segmented;
		tab = tabConfiguration.getTab();
		this.application = application;
		position = application.getPosition();
		this.tabConfiguration = tabConfiguration;
		attributesDelegate = new ApplicationAttributesDelegate(tab.attributesTab());
		
		initForm(ureq);
	}
	
	public Tab tab() {
		return tab;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!segmented) {
			setFormTitle("wizard.custom.legend", new String[] { tabConfiguration.getTitle(getLocale()), position.getMLTitle(getLocale()) });
		}
		
		String explanation = tabConfiguration.getHelp(getLocale());
		if(StringHelper.containsNonWhitespace(explanation)) {
			setFormTranslatedDescription(StringHelper.xssScan(RecruitingHelper.escWithBR(explanation)));
		}
		
		attributesDelegate.initAdditionalAttributes(formLayout, additionalAttributesEl, application, admin, editable, getLocale());
	}
	
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= attributesDelegate.validateFormLogic(additionalAttributesEl, admin);
		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		attributesDelegate.formInnerEvent(source);
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		commitChanges(application);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	public void commitChanges(Application app) {
		attributesDelegate.commitChanges(additionalAttributesEl, app);
	}
}
