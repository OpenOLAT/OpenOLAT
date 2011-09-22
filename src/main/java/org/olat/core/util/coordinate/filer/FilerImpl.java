/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.core.util.coordinate.filer;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.Filer;

/**
 * Description:<br>
 * an implementation of the Filer, for both singleVm and cluster mode.
 * @deprecated work in progress
 * 
 * <P>
 * Initial Date:  12.11.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class FilerImpl implements Filer {

	private String rootDir; // the path to the root directory of all data for the filer, e.g. c:/olatdata/newroot
	
	public FileTransaction getCurrentFileTransaction() {
		return null;
	}
	
	/**
	 * [called by spring]
	 *
	 */
	public void init() {
		//
	}

	String getRootDir() {
		return rootDir;
	}

	/**
	 * [used by spring]
	 * @param rootDir
	 */
	public void setRootDir(String rootDir) {
		this.rootDir = rootDir;
	}


	/**
	 * @param ores
	 * @return
	 */
	public String createTempFile(OLATResourceable ores) {
		// TODO Auto-generated method stub
		return null;
	}

}
