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

import org.olat.core.gui.GlobalSettings;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.WindowSettings;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.guistack.GuiStack;
import org.olat.core.gui.control.info.WindowControlInfo;
import org.olat.core.gui.control.util.ZIndexWrapper;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.WindowManagerImpl;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.event.GenericEventListener;

public class WindowControlMocker implements WindowControl{
	
	private final WindowBackOfficeMocker windowBackOffice = new WindowBackOfficeMocker();

	public WindowControlMocker() {
		//
	}

	@Override
	public void pushToMainArea(Component comp) {
		//
	}

	@Override
	public void pushAsModalDialog(Component comp) {
		//
	}

	@Override
	public boolean removeModalDialog(Component comp) {
		return false;
	}
	
	@Override
	public void pushAsTopModalDialog(Component comp) {
		//
	}

	@Override
	public boolean removeTopModalDialog(Component comp) {
		return false;
	}

	@Override
	public void pushAsCallout(Component comp, String targetId, CalloutSettings settings) {
		//
	}

	@Override
	public void pushFullScreen(Controller ctrl, String bodyClass) {
		//
	}

	@Override
	public void pop() {
		//
	}

	@Override
	public void setInfo(String string) {
		//
	}

	@Override
	public void setError(String string) {
		//
	}

	@Override
	public void setWarning(String string) {
		//
	}

	@Override
	public WindowControlInfo getWindowControlInfo() {
		return null;
	}

	@Override
	public void makeFlat() {
		//
	}

	@Override
	public BusinessControl getBusinessControl() {
		
		return new BusinessControl() {

			@Override
			public String getAsString() {
				return "";
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
				//
			}

			@Override
			public void dropLauncherEntries() {
				//
			}

			@Override
			public boolean hasContextEntry() {
				return false;
			}
		};
	}

	@Override
	public WindowBackOffice getWindowBackOffice() {
		return windowBackOffice;
	}

	public static class WindowBackOfficeMocker implements WindowBackOffice {
		
		private final WindowManager windowManager = new WindowManagerImpl();

		@Override
		public void dispose() {
			//
		}

		@Override
		public WindowManager getWindowManager() {
			return windowManager;
		}

		@Override
		public Window getWindow() {
			return null;
		}

		@Override
		public ChiefController getChiefController() {
			return null;
		}

		@Override
		public Controller createDevelopmentController(UserRequest ureq, WindowControl windowControl) {
			return null;
		}

		@Override
		public GlobalSettings getGlobalSettings() {
			return null;
		}

		@Override
		public WindowSettings getWindowSettings() {
			return null;
		}

		@Override
		public void setWindowSettings(WindowSettings settings) {
			//
			
		}

		@Override
		public Controller createDebugDispatcherController(UserRequest ureq, WindowControl windowControl) {
			return null;
		}

		@Override
		public Controller createInlineTranslationDispatcherController(UserRequest ureq, WindowControl windowControl) {
			return null;
		}

		@Override
		public Controller createAJAXController(UserRequest ureq) {
			return null;
		}

		@Override
		public boolean isDebuging() {
			return false;
		}

		@Override
		public GuiStack createGuiStack(Component initialComponent) {
			return null;
		}

		@Override
		public void sendCommandTo(Command wco) {
			//
		}

		@Override
		public List<ZIndexWrapper> getGuiMessages() {
			return Collections.emptyList();
		}

		@Override
		public void addCycleListener(GenericEventListener gel) {
			//
		}

		@Override
		public void removeCycleListener(GenericEventListener gel) {
			//
		}
	}
}
