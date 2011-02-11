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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 
package org.olat.core.gui.dev.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.olat.core.gui.dev.IDE;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for Trans
 * 
 * <P>
 * Initial Date: 05.02.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class EclipseIDE implements IDE {

	/**
	 * 
	 */
	public EclipseIDE() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.dev.IDE#openJavaSource(java.lang.String)
	 */
	public void openJavaSource(String className) {
		// connect to eclipse over localhost 8080.
		// we use a very simple protocol as com:numofbytesfollowing:commanddata
		try {
			Socket s = new Socket(InetAddress.getLocalHost(), 8080);
			OutputStream os = s.getOutputStream();
			String all = "1:"+className.length()+":"+className;
			os.write(all.getBytes("utf-8"));
			os.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	// TODO Auto-generated method stub

	}

}
