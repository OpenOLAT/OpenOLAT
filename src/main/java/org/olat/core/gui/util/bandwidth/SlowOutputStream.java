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
package org.olat.core.gui.util.bandwidth;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 
 * Description:<br>
 * inspired from http://jakarta.apache.org/jmeter for the licence see:
 * http://www.apache.org/licenses/LICENSE-2.0
 */

public class SlowOutputStream extends FilterOutputStream {

	private CPSPauser pauser;

	/**
	 * Create wrapped Output Stream toe emulate the requested CPS.
	 * 
	 * @param out
	 *            OutputStream
	 */
	public SlowOutputStream(OutputStream out, CPSPauser pauser) {
		super(out);
		this.pauser = pauser;
	}

	// Also handles write(byte[])
	public void write(byte[] b, int off, int len) throws IOException {
//		pauser.pause(len);
//		out.write(b, off, len);

		if (pauser.isOff()) {
			// unlimited, do not flush inbetween
			out.write(b, off, len);
		} else {
			// distribute answer more steadily
			int blocksize = 2048;
			// b[off] is the first byte written and b[off+len-1] is the last byte written by this operation
			for (int i = off; i < len+off; i++) {
				out.write(b[i]);
				if ((i % blocksize) == 0) {
					pauser.pause(blocksize);
					out.flush();
					//System.out.println("flushing "+blocksize+ " bytes");
				}
			}
			System.out.println("sent "+len+" bytes");
		}
		
	}

	public void write(int b) throws IOException {
		pauser.pause(1);
		out.write(b);
	}
}