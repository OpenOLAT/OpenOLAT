//<OLATCE-103>
/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2011 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * 
 * All rights reserved.
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
		selVC.addActionListener(this, FormEvent.ONCHANGE);
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