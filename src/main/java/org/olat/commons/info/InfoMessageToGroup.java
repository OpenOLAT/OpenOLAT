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
package org.olat.commons.info;

import org.olat.group.BusinessGroup;

/**
 * Interface for the respective Entity, which helps to get related objects
 * <p>
 * Initial date: Mai 09, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface InfoMessageToGroup {

	/**
	 * retrieve infoMessage object from a specific infoMessageToGroup entry
	 *
	 * @return related infoMessage object
	 */
	InfoMessage getInfoMessage();

	/**
	 * retrieve a businessGroup object from a specific infoMessageToGroup entry
	 *
	 * @return related businessGroup object
	 */
	BusinessGroup getBusinessGroup();
}
