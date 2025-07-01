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
package org.olat.core.gui.components.form.flexible.impl.elements.table.filter;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: Jun 27, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FlexiFilterBasicController extends FormBasicController {
	
	private FormLink clearButton;
	private FormLink updateButton;
	private FormLink closeButton;

	private FlexiFilterExtendedController filterCtrl;
	
	private final Translator compTranslator;
	private final FlexiTableExtendedFilter filter;
	private final Object preselectedValue;
	private final boolean withClose;

	public FlexiFilterBasicController(UserRequest ureq, WindowControl wControl, Translator compTranslator,
			boolean withClose, FlexiTableExtendedFilter filter, Object preselectedValue) {
		super(ureq, wControl, "filter_basic", Util.createPackageTranslator(FlexiTableElementImpl.class, ureq.getLocale()));
		this.compTranslator = compTranslator;
		this.filter = filter;
		this.preselectedValue = preselectedValue;
		this.withClose = withClose;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (formLayout instanceof FormLayoutContainer formCont) {
			formCont.contextPut("title", filter.getLabel());
		}
		if (withClose) {
			closeButton = uifactory.addFormLink("close", "", null, formLayout, Link.LINK + Link.NONTRANSLATED);
			closeButton.setTitle(translate("close"));
			closeButton.setIconLeftCSS("o_icon o_icon_close");
			closeButton.setElementCssClass("o_filter_close");
		}
		
		filterCtrl = filter.getController(ureq, getWindowControl(), mainForm, compTranslator, preselectedValue);
		listenTo(filterCtrl);
		formLayout.add("extended", filterCtrl.getInitialFormItem());
		
		clearButton = uifactory.addFormLink("clear", formLayout, Link.LINK);
		clearButton.setElementCssClass("o_filter_clear");
		clearButton.setEnabled(filterCtrl.isClearButtonEnabled());
		
		updateButton = uifactory.addFormLink("update", formLayout, Link.BUTTON);
		updateButton.setElementCssClass("o_sel_flexiql_update o_filter_update o_button_primary_light");
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem source, FormEvent fe) {
		if (source == clearButton) {
			super.propagateDirtinessToContainer(source, fe);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == filterCtrl) {
			if (event == FlexiFilterExtendedController.CLEAR_BUTTON_UI_EVENT) {
				clearButton.setEnabled(filterCtrl.isClearButtonEnabled());
			} else {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (clearButton == source) {
			filterCtrl.doClear(ureq);
		} else if(updateButton == source) {
			if(filterCtrl.validateFormLogic(ureq)) {
				filterCtrl.doUpdate(ureq);
			}
		} else if(closeButton == source) {
			formCancelled(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		filterCtrl.doUpdate(ureq);
	}

}
