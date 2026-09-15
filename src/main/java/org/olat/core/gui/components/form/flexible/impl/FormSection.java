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
package org.olat.core.gui.components.form.flexible.impl;

import java.util.Objects;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;

/**
 * A titled group of form items, optionally rendered as a sub-title instead of
 * a legend, and optionally collapsible with the collapsed state kept in the
 * GUI preferences.
 *
 * Always renders full width in its parent: a FormSection is a grouping
 * element, not a labeled field, so it must never be squeezed into the parent's
 * label/field column grid (see {@link #FormSection(String, Translator)}).
 *
 * Display-side counterpart: {@link org.olat.core.gui.components.sections.Sections}.
 * Both share the header markup and behaviour, see
 * {@link org.olat.core.gui.components.sections.SectionHeaderRenderer}.
 *
 * Initial date: 2026-09-10<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FormSection extends FormLayoutContainer {

	public enum Level {
		TITLE,
		SUB_TITLE
	}

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(FormLayoutContainer.class);
	private static final String PAGE = VELOCITY_ROOT + "/form_section.html";

	private Level level = Level.TITLE;
	private boolean collapsible = false;
	private boolean collapsed = false;
	private String persistedStatusId;

	protected FormSection(String name, Translator formTranslator) {
		super(null, name, formTranslator, FormLayout.LAYOUT_DEFAULT.layout(), PAGE, FormLayout.LAYOUT_DEFAULT.domWrapperRequired(), null);
		setFormLayout("nolayout");
		updateContext();
	}

	public static FormSection create(String name, Translator formTranslator) {
		return new FormSection(name, formTranslator);
	}

	@Override
	protected FormVelocityContainer createFormVelocityContainer(String id, String name, String page, Translator formTranslator) {
		return new FormSectionVelocityContainer(id, name, page, this, formTranslator);
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		if (this.level != level) {
			this.level = level;
			updateContext();
		}
	}

	public boolean isCollapsible() {
		return collapsible;
	}

	public void setCollapsible(boolean collapsible) {
		if (this.collapsible != collapsible) {
			this.collapsible = collapsible;
			updateContext();
		}
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		if (this.collapsed != collapsed) {
			this.collapsed = collapsed;
			updateContext();
		}
	}

	/**
	 * Reloads the collapsed state from the GUI preferences and persists further
	 * toggles under this id. Call after the initial {@link #setCollapsed(boolean)}
	 * default has been set, so a first-ever visit keeps that default.
	 */
	public void setPersistedStatusId(UserRequest ureq, String id) {
		if (Objects.equals(this.persistedStatusId, id)) {
			return;
		}
		this.persistedStatusId = id;
		Preferences prefs = ureq.getUserSession().getGuiPreferences();
		Boolean persisted = (Boolean) prefs.get(FormSection.class, id);
		if (persisted != null) {
			this.collapsed = persisted.booleanValue();
		}
		updateContext();
	}

	String getPersistedStatusId() {
		return persistedStatusId;
	}

	private void updateContext() {
		getFormItemComponent().contextPut("sec_subTitle", Boolean.valueOf(level == Level.SUB_TITLE));
		getFormItemComponent().contextPut("sec_collapsible", Boolean.valueOf(collapsible));
		getFormItemComponent().contextPut("sec_collapsed", Boolean.valueOf(collapsed));
		getFormItemComponent().contextPut("sec_persisted", Boolean.valueOf(persistedStatusId != null));
	}

}
