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

package org.olat.search.service.document.file;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.util.LittleEndian;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.logging.Tracing;
import org.olat.core.util.io.LimitedContentWriter;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.search.service.SearchResourceContext;

/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public class PowerPointDocument extends FileDocument {
	private static final long serialVersionUID = -6107766953370631805L;
	private static final Logger log = Tracing.createLoggerFor(PowerPointDocument.class);

	public static final String FILE_TYPE = "type.file.ppt";

	public PowerPointDocument() {
		super();
	}
	
	public static Document createDocument(SearchResourceContext leafResourceContext, VFSLeaf leaf) throws IOException,DocumentException,DocumentAccessException {
	    PowerPointDocument powerPointDocument = new PowerPointDocument();
	    powerPointDocument.init(leafResourceContext,leaf);
	    powerPointDocument.setFileType(FILE_TYPE);
		powerPointDocument.setCssIcon(CSSHelper.createFiletypeIconCssClassFor(leaf.getName()));
		if (log.isDebugEnabled()) log.debug(powerPointDocument.toString());
		return powerPointDocument.getLuceneDocument();
	}

	@Override
	public FileContent readContent(VFSLeaf leaf) throws IOException,DocumentException {
		if (log.isDebugEnabled()) log.debug("read PPT Content of leaf={}", leaf.getName());
		try (BufferedInputStream bis = new BufferedInputStream(leaf.getInputStream());
				LimitedContentWriter oStream = new LimitedContentWriter(100000, FileDocumentFactory.getMaxFileSize())) {
			extractText(bis, oStream);
			return new FileContent(oStream.toString());			
		} catch (Exception e) {
			throw new DocumentException("Can not read PPT Content. File=" + leaf.getName(), e);
		}
	}

	private void extractText(InputStream inStream, Writer outWriter) throws IOException {
		POIFSReader r = new POIFSReader();
		/* Register a listener for *all* documents. */
		r.registerListener(new MyPOIFSReaderListener(outWriter));
		r.read(inStream);
	}

	private static class MyPOIFSReaderListener implements POIFSReaderListener {
		private final Writer oStream;
	
		public MyPOIFSReaderListener(Writer oStream) {
			this.oStream = oStream;
		}
	
		@Override
		public void processPOIFSReaderEvent(POIFSReaderEvent event) {
			int errorCounter = 0;
      
			try(DocumentInputStream dis = event.getStream()) {
				byte[] btoWrite = new byte[dis.available()];
				dis.read(btoWrite, 0, dis.available());
				for (int i = 0; i < btoWrite.length - 20; i++) {
					long type = LittleEndian.getUShort(btoWrite, i + 2);
					long size = LittleEndian.getUInt(btoWrite, i + 4);
					if (type == 4008) {
						try {
							String chunk = new String(btoWrite, i + 4 + 1, (int)(size + 3));
							oStream.write(removeUnvisibleChars(chunk));
						} catch( IndexOutOfBoundsException ex) {
							errorCounter++;
						}
					}
				}
			} catch (Exception ex) {
				// Remove general Exception later, for now make it run 
				log.warn("Can not read PPT content.", ex);
			}
			if (errorCounter > 0 && log.isDebugEnabled()) {
				log.debug("Could not parse ppt properly. There were {} IndexOutOfBoundsException", errorCounter);
			}
		}
		
		/**
		 * Remove unvisible chars form input string.
		 * 
		 * @param inputString
		 * @return Return filtered string
		 */
		private String removeUnvisibleChars(String inputString) {
			Pattern p = Pattern.compile("[^a-zA-Z0-9\n\r!&#<>{}]");
			Matcher m = p.matcher(inputString);
			return m.replaceAll(" ");
		}
	}
}
