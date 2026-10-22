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
package org.olat.ims.qti21.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.ForbiddenMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Check that the mapper only delivers files from the package,
 * the submission directory and the assessment documents (OO-9957).
 *
 * Initial date: 22 oct. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ResourcesMapperTest extends OlatTestCase {

	private static final Logger log = Tracing.createLoggerFor(ResourcesMapperTest.class);

	private File itemFile;
	private File packageDir;
	private File submissionDir;
	private File tmpFolder;

	@Autowired
	private WebappHelper webappHelper;
	
	/**
	 * Layout:
	 * <pre>
	 * secret.txt
	 * package/item.xml
	 * package/image.png
	 * session/submissions/upload.txt
	 * session/assessmentdocs/123/doc.pdf
	 * </pre>
	 */
	@Before
	public void setUp() throws IOException {
		Assert.assertNotNull(webappHelper);
		
		String tmpDir = WebappHelper.getUserDataRoot();
		tmpFolder = new File(new File(tmpDir), UUID.randomUUID().toString());

		write(new File(tmpFolder, "secret.txt"));

		packageDir = new File(tmpFolder, "package");
		itemFile = write(new File(packageDir, "item.xml"));
		write(new File(packageDir, "image.png"));

		File sessionDir = new File(tmpFolder, "session");
		submissionDir = new File(sessionDir, "submissions");
		write(new File(submissionDir, "upload.txt"));
		write(new File(sessionDir, "assessmentdocs/123/doc.pdf"));
	}
	
	@After
	public void cleanUpFolder() {
		try {
			FileUtils.deleteDirsAndFiles(tmpFolder.toPath());
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Test
	public void packageResource() {
		ResourcesMapper mapper = new ResourcesMapper(itemFile.toURI(), packageDir, submissionDir);
		MediaResource resource = mapper.handle(null, requestWithHref("image.png"));
		Assert.assertTrue(resource instanceof FileMediaResource);
	}

	@Test
	public void packageResourceXmlForbidden() {
		ResourcesMapper mapper = new ResourcesMapper(itemFile.toURI(), packageDir, submissionDir);
		MediaResource resource = mapper.handle(null, requestWithHref("item.xml"));
		Assert.assertTrue(resource instanceof ForbiddenMediaResource);
	}

	@Test
	public void packageResourceTraversal() {
		ResourcesMapper mapper = new ResourcesMapper(itemFile.toURI(), packageDir, submissionDir);
		MediaResource resource = mapper.handle(null, requestWithHref("../secret.txt"));
		Assert.assertTrue(resource instanceof ForbiddenMediaResource);
	}

	@Test
	public void submission() {
		ResourcesMapper mapper = new ResourcesMapper(itemFile.toURI(), packageDir, submissionDir);
		MediaResource resource = mapper.handle(null, requestWithHref("submissions/1/upload.txt"));
		Assert.assertTrue(resource instanceof FileMediaResource);
	}

	@Test
	public void submissionTraversal() {
		ResourcesMapper mapper = new ResourcesMapper(itemFile.toURI(), packageDir, submissionDir);
		MediaResource resource = mapper.handle(null, requestWithHref("submissions/1/../../secret.txt"));
		Assert.assertFalse(resource instanceof FileMediaResource);
	}

	@Test
	public void submissionTraversalWithData() {
		ResourcesMapper mapper = new ResourcesMapper(itemFile.toURI(), packageDir, submissionDir);
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("data", "submissions/1/../../secret.txt");
		MediaResource resource = mapper.handle(null, request);
		Assert.assertFalse(resource instanceof FileMediaResource);
	}

	@Test
	public void submissionTraversalWithRelPath() {
		ResourcesMapper mapper = new ResourcesMapper(itemFile.toURI(), packageDir, submissionDir);
		MediaResource resource = mapper.handle("/submissions/1/../../secret.txt", new MockHttpServletRequest());
		Assert.assertFalse(resource instanceof FileMediaResource);
	}

	@Test
	public void submissionMap() {
		ResourcesMapper mapper = new ResourcesMapper(itemFile.toURI(), packageDir, Map.of(1l, submissionDir));
		MediaResource resource = mapper.handle(null, requestWithHref("submissions/1/upload.txt"));
		Assert.assertTrue(resource instanceof FileMediaResource);
	}

	@Test
	public void submissionMapTraversal() {
		ResourcesMapper mapper = new ResourcesMapper(itemFile.toURI(), packageDir, Map.of(1l, submissionDir));
		MediaResource resource = mapper.handle(null, requestWithHref("submissions/1/../../secret.txt"));
		Assert.assertFalse(resource instanceof FileMediaResource);
	}

	@Test
	public void assessmentDocument() {
		ResourcesMapper mapper = new ResourcesMapper(itemFile.toURI(), packageDir, submissionDir);
		MediaResource resource = mapper.handle(null, requestWithHref("assessmentdocs/1/123/doc.pdf"));
		Assert.assertTrue(resource instanceof FileMediaResource);
	}

	@Test
	public void assessmentDocumentTraversal() {
		ResourcesMapper mapper = new ResourcesMapper(itemFile.toURI(), packageDir, submissionDir);
		MediaResource resource = mapper.handle(null, requestWithHref("assessmentdocs/1/123/../../../secret.txt"));
		Assert.assertFalse(resource instanceof FileMediaResource);
	}

	/**
	 * The item session segment itself is used to escape the storage.
	 */
	@Test
	public void assessmentDocumentTraversalInItemSession() {
		ResourcesMapper mapper = new ResourcesMapper(itemFile.toURI(), packageDir, submissionDir);
		MediaResource resource = mapper.handle(null, requestWithHref("assessmentdocs/1/../../secret.txt"));
		Assert.assertFalse(resource instanceof FileMediaResource);
	}

	@Test
	public void assessmentDocumentMapTraversal() {
		ResourcesMapper mapper = new ResourcesMapper(itemFile.toURI(), packageDir, Map.of(1l, submissionDir));
		MediaResource resource = mapper.handle(null, requestWithHref("assessmentdocs/1/../../secret.txt"));
		Assert.assertFalse(resource instanceof FileMediaResource);
	}

	private static MockHttpServletRequest requestWithHref(String href) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("href", href);
		return request;
	}

	private static File write(File file) throws IOException {
		file.getParentFile().mkdirs();
		Files.writeString(file.toPath(), file.getName());
		return file;
	}
}
