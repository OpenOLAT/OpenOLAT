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
package org.olat.modules.assessment;

import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.modules.assessment.model.OverridableImpl;

/**
 * 
 * Initial date: 27 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface Overridable<T> {
	
	public static <T> Overridable<T> empty() {
		return new OverridableImpl<>();
	}
	
	public static <T> Overridable<T> of(T current) {
		return new OverridableImpl<>(current);
	}
	
	public T getCurrent();

	/**
	 * Set the current value. If the current value was overridden before, the new
	 * value is not saved as the current value but as the original.
	 *
	 * @param current
	 */
	public void setCurrent(T current);

	/**
	 * Overrides the current by a custom value. The original value is set to the
	 * prior current value if not already set.
	 *
	 * @param custom
	 * @param by
	 * @param at
	 */
	public void override(T custom, Identity by, Date at);
	
	public boolean isOverridden();
	
	/**
	 * Reset the current value to the original value and deletes the override informations.
	 *
	 */
	public void reset();

	public T getOriginal();

	public Identity getModBy();

	public Date getModDate();
	
}