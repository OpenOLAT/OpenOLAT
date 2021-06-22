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
package org.olat.modules.portfolio.handler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.olat.modules.portfolio.Binder;

/**
 * 
 * Initial date: 22 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderXStreamTest {
	
	@Test
	public void readBinderExportXML() throws URISyntaxException, IOException {
		URL binderUrl = BinderXStreamTest.class.getResource("binder_simple.xml");
		File binderFile = new File(binderUrl.toURI());
		
		Binder binder = BinderXStream.fromPath(binderFile.toPath());
		Assert.assertNotNull(binder);
	}
	
	@Test
	public void readBinderAssignmentsExportXML() throws URISyntaxException, IOException {
		URL binderUrl = BinderXStreamTest.class.getResource("binder_assignments.xml");
		File binderFile = new File(binderUrl.toURI());
		
		Binder binder = BinderXStream.fromPath(binderFile.toPath());
		Assert.assertNotNull(binder);
	}
	
	@Test
	public void readWriteBinder() throws URISyntaxException, IOException {
		URL binderUrl = BinderXStreamTest.class.getResource("binder_simple.xml");
		File binderFile = new File(binderUrl.toURI());
		
		Binder binder = BinderXStream.fromPath(binderFile.toPath());
		Assert.assertNotNull(binder);
		
		String xml = BinderXStream.toXML(binder);
		Assert.assertNotNull(xml);
		
	}

}
