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
package org.olat.course.nodes.document.ui;

import static java.util.Arrays.stream;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.DocumentCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 30 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocumentLayoutController extends FormBasicController {
	
	private static final String[] keys = new String[]{ DeliveryOptions.CONFIG_HEIGHT_AUTO, "460", "480", 
			"500", "520", "540", "560", "580",
			"600", "620", "640", "660", "680",
			"700", "720", "730", "760", "780",
			"800", "820", "840", "860", "880",
			"900", "920", "940", "960", "980",
			"1000", "1020", "1040", "1060", "1080",
			"1100", "1120", "1140", "1160", "1180",
			"1200", "1220", "1240", "1260", "1280",
			"1300", "1320", "1340", "1360", "1380"
	};
	
	private SingleSelection heightEl;

	private final ModuleConfiguration moduleConfig;

	public DocumentLayoutController(UserRequest ureq, WindowControl wControl, DocumentCourseNode courseNode) {
		super(ureq, wControl);
		moduleConfig = courseNode.getModuleConfiguration();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] values = new String[]{ translate("config.height.auto"), "460px", "480px", 
				"500px", "520px", "540px", "560px", "580px",
				"600px", "620px", "640px", "660px", "680px",
				"700px", "720px", "730px", "760px", "780px",
				"800px", "820px", "840px", "860px", "880px",
				"900px", "920px", "940px", "960px", "980px",
				"1000px", "1020px", "1040px", "1060px", "1080px",
				"1100px", "1120px", "1140px", "1160px", "1180px",
				"1200px", "1220px", "1240px", "1260px", "1280px",
				"1300px", "1320px", "1340px", "1360px", "1380px"
		};
		heightEl = uifactory.addDropdownSingleselect("config.height", "config.height", formLayout, keys, values, null);
		heightEl.addActionListener(FormEvent.ONCHANGE);
		String currentHight = moduleConfig.getStringValue(DocumentCourseNode.CONFIG_KEY_HEIGHT, DocumentCourseNode.CONFIG_HEIGHT_AUTO);
		if (stream(keys).anyMatch(key -> key.equals(currentHight))) {
			heightEl.select(currentHight, true);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == heightEl) {
			doSetLayoutConfigs(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doSetLayoutConfigs(UserRequest ureq) {
		if (heightEl.isOneSelected()) {
			String height = heightEl.getSelectedKey();
			moduleConfig.setStringValue(DocumentCourseNode.CONFIG_KEY_HEIGHT, height);
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
