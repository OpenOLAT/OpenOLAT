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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.portfolio.manager.EPFrontendManager;

/**
 * 
 * Description:<br>
 * Allows to set the attributes which then will be displayed for an artefact.
 * settings are persisted as property.
 * 
 * <P>
 * Initial Date:  13.07.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPArtefactAttributeSettingController extends FormBasicController {

	private Map<String, Boolean> artAttribConfig;
	private EPFrontendManager ePFMgr;

	public EPArtefactAttributeSettingController(UserRequest ureq, WindowControl wControl, Map<String, Boolean> artAttribConfig) {
		super(ureq, wControl);
		this.artAttribConfig = artAttribConfig;
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("display.option.intro");

		String[] keys = new String[] { "onoff" };
		String[] values = new String[] { translate("display.option.enabled") };
		Map<String, Boolean> allArtAttribs = ePFMgr.getArtefactAttributeConfig(null);
		for (Iterator<Entry<String, Boolean>> iterator = allArtAttribs.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Boolean> entry = iterator.next();
			String attKey = entry.getKey();
			Boolean attVal = artAttribConfig.get(attKey);			
			MultipleSelectionElement chkBox = uifactory.addCheckboxesHorizontal(attKey, formLayout, keys, values);
			chkBox.addActionListener(FormEvent.ONCHANGE );
			if (attVal == null) attVal = entry.getValue(); // either use users settings or the defaults
			chkBox.select(keys[0], attVal);
		}
		uifactory.addFormSubmitButton("display.option.submit", formLayout);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.form.flexible.FormItem,
	 *      org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof MultipleSelectionElement){
			MultipleSelectionElement chkBox = (MultipleSelectionElement) source;
			artAttribConfig.put(chkBox.getName(), chkBox.isSelected(0));
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//fire event to close overlay and update the displayed
		// artefacts
		ePFMgr.setArtefactAttributeConfig(getIdentity(), artAttribConfig);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void doDispose() {
		// nothing to dispose
	}

}
