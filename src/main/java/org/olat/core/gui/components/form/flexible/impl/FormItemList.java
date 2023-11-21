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
package org.olat.core.gui.components.form.flexible.impl;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;

public final class FormItemList implements FormItemCollection {
	
	private List<FormItem> items;
	
	public FormItemList(int initialCapacity) {
		items = new ArrayList<>(initialCapacity);
	}

	public void add(FormItem item) {
		items.add(item);
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		return items;
	}

	@Override
	public FormItem getFormComponent(String name) {
		for (FormItem item : items) {
			if (name.equals(item.getName())) {
				return item;
			}
		}
		return null;
	}
	
}