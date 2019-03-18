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
package org.olat.core.commons.modules.bc.comparators;

import java.text.Collator;
import java.util.Comparator;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;

/**
 * Sort by lock
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class LockComparator implements Comparator<VFSItem> {
	
	private final boolean sortAsc;
	private final Collator collator;
	private final VFSLockManager lockManager;
	
	public LockComparator(boolean sortAsc, Collator collator) {
		this.sortAsc = sortAsc;
		this.collator = collator;
		lockManager = CoreSpringFactory.getImpl(VFSLockManager.class);
	}
	
	@Override
	public int compare(VFSItem o1, VFSItem o2) {
		boolean l1 = lockManager.isLocked(o1, VFSLockApplicationType.vfs);
		boolean l2 = lockManager.isLocked(o2, VFSLockApplicationType.vfs);
		
		if(l1 && !l2) return sortAsc ? -1 : 1;
		if(!l1 && l2) return sortAsc ? 1 : -1;
		
		if (sortAsc) {
			if ((o1 instanceof VFSLeaf && o2 instanceof VFSLeaf) || (!(o1 instanceof VFSLeaf) && !(o2 instanceof VFSLeaf))) {
				return collator.compare(o1.getName(), o2.getName());
			} else {
				if (!(o1 instanceof VFSLeaf)) {

					return -1;
				} else {
					return 1;
				}
			}
		} else {
			if ((o1 instanceof VFSLeaf && o2 instanceof VFSLeaf) || (!(o1 instanceof VFSLeaf) && !(o2 instanceof VFSLeaf))) {
				return collator.compare(o2.getName(), o1.getName());
			} else {
				if (!(o1 instanceof VFSLeaf)) {

					return -1;
				} else {
					return 1;
				}
			}
		}
		
	}
}
