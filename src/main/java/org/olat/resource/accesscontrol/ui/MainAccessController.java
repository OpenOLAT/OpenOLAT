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

package org.olat.resource.accesscontrol.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;

/**
 * 
 * Description:<br>
 * A simple step to choose the way to access a resource if
 * several methods are available
 * 
 * <P>
 * Initial Date:  27 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MainAccessController extends FormBasicController {
	
	private FormLink backLink;
	private final List<OfferAccess> links;
	private final List<FormLink> accessButtons = new ArrayList<>();
	private final AccessControlModule acModule;
	private final ACService acService;
	
	private FormController accessCtrl;

	public MainAccessController(UserRequest ureq, WindowControl wControl, List<OfferAccess> links) {
		super(ureq, wControl, "choose_access_method");
		
		this.links = links;
		acModule = (AccessControlModule)CoreSpringFactory.getBean("acModule");
		acService = CoreSpringFactory.getImpl(ACService.class);
			
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		backLink = uifactory.addFormLink("back", formLayout, Link.LINK_BACK);
		
		FormLayoutContainer methodChooseContainer = FormLayoutContainer.createDefaultFormLayout("methodChooser", getTranslator());
		methodChooseContainer.setRootForm(mainForm);
		formLayout.add("methodChooser", methodChooseContainer);

		for(OfferAccess link:links) {
			AccessMethodHandler handler = acModule.getAccessMethodHandler(link.getMethod().getType());
			String methodName = handler.getMethodName(getLocale());
			FormLink accessButton = uifactory.addFormLink("m_" + link.getKey(), methodName, null, methodChooseContainer, Link.BUTTON + Link.NONTRANSLATED);
			accessButton.setUserObject(link);
			accessButtons.add(accessButton);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == accessCtrl) {
			if(event instanceof AccessEvent) {
				if(event.equals(AccessEvent.ACCESS_OK_EVENT)) {
					fireEvent(ureq, AccessEvent.ACCESS_OK_EVENT);
				} else {
					String msg = ((AccessEvent)event).getMessage();
					if(StringHelper.containsNonWhitespace(msg)) {
						getWindowControl().setError(msg);
					} else {
						showError("error.accesscontrol");
					}
				}
			}
			flc.remove(accessCtrl.getInitialFormItem());
			removeAsListenerAndDispose(accessCtrl);
			accessCtrl = null;
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(backLink == source) {
			if(accessCtrl != null) {
				flc.remove(accessCtrl.getInitialComponent());
				removeAsListenerAndDispose(accessCtrl);
				accessCtrl = null;
			}
		} else if(accessButtons.contains(source)) {
			methodChoosed(ureq, (OfferAccess)source.getUserObject());
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	protected void methodChoosed(UserRequest ureq, OfferAccess link) {
		if(link.getMethod().isNeedUserInteraction()) {
			AccessControlModule module = (AccessControlModule)CoreSpringFactory.getBean("acModule");
			AccessMethodHandler handler = module.getAccessMethodHandler(link.getMethod().getType());
			if(handler != null) {
				accessCtrl = handler.createAccessController(ureq, getWindowControl(), link, mainForm);
				listenTo(accessCtrl);
				flc.add("accessCmp", accessCtrl.getInitialFormItem());
			}
		} else {
			doAccessResource(ureq, link);
		}
	}
	
	protected void doAccessResource(UserRequest ureq, OfferAccess link) {
		AccessResult result = acService.accessResource(getIdentity(), link, null);
		if(result.isAccessible()) {
			fireEvent(ureq, AccessEvent.ACCESS_OK_EVENT);
		} else {
			fireEvent(ureq, new AccessEvent(AccessEvent.ACCESS_FAILED));
			showError("error.accesscontrol");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
