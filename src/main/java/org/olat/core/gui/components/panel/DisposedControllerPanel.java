/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.panel;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.i18n.I18nModule;

/**
 * A completely passive panel, with a singleton pattern to minimize memory footprint.
 * 
 * Initial date: 10 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DisposedControllerPanel implements Component {

	private static final ComponentRenderer RENDERER = new DisposedControllerPanelRenderer();
	private static final Map<Locale, DisposedControllerPanel> PANELS = new ConcurrentHashMap<>();
	private final Locale locale;
	
	private DisposedControllerPanel(Locale locale) {
		this.locale = locale;
	}
	
	public static final DisposedControllerPanel panel(Locale locale) {
		Locale loc = locale == null ? I18nModule.getDefaultLocale() : locale;
		return PANELS.computeIfAbsent(loc, DisposedControllerPanel::new);
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	@Override
	public String getComponentName() {
		return "disposed";
	}

	@Override
	public String getDispatchID() {
		return "disposed";
	}

	@Override
	public String getTimestamp() {
		return null;
	}

	@Override
	public Translator getTranslator() {
		return null;
	}

	@Override
	public void setTranslator(Translator translator) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	public String getElementCssClass() {
		return null;
	}

	@Override
	public String getLayout() {
		return null;
	}

	@Override
	public void setLayout(String layout) {
		//
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void setEnabled(boolean enabled) {
		//
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public void setVisible(boolean visible) {
		//
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isDirtyForUser() {
		return false;
	}

	@Override
	public void setDirty(boolean dirty) {
		//
	}

	@Override
	public boolean isDomReplaceable() {
		return false;
	}

	@Override
	public void setDomReplaceable(boolean domReplaceable) {
		//
	}

	@Override
	public boolean getSpanAsDomReplaceable() {
		return false;
	}

	@Override
	public void setSpanAsDomReplaceable(boolean spanReplaceable) {
		//
	}

	@Override
	public boolean isSilentlyDynamicalCmp() {
		return false;
	}

	@Override
	public ComponentCollection getParent() {
		return null;
	}

	@Override
	public void setParent(ComponentCollection parent) {
		//
	}

	@Override
	public void addListener(ComponentEventListener controller) {
		//
	}

	@Override
	public void removeListener(ComponentEventListener controller) {
		//
	}

	@Override
	public List<ComponentEventListener> debuginfoGetListeners() {
		return List.of();
	}

	@Override
	public void dispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		//
	}

	@Override
	public String getListenerInfo() {
		return "";
	}

	@Override
	public String getExtendedDebugInfo() {
		return "Disposed controller";
	}

	@Override
	public Controller getLatestDispatchedController() {
		return null;
	}

	@Override
	public Event getAndClearLatestFiredEvent() {
		return null;
	}

	@Override
	public boolean isDomReplacementWrapperRequired() {
		return false;
	}

	@Override
	public void setDomReplacementWrapperRequired(boolean required) {
		//
	}

	@Override
	public boolean isDomLayoutWrapper() {
		return false;
	}

	@Override
	public void setDomLayoutWrapper(boolean required) {
		//
	}
}
