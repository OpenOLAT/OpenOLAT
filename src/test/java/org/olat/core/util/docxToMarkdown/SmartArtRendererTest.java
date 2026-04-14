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
package org.olat.core.util.docxToMarkdown;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

/**
 * @author gnaegi, https://www.frentix.com
 */
public class SmartArtRendererTest {

	@Test
	public void renderNullInputs() {
		assertNull(SmartArtRenderer.render(null, null, null, 0, 0, null));
	}

	@Test
	public void renderSimpleDiagram() throws Exception {
		// Create a minimal DOCX with a SmartArt drawing
		String drawingXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<dsp:drawing xmlns:dsp=\"http://schemas.microsoft.com/office/drawing/2008/diagram\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
			+ "<dsp:spTree>"
			+ "<dsp:nvGrpSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvGrpSpPr/></dsp:nvGrpSpPr>"
			+ "<dsp:grpSpPr/>"
			+ "<dsp:sp modelId=\"{1}\">"
			+ "<dsp:nvSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvSpPr/></dsp:nvSpPr>"
			+ "<dsp:spPr>"
			+ "<a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"500000\" cy=\"300000\"/></a:xfrm>"
			+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>"
			+ "<a:solidFill><a:schemeClr val=\"accent1\"/></a:solidFill>"
			+ "<a:ln w=\"19050\"><a:solidFill><a:schemeClr val=\"accent1\"/></a:solidFill></a:ln>"
			+ "</dsp:spPr>"
			+ "<dsp:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:t>Hello</a:t></a:r></a:p></dsp:txBody>"
			+ "</dsp:sp>"
			+ "<dsp:sp modelId=\"{2}\">"
			+ "<dsp:nvSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvSpPr/></dsp:nvSpPr>"
			+ "<dsp:spPr>"
			+ "<a:xfrm><a:off x=\"600000\" y=\"0\"/><a:ext cx=\"400000\" cy=\"300000\"/></a:xfrm>"
			+ "<a:prstGeom prst=\"chevron\"><a:avLst/></a:prstGeom>"
			+ "<a:solidFill><a:schemeClr val=\"accent1\"/></a:solidFill>"
			+ "<a:ln w=\"19050\"><a:solidFill><a:schemeClr val=\"accent1\"/></a:solidFill></a:ln>"
			+ "</dsp:spPr>"
			+ "</dsp:sp>"
			+ "</dsp:spTree></dsp:drawing>";

		File docx = File.createTempFile("smartart_test", ".docx", new File("target"));
		docx.deleteOnExit();
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(docx))) {
			zos.putNextEntry(new ZipEntry("word/document.xml"));
			zos.write("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body/></w:document>"
				.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
			zos.putNextEntry(new ZipEntry("word/diagrams/drawing1.xml"));
			zos.write(drawingXml.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}

		File mediaDir = Files.createTempDirectory(java.nio.file.Path.of("target"), "smartart_media_").toFile();
		mediaDir.deleteOnExit();

		try (ZipFile zf = new ZipFile(docx)) {
			String svgFile = SmartArtRenderer.render(zf, "diagrams/drawing1.xml", mediaDir,
				5486400, 3200400, Collections.emptyMap());

			assertNotNull("SVG file must be created", svgFile);
			assertTrue("Filename must end with .svg", svgFile.endsWith(".svg"));

			File svg = new File(mediaDir, svgFile);
			assertTrue("SVG file must exist on disk", svg.exists());

			String content = Files.readString(svg.toPath());
			assertTrue("SVG must contain <svg", content.contains("<svg"));
			assertTrue("SVG must contain shape path", content.contains("<path") || content.contains("<rect"));
			assertTrue("SVG must contain text", content.contains("Hello"));
		}
	}

	@Test
	public void renderWithThemeColors() throws Exception {
		String drawingXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<dsp:drawing xmlns:dsp=\"http://schemas.microsoft.com/office/drawing/2008/diagram\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
			+ "<dsp:spTree>"
			+ "<dsp:nvGrpSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvGrpSpPr/></dsp:nvGrpSpPr>"
			+ "<dsp:grpSpPr/>"
			+ "<dsp:sp modelId=\"{1}\">"
			+ "<dsp:nvSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvSpPr/></dsp:nvSpPr>"
			+ "<dsp:spPr>"
			+ "<a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"500000\" cy=\"300000\"/></a:xfrm>"
			+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>"
			+ "<a:solidFill><a:schemeClr val=\"accent1\"/></a:solidFill>"
			+ "</dsp:spPr>"
			+ "<dsp:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:t>Themed</a:t></a:r></a:p></dsp:txBody>"
			+ "</dsp:sp>"
			+ "</dsp:spTree></dsp:drawing>";

		File docx = File.createTempFile("smartart_theme", ".docx", new File("target"));
		docx.deleteOnExit();
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(docx))) {
			zos.putNextEntry(new ZipEntry("word/document.xml"));
			zos.write("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body/></w:document>"
				.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
			zos.putNextEntry(new ZipEntry("word/diagrams/drawing1.xml"));
			zos.write(drawingXml.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}

		File mediaDir = Files.createTempDirectory(java.nio.file.Path.of("target"), "smartart_theme_").toFile();
		mediaDir.deleteOnExit();

		try (ZipFile zf = new ZipFile(docx)) {
			Map<String, String> themeColors = Map.of("accent1", "FF0000");
			String svgFile = SmartArtRenderer.render(zf, "diagrams/drawing1.xml", mediaDir,
				5486400, 3200400, themeColors);

			assertNotNull("SVG file must be created with theme colors", svgFile);
			File svg = new File(mediaDir, svgFile);
			String content = Files.readString(svg.toPath());
			assertTrue("SVG must contain the custom accent1 theme color #FF0000",
				content.contains("#FF0000") || content.contains("ff0000") || content.contains("FF0000"));
		}
	}

	@Test
	public void renderWithGradientFill() throws Exception {
		String drawingXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<dsp:drawing xmlns:dsp=\"http://schemas.microsoft.com/office/drawing/2008/diagram\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
			+ "<dsp:spTree>"
			+ "<dsp:nvGrpSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvGrpSpPr/></dsp:nvGrpSpPr>"
			+ "<dsp:grpSpPr/>"
			+ "<dsp:sp modelId=\"{1}\">"
			+ "<dsp:nvSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvSpPr/></dsp:nvSpPr>"
			+ "<dsp:spPr>"
			+ "<a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"500000\" cy=\"300000\"/></a:xfrm>"
			+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>"
			+ "<a:gradFill>"
			+ "<a:gsLst>"
			+ "<a:gs pos=\"0\"><a:srgbClr val=\"FF0000\"/></a:gs>"
			+ "<a:gs pos=\"100000\"><a:srgbClr val=\"0000FF\"/></a:gs>"
			+ "</a:gsLst>"
			+ "<a:lin ang=\"5400000\"/>"
			+ "</a:gradFill>"
			+ "</dsp:spPr>"
			+ "</dsp:sp>"
			+ "</dsp:spTree></dsp:drawing>";

		File docx = File.createTempFile("smartart_gradient", ".docx", new File("target"));
		docx.deleteOnExit();
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(docx))) {
			zos.putNextEntry(new ZipEntry("word/document.xml"));
			zos.write("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body/></w:document>"
				.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
			zos.putNextEntry(new ZipEntry("word/diagrams/drawing1.xml"));
			zos.write(drawingXml.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}

		File mediaDir = Files.createTempDirectory(java.nio.file.Path.of("target"), "smartart_grad_").toFile();
		mediaDir.deleteOnExit();

		try (ZipFile zf = new ZipFile(docx)) {
			String svgFile = SmartArtRenderer.render(zf, "diagrams/drawing1.xml", mediaDir,
				5486400, 3200400, Collections.emptyMap());

			assertNotNull("SVG file must be created with gradient fill", svgFile);
			File svg = new File(mediaDir, svgFile);
			String content = Files.readString(svg.toPath());
			assertTrue("SVG must contain linearGradient for gradient fill", content.contains("linearGradient"));
		}
	}

	@Test
	public void renderWithRotation() throws Exception {
		String drawingXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<dsp:drawing xmlns:dsp=\"http://schemas.microsoft.com/office/drawing/2008/diagram\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
			+ "<dsp:spTree>"
			+ "<dsp:nvGrpSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvGrpSpPr/></dsp:nvGrpSpPr>"
			+ "<dsp:grpSpPr/>"
			+ "<dsp:sp modelId=\"{1}\">"
			+ "<dsp:nvSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvSpPr/></dsp:nvSpPr>"
			+ "<dsp:spPr>"
			+ "<a:xfrm rot=\"5400000\"><a:off x=\"0\" y=\"0\"/><a:ext cx=\"500000\" cy=\"300000\"/></a:xfrm>"
			+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>"
			+ "<a:solidFill><a:srgbClr val=\"4472C4\"/></a:solidFill>"
			+ "</dsp:spPr>"
			+ "</dsp:sp>"
			+ "</dsp:spTree></dsp:drawing>";

		File docx = File.createTempFile("smartart_rotate", ".docx", new File("target"));
		docx.deleteOnExit();
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(docx))) {
			zos.putNextEntry(new ZipEntry("word/document.xml"));
			zos.write("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body/></w:document>"
				.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
			zos.putNextEntry(new ZipEntry("word/diagrams/drawing1.xml"));
			zos.write(drawingXml.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}

		File mediaDir = Files.createTempDirectory(java.nio.file.Path.of("target"), "smartart_rot_").toFile();
		mediaDir.deleteOnExit();

		try (ZipFile zf = new ZipFile(docx)) {
			String svgFile = SmartArtRenderer.render(zf, "diagrams/drawing1.xml", mediaDir,
				5486400, 3200400, Collections.emptyMap());

			assertNotNull("SVG file must be created with rotation", svgFile);
			File svg = new File(mediaDir, svgFile);
			String content = Files.readString(svg.toPath());
			assertTrue("SVG must contain rotate transform for rotated shape", content.contains("rotate"));
		}
	}

	@Test
	public void renderWithGroupShapes() throws Exception {
		String drawingXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<dsp:drawing xmlns:dsp=\"http://schemas.microsoft.com/office/drawing/2008/diagram\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
			+ "<dsp:spTree>"
			+ "<dsp:nvGrpSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvGrpSpPr/></dsp:nvGrpSpPr>"
			+ "<dsp:grpSpPr/>"
			+ "<dsp:grpSp>"
			+ "<dsp:nvGrpSpPr><dsp:cNvPr id=\"1\" name=\"Group\"/><dsp:cNvGrpSpPr/></dsp:nvGrpSpPr>"
			+ "<dsp:grpSpPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"1000000\" cy=\"300000\"/>"
			+ "<a:chOff x=\"0\" y=\"0\"/><a:chExt cx=\"1000000\" cy=\"300000\"/></a:xfrm></dsp:grpSpPr>"
			+ "<dsp:sp modelId=\"{1}\">"
			+ "<dsp:nvSpPr><dsp:cNvPr id=\"2\" name=\"\"/><dsp:cNvSpPr/></dsp:nvSpPr>"
			+ "<dsp:spPr>"
			+ "<a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"400000\" cy=\"300000\"/></a:xfrm>"
			+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>"
			+ "<a:solidFill><a:srgbClr val=\"4472C4\"/></a:solidFill>"
			+ "</dsp:spPr>"
			+ "<dsp:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:t>ShapeA</a:t></a:r></a:p></dsp:txBody>"
			+ "</dsp:sp>"
			+ "<dsp:sp modelId=\"{2}\">"
			+ "<dsp:nvSpPr><dsp:cNvPr id=\"3\" name=\"\"/><dsp:cNvSpPr/></dsp:nvSpPr>"
			+ "<dsp:spPr>"
			+ "<a:xfrm><a:off x=\"500000\" y=\"0\"/><a:ext cx=\"500000\" cy=\"300000\"/></a:xfrm>"
			+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>"
			+ "<a:solidFill><a:srgbClr val=\"ED7D31\"/></a:solidFill>"
			+ "</dsp:spPr>"
			+ "<dsp:txBody><a:bodyPr/><a:lstStyle/><a:p><a:r><a:t>ShapeB</a:t></a:r></a:p></dsp:txBody>"
			+ "</dsp:sp>"
			+ "</dsp:grpSp>"
			+ "</dsp:spTree></dsp:drawing>";

		File docx = File.createTempFile("smartart_group", ".docx", new File("target"));
		docx.deleteOnExit();
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(docx))) {
			zos.putNextEntry(new ZipEntry("word/document.xml"));
			zos.write("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body/></w:document>"
				.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
			zos.putNextEntry(new ZipEntry("word/diagrams/drawing1.xml"));
			zos.write(drawingXml.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}

		File mediaDir = Files.createTempDirectory(java.nio.file.Path.of("target"), "smartart_grp_").toFile();
		mediaDir.deleteOnExit();

		try (ZipFile zf = new ZipFile(docx)) {
			String svgFile = SmartArtRenderer.render(zf, "diagrams/drawing1.xml", mediaDir,
				5486400, 3200400, Collections.emptyMap());

			assertNotNull("SVG file must be created with group shapes", svgFile);
			File svg = new File(mediaDir, svgFile);
			String content = Files.readString(svg.toPath());
			assertTrue("SVG must contain first shape text ShapeA", content.contains("ShapeA"));
			assertTrue("SVG must contain second shape text ShapeB", content.contains("ShapeB"));
		}
	}

	@Test
	public void renderWithCustomGeometry() throws Exception {
		String drawingXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<dsp:drawing xmlns:dsp=\"http://schemas.microsoft.com/office/drawing/2008/diagram\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
			+ "<dsp:spTree>"
			+ "<dsp:nvGrpSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvGrpSpPr/></dsp:nvGrpSpPr>"
			+ "<dsp:grpSpPr/>"
			+ "<dsp:sp modelId=\"{1}\">"
			+ "<dsp:nvSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvSpPr/></dsp:nvSpPr>"
			+ "<dsp:spPr>"
			+ "<a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"500000\" cy=\"300000\"/></a:xfrm>"
			+ "<a:custGeom>"
			+ "<a:pathLst>"
			+ "<a:path w=\"100\" h=\"100\">"
			+ "<a:moveTo><a:pt x=\"0\" y=\"0\"/></a:moveTo>"
			+ "<a:lnTo><a:pt x=\"100\" y=\"0\"/></a:lnTo>"
			+ "<a:lnTo><a:pt x=\"50\" y=\"100\"/></a:lnTo>"
			+ "<a:close/>"
			+ "</a:path>"
			+ "</a:pathLst>"
			+ "</a:custGeom>"
			+ "<a:solidFill><a:srgbClr val=\"4472C4\"/></a:solidFill>"
			+ "</dsp:spPr>"
			+ "</dsp:sp>"
			+ "</dsp:spTree></dsp:drawing>";

		File docx = File.createTempFile("smartart_custgeom", ".docx", new File("target"));
		docx.deleteOnExit();
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(docx))) {
			zos.putNextEntry(new ZipEntry("word/document.xml"));
			zos.write("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body/></w:document>"
				.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
			zos.putNextEntry(new ZipEntry("word/diagrams/drawing1.xml"));
			zos.write(drawingXml.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}

		File mediaDir = Files.createTempDirectory(java.nio.file.Path.of("target"), "smartart_cg_").toFile();
		mediaDir.deleteOnExit();

		try (ZipFile zf = new ZipFile(docx)) {
			String svgFile = SmartArtRenderer.render(zf, "diagrams/drawing1.xml", mediaDir,
				5486400, 3200400, Collections.emptyMap());

			assertNotNull("SVG file must be created with custom geometry", svgFile);
			File svg = new File(mediaDir, svgFile);
			String content = Files.readString(svg.toPath());
			assertTrue("SVG must contain a <path element for custom geometry", content.contains("<path"));
		}
	}

	@Test
	public void renderWithTextFormatting() throws Exception {
		String drawingXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<dsp:drawing xmlns:dsp=\"http://schemas.microsoft.com/office/drawing/2008/diagram\""
			+ " xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
			+ "<dsp:spTree>"
			+ "<dsp:nvGrpSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvGrpSpPr/></dsp:nvGrpSpPr>"
			+ "<dsp:grpSpPr/>"
			+ "<dsp:sp modelId=\"{1}\">"
			+ "<dsp:nvSpPr><dsp:cNvPr id=\"0\" name=\"\"/><dsp:cNvSpPr/></dsp:nvSpPr>"
			+ "<dsp:spPr>"
			+ "<a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"500000\" cy=\"300000\"/></a:xfrm>"
			+ "<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom>"
			+ "<a:solidFill><a:srgbClr val=\"4472C4\"/></a:solidFill>"
			+ "</dsp:spPr>"
			+ "<dsp:txBody><a:bodyPr/><a:lstStyle/>"
			+ "<a:p><a:r><a:rPr b=\"1\" sz=\"2400\"/><a:t>BoldText</a:t></a:r></a:p>"
			+ "</dsp:txBody>"
			+ "</dsp:sp>"
			+ "</dsp:spTree></dsp:drawing>";

		File docx = File.createTempFile("smartart_textfmt", ".docx", new File("target"));
		docx.deleteOnExit();
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(docx))) {
			zos.putNextEntry(new ZipEntry("word/document.xml"));
			zos.write("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body/></w:document>"
				.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
			zos.putNextEntry(new ZipEntry("word/diagrams/drawing1.xml"));
			zos.write(drawingXml.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}

		File mediaDir = Files.createTempDirectory(java.nio.file.Path.of("target"), "smartart_tf_").toFile();
		mediaDir.deleteOnExit();

		try (ZipFile zf = new ZipFile(docx)) {
			String svgFile = SmartArtRenderer.render(zf, "diagrams/drawing1.xml", mediaDir,
				5486400, 3200400, Collections.emptyMap());

			assertNotNull("SVG file must be created with text formatting", svgFile);
			File svg = new File(mediaDir, svgFile);
			String content = Files.readString(svg.toPath());
			assertTrue("SVG must contain text content", content.contains("BoldText"));
			assertTrue("SVG must contain font-weight or bold for bold text runs",
				content.contains("font-weight") || content.contains("bold"));
		}
	}

	@Test
	public void renderMissingDrawingFile() throws Exception {
		File docx = File.createTempFile("smartart_missing", ".docx", new File("target"));
		docx.deleteOnExit();
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(docx))) {
			zos.putNextEntry(new ZipEntry("word/document.xml"));
			zos.write("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body/></w:document>"
				.getBytes(StandardCharsets.UTF_8));
			zos.closeEntry();
		}

		File mediaDir = Files.createTempDirectory(java.nio.file.Path.of("target"), "smartart_missing_").toFile();
		mediaDir.deleteOnExit();

		try (ZipFile zf = new ZipFile(docx)) {
			String result = SmartArtRenderer.render(zf, "diagrams/nonexistent.xml", mediaDir, 5000000, 3000000, null);
			assertNull("Missing drawing file must return null", result);
		}
	}
}
