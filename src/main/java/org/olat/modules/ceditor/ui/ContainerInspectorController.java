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
package org.olat.modules.ceditor.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.DB;
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
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.ContainerElement;
import org.olat.modules.ceditor.model.ContainerLayout;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContainerInspectorController extends FormBasicController implements PageElementInspectorController {
	
	private int count = 0;
	private ContainerElement container;
	private final PageElementStore<ContainerElement> store;
	private final List<FormLink> layoutLinks = new ArrayList<>();
	
	@Autowired
	private DB dbInstance;
	
	public ContainerInspectorController(UserRequest ureq, WindowControl wControl, ContainerElement container,
			PageElementStore<ContainerElement> store) {
		super(ureq, wControl, "container_inspector");
		this.container = container;
		this.store = store;
		
		initForm(ureq);
	}
	
	@Override
	public String getTitle() {
		return translate("inspector.layout");
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof ContainerEditorController && event instanceof ChangePartEvent) {
			ChangePartEvent cpe = (ChangePartEvent)event;
			if(cpe.isElement(container)) {
				container = (ContainerElement)cpe.getElement();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		layoutLinks.clear();
		
		ContainerLayout activeLayout = container.getContainerSettings().getType();
		
		for(ContainerLayout layout:ContainerLayout.values()) {
			if(layout.deprecated() && layout != activeLayout) {
				continue;
			}
			
			String id = "add." + (++count);
			String pseudoIcon = layout.pseudoIcons();
			FormLink layoutLink = uifactory.addFormLink(id, pseudoIcon, null, formLayout, Link.LINK | Link.NONTRANSLATED);
			if(activeLayout == layout) {
				layoutLink.setElementCssClass("active");
			}
			layoutLink.setUserObject(layout);
			layoutLinks.add(layoutLink);
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("layouts", layoutLinks);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink && ((FormLink)source).getUserObject() instanceof ContainerLayout) {
			ContainerLayout newLayout = (ContainerLayout)((FormLink)source).getUserObject();
			doSetLayout(ureq, newLayout);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSetLayout(UserRequest ureq, ContainerLayout newLayout) {
		ContainerSettings settings = container.getContainerSettings();
		settings.updateType(newLayout);
		container.setLayoutOptions(ContentEditorXStream.toXml(settings));
		container = store.savePageElement(container);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(container));
		
		for(FormLink layoutLink: layoutLinks) {
			boolean active = layoutLink.getUserObject() == newLayout;
			layoutLink.setElementCssClass(active ? "active" : "");
		}
	}
}
