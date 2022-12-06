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
package org.olat.core.gui.components;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.gui.translator.Translator;



/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface Component {
	
	public String getComponentName();
	
	public String getDispatchID();
	
	public String getTimestamp();
	
	public Translator getTranslator();

	public void setTranslator(Translator translator);
	
	public ComponentRenderer getHTMLRendererSingleton();
	
	public String getElementCssClass();
	
	public String getLayout();
	
	public void setLayout(String layout);
	
	public boolean isEnabled();
	
	public void setEnabled(boolean enabled);
	
	public boolean isVisible();
	
	public void setVisible(boolean visible);
	
	public boolean isDirty();
	
	public boolean isDirtyForUser();
	
	public void setDirty(boolean dirty);
	
	public boolean isDomReplaceable();
	
	public void setDomReplaceable(boolean domReplaceable);
	
	public boolean getSpanAsDomReplaceable();

	public void setSpanAsDomReplaceable(boolean spanReplaceable);
	
	public boolean isSilentlyDynamicalCmp();
	
	public ComponentCollection getParent();
	
	public void setParent(ComponentCollection parent);
	
	public void addListener(ComponentEventListener controller);
	
	public void removeListener(ComponentEventListener controller);
	
	public List<ComponentEventListener> debuginfoGetListeners();
	
	public void dispatchRequest(UserRequest ureq);
	
	public void validate(UserRequest ureq, ValidationResult vr);
	
	public String getListenerInfo();
	
	public String getExtendedDebugInfo();
	
	public Controller getLatestDispatchedController();
	
	public Event getAndClearLatestFiredEvent();

	/**
	 * @return true: component does not print DOM ID on element; false:
	 *         component always outputs an element with the dispatch ID as DOM
	 *         ID
	 */
	public boolean isDomReplacementWrapperRequired();
	
	/**
	 * 
	 */
	public void setDomReplacementWrapperRequired(boolean required);

}