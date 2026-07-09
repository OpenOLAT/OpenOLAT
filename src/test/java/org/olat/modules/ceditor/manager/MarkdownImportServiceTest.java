/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.ceditor.manager;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.model.ContainerLayout;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.model.jpa.ContainerPart;
import org.olat.modules.ceditor.model.jpa.ParagraphPart;
import org.olat.modules.ceditor.ui.PageElementTarget;

/**
 * Tests the column resolution of the markdown import. When importing into a
 * multi-column layout the imported parts AND the appended AI quiz placeholder
 * must land in the requested column, not always in the first one (OO-9497).
 *
 * Initial date: 9 Jul 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class MarkdownImportServiceTest {

	private ContainerSettings threeColumnSettings() {
		ContainerSettings settings = new ContainerSettings();
		settings.setType(ContainerLayout.block_3cols);
		settings.setNumOfColumns(3);
		return settings;
	}

	@Test
	public void effectiveColumnIndex_keepsRequestedColumn() {
		Assert.assertEquals(2, MarkdownImportService.effectiveColumnIndex(threeColumnSettings(), 2));
		Assert.assertEquals(1, MarkdownImportService.effectiveColumnIndex(threeColumnSettings(), 1));
	}

	@Test
	public void effectiveColumnIndex_firstColumn() {
		Assert.assertEquals(0, MarkdownImportService.effectiveColumnIndex(threeColumnSettings(), 0));
	}

	@Test
	public void effectiveColumnIndex_outOfRangeFallsBackToFirst() {
		Assert.assertEquals(0, MarkdownImportService.effectiveColumnIndex(threeColumnSettings(), 3));
		Assert.assertEquals(0, MarkdownImportService.effectiveColumnIndex(threeColumnSettings(), 99));
	}

	@Test
	public void effectiveColumnIndex_negativeFallsBackToFirst() {
		Assert.assertEquals(0, MarkdownImportService.effectiveColumnIndex(threeColumnSettings(), -1));
	}

	private ParagraphPart paragraph(long key) {
		ParagraphPart part = new ParagraphPart();
		part.setKey(Long.valueOf(key));
		return part;
	}

	/**
	 * A container whose layout was changed in the inspector: updateType() sets
	 * the new type but leaves the persisted numOfColumns field at its old
	 * value. The reference search must still find elements in the extra blocks.
	 */
	@Test
	public void resolveTargetContainer_findsReferenceInBlockBeyondStaleNumOfColumns() {
		ContainerSettings settings = new ContainerSettings();
		// default numOfColumns is 2; updateType does NOT touch it
		settings.updateType(ContainerLayout.block_3rows);
		settings.getColumn(2).getElementIds().add("42");

		ContainerPart container = new ContainerPart();
		container.setKey(Long.valueOf(7));
		container.setLayoutOptions(ContentEditorXStream.toXml(settings));

		int[] outColumn = new int[]{ 0 };
		int[] outInsertIndex = new int[]{ -1 };
		ContainerPart resolved = MarkdownImportService.resolveTargetContainer(List.of(container),
				null, -1, "42", PageElementTarget.below, outColumn, outInsertIndex);

		Assert.assertEquals(container, resolved);
		Assert.assertEquals(2, outColumn[0]);
		Assert.assertEquals(1, outInsertIndex[0]);
	}

	@Test
	public void pageInsertIndex_belowReferenceInsertsAfterIt() {
		List<PagePart> parts = List.of(paragraph(1), paragraph(2), paragraph(3));
		Assert.assertEquals(2, MarkdownImportService.pageInsertIndex(parts, "2", PageElementTarget.below));
	}

	@Test
	public void pageInsertIndex_aboveReferenceInsertsBeforeIt() {
		List<PagePart> parts = List.of(paragraph(1), paragraph(2), paragraph(3));
		Assert.assertEquals(1, MarkdownImportService.pageInsertIndex(parts, "2", PageElementTarget.above));
	}

	@Test
	public void pageInsertIndex_unknownReferenceAppendsAtEnd() {
		List<PagePart> parts = List.of(paragraph(1), paragraph(2));
		Assert.assertEquals(-1, MarkdownImportService.pageInsertIndex(parts, "99", PageElementTarget.below));
	}

	@Test
	public void pageInsertIndex_noReferenceOrWrongTargetAppendsAtEnd() {
		List<PagePart> parts = List.of(paragraph(1), paragraph(2));
		Assert.assertEquals(-1, MarkdownImportService.pageInsertIndex(parts, null, PageElementTarget.below));
		Assert.assertEquals(-1, MarkdownImportService.pageInsertIndex(parts, "1", PageElementTarget.within));
	}

	private java.util.ArrayList<String> ids(String... values) {
		return new java.util.ArrayList<>(List.of(values));
	}

	/**
	 * A follow-up part (the AI quiz placeholder) must land directly after the
	 * imported parts when they were inserted mid-column — not at the very end
	 * of the column (OO-9497).
	 */
	@Test
	public void addElementId_insertsAtGivenIndex() {
		List<String> elementIds = ids("a", "b", "c");
		MarkdownImportService.addElementId(elementIds, "quiz", 1);
		Assert.assertEquals(List.of("a", "quiz", "b", "c"), elementIds);
	}

	@Test
	public void addElementId_indexAtSizeAppends() {
		List<String> elementIds = ids("a", "b");
		MarkdownImportService.addElementId(elementIds, "quiz", 2);
		Assert.assertEquals(List.of("a", "b", "quiz"), elementIds);
	}

	@Test
	public void addElementId_negativeIndexAppends() {
		List<String> elementIds = ids("a", "b");
		MarkdownImportService.addElementId(elementIds, "quiz", -1);
		Assert.assertEquals(List.of("a", "b", "quiz"), elementIds);
	}

	@Test
	public void addElementId_outOfRangeIndexAppends() {
		List<String> elementIds = ids("a", "b");
		MarkdownImportService.addElementId(elementIds, "quiz", 5);
		Assert.assertEquals(List.of("a", "b", "quiz"), elementIds);
	}
}
