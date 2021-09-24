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
package org.olat.group.ui.run;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.olat.basesecurity.GroupRoles;
import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.commons.info.InfoSubscriptionManager;
import org.olat.commons.info.manager.MailFormatter;
import org.olat.commons.info.ui.InfoDisplayController;
import org.olat.commons.info.ui.InfoSecurityCallback;
import org.olat.commons.info.ui.SendInfoMailFormatter;
import org.olat.commons.info.ui.SendMailOption;
import org.olat.commons.info.ui.SendSubscriberMailOption;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.UserSession;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 15.03.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class InfoGroupRunController extends BasicController {
	
	public static final String resSubPath = InfoMessageFrontendManager.businessGroupResSubPath;

	private final VelocityContainer runVC;
	private final InfoDisplayController infoDisplayController;
	private ContextualSubscriptionController subscriptionController;
	
	private final String businessPath;
	
	@Autowired
	private InfoSubscriptionManager subscriptionManager;

	public InfoGroupRunController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup,
			boolean canAccess, boolean isAdmin, boolean readOnly) {
		super(ureq, wControl);
		
		long groupId = businessGroup.getKey();
		OLATResourceable infoResourceable = new InfoOLATGroupResourceable(groupId);
		businessPath = normalizeBusinessPath(wControl.getBusinessControl().getAsString());
		
		UserSession usess = ureq.getUserSession();
		if(!usess.getRoles().isGuestOnly()) {
			SubscriptionContext subContext = subscriptionManager.getInfoSubscriptionContext(infoResourceable, resSubPath);
			PublisherData pdata = subscriptionManager.getInfoPublisherData(infoResourceable, businessPath);
			subscriptionController = new ContextualSubscriptionController(ureq, getWindowControl(), subContext, pdata);
			listenTo(subscriptionController);
		}

		boolean canAddAndEdit = (isAdmin || canAccess) && !readOnly;
		InfoSecurityCallback secCallback = new InfoGroupSecurityCallback(getIdentity(), canAddAndEdit, isAdmin);
		infoDisplayController = new InfoDisplayController(ureq, wControl, secCallback, businessGroup, resSubPath, businessPath);
		SendMailOption subscribers = new SendSubscriberMailOption(infoResourceable, resSubPath, getLocale());
		infoDisplayController.addSendMailOptions(subscribers);
		SendMailOption coaches = new SendGroupMembersMailOption(businessGroup, GroupRoles.coach, translate("sendtochooser.form.radio.owners"));
		infoDisplayController.addSendMailOptions(coaches);
		SendMailOption participants = new SendGroupMembersMailOption(businessGroup, GroupRoles.participant, translate("sendtochooser.form.radio.partip"));
		infoDisplayController.addSendMailOptions(participants);

		MailFormatter mailFormatter = new SendInfoMailFormatter(businessGroup.getName(), businessPath, getTranslator());
		infoDisplayController.setSendMailFormatter(mailFormatter);
		listenTo(infoDisplayController);

		runVC = createVelocityContainer("run");
		if(subscriptionController != null) {
			runVC.put("infoSubscription", subscriptionController.getInitialComponent());
		}
		runVC.put("displayInfos", infoDisplayController.getInitialComponent());
		
		putInitialPanel(runVC);
	}

	/**
	 * Remove ROOT, remove identity context entry or duplicate, 
	 * @param url
	 * @return
	 */
	private String normalizeBusinessPath(String url) {
		if (url == null) return null;
		if (url.startsWith("ROOT")) {
			url = url.substring(4, url.length());
		}
		List<String> tokens = new ArrayList<>();
		for(StringTokenizer tokenizer = new StringTokenizer(url, "[]"); tokenizer.hasMoreTokens(); ) {
			String token = tokenizer.nextToken();
			if(token.startsWith("Identity")) {
				//The portlet "My courses" add an Identity context entry to the business path
				//ignore it
				continue;
			}
			if(!tokens.contains(token)) {
				tokens.add(token);
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for(String token:tokens) {
			sb.append('[').append(token).append(']');
		}
		return sb.toString();
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private class InfoGroupSecurityCallback implements InfoSecurityCallback {
		private final boolean canAdd;
		private final boolean canAdmin;
		private final Identity identity;
		
		public InfoGroupSecurityCallback(Identity identity, boolean canAdd, boolean canAdmin) {
			this.canAdd = canAdd;
			this.canAdmin = canAdmin;
			this.identity = identity;
		}
		
		@Override
		public boolean canRead() {
			return true;
		}

		@Override
		public boolean canAdd() {
			return canAdd;
		}

		@Override
		public boolean canEdit(InfoMessage infoMessage) {
			return identity.equals(infoMessage.getAuthor()) || canAdmin;
		}

		@Override
		public boolean canDelete() {
			return canAdmin;
		}
	}
	
	private class InfoOLATGroupResourceable implements OLATResourceable {
		private final Long resId;
		
		public InfoOLATGroupResourceable(Long groupId) {
			this.resId = groupId;
		}
		
		@Override
		public String getResourceableTypeName() {
			return OresHelper.calculateTypeName(BusinessGroup.class);
		}

		@Override
		public Long getResourceableId() {
			return resId;
		}
	}
}
