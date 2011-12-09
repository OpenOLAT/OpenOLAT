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

import java.io.UnsupportedEncodingException;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import org.olat.core.logging.OLATRuntimeException;

/**
 * Description:<br>
 * Log handler that logs to a virtual file system leaf. The handler opens the file
 * for each log entry and closes it after that immediately. (in order to not waste too many unix filehandles)
 * <P>
 * Initial Date:  Aug 30, 2005 <br>
 * @author gnaegi
 */
public class VFSLeafHandler extends StreamHandler {
	private VFSLeaf vfsLeaf;
		
	/**
	 * @param vfsLeaf The logfile
	 * @param formatter
	 */
	public VFSLeafHandler(VFSLeaf vfsLeaf, Formatter formatter) {
		setFormatter(formatter);
		try {
			setEncoding("utf8");
		} catch (SecurityException e) {
			throw new OLATRuntimeException(this.getClass(), "Error when setting encoding of to 'utf8' ",e);
		} catch (UnsupportedEncodingException e) {
			throw new OLATRuntimeException(this.getClass(), "Error when setting encoding of to 'utf8' ",e);
		}
		this.vfsLeaf = vfsLeaf;
	}
	
	/**
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */
	public synchronized void publish(LogRecord arg0) {  //o_clusterOK by:fj, as long as file is written to from one vm only
		setOutputStream(vfsLeaf.getOutputStream(true));
		super.publish(arg0);
		close();
	}
}
