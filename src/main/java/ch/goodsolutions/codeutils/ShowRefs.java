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
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland<br>
 * http://www.goodsolutions.ch All rights reserved.
 * <p>
 */
package ch.goodsolutions.codeutils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.util.FileUtils;
import org.olat.core.util.FileVisitor;

/**
 * Description:<br>
 * TODO: Felix Class Description for Copy
 * <P>
 * Initial Date: 21.04.2006 <br>
 * 
 * @author Felix
 */
public class ShowRefs {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final String base = "C:/home/patrick/workspace/olat3/webapp";
		
		//final String base = "C:/development/eclipseworkspace31/olat50head/webapp";

		//final String base = "C:/development/workspace/olat5head/webapp";
		//final String base = "C:/development/eclipseworkspace31/olat50head/webapp";
		final String srcbase = base + "/WEB-INF/src";
		
		final String pack = "org.olat.core";
		//final String pack = "org.olat.core.util";
		//final String pack = "org.olat.core.extensions";
		
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
					String data = FileUtils.load(file, "utf-8");
					Pattern p = Pattern.compile("\nimport (.*);");
					Matcher m = p.matcher(data);
					String match;
					while (m.find()) {
						match = m.group(1); // e.g. java.util.ArrayList
						if (!match.startsWith(pack+".")) {
							if (!onlyP) {
								String pke = match;
								if (!omitAllWithJava || !pke.startsWith("java")) {
									pset.add(pke +"\t("+file.getAbsolutePath().substring(srcbase.length())+")");
								}
							} else {
								String pk = match.substring(0, match.lastIndexOf("."));
								if (!omitAllWithJava || !pk.startsWith("java")) {
									pset.add(pk);
								}
							}
							
							//System.out.println(pk);
						}
					}
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
