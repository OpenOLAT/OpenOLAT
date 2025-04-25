/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.user;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Apr 24, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class UserPropertiesInfoController extends UserInfoController {
	
	public static final String DEFAULT_USAGE_IDENTIFYER = "org.olat.admin.user.UserShortDescription";
	
	private Identity infoIdentity;
	private String usageIdentifyer;
	private boolean isAdministrativeUser;
	private LabelValues additionalLabelValues;
	private int counter = 0;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private UserPortraitService userPortraitService;
	
	public UserPropertiesInfoController(UserRequest ureq, WindowControl wControl, Identity infoIdentity) {
		this(ureq, wControl, infoIdentity, null, null);
	}
	
	public UserPropertiesInfoController(UserRequest ureq, WindowControl wControl, Identity infoIdentity,
			String customUsageIdentifier, LabelValues additionalLabelValues) {
		super(ureq, wControl, null, null);
		init(ureq, infoIdentity, customUsageIdentifier, additionalLabelValues, null);
	}
	
	public UserPropertiesInfoController(UserRequest ureq, WindowControl wControl, Form mainForm, Identity infoIdentity) {
		this(ureq, wControl, mainForm, infoIdentity, null, null, null);
	}
	
	public UserPropertiesInfoController(UserRequest ureq, WindowControl wControl, Form mainForm, Identity infoIdentity,
			String customUsageIdentifier, LabelValues additionalLabelValues, UserInfoProfileConfig profileConfig) {
		super(ureq, wControl, mainForm, null, null);
		init(ureq, infoIdentity, customUsageIdentifier, additionalLabelValues, profileConfig);
	}
	
	private void init(UserRequest ureq, Identity infoIdentity, String customUsageIdentifier,
			LabelValues additionalLabelValues, UserInfoProfileConfig customProfileConfig) {
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.infoIdentity = infoIdentity;
		this.additionalLabelValues = additionalLabelValues;
		
		usageIdentifyer = StringHelper.containsNonWhitespace(customUsageIdentifier)
				? customUsageIdentifier
				: DEFAULT_USAGE_IDENTIFYER;
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		
		UserInfoProfileConfig profileConfig = customProfileConfig != null
				? customProfileConfig
				: userPortraitService.createProfileConfig();
		initProfileConfig(profileConfig);
		initPortraitUser(userPortraitService.createPortraitUser(getLocale(), infoIdentity));
		initForm(ureq);
	}
	
	@Override
	protected void initFormItems(FormLayoutContainer itemsCont, Controller listener, UserRequest ureq) {
		User user = infoIdentity.getUser();
		
		addAdditionalRows(itemsCont, LabelValue::isBefore);
		
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		
		boolean alreadyDefinedUsername = userPropertyHandlers.stream().anyMatch(handler -> UserConstants.NICKNAME.equals(handler.getName()));
		if (!alreadyDefinedUsername && (getIdentity().equals(infoIdentity) || isAdministrativeUser)) {
			String nickName = user.getProperty(UserConstants.NICKNAME, getLocale());
			uifactory.addStaticTextElement("nickname", "table.name.nickName", nickName, itemsCont);
		}
		
		if (isAdministrativeUser) {
			uifactory.addStaticTextElement("key", "table.name.id", String.valueOf(infoIdentity.getKey()), itemsCont);
		}
		
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			String name = userPropertyHandler.getName();
			
			// First name and last name are already on the profile card.
			if (UserConstants.FIRSTNAME.equals(name) || UserConstants.LASTNAME.equals(name) ) {
				continue;
			}
			
			String value = userPropertyHandler.getUserProperty(user, getLocale());
			if (StringHelper.containsNonWhitespace(value)) {
				value = StringHelper.escapeHtml(value);
				String i18nLabel = userPropertyHandler.i18nFormElementLabelKey();
				uifactory.addStaticTextElement(name, i18nLabel, value, itemsCont);
			}
		}
		
		addAdditionalRows(itemsCont, row -> !row.isBefore());
	}

	private void addAdditionalRows(FormLayoutContainer itemsCont, Predicate<LabelValue> predicate) {
		if (additionalLabelValues != null && !additionalLabelValues.getRows().isEmpty()) {
			additionalLabelValues.getRows().stream()
				.filter(predicate)
				.forEach(row -> {
					StaticTextElement additionalEl = uifactory.addStaticTextElement("additional_" + counter++, null, row.getValue(), itemsCont);
					additionalEl.setLabel("noTransOnlyParam", new String[] {row.getLabel()});
			});
		}
	}
	
	public static class LabelValues {
		
		private final List<LabelValue> labelValues;

		private LabelValues(Builder builder) {
			this.labelValues = new ArrayList<>(builder.labelValues);
		}

		public List<LabelValue> getRows() {
			return labelValues;
		}

		public static Builder builder() {
			return new Builder();
		}
	}
	
	public static final class Builder {
		
		private List<LabelValue> labelValues = new ArrayList<>();

		private Builder() {
		}
		
		public Builder addBefore(String label, String value) {
			labelValues.add(new LabelValue(label, value, true));
			return this;
		}

		public Builder add(String label, String value) {
			labelValues.add(new LabelValue(label, value, false));
			return this;
		}

		public LabelValues build() {
			return new LabelValues(this);
		}
	}
	
	public static class LabelValue {
		
		private final boolean before;
		private final String label;
		private final String value;
		
		private LabelValue(String label, String value, boolean before) {
			this.label = label;
			this.value = value;
			this.before = before;
		}
		
		public boolean isBefore() {
			return before;
		}
		
		public String getLabel() {
			return label;
		}
		
		public String getValue() {
			return value;
		}
		
	}

}
