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

package org.olat.core.logging;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;

public class LogHelper {
	private final static Pattern patWci = Pattern.compile(".*\\[(.*?)\\] DEBUG Window\\$WindowStats.*\\^%\\^ wci:(.*):%%.*");
	private final static Pattern patTime = Pattern.compile("([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9] [0-9][0-9]:[0-9][0-9]:[0-9][0-9]),.*\\[(.*?)\\] DEBUG Window.*\\^%\\^ time total to serve inline:([0-9]+)");

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File lo = new File("c:/tmp/otemp.txt");
		VFSLeaf l = new LocalFileImpl(lo);
		VFSLeaf to = new LocalFileImpl(new File("c:/tmp/output.txt"));
		System.out.println(buildUsageAndTimeStats(l, to));
	}
	
	public static String buildUsageAndTimeStats(VFSLeaf logfile, VFSLeaf toFile) {
		
		try {
			//String test = "2006-12-05 07:16:07,605 [TP-Processor15] DEBUG Window$WindowStats  - OLAT::DEBUG ^%^ D1207948 ^%^ org.olat.core.gui.components ^%^ adminfj ^%^ 212.203.56.165 ^%^ http://www.olat.unizh.ch/olat/auth/1%3A4%3A58039%3A0%3A0%3Acid%3Asetlevel/?logger=org.olat.core.gui.components.Window&level=debug ^%^ Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.0.8) Gecko/20061025 Firefox/1.5.0.8 ^%^ n/a ^%^ wci:org.olat.core.commons.chiefcontrollers.FullChiefController:org.olat.admin.SystemAdminMainController:org.olat.admin.sysinfo.SysinfoController:%%loglevels%%dsf [TP-Processor15] DEBUG Window$WindowStats - OLAT::DEBUG ^%^ wci:oab.sdf:aasd.dsfdsf:%%dsfdsf  ";
			//String test = "[TP-Processor15] DEBUG Window  - ...i ^%^ time total to serve inline:178";			
			int level = 3;
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(toFile.getOutputStream(false)));
			BufferedReader br = new BufferedReader(new InputStreamReader(logfile.getInputStream(), "utf-8"));
			String line;
			StringBuilder sb = new StringBuilder();
			Map<String,String> temp = new HashMap<String,String>();
			while ((line = br.readLine()) != null) {
				// if curline has DEBUG Window$WindowStats -> grep wci:.... part
				Matcher m = patWci.matcher(line);
				if (m.matches()) {
					String procnum = m.group(1);
					String wci = m.group(2);
					// bla:blu:blö -> if level = 1 -> bla, if 2 -> blu, etc.
					String[] cuts = wci.split(":");
					String to = "";
					for (int i = 0; i < cuts.length && i < level; i++) {						
						to+= cuts[i] + ":";
					}
					
					temp.put(procnum, to);
				} else {
					// try second matcher
					Matcher mt = patTime.matcher(line);
					if (mt.matches()) {
						String timestamp = mt.group(1);
						String procnum = mt.group(2);
						String time = mt.group(3);
						// retrieve previously logged wci
						String wci = temp.get(procnum);
						temp.remove(procnum);
						if (wci != null) {
							String all = timestamp+";"+time+";"+wci;
							//System.out.println(all);
							bw.write(all+"\r\n");
						} // else WARN, inconsistent log, maybe in progress of appending
					}
				}
			}
			br.close();
			bw.close();
			return sb.toString();
		} catch (IOException e) {
			//throw new OLATRuntimeException(LogHelper.class, "error reading OLAT error log at " + logfile.getName(), e);
		}
		return null; // some i/o error? can that happen since the log is only opened for appending?
	}

}
