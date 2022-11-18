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
package org.olat.core.gui.components.emptystate;

import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.velocity.VelocityContainer;

/**
 * 
 * Initial date: 22 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EmptyStateFactory {
	
	public static EmptyState create(String name, VelocityContainer vc, ComponentEventListener listener, EmptyStateConfig config) {
		EmptyState emptyState = create(name, vc, listener);
		emptyState.setIconCss(config.getIconCss());
		emptyState.setIndicatorIconCss(config.getIndicatorIconCss());
		emptyState.setMessageI18nKey(config.getMessageI18nKey());
		emptyState.setMessageI18nArgs(config.getMessageI18nArgs());
		emptyState.setMessageTranslated(config.getMessageTranslated());
		emptyState.setHintI18nKey(config.getHintI18nKey());
		emptyState.setHintI18nArgs(config.getHintI18nArgs());
		emptyState.setButtonI18nKey(config.getButtonI18nKey());
		return emptyState;
	}
	
	public static EmptyState create(String name, VelocityContainer vc, ComponentEventListener listener) {
		EmptyState emptyState = new EmptyState(name);
		if (listener != null) {
			emptyState.addListener(listener);
		}
		if (vc != null) {
			vc.put(emptyState.getComponentName(), emptyState);
		}
		return emptyState;
	}

}
