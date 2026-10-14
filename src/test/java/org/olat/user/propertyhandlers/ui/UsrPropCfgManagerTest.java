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
package org.olat.user.propertyhandlers.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Test;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyUsageContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 12 Oct 2026<br>
 * @author uhensler, https://www.frentix.com
 *
 */
public class UsrPropCfgManagerTest extends OlatTestCase {

	private static final String PROFILE_CTX = "org.olat.user.ProfileFormController";
	private static final String ADMIN_SEARCH_CTX = "org.olat.admin.user.UsermanagerUserSearchForm";
	private static final String REGISTRATION_CTX = "org.olat.registration.RegistrationPersonalDataController";

	@Autowired
	private UsrPropCfgManager usrPropCfgManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserModule userModule;

	@After
	public void restoreSystemDefault() {
		usrPropCfgManager.resetToSystemDefault(JunitTestHelper.getDefaultActor());
		waitMessageAreConsumed();
	}

	@Test
	public void applyPreset_unknownHandlerFollowsDeactivatedByDefault() {
		assertThat(userModule.getDeactivatedByDefaultProperties()).contains("skype", "xing", "linkedin", "icq");

		usrPropCfgManager.applyPreset(new Properties());

		UsrPropCfgObject cfg = usrPropCfgManager.getUserPropertiesConfigObject();
		assertThat(cfg.isActiveHandler(cfg.getPropertyHandler("skype"))).isFalse();
		assertThat(cfg.isActiveHandler(cfg.getPropertyHandler("xing"))).isFalse();
		assertThat(cfg.isActiveHandler(cfg.getPropertyHandler("linkedin"))).isFalse();
		assertThat(cfg.isActiveHandler(cfg.getPropertyHandler("icq"))).isFalse();
		assertThat(cfg.isActiveHandler(cfg.getPropertyHandler("birthDay"))).isTrue();
		assertThat(cfg.isActiveHandler(cfg.getPropertyHandler("homepage"))).isTrue();
	}

	@Test
	public void refreshUserManagerProperties_publishesInMemoryContextChange() {
		UsrPropCfgObject cfg = usrPropCfgManager.getUserPropertiesConfigObject();
		UserPropertyHandler birthDay = cfg.getPropertyHandler("birthDay");
		cfg.getUsageContexts().get(REGISTRATION_CTX).addPropertyHandler(0, birthDay);

		assertThat(names(userManager.getUserPropertyHandlersFor(REGISTRATION_CTX, false))).doesNotContain("birthDay");

		usrPropCfgManager.refreshUserManagerProperties();

		assertThat(names(userManager.getUserPropertyHandlersFor(REGISTRATION_CTX, false))).startsWith("birthDay");
	}

	@Test
	public void resetToSystemDefault_keepsEmailInRegistrationContext() {
		usrPropCfgManager.resetToSystemDefault(JunitTestHelper.getDefaultActor());

		assertThat(names(userManager.getUserPropertyHandlersFor(REGISTRATION_CTX, false))).startsWith("email");
	}

	@Test
	public void resetToMinimal_keepsEmailInRegistrationContext() {
		usrPropCfgManager.resetToMinimal(JunitTestHelper.getDefaultActor());

		assertThat(names(userManager.getUserPropertyHandlersFor(REGISTRATION_CTX, false))).startsWith("email");
	}

	@Test
	public void applyPreset_deactivatedHandlerLeavesUnknownContext() {
		Properties props = new Properties();
		props.setProperty("activeuserpropertyhandlers", "firstName,lastName,email,");
		props.setProperty("deactiveuserpropertyhandlers", "birthDay,");

		usrPropCfgManager.applyPreset(props);

		List<String> profileNames = names(userManager.getUserPropertyHandlersFor(PROFILE_CTX, true));
		assertThat(profileNames).doesNotContain("birthDay").contains("firstName");
	}

	@Test
	public void createSystemDefaultProperties_coversAllHandlersAndContexts() {
		Properties props = usrPropCfgManager.createSystemDefaultProperties();

		List<String> active = split(props.getProperty("activeuserpropertyhandlers"));
		List<String> deactive = split(props.getProperty("deactiveuserpropertyhandlers"));
		assertThat(deactive).containsExactlyInAnyOrder("icq", "linkedin", "skype", "xing");
		assertThat(active).contains("nickName", "birthDay").doesNotContain("skype");

		int registered = usrPropCfgManager.getUserPropertiesConfigObject().getPropertyHandlers().size();
		assertThat(active.size() + deactive.size()).isEqualTo(registered);

		assertThat(props.getProperty(REGISTRATION_CTX)).isEqualTo("1");
		assertThat(split(props.getProperty(REGISTRATION_CTX + "_hndl_"))).containsExactly("firstName", "lastName");
		assertThat(split(props.getProperty(REGISTRATION_CTX + "_hndl_mandatory"))).containsExactly("firstName", "lastName");

		List<String> profile = split(props.getProperty(PROFILE_CTX + "_hndl_"));
		assertThat(profile).startsWith("nickName").doesNotContain("skype");
	}

	@Test
	public void resetToSystemDefault_restoresGroupAndActivation() {
		UsrPropCfgObject cfg = usrPropCfgManager.getUserPropertiesConfigObject();
		UserPropertyHandler birthDay = cfg.getPropertyHandler("birthDay");
		String group = birthDay.getGroup();
		birthDay.setGroup("changedGroup");
		cfg.setHandlerAsActive(birthDay, false);
		usrPropCfgManager.saveUserPropertiesConfig();
		assertThat(names(userManager.getUserPropertyHandlersFor(PROFILE_CTX, true))).doesNotContain("birthDay");

		usrPropCfgManager.resetToSystemDefault(JunitTestHelper.getDefaultActor());

		cfg = usrPropCfgManager.getUserPropertiesConfigObject();
		birthDay = cfg.getPropertyHandler("birthDay");
		assertThat(birthDay.getGroup()).isEqualTo(group);
		assertThat(cfg.isActiveHandler(birthDay)).isTrue();
		assertThat(names(userManager.getUserPropertyHandlersFor(PROFILE_CTX, true))).contains("birthDay");
		assertThat(cfg.isActiveHandler(cfg.getPropertyHandler("skype"))).isFalse();
	}

	@Test
	public void createMinimalProperties_onlyFourPropertiesInContexts() {
		Properties props = usrPropCfgManager.createMinimalProperties();

		List<String> active = split(props.getProperty("activeuserpropertyhandlers"));
		assertThat(active).containsExactlyInAnyOrder("email", "emailDisabled", "emchangeKey", "firstName", "lastName", "nickName");
		assertThat(split(props.getProperty("deactiveuserpropertyhandlers"))).contains("birthDay");
		assertThat(split(props.getProperty(PROFILE_CTX + "_hndl_"))).containsExactly("nickName", "firstName", "lastName", "email");
		assertThat(split(props.getProperty(ADMIN_SEARCH_CTX + "_hndl_"))).containsExactly("firstName", "lastName", "email");
	}

	@Test
	public void resetToMinimal_appliesToLiveConfiguration() {
		usrPropCfgManager.resetToMinimal(JunitTestHelper.getDefaultActor());

		assertThat(names(userManager.getUserPropertyHandlersFor(PROFILE_CTX, true)))
				.containsExactly("nickName", "firstName", "lastName", "email");
		assertThat(names(userManager.getUserPropertyHandlersFor(ADMIN_SEARCH_CTX, true)))
				.containsExactly("firstName", "lastName", "email");
		UsrPropCfgObject cfg = usrPropCfgManager.getUserPropertiesConfigObject();
		assertThat(cfg.isActiveHandler(cfg.getPropertyHandler("birthDay"))).isFalse();
		assertThat(cfg.isActiveHandler(cfg.getPropertyHandler("nickName"))).isTrue();
	}

	@Test
	public void resetToMinimal_survivesReloadFromPersistedProperties() {
		usrPropCfgManager.resetToMinimal(JunitTestHelper.getDefaultActor());

		usrPropCfgManager.reloadFromPersistedProperties();

		assertThat(names(userManager.getUserPropertyHandlersFor(PROFILE_CTX, true)))
				.containsExactly("nickName", "firstName", "lastName", "email");
		assertThat(names(userManager.getUserPropertyHandlersFor(ADMIN_SEARCH_CTX, true)))
				.containsExactly("firstName", "lastName", "email");
		UsrPropCfgObject cfg = usrPropCfgManager.getUserPropertiesConfigObject();
		assertThat(cfg.isActiveHandler(cfg.getPropertyHandler("birthDay"))).isFalse();
	}

	@Test
	public void resetToSystemDefault_survivesReloadFromPersistedProperties() {
		usrPropCfgManager.resetToMinimal(JunitTestHelper.getDefaultActor());
		usrPropCfgManager.resetToSystemDefault(JunitTestHelper.getDefaultActor());

		usrPropCfgManager.reloadFromPersistedProperties();

		UsrPropCfgObject cfg = usrPropCfgManager.getUserPropertiesConfigObject();
		assertThat(cfg.isActiveHandler(cfg.getPropertyHandler("birthDay"))).isTrue();
		assertThat(cfg.isActiveHandler(cfg.getPropertyHandler("skype"))).isFalse();
		assertThat(names(userManager.getUserPropertyHandlersFor(PROFILE_CTX, true)))
				.contains("nickName", "birthDay").doesNotContain("skype");
		assertThat(names(userManager.getUserPropertyHandlersFor(REGISTRATION_CTX, true)))
				.containsExactly("email", "firstName", "lastName");
	}

	@Test
	public void setHandlerActive_deactivateKeepsContextConfiguration() {
		UsrPropCfgObject cfg = usrPropCfgManager.getUserPropertiesConfigObject();
		UserPropertyHandler birthDay = cfg.getPropertyHandler("birthDay");
		List<String> contextsBefore = cfg.contextsWith(birthDay);
		assertThat(contextsBefore).contains(PROFILE_CTX);

		usrPropCfgManager.setHandlerActive(JunitTestHelper.getDefaultActor(), birthDay, false);

		assertThat(cfg.isActiveHandler(birthDay)).isFalse();
		assertThat(cfg.contextsWith(birthDay)).isEqualTo(contextsBefore);
		assertThat(names(userManager.getUserPropertyHandlersFor(PROFILE_CTX, true))).doesNotContain("birthDay");
		assertThat(names(userManager.getAllUserPropertyHandlers())).doesNotContain("birthDay");

		usrPropCfgManager.setHandlerActive(JunitTestHelper.getDefaultActor(), birthDay, true);

		assertThat(cfg.contextsWith(birthDay)).isEqualTo(contextsBefore);
		assertThat(names(userManager.getUserPropertyHandlersFor(PROFILE_CTX, true))).contains("birthDay");
	}

	@Test
	public void setHandlerActive_deactivateSurvivesReloadFromPersistedProperties() {
		UsrPropCfgObject cfg = usrPropCfgManager.getUserPropertiesConfigObject();
		UserPropertyHandler birthDay = cfg.getPropertyHandler("birthDay");
		List<String> contextsBefore = cfg.contextsWith(birthDay);
		usrPropCfgManager.setHandlerActive(JunitTestHelper.getDefaultActor(), birthDay, false);

		usrPropCfgManager.reloadFromPersistedProperties();

		cfg = usrPropCfgManager.getUserPropertiesConfigObject();
		birthDay = cfg.getPropertyHandler("birthDay");
		assertThat(cfg.isActiveHandler(birthDay)).isFalse();
		assertThat(cfg.contextsWith(birthDay)).isEqualTo(contextsBefore);
		assertThat(names(userManager.getUserPropertyHandlersFor(PROFILE_CTX, true))).doesNotContain("birthDay");
	}

	@Test
	public void setHandlerActive_activateSeedsFromSystemDefaultWhenInNoContext() {
		Properties defaults = usrPropCfgManager.createSystemDefaultProperties();
		List<String> defaultContexts = defaults.stringPropertyNames().stream()
				.filter(key -> key.endsWith("_hndl_") && split(defaults.getProperty(key)).contains("birthDay"))
				.map(key -> key.substring(0, key.length() - "_hndl_".length()))
				.toList();
		assertThat(defaultContexts).contains(PROFILE_CTX);

		usrPropCfgManager.resetToMinimal(JunitTestHelper.getDefaultActor());
		UsrPropCfgObject cfg = usrPropCfgManager.getUserPropertiesConfigObject();
		UserPropertyHandler birthDay = cfg.getPropertyHandler("birthDay");
		assertThat(cfg.contextsWith(birthDay)).isEmpty();

		usrPropCfgManager.setHandlerActive(JunitTestHelper.getDefaultActor(), birthDay, true);

		assertThat(cfg.contextsWith(birthDay)).containsExactlyInAnyOrderElementsOf(defaultContexts);
		assertThat(names(userManager.getUserPropertyHandlersFor(PROFILE_CTX, true))).contains("birthDay");
	}

	@Test
	public void setHandlerActive_activateKeepsExistingContextsUntouched() {
		UsrPropCfgObject cfg = usrPropCfgManager.getUserPropertiesConfigObject();
		UserPropertyHandler birthDay = cfg.getPropertyHandler("birthDay");
		cfg.getUsageContexts().get(PROFILE_CTX).removePropertyHandler(birthDay);
		List<String> contextsBefore = cfg.contextsWith(birthDay);
		assertThat(contextsBefore).isNotEmpty().doesNotContain(PROFILE_CTX);
		usrPropCfgManager.setHandlerActive(JunitTestHelper.getDefaultActor(), birthDay, false);

		usrPropCfgManager.setHandlerActive(JunitTestHelper.getDefaultActor(), birthDay, true);

		assertThat(cfg.contextsWith(birthDay)).isEqualTo(contextsBefore);
	}

	@Test
	public void contextsWithAndWithout_complementEachOther() {
		UsrPropCfgObject cfg = usrPropCfgManager.getUserPropertiesConfigObject();
		UserPropertyHandler birthDay = cfg.getPropertyHandler("birthDay");

		List<String> with = cfg.contextsWith(birthDay);
		List<String> without = cfg.contextsWithout(birthDay);

		assertThat(with).isNotEmpty().doesNotContainAnyElementsOf(without);
		assertThat(with.size() + without.size()).isEqualTo(cfg.getUsageContexts().size());
	}

	@Test
	public void activeHandlerCount_ignoresInactiveHandlers() {
		UsrPropCfgObject cfg = usrPropCfgManager.getUserPropertiesConfigObject();
		UserPropertyHandler birthDay = cfg.getPropertyHandler("birthDay");
		UserPropertyUsageContext profile = cfg.getUsageContexts().get(PROFILE_CTX);
		int before = cfg.activeHandlerCount(profile);

		usrPropCfgManager.setHandlerActive(JunitTestHelper.getDefaultActor(), birthDay, false);

		assertThat(cfg.activeHandlerCount(profile)).isEqualTo(before - 1);
		assertThat(profile.getPropertyHandlers()).contains(birthDay);
		assertThat(cfg.activeHandlersOf(profile)).doesNotContain(birthDay);
	}

	@Test
	public void effectiveContexts_areCopiesWithoutInactiveHandlers() {
		UsrPropCfgObject cfg = usrPropCfgManager.getUserPropertiesConfigObject();
		UserPropertyHandler birthDay = cfg.getPropertyHandler("birthDay");
		usrPropCfgManager.setHandlerActive(JunitTestHelper.getDefaultActor(), birthDay, false);

		UserPropertyUsageContext master = cfg.getUsageContexts().get(PROFILE_CTX);
		UserPropertyUsageContext effective = userManager.getUserPropertiesConfig().getUserPropertyUsageContexts().get(PROFILE_CTX);

		assertThat(effective).isNotSameAs(master);
		assertThat(effective.getDescription()).isEqualTo(master.getDescription());
		assertThat(effective.contains(birthDay)).isFalse();
		assertThat(master.contains(birthDay)).isTrue();
	}

	private static List<String> names(List<UserPropertyHandler> handlers) {
		return handlers.stream().map(UserPropertyHandler::getName).toList();
	}

	private static List<String> split(String value) {
		if (value == null || value.isEmpty()) {
			return List.of();
		}
		return Arrays.stream(value.split(",")).filter(s -> !s.isEmpty()).toList();
	}
}
