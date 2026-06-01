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
package org.olat.core.dispatcher.mapper.sandbox;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.media.DefaultMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;

/**
 * 
 * Initial date: 26 mai 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ControllerDeliveryMapper implements Mapper {
	
	private static final Logger log = Tracing.createLoggerFor(ControllerDeliveryMapper.class);
	
	private Window window;
	private PopupBrowserWindow pbw;
	private final WindowControl wControl;
	private final ControllerCreator creator;
	
	public ControllerDeliveryMapper(WindowControl wControl, ControllerCreator creator) {
		this.wControl = wControl;
		this.creator = creator;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		return new WrapperResource(request);
	}
	
	private class WrapperResource extends DefaultMediaResource {
		
		private final HttpServletRequest request;
		
		public WrapperResource(HttpServletRequest request) {
			this.request = request;
			setLastModified(System.currentTimeMillis());
		}

		@Override
		public void prepare(HttpServletResponse response) {
			try {
				String uriPrefix = Settings.getServerContextPath() + DispatcherModule.PATH_AUTHENTICATED;
				UserRequest ureq = new UserRequestImpl(uriPrefix, request, response);
				UserSession usess = ureq.getUserSession();
				if(usess != null && usess.isAuthenticated()) {
					if(window == null) {
						Windows.getWindows(ureq).getWindowManager().setAjaxWanted(ureq);
						
						pbw = wControl.getWindowBackOffice()
								.getWindowManager().createNewPopupBrowserWindowFor(ureq, creator);
						pbw.setForPrint(false);
						window = pbw.getPopupWindowControl().getWindowBackOffice().getWindow();
						window.setUriPrefix(ureq.getUriPrefix());
						pbw.open(ureq);
						
						Windows.getWindows(ureq).registerWindow(window.getWindowBackOffice().getChiefController());
						ureq.overrideWindowComponentID(window.getDispatchID());
					}
					window.dispatchRequest(ureq, true);
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
}
