/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.author;

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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthoringEditRuntimeTypeController extends FormBasicController {

	private FormLink changeRuntimeTypeButton;

	private RepositoryEntry entry;
	private final boolean readOnly;
	
	private CloseableModalController cmc;
	private EditRuntimeTypeController editRuntimeTypeCtrl;
	
	@Autowired
	private RepositoryService repositoryService;
	
	public AuthoringEditRuntimeTypeController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		this.entry = entry;
		this.readOnly = readOnly;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("details.runtime.type");
		formLayout.setElementCssClass("o_ac_runtime_type_form");
		
		String page = velocity_root +"/runtimetype.html";
		FormLayoutContainer changeRuntimeCont = uifactory.addCustomFormLayout("runtime", "change.runtime.type.label", page, formLayout);
		
		RepositoryEntryRuntimeType type = entry.getRuntimeType();
		String iconCss = (type == RepositoryEntryRuntimeType.standalone) ? "o_icon_people" : "o_icon_link";
		changeRuntimeCont.contextPut("iconCss", iconCss);
		changeRuntimeCont.contextPut("title", translate("runtime.type." + type.name() + ".title"));
		changeRuntimeCont.contextPut("text", translate("runtime.type." + type.name() + ".desc"));
		
		changeRuntimeTypeButton = uifactory.addFormLink("change.runtime.type", "change.run", "change", null, changeRuntimeCont, Link.LINK);
		changeRuntimeTypeButton.setIconLeftCSS("o_icon o_icon_edit");
		changeRuntimeTypeButton.setVisible(!readOnly);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editRuntimeTypeCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				entry = editRuntimeTypeCtrl.getRepositoryEntry();
			}
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editRuntimeTypeCtrl);
		removeAsListenerAndDispose(cmc);
		editRuntimeTypeCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(changeRuntimeTypeButton == source) {
			doChangeRuntimeType(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doChangeRuntimeType(UserRequest ureq) {
		entry = repositoryService.loadByKey(entry.getKey());
		editRuntimeTypeCtrl = new EditRuntimeTypeController(ureq, getWindowControl(), entry);
		listenTo(editRuntimeTypeCtrl);
		
		String title = translate("change.runtime.type.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editRuntimeTypeCtrl.getInitialComponent(),
				true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}
