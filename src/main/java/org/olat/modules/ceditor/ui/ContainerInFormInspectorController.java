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
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.ContainerElement;
import org.olat.modules.ceditor.model.ContainerLayout;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.ui.MediaUIHelper;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContainerInFormInspectorController extends FormBasicController implements PageElementInspectorController {

	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private int count = 0;
	private ContainerElement container;
	private final PageElementStore<ContainerElement> store;
	private final List<FormLink> layoutLinks = new ArrayList<>();
	private TextElement nameEl;

	@Autowired
	private DB dbInstance;

	public ContainerInFormInspectorController(UserRequest ureq, WindowControl wControl, ContainerElement container,
											  PageElementStore<ContainerElement> store) {
		super(ureq, wControl, "tabs_inspector");
		this.container = container;
		this.store = store;
		initForm(ureq);
	}
	
	@Override
	public String getTitle() {
		if (container.getContainerSettings() != null &&
				StringHelper.containsNonWhitespace(container.getContainerSettings().getName())) {
			return container.getContainerSettings().getName();
		}
		return translate("container.name");
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof ContainerEditorController &&
				event instanceof ChangePartEvent changePartEvent &&
				changePartEvent.getElement() instanceof ContainerElement containerElement) {
			if (changePartEvent.isElement(container)) {
				this.container = containerElement;
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);

		addLayoutTab(formLayout);
		addNameTab(formLayout);
	}

	private void addLayoutTab(FormItemContainer formLayout) {
		String page = velocity_root + "/container_inspector.html";
		FormLayoutContainer layoutCont = FormLayoutContainer.createCustomFormLayout("layout", getTranslator(), page);
		formLayout.add(layoutCont);
		tabbedPane.addTab(getTranslator().translate("tab.layout"), layoutCont);

		layoutLinks.clear();

		ContainerLayout activeLayout = getContainerSettings().getType();

		for (ContainerLayout layout:ContainerLayout.values()) {
			if (layout.deprecated() && layout != activeLayout) {
				continue;
			}

			String id = "add." + (++count);
			String pseudoIcon = layout.pseudoIcons();
			FormLink layoutLink = uifactory.addFormLink(id, pseudoIcon, null, layoutCont, Link.LINK | Link.NONTRANSLATED);
			if (activeLayout == layout) {
				layoutLink.setElementCssClass("active");
			}
			layoutLink.setUserObject(layout);
			layoutLinks.add(layoutLink);
		}

		layoutCont.contextPut("layouts", layoutLinks);
	}

	private ContainerSettings getContainerSettings() {
		if (container.getContainerSettings() != null) {
			return container.getContainerSettings();
		}
		return new ContainerSettings();
	}

	private void addNameTab(FormItemContainer formLayout) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("name", getTranslator());
		formLayout.add(layoutCont);
		tabbedPane.addTab(getTranslator().translate("container.name"), layoutCont);

		String name = getContainerSettings().getName();
		nameEl = uifactory.addTextElement("container.name", null, 128, name, layoutCont);
		nameEl.setPlaceholderKey("untitled", new String[] {""});
		nameEl.addActionListener(FormEvent.ONCHANGE);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if (!(fiSrc instanceof TextElement)) {
			super.propagateDirtinessToContainer(fiSrc, fe);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink formLink && formLink.getUserObject() instanceof ContainerLayout containerLayout) {
			doSetLayout(ureq, containerLayout);
		} else if (source == nameEl) {
			doSetName(ureq);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
	}

	private void doSetName(UserRequest ureq) {
		ContainerSettings containerSettings = getContainerSettings();
		containerSettings.setName(nameEl.getValue());
		container.setLayoutOptions(ContentEditorXStream.toXml(containerSettings));
		container = store.savePageElement(container);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(container));
	}

	private void doSetLayout(UserRequest ureq, ContainerLayout newLayout) {
		ContainerSettings settings = getContainerSettings();
		settings.updateType(newLayout);
		container.setLayoutOptions(ContentEditorXStream.toXml(settings));
		container = store.savePageElement(container);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(container));
		
		for (FormLink layoutLink: layoutLinks) {
			boolean active = layoutLink.getUserObject() == newLayout;
			layoutLink.setElementCssClass(active ? "active" : "");
		}
	}
}
