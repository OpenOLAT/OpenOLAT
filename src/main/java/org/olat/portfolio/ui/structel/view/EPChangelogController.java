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

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.manager.EPNotificationsHandler;
import org.olat.portfolio.manager.EPNotificationsHelper;
import org.olat.portfolio.model.structel.EPAbstractMap;
import org.olat.portfolio.model.structel.EPDefaultMap;
import org.olat.portfolio.model.structel.EPMapShort;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.EPStructuredMapTemplate;
import org.olat.portfolio.ui.structel.EPMapKeyEvent;
import org.olat.portfolio.ui.structel.EPStructureEvent;

/**
 * 
 * Displays a changelog for the current EPMap<br />
 * ( this is the "Changelog/Änderungsprotokoll" Tab in a EPMap )
 * 
 * @author strentini, sergio.trentini@frentix.com, http://www.frentix.com
 * 
 */
public class EPChangelogController extends FormBasicController {

	private static final Logger logger = Tracing.createLoggerFor(EPChangelogController.class);

	private ContextualSubscriptionController cSubscriptionCtrl;
	private SubscriptionContext subsContext;
	private EPAbstractMap map;
	private DateChooser dateChooser;
	private final EPFrontendManager ePFMgr;

	public EPChangelogController(UserRequest ureq, WindowControl wControl, EPAbstractMap map) {
		super(ureq, wControl, "changelog");
		
		ePFMgr = CoreSpringFactory.getImpl(EPFrontendManager.class);

		this.map = map;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// flc.contextPut("locale", getLocale());

		/* the subscription context + component */
		if (logger.isDebugEnabled())
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
		dateChooser.addActionListener(FormEvent.ONCHANGE);

		/* display the changelog */
		updateChangelogDisplay();
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.form.flexible.FormItem,
	 *      org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == dateChooser && !dateChooser.hasError()) {
			updateChangelogDisplay();
		} else if (source instanceof FormLink) {
			fireEvent(ureq, new EPMapKeyEvent(EPStructureEvent.SELECT, getKeyFromFormLink((FormLink) source)));
		}
	}

	/**
	 * gets the userObject from the given FormLink and tries to parse it as
	 * Long. returns 0L if userObject cannot be parsed to Long
	 * 
	 * @param link
	 * @return
	 */
	private Long getKeyFromFormLink(FormLink link) {
		Long mapKeyFromLink = 0L;
		try {
			Object userObject = link.getUserObject();
			if (userObject != null) {
				mapKeyFromLink = Long.parseLong(userObject.toString());
			}
		} catch (NumberFormatException e) {
			// key is not Long, ignore and return 0
		}
		return mapKeyFromLink;
	}

	/**
	 * update the changelog-list according to selected date. this method is
	 * invoked on initForm and again when user changes date in dateChooser
	 */
	private void updateChangelogDisplay() {
		// init the helper;
		String path = getWindowControl().getBusinessControl().getAsString();
		EPNotificationsHelper helper = new EPNotificationsHelper(path, getLocale());

		// get the date from the dateChooser component
		Date compareDate = dateChooser.getDate();
		EPMapShort mapShort = ePFMgr.loadMapShortByResourceId(map.getOlatResource().getResourceableId());
		List<SubscriptionListItem> allItems = new ArrayList<>(0);
		// get subscriptionListItems according to map type
		if (map instanceof EPDefaultMap || map instanceof EPStructuredMapTemplate) {
			allItems = helper.getAllSubscrItemsDefault(compareDate, mapShort);
		} else if (map instanceof EPStructuredMap) {
			allItems = helper.getAllSubscrItemsStructured(compareDate, mapShort);
		}

		List<SubscriptionItemBundle> bundles = getItemBundlesForSubscriptionItems(allItems);
		flc.contextPut("subscriptionItems", bundles);
	}

	/**
	 * 
	 * @param subscriptionItems
	 * @return
	 */
	private List<SubscriptionItemBundle> getItemBundlesForSubscriptionItems(List<SubscriptionListItem> subscriptionItems) {
		List<SubscriptionItemBundle> bundles = new ArrayList<>();
		Formatter f = Formatter.getInstance(getTranslator().getLocale());

		for (int i = 0; i < subscriptionItems.size(); i++) {
			SubscriptionListItem listItem = subscriptionItems.get(i);
			SubscriptionItemBundle bundle = new SubscriptionItemBundle();
			bundle.setDateString(f.formatDate(listItem.getDate()));
			String linkName = "subscrIL_" + i;
			bundle.setLinkName(linkName);
			String text = listItem.getDescription();
			FormLink link = uifactory.addFormLink(linkName, text, null, flc, Link.NONTRANSLATED);
			link.setUserObject(listItem.getUserObject());
			bundle.setCssClass(listItem.getIconCssClass());
			bundles.add(bundle);
		}
		return bundles;
	}

	@Override
	protected void doDispose() {
		// cSubscriptionCtrl gets disposed
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to do
	}

	/**
	 * 
	 * @author strentini
	 * 
	 */
	public class SubscriptionItemBundle {
		private String linkName;
		private String dateString;
		private String cssClass;

		public void setDateString(String dateString) {
			this.dateString = dateString;
		}

		public String getDateString() {
			return dateString;
		}

		public void setLinkName(String linkName) {
			this.linkName = linkName;
		}

		public String getLinkName() {
			return linkName;
		}

		public void setCssClass(String cssClass) {
			this.cssClass = cssClass;
		}

		public String getCssClass() {
			return cssClass;
		}
	}

	public void refreshNewsList() {
		updateChangelogDisplay();
	}
}
