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
package org.olat.modules.portfolio;

/**
 * 
 * Initial date: 29.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderDeliveryOptions {
	
	private boolean allowNewEntries;
	private boolean allowDeleteBinder;
	private boolean allowTemplatesFolder;

	public boolean isAllowNewEntries() {
		return allowNewEntries;
	}

	public void setAllowNewEntries(boolean allowNewEntries) {
		this.allowNewEntries = allowNewEntries;
	}
	
	public boolean isAllowDeleteBinder() {
		return allowDeleteBinder;
	}

	public void setAllowDeleteBinder(boolean allowDeleteBinder) {
		this.allowDeleteBinder = allowDeleteBinder;
	}

	public boolean isAllowTemplatesFolder() {
		return allowTemplatesFolder;
	}

	public void setAllowTemplatesFolder(boolean allowTemplatesFolder) {
		this.allowTemplatesFolder = allowTemplatesFolder;
	}

	public static BinderDeliveryOptions defaultOptions() {
		BinderDeliveryOptions options = new BinderDeliveryOptions();
		options.setAllowNewEntries(false);
		options.setAllowDeleteBinder(false);
		options.setAllowTemplatesFolder(false);
		return options;
	}
}
