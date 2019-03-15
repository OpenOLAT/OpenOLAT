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
package org.olat.core.commons.services.vfs.manager;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.version.VersionsFileImpl;
import org.olat.test.OlatTestCase;

/**
 * 
 * Initial date: 13 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSXStreamTest extends OlatTestCase {
	
	private static final OLog log = Tracing.createLoggerFor(VFSXStreamTest.class);
	
	@Test
	public void readOldVersions() throws IOException {
		VersionsFileImpl versions = null;
		try(InputStream in = VFSXStreamTest.class.getResourceAsStream("house.versions.jpg.xml")) {
			versions = (VersionsFileImpl)VFSXStream.read(in);
			
		} catch(IOException | OLATRuntimeException e) {
			log.error("", e);
			throw e;
		}
		
		Assert.assertNotNull(versions);
	}

}
