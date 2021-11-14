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
package org.olat.modules.qpool.ui;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.ui.events.QPoolEvent;

/**
 * 
 * Initial date: 21.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ShareItemOptionController extends FormBasicController {

	private final String[] keys = {"yes","no"};
	private SingleSelection editableEl;
	
	private final QPoolService qpoolService;
	
	private final List<Pool> pools;
	private final List<BusinessGroup> groups;
	private final List<QuestionItemShort> items;
	
	public ShareItemOptionController(UserRequest ureq, WindowControl wControl,
			List<QuestionItemShort> items, List<BusinessGroup> groups, List<Pool> pools) {	
		super(ureq, wControl, "share_options");
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		
		this.pools = pools;
		this.items = items;
		this.groups = groups;
		
		initForm(ureq);
	}
	
	public List<QuestionItemShort> getItems() {
		return items;
	}
	
	public List<Pool> getPools() {
		return pools;
	}
	
	public List<BusinessGroup> getGroups() {
		return groups;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		StringBuilder names = new StringBuilder();
		if(groups != null && !groups.isEmpty()) {
			for(BusinessGroup group:groups) {
				if(names.length() > 0) names.append(", ");
				names.append(group.getName());
			}
		}
		if(pools != null && !pools.isEmpty()) {
			for(Pool pool:pools) {
				if(names.length() > 0) names.append(", ");
				names.append(pool.getName());
			}
		}

		if(formLayout instanceof FormLayoutContainer) {
			((FormLayoutContainer)formLayout).contextPut("shares", names.toString());
		}

		FormLayoutContainer mailCont = FormLayoutContainer.createDefaultFormLayout("editable", getTranslator());
		formLayout.add(mailCont);
		String[] values = new String[]{
				translate("yes"),
				translate("no")
		};
		editableEl = uifactory.addRadiosVertical("share.editable", "share.editable", mailCont, keys, values);
		editableEl.select("no", true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean editable = editableEl.isOneSelected() && editableEl.isSelected(0);
		if(groups != null && !groups.isEmpty()) {
			qpoolService.shareItemsWithGroups(items, groups, editable);
		}
		if(pools != null && !pools.isEmpty()) {
			qpoolService.addItemsInPools(items, pools, editable);
		}
		fireEvent(ureq, new QPoolEvent(QPoolEvent.ITEM_SHARED));
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
