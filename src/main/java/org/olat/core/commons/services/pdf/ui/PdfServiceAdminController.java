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
package org.olat.core.commons.services.pdf.ui;

import java.util.List;

import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfSPI;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PdfServiceAdminController extends BasicController {
	
	private final VelocityContainer mainVC;
	private final PdfProviderController providerSelectorCtrl;
	
	private Controller providerCtrl;
	
	@Autowired
	private PdfModule pdfModule;
	
	public PdfServiceAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("admin");
		
		providerSelectorCtrl = new PdfProviderController(ureq, getWindowControl());
		listenTo(providerSelectorCtrl);
		mainVC.put("selector", providerSelectorCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
		updateUI(ureq);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(providerSelectorCtrl == source) {
			updateUI(ureq);
		}
		super.event(ureq, source, event);
	}
	
	private void updateUI(UserRequest ureq) {
		removeAsListenerAndDispose(providerCtrl);
		
		if(pdfModule.isEnabled() && pdfModule.getPdfServiceProvider() != null) {
			providerCtrl = pdfModule.getPdfServiceProvider().createAdminController(ureq, getWindowControl());
			listenTo(providerCtrl);
			mainVC.put("settings", providerCtrl.getInitialComponent());
		} else {
			mainVC.remove("settings");
		}
	}

	private class PdfProviderController extends FormBasicController {
		
		private SingleSelection providerEl;
		private MultipleSelectionElement enableEl;
		
		public PdfProviderController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);
			
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormTitle("pdf.service.title");
			setFormDescription("pdf.service.description");
			
			String[] keys = new String[] { "on" };
			String[] values = new String[] { translate("on") };
			enableEl = uifactory.addCheckboxesHorizontal("pdf.enable", formLayout, keys, values);
			enableEl.addActionListener(FormEvent.ONCHANGE);
			if(pdfModule.isEnabled()) {
				enableEl.select(keys[0], true);
			}
			
			List<PdfSPI> spies = pdfModule.getPdfServiceProviders();
			String[] spiKeys = new String[spies.size() + 1];
			String[] spiValues = new String[spies.size() + 1];
			spiValues[0] = spiKeys[0] = "-";
			for(int i=spies.size() + 1; i-->1; ) {
				spiValues[i] = spiKeys[i] = spies.get(i - 1).getId();
			}
			
			providerEl = uifactory.addDropdownSingleselect("pdf.providers", formLayout, spiKeys, spiValues);
			providerEl.addActionListener(FormEvent.ONCHANGE);
			providerEl.setVisible(enableEl.isAtLeastSelected(1));
			if(pdfModule.getPdfServiceProvider() != null) {
				for(PdfSPI spi:spies) {
					if(spi.getId().equals(pdfModule.getPdfServiceProvider().getId())) {
						providerEl.select(spi.getId(), true);
					}
				}
			}
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(enableEl == source) {
				boolean enabled = enableEl.isAtLeastSelected(1);
				providerEl.setVisible(enabled);
				pdfModule.setEnabled(enabled);
				if(!enabled) {
					pdfModule.setPdfServiceProvider(null);
				}
				fireEvent(ureq, Event.DONE_EVENT);
			} else if(providerEl == source) {
				selectProvider();
				fireEvent(ureq, Event.DONE_EVENT);
			}
			super.formInnerEvent(ureq, source, event);
		}
		
		private void selectProvider() {
			boolean found = false;
			List<PdfSPI> spies = pdfModule.getPdfServiceProviders();
			for(PdfSPI spi:spies) {
				if(providerEl.getSelectedKey().equals(spi.getId())) {
					pdfModule.setPdfServiceProvider(spi);
					found = true;
				}
			}
			
			if(!found) {
				pdfModule.setPdfServiceProvider(null);
			}
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
	}
}
