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
package org.olat.fileresource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.PathUtils;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.fileresource.types.ScormCPFileResource;

/**
 * Test if the system to detect SCORM or CP packages within a directory
 * works as expect with in several different constellations.
 * 
 * 
 * Initial date: 25.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FileResourceTest {
	
	@Test
	public void scormPackage() throws URISyntaxException {
		URL fileUrl = FileResourceTest.class.getResource("very_simple_scorm.zip");
		File file = new File(fileUrl.toURI());
		ResourceEvaluation eval = ScormCPFileResource.evaluate(file, file.getName());
		Assert.assertTrue(eval.isValid());
	}
	
	@Test
	public void scormPackage_withinDirectory() throws URISyntaxException {
		URL fileUrl = FileResourceTest.class.getResource("very_simple_scorm_in_directory.zip");
		File file = new File(fileUrl.toURI());
		ResourceEvaluation eval = ScormCPFileResource.evaluate(file, file.getName());
		Assert.assertTrue(eval.isValid());
	}
	
	@Test
	public void scormPackage_scosInSubDirectory() throws URISyntaxException, IOException {
		URL fileUrl = FileResourceTest.class.getResource("scorm_scos_in_sub_directory.zip");
		File file = new File(fileUrl.toURI());
		ResourceEvaluation eval = ScormCPFileResource.evaluate(file, file.getName());
		Assert.assertTrue(eval.isValid());
	}
	
	@Test
	public void scormPackage_invalid() throws URISyntaxException {
		URL fileUrl = FileResourceTest.class.getResource("invalid_scorm.zip");
		File file = new File(fileUrl.toURI());
		ResourceEvaluation eval = ScormCPFileResource.evaluate(file, file.getName());
		Assert.assertFalse(eval.isValid());
	}
	
	@Test
	public void qtiPackage() throws URISyntaxException, IOException {
		URL fileUrl = FileResourceTest.class.getResource("qti.zip");
		File file = new File(fileUrl.toURI());
		Path path = FileResource.getResource(file, file.getName());
		//must be root
		Assert.assertEquals(0, path.getNameCount());
		PathUtils.closeSubsequentFS(path);
	}
	


}
