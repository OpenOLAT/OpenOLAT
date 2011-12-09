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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.util.LittleEndian;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * @author Christian Guretzki
 */
public class PPT2Text {
	
	
  public static void extractText(InputStream inStream, OutputStream stream ) throws IOException {
    POIFSReader r = new POIFSReader();
    /* Register a listener for *all* documents. */
    r.registerListener(new MyPOIFSReaderListener(stream));
    r.read(inStream);
  }

  static class MyPOIFSReaderListener implements POIFSReaderListener {
  	private static final OLog log = Tracing.createLoggerFor(PPT2Text.class);

	  private final OutputStream oStream;
	
	  public MyPOIFSReaderListener(OutputStream oStream) {
			this.oStream = oStream;
		}
	
		public void processPOIFSReaderEvent(POIFSReaderEvent event) {
      int errorCounter = 0;
      
      try {
        DocumentInputStream dis = null;
        dis = event.getStream();
        
        byte btoWrite[] = new byte[dis.available()];
        dis.read(btoWrite, 0, dis.available());
        for (int i = 0; i < btoWrite.length - 20; i++) {
          long type = LittleEndian.getUShort(btoWrite, i + 2);
          long size = LittleEndian.getUInt(btoWrite, i + 4);
          if (type == 4008) {
            try {
          	  oStream.write(btoWrite, i + 4 + 1, (int) size + 3);
            } catch( IndexOutOfBoundsException ex) {
              errorCounter++;
            }
          }
        }
      } catch (Exception ex) {
      	// FIXME:chg: Remove general Exception later, for now make it run 
        log.warn("Can not read PPT content.", ex);
      }
      if (errorCounter > 0) {
      	if (log.isDebug()) log.debug("Could not parse ppt properly. There were " + errorCounter + " IndexOutOfBoundsException");
      }
    }
	}
}