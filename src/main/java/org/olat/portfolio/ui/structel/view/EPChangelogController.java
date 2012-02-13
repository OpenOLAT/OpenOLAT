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
package org.olat.portfolio.ui.structel.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.notifications.ContextualSubscriptionController;
import org.olat.core.util.notifications.PublisherData;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.notifications.items.SubscriptionListItem;
import org.olat.portfolio.manager.EPNotificationsHandler;
import org.olat.portfolio.manager.EPNotificationsHelper;
import org.olat.portfolio.model.structel.EPAbstractMap;
import org.olat.portfolio.model.structel.EPDefaultMap;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.EPStructuredMapTemplate;

/**
 * 
 * Displays a changelog for the current EPMap<br />
 * ( this is the "Changelog/Änderungsprotokoll" Tab in a EPMap )
 * 
 * @author strentini, sergio.trentini@frentix.com, http://www.frentix.com
 * 
 */
public class EPChangelogController extends FormBasicController {

	private static OLog logger = Tracing.createLoggerFor(EPChangelogController.class);

	private ContextualSubscriptionController cSubscriptionCtrl;
	private SubscriptionContext subsContext;
	private EPAbstractMap map;
	private DateChooser dateChooser;

	public EPChangelogController(UserRequest ureq, WindowControl wControl, EPAbstractMap map) {
		super(ureq, wControl, "changelog");

		this.map = map;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("locale", getLocale());

		/* the subscription context + component */
		if (logger.isDebug())
			logger.debug("creating subscriptionContext for Map: " + map.getTitle() + ", getResourceableId: ->" + map.getResourceableId() + ", key: "
					+ map.getKey());
		subsContext = new SubscriptionContext(EPNotificationsHandler.TYPENNAME, map.getResourceableId(), EPNotificationsHandler.TYPENNAME);
		if (subsContext != null) {
			String businessPath = "[EPDefaultMap:" + map.getKey() + "]";
			PublisherData data = new PublisherData(EPNotificationsHandler.TYPENNAME, null, businessPath);
			cSubscriptionCtrl = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, data);
			listenTo(cSubscriptionCtrl);
			flc.put("subscription", cSubscriptionCtrl.getInitialComponent());
		}

		/* the datechooser */
		dateChooser = uifactory.addDateChooser("dateChooser", "news.since", null, formLayout);
		dateChooser.setDate(new Date());
		dateChooser.addActionListener(this, FormEvent.ONCHANGE);

		/* display the changelog */
		updateChangelogDisplay(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.form.flexible.FormItem,
	 *      org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == dateChooser && !dateChooser.hasError()) {
			updateChangelogDisplay(ureq);
		}
	}

	/**
	 * update the changelog-list according to selected date. this method is
	 * invoked on initForm and again when user changes date in dateChooser
	 */
	private void updateChangelogDisplay(UserRequest ureq) {
		// init the helper;
		String path = getWindowControl().getBusinessControl().getAsString();
		EPNotificationsHelper helper = new EPNotificationsHelper(path, getLocale(), ureq.getIdentity());

		// get the date from the dateChooser component
		Date compareDate = dateChooser.getDate();

		List<SubscriptionListItem> allItems = new ArrayList<SubscriptionListItem>(0);
		// get subscriptionListItems according to map type
		if (map instanceof EPDefaultMap || map instanceof EPStructuredMapTemplate) {
			allItems = helper.getAllSubscrItems_Default(compareDate, (EPDefaultMap) map);
		} else if (map instanceof EPStructuredMap) {
			allItems = helper.getAllSubscrItems_Structured(compareDate, (EPStructuredMap) map);
		}

		flc.contextPut("subscriptionListItems", allItems);
	}

	@Override
	protected void doDispose() {
		//cSubscriptionCtrl gets disposed	
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do
	}

}
