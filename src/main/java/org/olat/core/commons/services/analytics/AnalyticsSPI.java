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
package org.olat.core.commons.services.analytics;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.StringOutput;

/**
 * The AnalyticsSPI offers methods to analyse site usage and patterns based on a
 * specific implementations. The analytics service normally runs on another
 * system. An example of a well known analytics service is google analytics.
 * 
 * Initial date: 15 feb. 2018<br>
 * 
 * @author Florian Gnägi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
public interface AnalyticsSPI {

	/**
	 * @return A unique ID to identify the analytics service
	 */
	public String getId();

	/**
	 * @return A printable name for the analytics service. Is not
	 *         internationalized
	 */
	public String getName();

	/**
	 * Factory method to create an admin form controller to configure the
	 * analytics service.
	 * 
	 * @param ureq
	 *            The user request
	 * @param wControl
	 *            The window control
	 * @return The admin controller
	 */
	public Controller createAdminController(UserRequest ureq, WindowControl wControl);

	/**
	 * @return true: the service is properly configured; false: the service is
	 *         not configured
	 */
	public boolean isValid();

	/**
	 * Call this method on initial page load to inject necessary javascript code
	 * 
	 * @return javascript code that can be added to the header of the page
	 */
	public String analyticsInitPageJavaScript();

	/**
	 * Call this method on each change on the page that should be reported to
	 * the analytics tool. Add the necessary javascript code to the string
	 * builder which will then be executed on the client.
	 * 
	 * @param sb
	 *            The string builder to which the JavaScript code can be added
	 * @param title
	 *            The title of the current location
	 * @param url
	 *            The location as an URL part
	 */
	public void analyticsCountPageJavaScript(StringBuilder sb, String title, String url);
	
	/**
	 * The script can rely on the download attribute.
	 * 
	 * @param sb The string builder
	 */
	public void analyticsCountOnclickJavaScript(StringOutput sb);

}
