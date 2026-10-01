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
package org.olat.course.nodes;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;
import java.util.Locale;

import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.fileresource.types.PdfFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExportLinkEnum;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * Initial date: 2026-10-01<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class DocumentCourseNodeTest extends OlatTestCase {

	private static final String FILE_NAME = "document.pdf";

	@Test
	public void exportImport_withReference() throws Exception {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("doc-exp");
		RepositoryEntry source = createPdfEntry(author);
		DocumentCourseNode exportNode = createNode(source);
		File dir = Files.createTempDirectory("doc-node").toFile();

		exportNode.exportNode(dir, null, RepositoryEntryImportExportLinkEnum.WITH_REFERENCE);
		assertThat(new File(dir, exportNode.getIdent()).list()).containsExactlyInAnyOrder("repo.xml", "repo.zip", "document.properties");
		DocumentCourseNode importNode = createNode(source);
		importNode.setIdent(exportNode.getIdent());
		importNode.importNode(dir, null, author, JunitTestHelper.getDefaultOrganisation(), Locale.GERMAN,
				RepositoryEntryImportExportLinkEnum.WITH_REFERENCE);

		RepositoryEntry imported = importNode.getReferencedRepositoryEntry();
		assertThat(imported).isNotNull();
		assertThat(imported.getKey()).isNotEqualTo(source.getKey());
		VFSLeaf leaf = importNode.getDocumentSource(null).getVfsLeaf();
		assertThat(leaf).isNotNull();
		assertThat(leaf.getName()).isEqualTo(FILE_NAME);
		assertThat(imported.getOlatResource().getResourceableTypeName()).isEqualTo(PdfFileResource.TYPE_NAME);
	}

	@Test
	public void importNode_none() throws Exception {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("doc-none");
		DocumentCourseNode node = createNode(createPdfEntry(author));
		File dir = Files.createTempDirectory("doc-node").toFile();

		node.importNode(dir, null, author, JunitTestHelper.getDefaultOrganisation(), Locale.GERMAN,
				RepositoryEntryImportExportLinkEnum.NONE);

		assertThat(node.getModuleConfiguration().has(DocumentCourseNode.CONFIG_DOC_REPO_SOFT_KEY)).isFalse();
		assertThat(node.isConfigValid().isError()).isTrue();
	}

	@Test
	public void importNode_softKey() throws Exception {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAuthor("doc-soft");
		RepositoryEntry source = createPdfEntry(author);
		DocumentCourseNode node = createNode(source);
		File dir = Files.createTempDirectory("doc-node").toFile();

		node.importNode(dir, null, author, JunitTestHelper.getDefaultOrganisation(), Locale.GERMAN,
				RepositoryEntryImportExportLinkEnum.WITH_SOFT_KEY);

		assertThat(node.getReferencedRepositoryEntry()).isEqualTo(source);
	}

	@Test
	public void isConfigValid_unresolvedSoftKey() {
		DocumentCourseNode node = new DocumentCourseNode();
		node.getModuleConfiguration().setStringValue(DocumentCourseNode.CONFIG_DOC_REPO_SOFT_KEY, "unknown-soft-key");

		assertThat(node.isConfigValid().isError()).isTrue();
	}

	private DocumentCourseNode createNode(RepositoryEntry entry) {
		DocumentCourseNode node = new DocumentCourseNode();
		node.setDocumentFromRepository(entry);
		return node;
	}

	private RepositoryEntry createPdfEntry(Identity author) throws Exception {
		File file = new File(Files.createTempDirectory("doc-source").toFile(), FILE_NAME);
		Files.writeString(file.toPath(), "%PDF-1.4");
		return RepositoryHandlerFactory.getInstance().getRepositoryHandler(PdfFileResource.TYPE_NAME)
				.importResource(author, null, "Document", "", RepositoryEntryImportExportLinkEnum.NONE,
						JunitTestHelper.getDefaultOrganisation(), Locale.GERMAN, file, FILE_NAME);
	}
}
