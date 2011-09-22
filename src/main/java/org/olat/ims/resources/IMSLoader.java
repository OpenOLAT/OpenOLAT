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
package org.olat.ims.resources;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.dom4j.Document;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XMLParser;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  13.06.2006 <br>
 *
 * @author Felix Jost
 */
public class IMSLoader {
	
	/**
	 * Reads an IMS XML Document if supported by /org/olat/ims/resources.
	 * @param documentF
	 * @return document
	 */
	public static Document loadIMSDocument(VFSLeaf documentF) {
		InputStream in = null;
		BufferedInputStream bis = null;
		Document doc = null;
		try {
			in = documentF.getInputStream();
			bis = new BufferedInputStream(in);
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			doc = xmlParser.parse(bis, false);
		}	catch (Exception e) { return null; }
		finally {
			try {
				if (in != null)	in.close();
				if (bis != null) bis.close();
			}	catch (Exception e) {
				// we did our best to close the inputStream
			}
		}
		return doc;
	}
	
	/**
	 * Reads an IMS XML Document if supported by /org/olat/ims/resources.
	 * @param documentF
	 * @return document
	 */
	public static Document loadIMSDocument(File documentF) {
		FileInputStream in = null;
		BufferedInputStream bis = null;
		Document doc = null;
		try {
			in = new FileInputStream(documentF);
			bis = new BufferedInputStream(in);
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			doc = xmlParser.parse(bis, false);
		}	catch (Exception e) { return null; }
		finally {
			try {
				if (in != null)	in.close();
				if (bis != null) bis.close();
			}	catch (Exception e) {
				// we did our best to close the inputStream
			}
		}
		return doc;
	}
}
