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
package org.olat.repository.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryAdminConfigurationController extends FormBasicController {

	private static final String NOTIFICATION_REPOSITORY_STATUS_CHANGED = "notification.repository.status.changed";
	private static final String[] keys = {"on"};
	private static final String[] leaveKeys = {
			RepositoryEntryAllowToLeaveOptions.atAnyTime.name(),
			RepositoryEntryAllowToLeaveOptions.afterEndDate.name(),
			RepositoryEntryAllowToLeaveOptions.never.name()
	};

	private FormLink enableAllSubscribersLink;
	private FormLink disableAllSubscribersLink;
	private SingleSelection leaveEl;
	private MultipleSelectionElement ratingEl;
	private MultipleSelectionElement membershipEl;
	private MultipleSelectionElement commentEl;
	private MultipleSelectionElement myCourseSearchEl;
	private MultipleSelectionElement taxonomyEl;
	private MultipleSelectionElement notificationEl;

	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private NotificationsManager notificationsManager;
	
	public RepositoryAdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer searchCont = FormLayoutContainer.createDefaultFormLayout("search", getTranslator());
		searchCont.setFormContextHelp("manual_admin/administration/Modules_Repository/");
		searchCont.setFormTitle(translate("repository.admin.title"));
		formLayout.add(searchCont);
		searchCont.setRootForm(mainForm);

		boolean searchEnabled = repositoryModule.isMyCoursesSearchEnabled();
		String[] values = new String[] { translate("on") };
		myCourseSearchEl = uifactory.addCheckboxesHorizontal("my.course.search.enabled", searchCont, keys, values);
		myCourseSearchEl.addActionListener(FormEvent.ONCHANGE);
		myCourseSearchEl.select(keys[0], searchEnabled);
		
		boolean commentEnabled = repositoryModule.isCommentEnabled();
		commentEl = uifactory.addCheckboxesHorizontal("my.course.comment.enabled", searchCont, keys, values);
		commentEl.addActionListener(FormEvent.ONCHANGE);
		commentEl.select(keys[0], commentEnabled);
		
		boolean ratingEnabled = repositoryModule.isRatingEnabled();
		ratingEl = uifactory.addCheckboxesHorizontal("my.course.rating.enabled", searchCont, keys, values);
		ratingEl.addActionListener(FormEvent.ONCHANGE);
		ratingEl.select(keys[0], ratingEnabled);

		boolean requestMembershipEnabled = repositoryModule.isRequestMembershipEnabled();
		membershipEl = uifactory.addCheckboxesHorizontal("rentry.request.membership", searchCont, keys, values);
		membershipEl.addActionListener(FormEvent.ONCHANGE);
		membershipEl.select(keys[0], requestMembershipEnabled);
		
		SelectionValues taxonomySV = new SelectionValues();
		taxonomyService.getTaxonomyList().forEach(
				taxonomy -> taxonomySV.add(entry(
						taxonomy.getKey().toString(), 
						taxonomy.getDisplayName())));
		taxonomyEl = uifactory.addCheckboxesVertical("selected.taxonomy.tree", searchCont, taxonomySV.keys(), taxonomySV.values(), 1);
		repositoryModule.getTaxonomyRefs().forEach(taxonomy -> taxonomyEl.select(taxonomy.getKey().toString(), true));
		taxonomyEl.addActionListener(FormEvent.ONCHANGE);
		
		// Leave
		FormLayoutContainer leaveCont = FormLayoutContainer.createDefaultFormLayout("leave", getTranslator());
		leaveCont.setFormTitle(translate("repository.admin.leave.title"));
		formLayout.add(leaveCont);
		leaveCont.setRootForm(mainForm);
		
		String[] leaveValues = new String[] {
				translate("rentry.leave.atanytime"),
				translate("rentry.leave.afterenddate"),
				translate("rentry.leave.never")
		};
		leaveEl = uifactory.addDropdownSingleselect("leave.courses", "repository.admin.leave.label", leaveCont, leaveKeys, leaveValues, null);
		leaveEl.addActionListener(FormEvent.ONCHANGE);
		RepositoryEntryAllowToLeaveOptions leaveOption = repositoryModule.getAllowToLeaveDefaultOption();
		if(leaveOption != null) {
			leaveEl.select(leaveOption.name(), true);
		} else {
			leaveEl.select(RepositoryEntryAllowToLeaveOptions.atAnyTime.name(), true);
		}

		FormLayoutContainer notificationCont = FormLayoutContainer.createDefaultFormLayout("notification", getTranslator());
		notificationCont.setFormTitle(translate("repository.admin.notification.title"));
		notificationCont.setFormInfo(translate("repository.admin.notification.desc"));
		formLayout.add(notificationCont);
		notificationCont.setRootForm(mainForm);

		String[] notiKeys = new String[]{
				NOTIFICATION_REPOSITORY_STATUS_CHANGED
		};

		String[] notiValues = new String[]{
				translate("repository.admin.notification")
		};

		boolean statusChangedNotificationEnabled = repositoryModule.isRepoStatusChangedNotificationEnabledDefault();
		notificationEl = uifactory.addCheckboxesVertical("repository.admin.notification.label", notificationCont, notiKeys, notiValues, 1);
		notificationEl.addActionListener(FormEvent.ONCHANGE);
		notificationEl.select(NOTIFICATION_REPOSITORY_STATUS_CHANGED, statusChangedNotificationEnabled);

		// TODO Darstellung inaktiver abos

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		notificationCont.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		uifactory.addStaticTextElement("maintButtonLabel", getTranslator().translate("repository.admin.subscribers"), buttonsCont);
		enableAllSubscribersLink = uifactory.addFormLink("repository.admin.enable.all.subscribers", buttonsCont, Link.BUTTON);
		disableAllSubscribersLink = uifactory.addFormLink("repository.admin.disable.all.subscribers", buttonsCont, Link.BUTTON);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(myCourseSearchEl == source) {
			boolean on = !myCourseSearchEl.getSelectedKeys().isEmpty();
			repositoryModule.setMyCoursesSearchEnabled(on);
			getWindowControl().setInfo("saved");
		} else if(commentEl == source) {
			boolean on = !commentEl.getSelectedKeys().isEmpty();
			repositoryModule.setCommentEnabled(on);
			getWindowControl().setInfo("saved");
		} else if(ratingEl == source) {
			boolean on = !ratingEl.getSelectedKeys().isEmpty();
			repositoryModule.setRatingEnabled(on);
			getWindowControl().setInfo("saved");
		} else if(membershipEl == source) {
			boolean on = !membershipEl.getSelectedKeys().isEmpty();
			repositoryModule.setRequestMembershipEnabled(on);
			getWindowControl().setInfo("saved");
		} else if (notificationEl == source) {
			repositoryModule.setRepoStatusChangedNotificationEnabledDefault(notificationEl.isKeySelected(NOTIFICATION_REPOSITORY_STATUS_CHANGED));
			getWindowControl().setInfo("saved");
		} else if (taxonomyEl == source) {
			List<TaxonomyRef> taxonomyRefs = taxonomyEl.getSelectedKeys().stream()
					.map(Long::valueOf)
					.map(TaxonomyRefImpl::new).
					collect(Collectors.toList());
			repositoryModule.setTaxonomyRefs(taxonomyRefs);
			getWindowControl().setInfo("saved");
		} else if(leaveEl == source) {
			String selectedOption = leaveEl.getSelectedKey();
			RepositoryEntryAllowToLeaveOptions option = RepositoryEntryAllowToLeaveOptions.valueOf(selectedOption);
			repositoryModule.setAllowToLeaveDefaultOption(option);
			getWindowControl().setInfo("saved");
		} else if (enableAllSubscribersLink == source
				|| disableAllSubscribersLink == source) {
			SubscriptionContext subContext = repositoryService.getSubscriptionContext();
			PublisherData publisherData = repositoryService.getPublisherData();
			Publisher publisher = notificationsManager.getOrCreatePublisher(subContext, publisherData);
			notificationsManager.updateAllSubscribers(publisher, enableAllSubscribersLink == source);
			getWindowControl().setInfo("saved");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//lifecycleAdminCtrl.formOK(ureq);
	}
}