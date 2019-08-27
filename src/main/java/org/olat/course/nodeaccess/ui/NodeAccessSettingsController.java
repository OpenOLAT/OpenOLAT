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
package org.olat.course.nodeaccess.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodeaccess.NodeAccessProviderIdentifier;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NodeAccessSettingsController extends FormBasicController {
	
	private SingleSelection nodeAccessEl;
	
	private final String nodeAccessType;
	
	@Autowired
	private NodeAccessService nodeAccessService;
	
	public NodeAccessSettingsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl);
		ICourse course = CourseFactory.loadCourse(entry);
		this.nodeAccessType = course.getCourseConfig().getNodeAccessType();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("settings.title");
		
		KeyValues nodeAccessKV = new KeyValues();
		for (NodeAccessProviderIdentifier identifier : nodeAccessService.getNodeAccessProviderIdentifer()) {
			nodeAccessKV.add(KeyValues.entry(identifier.getType(), identifier.getDisplayName(getLocale())));
		}
		nodeAccessEl = uifactory.addDropdownSingleselect("settings.type", "settings.type", formLayout,
				nodeAccessKV.keys(), nodeAccessKV.values());
		nodeAccessEl.setEnabled(false);
		nodeAccessEl.select(nodeAccessType, true);
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
