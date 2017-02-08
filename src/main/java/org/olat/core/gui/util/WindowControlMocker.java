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
package org.olat.core.gui.util;

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.info.WindowControlInfo;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;

public class WindowControlMocker implements WindowControl{

	public WindowControlMocker() {

	}

	@Override
	public void pushToMainArea(Component comp) {
		
	}

	@Override
	public void pushAsModalDialog(Component comp) {
		
	}

	@Override
	public void pushAsCallout(Component comp, String targetId, CalloutSettings settings) {
		
	}

	@Override
	public void pop() {
		
	}

	@Override
	public void setInfo(String string) {
		
	}

	@Override
	public void setError(String string) {
		
	}

	@Override
	public void setWarning(String string) {
		
	}

	@Override
	public WindowControlInfo getWindowControlInfo() {
		return null;
	}

	@Override
	public void makeFlat() {
		
	}

	@Override
	public BusinessControl getBusinessControl() {
		
		BusinessControl control = new BusinessControl() {

			@Override
			public String getAsString() {
				return null;
			}

			@Override
			public List<ContextEntry> getEntries() {
				return Collections.<ContextEntry>emptyList();
			}
			
			@Override
			public List<ContextEntry> getEntriesDownTheControls() {
				return Collections.<ContextEntry>emptyList();
			}

			@Override
			public ContextEntry popLauncherContextEntry() {
				return null;
			}

			@Override
			public ContextEntry getCurrentContextEntry() {
				return null;
			}

			@Override
			public void setCurrentContextEntry(ContextEntry cw) {
			}

			@Override
			public void dropLauncherEntries() {

			}

			@Override
			public boolean hasContextEntry() {
				return false;
			}
			
		};
		
		return control;
		
	}

	@Override
	public WindowBackOffice getWindowBackOffice() {
		return null;
	}


}
