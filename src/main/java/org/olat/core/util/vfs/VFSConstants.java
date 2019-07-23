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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.util.vfs;

/**
 * Initial Date:  24.06.2005 <br>
 *
 * @author Felix Jost
 */
public class VFSConstants {
	/**
	 * Comment for <code>OK</code>
	 */
	public static final VFSStatus YES = new VFSStatus();
	/**
	 * Comment for <code>NO</code>
	 */
	public static final VFSStatus NO = new VFSStatus();
	/**
	 * Comment for <code>NO_SECURITY_DENIED</code>
	 */
	public static final VFSStatus NO_SECURITY_DENIED = new VFSStatus();
	/**
	 * Comment for <code>SUCCESS</code>
	 */
	public static final VFSStatus SUCCESS = new VFSStatus();
	/**
	 * 
	 */
	public static final VFSStatus ERROR_SOURCE_NOT_COPYABLE = new VFSStatus();
	
	/**
	 * Comment for <code>ERROR_FAILED</code>
	 */
	public static final VFSStatus ERROR_FAILED = new VFSStatus();

	/**
	 * Comment for <code>ERROR_QUOTA_EXCEEDED</code>
	 */
	public static final VFSStatus ERROR_QUOTA_EXCEEDED = new VFSStatus();
	
	/**
	 * thrown if on a copy operation, the source and target overlap.
	 */
	public static final VFSStatus ERROR_OVERLAPPING = new VFSStatus();
	
	/**
	 * thrown if on a copy operation, the source and target name identical.
	 */
	public static final VFSStatus ERROR_NAME_ALREDY_USED = new VFSStatus();

	public static final long UNDEFINED = -1;
}

