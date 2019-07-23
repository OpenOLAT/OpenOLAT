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
package org.olat.portfolio.ui.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.EPFilterSettings;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * shows available filters and let user select from it
 * 
 * <P>
 * Initial Date:  12.11.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPFilterSelectController extends FormBasicController {

	@Autowired
	private EPFrontendManager ePFMgr;
	private FormLink adaptBtn;
	private SingleSelection filterSel;
	private ArrayList<EPFilterSettings> nonEmptyFilters;
	private String presetFilterID;

	public EPFilterSelectController(UserRequest ureq, WindowControl wControl, String presetFilterID) {
		super(ureq, wControl);
		this.presetFilterID = presetFilterID;
		
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer, org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {		
		List<EPFilterSettings> existingFilters = ePFMgr.getSavedFilterSettings(getIdentity());
		for(Iterator<EPFilterSettings> existingFilterIt=existingFilters.iterator(); existingFilterIt.hasNext(); ) {
			if(existingFilterIt.next().isFilterEmpty()) {
				existingFilterIt.remove();
			}
		}

		int amount = existingFilters.size() + 1;
		nonEmptyFilters = new ArrayList<>(amount);
		String[] theKeys = new String[amount];
		String[] theValues = new String[amount];
		theKeys[0] = String.valueOf(0);
		theValues[0] = translate("filter.all");
		int i=1;
		String presetFilterIndex = "0";
		for (EPFilterSettings epFilterSettings : existingFilters) {
			theKeys[i] = epFilterSettings.getFilterId();
			theValues[i] = epFilterSettings.getFilterName();
			if (presetFilterID != null && presetFilterID.equals(epFilterSettings.getFilterId())) {
				presetFilterIndex = epFilterSettings.getFilterId(); 
			}
			nonEmptyFilters.add(epFilterSettings);
			i++;
		}
		// don't show anything if no filter exists
		if (!nonEmptyFilters.isEmpty()) {
			String page = velocity_root + "/filter_select.html";
			FormLayoutContainer selection = FormLayoutContainer.createCustomFormLayout("filter_selection", getTranslator(), page);
			selection.setRootForm(mainForm);
			selection.setLabel("filter.select", null);
			formLayout.add(selection);
			
			filterSel = uifactory.addDropdownSingleselect("filter.select", selection, theKeys, theValues, null);
			filterSel.addActionListener(FormEvent.ONCHANGE);
			filterSel.select(presetFilterIndex, true);
			adaptBtn = uifactory.addFormLink("filter.adapt", selection);
			adaptBtn.setVisible(!presetFilterIndex.equals("0"));
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest, org.olat.core.gui.components.form.flexible.FormItem, org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == filterSel){
			int selFilter = filterSel.getSelected();
			EPFilterSettings selFilterSet ;
			if (selFilter != 0) {
				selFilterSet = nonEmptyFilters.get(selFilter-1);
			} else {
				// all was selected, fire an empty filter
				selFilterSet = new EPFilterSettings();
			}
			fireEvent(ureq, new PortfolioFilterChangeEvent(selFilterSet));
		} else if (source == adaptBtn){
			// launch search view
			int selFilter = filterSel.getSelected();
			if(selFilter > 0) {
				EPFilterSettings selFilterSet = nonEmptyFilters.get(selFilter-1);
				fireEvent(ureq, new PortfolioFilterEditEvent(selFilterSet));
			}
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to persist
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing
	}

}
