/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
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
package org.olat.core.commons.services.ai;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 *
 * Base AI service provider interface. Implement this interface to register an
 * AI service provider. To support specific features, also implement the
 * corresponding feature interfaces (e.g. AiMCQuestionGeneratorSPI).
 *
 * Initial date: 22.05.2024<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public interface AiSPI {

	/**
	 * @return The technical identifier of the SPI
	 */
	public String getId();

	/**
	 * @return The human readable identifier / name of the SPI, e.g the product name
	 */
	public String getName();

	/**
	 * @return true: this SPI is enabled by the admin; false: disabled
	 */
	public boolean isEnabled();

	/**
	 * Enable or disable this SPI. Stores the state persistently.
	 *
	 * @param enabled true to enable, false to disable
	 */
	public void setEnabled(boolean enabled);

	/**
	 * Factory method to create an admin interface to configure the SPI
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public Controller createAdminController(UserRequest ureq, WindowControl wControl);

}
