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
*/

package org.olat.gui.demo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.olat.core.util.FileUtils;


/**
 *  Description:<br>
 * 
 * @author Felix Jost
 */
public class CheckTranslationKeys {
	private static Map<File,Properties> fileToProp = new HashMap<>();
	private static Map<File,String> fileToCont = new HashMap<>();
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String path ="c:/workspace/olat3/webapp/";
		File basedir = new File(path);
		List<File> res = new ArrayList<>();
		buildList(res, basedir);
		for (Iterator<File> iter = res.iterator(); iter.hasNext();) {
			File file = iter.next();
			String name = file.getName();
			if (name.startsWith("LocalStrings") && name.endsWith(".properties")) {
				//
				Properties p = new Properties();
				p.load(new FileInputStream(file));
				fileToProp.put(file, p);
				System.out.println("read prop "+file.getAbsolutePath());
			}
			else if (name.toLowerCase().endsWith(".html") || name.toLowerCase().endsWith(".java")) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				FileUtils.copy(new FileInputStream(file), bos);
				String cont = bos.toString();
				fileToCont.put(file, cont);
				System.out.println("read java/html "+file.getAbsolutePath());
			}
		}
		// all in RAM now...
		// check
		findInHTMLorJava("sdfsdfsdfaaaaa"+"aaaaaaaaaaaafsdf");
		
		List<String> dispList = new ArrayList<>();
		for (Iterator<File> iter = fileToProp.keySet().iterator(); iter.hasNext();) {
			File fil = iter.next();
			Properties p = fileToProp.get(fil);
			for (Iterator<Object> iterator = p.keySet().iterator(); iterator.hasNext(); ) {
				String key = (String) iterator.next();
				boolean ok = findInHTMLorJava(key);
				String value = p.getProperty(key);
				if (!ok) {
					String msg = fil.getAbsolutePath().substring(path.length())+" unused key "+key+ "="+value;
					dispList.add(msg);
				}
			}
		}
		Collections.sort(dispList);
		for (Iterator<String> iter = dispList.iterator(); iter.hasNext();) {
			String out = iter.next();
			System.out.println(out);
		}
		
	}
	
	/**
	 * @param key
	 * @return True if key found.
	 */
	private static boolean findInHTMLorJava(String key) {
		String search = "\""+key+"\"";
		for (Iterator<File> iter = fileToCont.keySet().iterator(); iter.hasNext();) {
			File fil = iter.next();
			String cont = fileToCont.get(fil);
			if (cont.indexOf(search) != -1) {
				return true;
			}
		}
		return false;
	}

	private static void buildList(List<File> fileList, File cur) { 
		if (cur.isDirectory()) {
			File[] children = cur.listFiles();
			for (int i = 0; i < children.length; i++) {
				File curChd = children[i];
				buildList(fileList, curChd);
			}
		}
		else { // regular file
			fileList.add(cur);
		}
	}
}
