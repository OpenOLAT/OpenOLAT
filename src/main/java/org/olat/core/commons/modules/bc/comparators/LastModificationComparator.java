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

import java.util.Comparator;

import org.olat.core.util.vfs.VFSItem;

/**
 * 
 * Initial date: 4 janv. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LastModificationComparator implements Comparator<VFSItem> {

	@Override
	public int compare(VFSItem m1, VFSItem m2) {
		if(m1 == null) return -1;
		if(m2 == null) return 1;
		
		long t1 = m1.getLastModified();
		long t2 = m2.getLastModified();
		return Long.compare(t1, t2);
	}
}
