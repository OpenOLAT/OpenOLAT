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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.NewControllerFactory;
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
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.manager.PortfolioNotificationsHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HistoryController extends FormBasicController {
	
	private DateChooser dateChooser;

	private ContextualSubscriptionController cSubscriptionCtrl;
	
	private int counter;
	private Binder binder;
	private SubscriptionContext subsContext;
	private final BinderSecurityCallback secCallback;
	
	@Autowired
	private PortfolioNotificationsHandler notificationsHandler;
	 
	public HistoryController(UserRequest ureq, WindowControl wControl, BinderSecurityCallback secCallback, Binder binder) {
		super(ureq, wControl, "history");
		this.binder = binder;
		this.secCallback = secCallback;
		initForm(ureq);
		updateChangeLog();
		
		if (secCallback.canNewAssignment()) {
			// in template mode, add editor class to toolbar
			initialPanel.setCssClass("o_edit_mode");
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (!ureq.getUserSession().getRoles().isGuestOnly()) {			
			subsContext = new SubscriptionContext(PortfolioNotificationsHandler.TYPE_NAME, binder.getKey(), PortfolioNotificationsHandler.TYPE_NAME);
			if (subsContext != null) {
				String businessPath = "[Binder:" + binder.getKey() + "]";
				PublisherData data = new PublisherData(PortfolioNotificationsHandler.TYPE_NAME, null, businessPath);
				cSubscriptionCtrl = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, data);
				listenTo(cSubscriptionCtrl);
				flc.put("subscription", cSubscriptionCtrl.getInitialComponent());
			}
		}
		
		dateChooser = uifactory.addDateChooser("dateChooser", "changes.since", null, formLayout);
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.DATE, -8);
		dateChooser.setDate(cal.getTime());
		dateChooser.addActionListener(FormEvent.ONCHANGE);
	}
	
	protected void updateChangeLog() {
		Date date = dateChooser.getDate();
		List<SubscriptionListItem> items = notificationsHandler.getAllItems(binder, secCallback, date, getLocale());
		Formatter formatter = Formatter.getInstance(getLocale());
		
		List<SubscriptionListItemWrapper> wrappers = new ArrayList<>(items.size());
		for (SubscriptionListItem item:items) {
			String dateString = formatter.formatDate(item.getDate());
			String linkName = "subscrIL_" + (counter++);
			String linkLabel = StringHelper.escapeHtml(item.getDescription());
			FormLink link = uifactory.addFormLink(linkName, linkLabel, null, flc, Link.NONTRANSLATED);
			link.setUserObject(item.getBusinessPath());
			SubscriptionListItemWrapper bundle = new SubscriptionListItemWrapper(linkName, dateString, item.getIconCssClass());
			wrappers.add(bundle);
		}
		flc.contextPut("subscriptionItems", wrappers);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(dateChooser == source) {
			updateChangeLog();
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			Object uobject = link.getUserObject();
			if(uobject instanceof String) {
				String businessPath = (String)uobject;
				doSelectByContextEntry(ureq, businessPath);
			}	
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelectByContextEntry(UserRequest ureq, String businessPath) {
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	 
	public static class SubscriptionListItemWrapper {
		private String linkName;
		private String dateString;
		private String cssClass;

		public SubscriptionListItemWrapper(String linkName, String dateString, String cssClass) {
			this.linkName = linkName;
			this.dateString = dateString;
			this.cssClass = cssClass;
		}

		public String getDateString() {
			return dateString;
		}

		public String getLinkName() {
			return linkName;
		}

		public String getCssClass() {
			return cssClass;
		}
	}
}
