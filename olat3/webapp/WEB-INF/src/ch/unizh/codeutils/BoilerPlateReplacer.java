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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package ch.unizh.codeutils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.core.util.FileUtils;
import org.olat.core.util.FileVisitor;

/**
 * Description:<br>
 * TODO: patrickb Class Description for BoilerPlateReplacer
 * 
 * <P>
 * Initial Date:  29.09.2006 <br>
 * @author patrickb
 */
public class BoilerPlateReplacer {

	final static String newBoilerPlate = "/**\n" + "* OLAT - Online Learning and Training<br />\n" + "* http://www.olat.org\n" + "* <p>\n"
			+ "* Licensed under the Apache License, Version 2.0 (the \"License\"); <br />\n"
			+ "* you may not use this file except in compliance with the License.<br />\n" + "* You may obtain a copy of the License at\n"
			+ "* <p>\n" + "* http://www.apache.org/licenses/LICENSE-2.0\n" + "* <p>\n"
			+ "* Unless required by applicable law or agreed to in writing,<br />\n"
			+ "* software distributed under the License is distributed on an \"AS IS\" BASIS, <br />\n"
			+ "* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />\n"
			+ "* See the License for the specific language governing permissions and <br />\n" + "* limitations under the License.\n" + "* <p>\n"
			+ "* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />\n" + "* University of Zurich, Switzerland.\n"
			+ "* <p>\n" + "*/ \n";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final String base = "C:/home/patrick/workspace/olat3/webapp";
		
		final String srcbase = base + "/WEB-INF/src";
		
		final String pack = "org.olat";
		
		final boolean onlyP = false;
		final boolean omitAllWithJava = true;
		
		final String pkgbase = srcbase+"/"+pack.replace('.','/');

		final Set pset = new HashSet();
		
		FileVisitor fv2 = new FileVisitor() {

			public void visit(File file) {
				String fname = file.getName();
				//System.out.println("filename: "+file.getAbsolutePath());
				if (!file.isDirectory() && fname.endsWith(".java")) {
					// get the file
					System.out.println(fname);
					String data = FileUtils.load(file, "utf-8");
					data = data.replaceAll("/\\*\\*[^/]*http://[^/]*\\*/", newBoilerPlate);
					FileUtils.save(file, data, "utf-8");
				}					
			}
		};

		FileUtils.visitRecursively(new File(pkgbase), fv2);
		List li = new ArrayList(pset);
		Collections.sort(li);
		for (Iterator it_p = li.iterator(); it_p.hasNext();) {
			String pa = (String) it_p.next();
			if (pa.startsWith("org.olat")) System.out.println(pa);
			
		}
		
	}
}
