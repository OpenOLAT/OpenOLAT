//<OLATCE-103>
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.vc;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

import de.bps.course.nodes.vc.provider.VCProvider;
import de.bps.course.nodes.vc.provider.VCProviderFactory;

/**
 * 
 * Description:<br>
 * Support selection of a virtual classroom, if there are multiple ones registered.
 * 
 * <P>
 * Initial Date:  07.01.2011 <br>
 * @author skoeber
 */
public class VCSelectionForm extends FormBasicController {
	
	private SingleSelection selVC;
	private String selectedProvider;

	public VCSelectionForm(UserRequest ureq, WindowControl wControl, String selectedProvider) {
		super(ureq, wControl);
		this.selectedProvider = selectedProvider;
		initForm(flc, this, ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int num = VCProviderFactory.getProviders().size();
		String[] keys = new String[num];
		String[] vals = new String[num];
		int i = 0;
		for(VCProvider provider : VCProviderFactory.getProviders()) {
			keys[i] = provider.getProviderId();
			vals[i] = provider.getDisplayName();
			i++;
		}
		
		selVC = uifactory.addDropdownSingleselect("config.select.vc", flc, keys, vals, null);
		selVC.select(selectedProvider, true);
		selVC.addActionListener(FormEvent.ONCHANGE);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == selVC) {
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	public String getSelectedProvider() {
		return selVC.getSelectedKey();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}

}
//<OLATCE-103>