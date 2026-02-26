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
package org.olat.core.gui.render.velocity;

import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;

/**
 * 
 * Initial date: 26 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ReadOnlyContext extends AbstractContext {
	
	private final Context context;
	
	public ReadOnlyContext(Context context) {
		this.context = context;
	}

	@Override
	public Object internalGet(String key) {
		return context.get(key);
	}

	@Override
	public Object internalPut(String key, Object value) {
		return context.put(key, key);
	}

	@Override
	public boolean internalContainsKey(String key) {
		return context.containsKey(key);
	}

	@Override
	public String[] internalGetKeys() {
		return context.getKeys();
	}

	@Override
	public Object internalRemove(String key) {
		return null;
	}
}
