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

package org.olat.core.util.vfs.filters;

import org.olat.core.util.vfs.VFSItem;

/**
 * <h3>Description:</h3>
 * This filter implements a composite filter
 * 
 */
public abstract class VFSItemCompositeFilter implements VFSItemFilter {
	private VFSItemFilter compositeFilter;

	/**
	 * @param compositeFilter
	 */
	public final void setCompositeFilter(VFSItemFilter compositeFilter) {
		this.compositeFilter = compositeFilter;		
	}

	/**
	 * @see org.olat.core.util.vfs.filters.VFSItemFilter#accept(org.olat.core.util.vfs.VFSItem)
	 */
	public final boolean accept(VFSItem vfsItem) {
		// check first composite filter
		if (compositeFilter != null && ! compositeFilter.accept(vfsItem)) {
			return false;
		}
		// check now other filter
		return acceptFilter(vfsItem);
	}

	/**
	 * Implement this method for your filter, will be called by the original
	 * accept method of the abstract class
	 * 
	 * @param vfsItem
	 * @return
	 */
	protected abstract boolean acceptFilter(VFSItem vfsItem);

}
