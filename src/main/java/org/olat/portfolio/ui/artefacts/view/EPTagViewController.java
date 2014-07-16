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
package org.olat.portfolio.ui.artefacts.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Show the tags read-only
 * 
 * 
 * Initial date: 16.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EPTagViewController extends FormBasicController {
	
	private List<String> tags;
	@Autowired
	private EPFrontendManager ePFMgr;

	public EPTagViewController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		tags = ePFMgr.getArtefactTags(artefact);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("tags.view.header");
		
		Map<String,String> tagsMap = new HashMap<>();
		for(String tag:tags) {
			tagsMap.put(tag, tag);
		}
		
		TextBoxListElement tagListElement = uifactory.addTextBoxListElement("artefact.tags", null, "tag.input.hint", tagsMap, formLayout, getTranslator());
		tagListElement.setEnabled(false);
	}

	@Override
	protected void doDispose() {
		//nothing
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//do nothing
	}
}