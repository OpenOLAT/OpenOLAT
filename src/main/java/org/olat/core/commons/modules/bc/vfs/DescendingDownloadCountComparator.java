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
package org.olat.core.commons.modules.bc.vfs;

import java.util.Comparator;

import org.olat.core.util.vfs.VFSLeaf;

/**
 * <p>
 * Ordering OlatRootFileImpls by their download count descendingly
 * <p>
 * Initial Date:  Sep 16, 2009 <br>
 * @author gwassmann, gwassmann@frentix.com, www.frentix.com
 */
public class DescendingDownloadCountComparator implements Comparator<VFSLeaf> {
	
	@Override
	public int compare(VFSLeaf o1, VFSLeaf o2) {
		int d1 = o1.getMetaInfo().getDownloadCount();
		int d2 = o2.getMetaInfo().getDownloadCount();
		return d1 > d2 ? -1 : 1;
	}
}
