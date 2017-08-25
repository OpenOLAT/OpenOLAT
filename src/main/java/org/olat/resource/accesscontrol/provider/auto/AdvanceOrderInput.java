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
package org.olat.resource.accesscontrol.provider.auto;

import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.resource.accesscontrol.provider.auto.model.AutoAccessMethod;

/**
 * Parameter Object to bundle the parameters to create the advance orders.
 *
 * Initial date: 17.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface AdvanceOrderInput {

	/**
	 * The class of the AccessMethod to choose.
	 */
	public Class<? extends AutoAccessMethod> getMethodClass();

	/**
	 * The identity for which the advance orders are created.
	 */
	public Identity getIdentity();

	/**
	 * The keys of the course attributes to search for.
	 */
	public Set<IdentifierKey> getKeys();

	/**
	 * The raw values to search.
	 */
	public String getRawValues();

	/**
	 * Type of the splitter to split the raw values in a set of single values.
	 */
	public String getSplitterType();
}
