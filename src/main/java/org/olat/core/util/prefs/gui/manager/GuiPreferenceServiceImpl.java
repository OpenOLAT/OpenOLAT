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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.prefs.db.PreferencesImpl;
import org.olat.core.util.prefs.gui.GuiPreference;
import org.olat.core.util.prefs.gui.GuiPreferenceService;
import org.olat.core.util.prefs.gui.model.GuiPreferenceImpl;
import org.olat.core.util.prefs.ram.RamPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial date: Dez 05, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class GuiPreferenceServiceImpl implements GuiPreferenceService {


	private static final Logger log = Tracing.createLoggerFor(GuiPreferenceServiceImpl.class);

	@Autowired
	private DB dbInstance;

	@Override
	public GuiPreference createGuiPreferenceEntry(IdentityRef identity, String attributedClass, String prefKey, String prefValue) {
		GuiPreferenceImpl guiPref = new GuiPreferenceImpl();
		guiPref.setCreationDate(new Date());
		guiPref.setLastModified(guiPref.getCreationDate());
		guiPref.setIdentity(identity);
		guiPref.setAttributedClass(attributedClass);
		guiPref.setPrefKey(prefKey);
		guiPref.setPrefValue(prefValue);

		return guiPref;
	}

	@Override
	public GuiPreference updateGuiPreferences(GuiPreference guiPreference) {
		guiPreference.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(guiPreference);
	}

	@Override
	public GuiPreference persistOrLoad(GuiPreference guiPreference) {
		try {
			dbInstance.commit();
			dbInstance.getCurrentEntityManager().persist(guiPreference);
			dbInstance.commit();
		} catch (PersistenceException | DBRuntimeException e) {
			if (PersistenceHelper.isConstraintViolationException(e)) {
				log.warn("", e);
				dbInstance.rollback();
				List<GuiPreference> guiPreferences = loadGuiPrefsByUniqueProperties(guiPreference.getIdentity(), guiPreference.getAttributedClass(), guiPreference.getPrefKey());
				// if all three parameters are set, then only one result can be loaded, thus .get(0)
				guiPreference = !guiPreferences.isEmpty() ? guiPreferences.get(0) : null;
			} else {
				log.warn("", e);
			}
		}
		return guiPreference;
	}

	@Override
	public List<GuiPreference> loadGuiPrefsByUniqueProperties(IdentityRef identity, String attributedClass, String prefKey) {
		QueryBuilder qb = new QueryBuilder();
		qb.append("select gp from guiprefs as gp");
		if (identity != null) {
			qb.and().append("gp.identity.key=:identityKey");
		}
		if (attributedClass != null) {
			qb.and().append("gp.attributedClass=:attributedClass");
		}
		if (prefKey != null) {
			qb.and().append("gp.prefKey=:prefKey");
		}

		TypedQuery<GuiPreference> query = dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), GuiPreference.class);

		if (identity != null) {
			query.setParameter("identityKey", identity.getKey());
		}
		if (attributedClass != null) {
			query.setParameter("attributedClass", attributedClass);
		}
		if (prefKey != null) {
			query.setParameter("prefKey", prefKey);
		}

		return query.getResultList().isEmpty() ? Collections.emptyList() : query.getResultList();
	}

	@Override
	public Map<String, Long> countDistinctAttrClass() {
		QueryBuilder qb = new QueryBuilder();
		qb.append("select gp.attributedClass, count(gp.attributedClass) from guiprefs as gp")
				.append(" group by gp.attributedClass");

		List<Object[]> countedObjects = dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), Object[].class)
				.getResultList();

		Map<String, Long> attrClassToCount = new HashMap<>();
		for (Object[] obj : countedObjects) {
			attrClassToCount.put((String) obj[0], (Long) obj[1]);
		}

		return attrClassToCount;
	}

	@Override
	public void deleteGuiPrefsByUniqueProperties(IdentityRef identity, String attributedClass, String prefKey) {
		List<GuiPreference> guiPreferences = loadGuiPrefsByUniqueProperties(identity, attributedClass, prefKey);
		for (GuiPreference guiPreference : guiPreferences) {
			if (guiPreference instanceof GuiPreferenceImpl impl && impl.getKey() != null) {
				dbInstance.deleteObject(impl);
			}
		}
	}

	@Override
	public void deleteGuiPreference(GuiPreference guiPreference) {
		if (guiPreference instanceof GuiPreferenceImpl impl && impl.getKey() != null) {
			dbInstance.deleteObject(impl);
		}
	}

	@Override
	public Preferences getPreferencesFor(Identity identity, boolean useTransientPreferences) {
		if (useTransientPreferences) {
			return new RamPreferences();
		} else {
			return new PreferencesImpl(this, identity);
		}
	}
}
