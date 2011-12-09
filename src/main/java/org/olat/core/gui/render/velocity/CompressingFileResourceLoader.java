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
package org.olat.core.gui.render.velocity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.olat.core.logging.AssertException;
import org.olat.core.util.FileUtils;

/**
 * Description:<br>
 * Compresses a velocity template before delivering it.
 * since velocity doesn't provide easy access to its AST, we do it here on the inputstream level.
 * - change multiple spaces into a single one.
 * - ... 
 * 
 * <P>
 * Initial Date: 20.09.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class CompressingFileResourceLoader extends FileResourceLoader {
	private final static Pattern p0 = Pattern.compile("[\\t ]*##.*$", Pattern.MULTILINE);
	private final static Pattern p = Pattern.compile("[\\t ]{2,}", Pattern.MULTILINE);
	private final static Pattern p2 = Pattern.compile("^[\\t ]+", Pattern.MULTILINE);	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.velocity.runtime.resource.loader.FileResourceLoader#getResourceStream(java.lang.String)
	 */
	@Override
	public InputStream getResourceStream(String templateName) throws ResourceNotFoundException {
		InputStream origIs = super.getResourceStream(templateName);
		// only compress .html files
		if (!templateName.endsWith(".html")) {
			return origIs;
		}
		String enc = VelocityModule.getInputEncoding();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FileUtils.copy(origIs, baos);
		FileUtils.closeSafely(origIs);
		
		try {
			String templateContent = baos.toString(enc);
			String a = templateContent;
			//System.out.println("load..." + templateName);
			a = p0.matcher(a).replaceAll("");	
			//p0.matcher("").appendReplacement(sb, replacement)
			a = p.matcher(a).replaceAll(" ");			
			a = p2.matcher(a).replaceAll("");

			//Pattern p3 = Pattern.compile(">[\n\r]+<", Pattern.MULTILINE);
			//a = p3.matcher(a).replaceAll("><");
			//System.out.println("% compressed: old: "+templateContent.length()+", new: "+a.length()+" new % "+(100*a.length()/templateContent.length()));
			ByteArrayInputStream bis = new ByteArrayInputStream(a.getBytes(enc));
			return bis;
		} catch (UnsupportedEncodingException e) {
			throw new AssertException("cannot handle encoding:"+enc, e);
		}
		
		
	}
}
