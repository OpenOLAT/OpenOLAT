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

package org.olat.core.util.prefs.gui.manager;

import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.prefs.gui.GuiPreference;
import org.olat.core.util.prefs.gui.GuiPreferenceService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Description:<br>
 * Unit tests for the GuiPreferenceService.
 *
 * <p>
 * Initial date: Dez 11, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GuiPreferenceServiceTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private GuiPreferenceService guiPreferenceService;


	@Test
	public void testUpdateGuiPreferences() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		String attributedClass = "org.olat.core.gui.WindowManager";
		String prefKey = "resume-prefs";
		String prefValue = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::resume-prefs</string>\n" +
				"      <string>auto</string>\n" +
				"</entry>";
		GuiPreference gp = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass, prefKey, prefValue);
		assertNotNull(gp);

		guiPreferenceService.persistOrLoad(gp);
		dbInstance.commitAndCloseSession();

		assertNotNull(gp.getKey());

		String newPrefValue = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::resume-prefs</string>\n" +
				"      <string>none</string>\n" +
				"</entry>";

		gp.setPrefValue(newPrefValue);

		GuiPreference updatedGp = guiPreferenceService.updateGuiPreferences(gp);
		updatedGp.setLastModified(DateUtils.addDays(gp.getLastModified(), 1));
		assertNotNull(updatedGp);
		assertEquals(newPrefValue, updatedGp.getPrefValue());
		assertNotEquals(prefValue, updatedGp.getPrefValue());
		assertNotEquals(gp.getLastModified(), updatedGp.getLastModified());
	}

	@Test
	public void testLoadGuiPrefsByIdentity() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		String attributedClassId11 = "org.olat.core.gui.WindowManager";
		String prefKeyId11 = "resume-prefs";
		String prefValueId11 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::resume-prefs</string>\n" +
				"      <string>auto</string>\n" +
				"</entry>";
		GuiPreference gp11 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClassId11, prefKeyId11, prefValueId11);
		assertNotNull(gp11);

		guiPreferenceService.persistOrLoad(gp11);
		dbInstance.commitAndCloseSession();

		String attributedClassId12 = "org.olat.core.gui.components.form.flexible.elements.FlexiTableElement";
		String prefKeyId12 = "gbg-list-my";
		String prefValueId12 = "<entry>\n" +
				"      <string>org.olat.core.gui.components.form.flexible.elements.FlexiTableElement::gbg-list-my</string>\n" +
				"      <org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTablePreferences>\n" +
				"        <pageSize>20</pageSize>\n" +
				"        <sortDirection>true</sortDirection>\n" +
				"        <sortedColumnKey>tableheaderlastusage</sortedColumnKey>\n" +
				"        <enabledColumnKey>\n" +
				"          <string>tableheaderresources</string>\n" +
				"          <string>tableheaderstatus</string>\n" +
				"          <string>tableheaderacmethod</string>\n" +
				"          <string>tableheadermark</string>\n" +
				"          <string>tableheaderlastusage</string>\n" +
				"          <string>tableheaderrole</string>\n" +
				"        </enabledColumnKey>\n" +
				"        <customTabs/>\n" +
				"        <rendererType>classic</rendererType>\n" +
				"      </org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTablePreferences>\n" +
				"</entry>";
		GuiPreference gp12 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClassId12, prefKeyId12, prefValueId12);
		assertNotNull(gp12);

		guiPreferenceService.persistOrLoad(gp12);
		dbInstance.commitAndCloseSession();

		String attributedClassId21 = "org.olat.core.gui.WindowManager";
		String prefKeyId21 = "landing-page";
		String prefValueId21 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp21 = guiPreferenceService.createGuiPreferenceEntry(identity2, attributedClassId21, prefKeyId21, prefValueId21);
		assertNotNull(gp21);

		guiPreferenceService.persistOrLoad(gp21);
		dbInstance.commitAndCloseSession();

		List<GuiPreference> guiPreferencesByIdentity1 = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, null, null);
		assertEquals(2, guiPreferencesByIdentity1.size());
		List<GuiPreference> guiPreferencesByIdentity2 = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity2, null, null);
		assertEquals(1, guiPreferencesByIdentity2.size());
		assertEquals(attributedClassId21, guiPreferencesByIdentity2.get(0).getAttributedClass());
		assertEquals(prefKeyId21, guiPreferencesByIdentity2.get(0).getPrefKey());
		assertEquals(prefValueId21, guiPreferencesByIdentity2.get(0).getPrefValue());
	}

	@Test
	public void testLoadGuiPrefsByIdentityAndAttributedClass() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		String attributedClass = "org.olat.core.gui.WindowManager";
		String prefKeyId11 = "resume-prefs";
		String prefValueId11 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::resume-prefs</string>\n" +
				"      <string>auto</string>\n" +
				"</entry>";
		GuiPreference gp11 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass, prefKeyId11, prefValueId11);
		assertNotNull(gp11);

		guiPreferenceService.persistOrLoad(gp11);
		dbInstance.commitAndCloseSession();

		String prefKeyId12 = "landing-page";
		String prefValueId12 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp12 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass, prefKeyId12, prefValueId12);
		assertNotNull(gp12);

		guiPreferenceService.persistOrLoad(gp12);
		dbInstance.commitAndCloseSession();

		String prefKeyId21 = "landing-page";
		String prefValueId21 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp21 = guiPreferenceService.createGuiPreferenceEntry(identity2, attributedClass, prefKeyId21, prefValueId21);
		assertNotNull(gp21);

		guiPreferenceService.persistOrLoad(gp21);
		dbInstance.commitAndCloseSession();

		List<GuiPreference> guiPreferencesByIdAndAttrClass1 = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, attributedClass, null);
		assertEquals(2, guiPreferencesByIdAndAttrClass1.size());
		List<GuiPreference> guiPreferencesByIdAndAttrClass2 = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity2, attributedClass, null);
		assertEquals(1, guiPreferencesByIdAndAttrClass2.size());
		assertEquals(attributedClass, guiPreferencesByIdAndAttrClass2.get(0).getAttributedClass());
		assertEquals(prefKeyId21, guiPreferencesByIdAndAttrClass2.get(0).getPrefKey());
		assertEquals(prefValueId21, guiPreferencesByIdAndAttrClass2.get(0).getPrefValue());
	}

	@Test
	public void testLoadGuiPrefsByAttributedClass() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		String attributedClass1 = "org.olat.core.gui.WindowManager" + UUID.randomUUID();
		String prefKeyId11 = "resume-prefs";
		String prefValueId11 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::resume-prefs</string>\n" +
				"      <string>auto</string>\n" +
				"</entry>";
		GuiPreference gp11 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass1, prefKeyId11, prefValueId11);
		assertNotNull(gp11);

		guiPreferenceService.persistOrLoad(gp11);
		dbInstance.commitAndCloseSession();

		String attributedClass2 = "org.olat.core.gui.components.form.flexible.elements.FlexiTableElement" + UUID.randomUUID();
		String prefKeyId12 = "gbg-list-my";
		String prefValueId12 = "<entry>\n" +
				"      <string>org.olat.core.gui.components.form.flexible.elements.FlexiTableElement::gbg-list-my</string>\n" +
				"      <org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTablePreferences>\n" +
				"        <pageSize>20</pageSize>\n" +
				"        <sortDirection>true</sortDirection>\n" +
				"        <sortedColumnKey>tableheaderlastusage</sortedColumnKey>\n" +
				"        <enabledColumnKey>\n" +
				"          <string>tableheaderresources</string>\n" +
				"          <string>tableheaderstatus</string>\n" +
				"          <string>tableheaderacmethod</string>\n" +
				"          <string>tableheadermark</string>\n" +
				"          <string>tableheaderlastusage</string>\n" +
				"          <string>tableheaderrole</string>\n" +
				"        </enabledColumnKey>\n" +
				"        <customTabs/>\n" +
				"        <rendererType>classic</rendererType>\n" +
				"      </org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTablePreferences>\n" +
				"</entry>";
		GuiPreference gp12 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass2, prefKeyId12, prefValueId12);
		assertNotNull(gp12);

		guiPreferenceService.persistOrLoad(gp12);
		dbInstance.commitAndCloseSession();

		String prefKeyId21 = "landing-page";
		String prefValueId21 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp21 = guiPreferenceService.createGuiPreferenceEntry(identity2, attributedClass1, prefKeyId21, prefValueId21);
		assertNotNull(gp21);

		guiPreferenceService.persistOrLoad(gp21);
		dbInstance.commitAndCloseSession();

		List<GuiPreference> guiPreferencesByAttrClass1 = guiPreferenceService.loadGuiPrefsByUniqueProperties(null, attributedClass1, null);
		assertEquals(2, guiPreferencesByAttrClass1.size());
		List<GuiPreference> guiPreferencesByAttrClass2 = guiPreferenceService.loadGuiPrefsByUniqueProperties(null, attributedClass2, null);
		assertEquals(1, guiPreferencesByAttrClass2.size());
		assertEquals(attributedClass2, guiPreferencesByAttrClass2.get(0).getAttributedClass());
		assertEquals(prefKeyId12, guiPreferencesByAttrClass2.get(0).getPrefKey());
		assertEquals(prefValueId12, guiPreferencesByAttrClass2.get(0).getPrefValue());
	}

	@Test
	public void testLoadGuiPrefsByAttributedClassAndKey() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		String attributedClass = "org.olat.core.gui.WindowManager";
		String prefKey1 = "resume-prefs" + UUID.randomUUID();
		String prefValueId11 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::resume-prefs</string>\n" +
				"      <string>auto</string>\n" +
				"</entry>";
		GuiPreference gp11 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass, prefKey1, prefValueId11);
		assertNotNull(gp11);

		guiPreferenceService.persistOrLoad(gp11);
		dbInstance.commitAndCloseSession();

		String prefKey2 = "landing-page" + UUID.randomUUID();
		String prefValueId12 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp12 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass, prefKey2, prefValueId12);
		assertNotNull(gp12);

		guiPreferenceService.persistOrLoad(gp12);
		dbInstance.commitAndCloseSession();

		String prefValueId21 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp21 = guiPreferenceService.createGuiPreferenceEntry(identity2, attributedClass, prefKey2, prefValueId21);
		assertNotNull(gp21);

		guiPreferenceService.persistOrLoad(gp21);
		dbInstance.commitAndCloseSession();

		List<GuiPreference> guiPreferencesByAttrClassAndKey = guiPreferenceService.loadGuiPrefsByUniqueProperties(null, attributedClass, prefKey1);
		assertEquals(1, guiPreferencesByAttrClassAndKey.size());
		assertEquals(attributedClass, guiPreferencesByAttrClassAndKey.get(0).getAttributedClass());
		assertEquals(prefKey1, guiPreferencesByAttrClassAndKey.get(0).getPrefKey());
		assertEquals(prefValueId11, guiPreferencesByAttrClassAndKey.get(0).getPrefValue());
		List<GuiPreference> guiPreferencesByAttrClassAndKey2 = guiPreferenceService.loadGuiPrefsByUniqueProperties(null, attributedClass, prefKey2);
		assertEquals(2, guiPreferencesByAttrClassAndKey2.size());
	}

	@Test
	public void testLoadGuiPref() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		String attributedClass = "org.olat.core.gui.WindowManager";
		String prefKey1 = "resume-prefs";
		String prefValue1 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::resume-prefs</string>\n" +
				"      <string>auto</string>\n" +
				"</entry>";
		GuiPreference gp11 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass, prefKey1, prefValue1);
		assertNotNull(gp11);

		guiPreferenceService.persistOrLoad(gp11);
		dbInstance.commitAndCloseSession();

		String prefKey2 = "landing-page";
		String prefValue2 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp21 = guiPreferenceService.createGuiPreferenceEntry(identity2, attributedClass, prefKey2, prefValue2);
		assertNotNull(gp21);

		guiPreferenceService.persistOrLoad(gp21);
		dbInstance.commitAndCloseSession();

		List<GuiPreference> guiPreferencesByUniqueProperties = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, attributedClass, prefKey1);
		assertEquals(1, guiPreferencesByUniqueProperties.size());
		assertEquals(attributedClass, guiPreferencesByUniqueProperties.get(0).getAttributedClass());
		assertEquals(prefKey1, guiPreferencesByUniqueProperties.get(0).getPrefKey());
		assertEquals(prefValue1, guiPreferencesByUniqueProperties.get(0).getPrefValue());
		List<GuiPreference> guiPreferencesByUniqueProperties2 = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity2, attributedClass, prefKey2);
		assertEquals(1, guiPreferencesByUniqueProperties2.size());
		assertEquals(attributedClass, guiPreferencesByUniqueProperties2.get(0).getAttributedClass());
		assertEquals(prefKey2, guiPreferencesByUniqueProperties2.get(0).getPrefKey());
		assertEquals(prefValue2, guiPreferencesByUniqueProperties2.get(0).getPrefValue());
	}


	@Test
	public void testDeleteGuiPrefsByIdentity() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		String attributedClass = "org.olat.core.gui.WindowManager";
		String prefKey1 = "resume-prefs" + UUID.randomUUID();
		String prefValueId11 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::resume-prefs</string>\n" +
				"      <string>auto</string>\n" +
				"</entry>";
		GuiPreference gp11 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass, prefKey1, prefValueId11);
		assertNotNull(gp11);

		guiPreferenceService.persistOrLoad(gp11);
		dbInstance.commitAndCloseSession();

		String prefKey2 = "landing-page" + UUID.randomUUID();
		String prefValueId12 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp12 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass, prefKey2, prefValueId12);
		assertNotNull(gp12);

		guiPreferenceService.persistOrLoad(gp12);
		dbInstance.commitAndCloseSession();

		String prefValueId21 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp21 = guiPreferenceService.createGuiPreferenceEntry(identity2, attributedClass, prefKey2, prefValueId21);
		assertNotNull(gp21);

		guiPreferenceService.persistOrLoad(gp21);
		dbInstance.commitAndCloseSession();

		guiPreferenceService.deleteGuiPrefsByUniqueProperties(identity, null, null);
		List<GuiPreference> guiPreferencesDeleted = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, null, null);
		assertTrue(guiPreferencesDeleted.isEmpty());
		List<GuiPreference> guiPreferencesDeleted2 = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity2, null, null);
		assertFalse(guiPreferencesDeleted2.isEmpty());
	}

	@Test
	public void testDeleteGuiPrefsByIdentityAndAttributedClass() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		String attributedClass = "org.olat.core.gui.WindowManager";
		String prefKey1 = "resume-prefs";
		String prefValueId11 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::resume-prefs</string>\n" +
				"      <string>auto</string>\n" +
				"</entry>";
		GuiPreference gp11 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass, prefKey1, prefValueId11);
		assertNotNull(gp11);

		guiPreferenceService.persistOrLoad(gp11);
		dbInstance.commitAndCloseSession();

		String attributedClass2 = "org.olat.core.gui.WindowManager" + CodeHelper.getUniqueID();
		String prefKey2 = "landing-page";
		String prefValueId12 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp12 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass2, prefKey2, prefValueId12);
		assertNotNull(gp12);

		guiPreferenceService.persistOrLoad(gp12);
		dbInstance.commitAndCloseSession();

		String prefValueId21 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp21 = guiPreferenceService.createGuiPreferenceEntry(identity2, attributedClass2, prefKey2, prefValueId21);
		assertNotNull(gp21);

		guiPreferenceService.persistOrLoad(gp21);
		dbInstance.commitAndCloseSession();

		List<GuiPreference> guiPreferencesDeletedPre = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, null, null);
		assertEquals(2, guiPreferencesDeletedPre.size());
		guiPreferenceService.deleteGuiPrefsByUniqueProperties(identity, attributedClass2, null);
		List<GuiPreference> guiPreferencesDeleted = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, attributedClass2, null);
		assertTrue(guiPreferencesDeleted.isEmpty());
		List<GuiPreference> guiPreferencesDeletedPost = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, null, null);
		assertEquals(1, guiPreferencesDeletedPost.size());
		List<GuiPreference> guiPreferencesDeleted2 = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity2, attributedClass2, null);
		assertFalse(guiPreferencesDeleted2.isEmpty());
	}

	@Test
	public void testDeleteGuiPrefsByAttributedClass() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		String attributedClass = "org.olat.core.gui.WindowManager";
		String prefKey1 = "resume-prefs" + UUID.randomUUID();
		String prefValueId11 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::resume-prefs</string>\n" +
				"      <string>auto</string>\n" +
				"</entry>";
		GuiPreference gp11 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass, prefKey1, prefValueId11);
		assertNotNull(gp11);

		guiPreferenceService.persistOrLoad(gp11);
		dbInstance.commitAndCloseSession();

		String attributedClass2 = "org.olat.core.gui.WindowManager" + CodeHelper.getUniqueID();
		String prefKey2 = "landing-page" + UUID.randomUUID();
		String prefValueId12 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp12 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass2, prefKey2, prefValueId12);
		assertNotNull(gp12);

		guiPreferenceService.persistOrLoad(gp12);
		dbInstance.commitAndCloseSession();

		String prefValueId21 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp21 = guiPreferenceService.createGuiPreferenceEntry(identity2, attributedClass, prefKey2, prefValueId21);
		assertNotNull(gp21);

		guiPreferenceService.persistOrLoad(gp21);
		dbInstance.commitAndCloseSession();

		guiPreferenceService.deleteGuiPrefsByUniqueProperties(null, attributedClass2, null);
		List<GuiPreference> guiPreferencesDeleted = guiPreferenceService.loadGuiPrefsByUniqueProperties(null, attributedClass2, null);
		assertTrue(guiPreferencesDeleted.isEmpty());
		List<GuiPreference> guiPreferencesDeleted2 = guiPreferenceService.loadGuiPrefsByUniqueProperties(null, attributedClass, null);
		assertFalse(guiPreferencesDeleted2.isEmpty());
	}

	@Test
	public void testDeleteGuiPrefsByAttributedClassAndKey() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		String attributedClass = "org.olat.core.gui.WindowManager";
		String prefKey1 = "resume-prefs" + UUID.randomUUID();
		String prefValueId11 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::resume-prefs</string>\n" +
				"      <string>auto</string>\n" +
				"</entry>";
		GuiPreference gp11 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass, prefKey1, prefValueId11);
		assertNotNull(gp11);

		guiPreferenceService.persistOrLoad(gp11);
		dbInstance.commitAndCloseSession();

		String attributedClass2 = "org.olat.core.gui.WindowManager" + CodeHelper.getUniqueID();
		String prefKey2 = "landing-page" + UUID.randomUUID();
		String prefValueId12 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp12 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass2, prefKey2, prefValueId12);
		assertNotNull(gp12);

		guiPreferenceService.persistOrLoad(gp12);
		dbInstance.commitAndCloseSession();

		String prefValueId21 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp21 = guiPreferenceService.createGuiPreferenceEntry(identity2, attributedClass, prefKey2, prefValueId21);
		assertNotNull(gp21);

		guiPreferenceService.persistOrLoad(gp21);
		dbInstance.commitAndCloseSession();

		List<GuiPreference> guiPreferencesDeletedPre = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, null, null);
		assertEquals(2, guiPreferencesDeletedPre.size());
		guiPreferenceService.deleteGuiPrefsByUniqueProperties(null, attributedClass2, prefKey2);
		List<GuiPreference> guiPreferencesDeleted = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, null, null);
		assertEquals(1, guiPreferencesDeleted.size());
		assertEquals(attributedClass, guiPreferencesDeleted.get(0).getAttributedClass());
		assertEquals(prefKey1, guiPreferencesDeleted.get(0).getPrefKey());
		List<GuiPreference> guiPreferencesDeleted2 = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity2, null, null);
		assertEquals(1, guiPreferencesDeleted2.size());
	}

	@Test
	public void testDeleteSpecificGuiPref() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsUser("gui-pref-" + CodeHelper.getUniqueID());
		String attributedClass = "org.olat.core.gui.WindowManager";
		String prefKey1 = "resume-prefs" + UUID.randomUUID();
		String prefValueId11 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::resume-prefs</string>\n" +
				"      <string>auto</string>\n" +
				"</entry>";
		GuiPreference gp11 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass, prefKey1, prefValueId11);
		assertNotNull(gp11);

		guiPreferenceService.persistOrLoad(gp11);
		dbInstance.commitAndCloseSession();

		String attributedClass2 = "org.olat.core.gui.WindowManager" + CodeHelper.getUniqueID();
		String prefKey2 = "landing-page" + UUID.randomUUID();
		String prefValueId12 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp12 = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass2, prefKey2, prefValueId12);
		assertNotNull(gp12);

		guiPreferenceService.persistOrLoad(gp12);
		dbInstance.commitAndCloseSession();

		String prefValueId21 = "<entry>\n" +
				"      <string>org.olat.core.gui.WindowManager::landing-page</string>\n" +
				"      <string>/RepositoryEntry/7208960/CourseNode/107718353400877</string>\n" +
				"</entry>";
		GuiPreference gp21 = guiPreferenceService.createGuiPreferenceEntry(identity2, attributedClass, prefKey2, prefValueId21);
		assertNotNull(gp21);

		guiPreferenceService.persistOrLoad(gp21);
		dbInstance.commitAndCloseSession();

		List<GuiPreference> guiPreferencesDeletedPre = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, null, null);
		assertEquals(2, guiPreferencesDeletedPre.size());
		guiPreferenceService.deleteGuiPrefsByUniqueProperties(identity, attributedClass, prefKey1);
		List<GuiPreference> guiPreferencesDeleted = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, null, null);
		assertEquals(1, guiPreferencesDeleted.size());
		assertEquals(attributedClass2, guiPreferencesDeleted.get(0).getAttributedClass());
		assertEquals(prefKey2, guiPreferencesDeleted.get(0).getPrefKey());
		List<GuiPreference> guiPreferencesDeleted2 = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity2, null, null);
		assertEquals(1, guiPreferencesDeleted2.size());
	}
}