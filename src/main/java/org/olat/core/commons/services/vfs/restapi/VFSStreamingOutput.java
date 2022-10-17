/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.vfs.restapi;

import java.io.InputStream;
import java.io.OutputStream;

import jakarta.ws.rs.core.StreamingOutput;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * Initial Date:  26 jan. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class VFSStreamingOutput implements StreamingOutput {
	
	private static final Logger log = Tracing.createLoggerFor(VFSStreamingOutput.class);
	
	private final VFSLeaf leaf;
	
	public VFSStreamingOutput(VFSLeaf leaf) {
		this.leaf = leaf;
	}

	@Override
	public void write(OutputStream output) {
		try(InputStream in = leaf.getInputStream()) {
			FileUtils.copy(in, output);
		} catch(Exception e) {
			log.error("", e);
		}
	}
}