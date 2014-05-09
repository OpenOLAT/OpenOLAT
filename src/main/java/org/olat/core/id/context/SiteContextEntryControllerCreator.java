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

package org.olat.core.id.context;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.navigation.SiteInstance;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  18 janv. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SiteContextEntryControllerCreator extends DefaultContextEntryControllerCreator {
	private final Class<? extends SiteInstance> site;
	
	public SiteContextEntryControllerCreator(Class<? extends SiteInstance> site) {
		this.site = site;
	}

	@Override
	public ContextEntryControllerCreator clone() {
		return this;
	}

	@Override
	public String getSiteClassName(List<ContextEntry> ces, UserRequest ureq) {
		return site.getName();
	}
}
