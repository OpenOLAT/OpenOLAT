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
package org.olat.core.commons.services.ai.spi.localonnx.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.ai.spi.localonnx.LocalOnnxSPI;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin page for managing local ONNX embedding models.
 * Models are placed on the server filesystem; this page does not accept uploads.
 *
 * Initial date: 2026-06-10<br>
 * @author uhensler, https://www.frentix.com
 */
public class LocalOnnxModelAdminController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(LocalOnnxModelAdminController.class);

	private StaticTextElement modelsEl;
	private FormLink rescanLink;
	private FormLink validateLink;

	private final Map<String, String> validateResults = new HashMap<>();

	@Autowired
	private LocalOnnxSPI localOnnxSpi;
	@Autowired
	private TaxonomyMatchingModule matchingModule;

	public LocalOnnxModelAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(LocalOnnxModelAdminController.class, ureq.getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("local.onnx.admin.title");

		uifactory.addStaticTextElement("local.onnx.admin.info", null, buildInfoHtml(), formLayout);

		modelsEl = uifactory.addStaticTextElement("local.onnx.admin.models", null, buildModelsHtml(), formLayout);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		rescanLink = uifactory.addFormLink("local.onnx.action.rescan", "local.onnx.action.rescan", null, buttonsCont, Link.BUTTON);
		validateLink = uifactory.addFormLink("local.onnx.action.validate", "local.onnx.action.validate", null, buttonsCont, Link.BUTTON);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == rescanLink) {
			localOnnxSpi.invalidateLoadedModel();
			validateResults.clear();
			modelsEl.setValue(buildModelsHtml());
			log.info("Local ONNX models rescanned by {}", getIdentity().getKey());
		} else if (source == validateLink) {
			runValidation();
			modelsEl.setValue(buildModelsHtml());
			log.info("Local ONNX models validated by {}", getIdentity().getKey());
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// no form submission
	}

	private void runValidation() {
		validateResults.clear();
		for (String modelName : localOnnxSpi.getAvailableEmbeddingModels()) {
			String error = localOnnxSpi.validateModel(modelName);
			validateResults.put(modelName, error);
		}
	}

	private String buildInfoHtml() {
		String modelDir = matchingModule != null ? matchingModule.getLocalModelDir() : null;
		boolean dirConfigured = StringHelper.containsNonWhitespace(modelDir);

		StringBuilder sb = new StringBuilder();
		sb.append("<div class='o_info'>");

		sb.append("<p>").append(translate("local.onnx.admin.info.intro")).append("</p>");

		sb.append("<p><strong>").append(translate("local.onnx.admin.dir.label")).append(":</strong><br>");
		if (dirConfigured) {
			sb.append("<code>").append(StringHelper.escapeHtml(modelDir)).append("</code>");
		} else {
			sb.append("<span class='text-danger'>").append(translate("local.onnx.admin.dir.notconfigured")).append("</span>");
		}
		sb.append("</p>");

		sb.append("<p><strong>").append(translate("local.onnx.admin.layout.title")).append(":</strong></p>");
		sb.append("<pre style='margin-left:1em'>");
		if (dirConfigured) {
			sb.append(StringHelper.escapeHtml(modelDir)).append("/\n");
		} else {
			sb.append("&lt;localModelDir&gt;/\n");
		}
		sb.append("  &lt;model-name&gt;/\n");
		sb.append("    model.onnx          # ").append(translate("local.onnx.admin.layout.onnx")).append("\n");
		sb.append("    tokenizer.json      # ").append(translate("local.onnx.admin.layout.tokenizer")).append("\n");
		sb.append("    model.onnx_data     # ").append(translate("local.onnx.admin.layout.externaldata")).append("\n");
		sb.append("</pre>");

		sb.append("<p>").append(translate("local.onnx.admin.naming.rule")).append("</p>");
		sb.append("<p>").append(translate("local.onnx.admin.externaldata")).append("</p>");
		sb.append("<p>").append(translate("local.onnx.admin.permissions")).append("</p>");
		sb.append("<p>").append(translate("local.onnx.admin.activate")).append("</p>");
		sb.append("</div>");
		sb.append("<div class='o_warning'>").append(translate("local.onnx.admin.security")).append("</div>");

		return sb.toString();
	}

	private String buildModelsHtml() {
		List<String> models = localOnnxSpi.getAvailableEmbeddingModels().stream()
				.sorted(String.CASE_INSENSITIVE_ORDER).toList();
		String modelDir = matchingModule != null ? matchingModule.getLocalModelDir() : null;

		StringBuilder sb = new StringBuilder();
		sb.append("<h5>").append(translate("local.onnx.admin.models.title")).append("</h5>");

		if (models.isEmpty()) {
			sb.append("<p class='text-muted'>").append(translate("local.onnx.admin.models.none")).append("</p>");
			return sb.toString();
		}

		boolean showStatus = !validateResults.isEmpty();
		sb.append("<table class='table table-condensed table-bordered' style='width:auto'>");
		sb.append("<thead><tr>");
		sb.append("<th>").append(translate("local.onnx.admin.models.col.name")).append("</th>");
		sb.append("<th>").append(translate("local.onnx.admin.models.col.size")).append("</th>");
		sb.append("<th>").append(translate("local.onnx.admin.models.col.externaldata")).append("</th>");
		if (showStatus) {
			sb.append("<th>").append(translate("local.onnx.admin.models.col.status")).append("</th>");
		}
		sb.append("</tr></thead><tbody>");

		for (String model : models) {
			sb.append("<tr>");
			sb.append("<td>").append(StringHelper.escapeHtml(model)).append("</td>");

			long totalSize = 0;
			boolean hasExternalData = false;
			if (StringHelper.containsNonWhitespace(modelDir)) {
				Path modelPath = Paths.get(modelDir).resolve(LocalOnnxSPI.sanitize(model));
				totalSize = directorySize(modelPath);
				hasExternalData = hasExternalDataFile(modelPath);
			}
			sb.append("<td>").append(formatSize(totalSize)).append("</td>");
			sb.append("<td>").append(hasExternalData ? "&#10003;" : "").append("</td>");

			if (showStatus) {
				if (validateResults.containsKey(model)) {
					String error = validateResults.get(model);
					if (error == null) {
						sb.append("<td><span class='text-success'>").append(translate("local.onnx.validate.ok")).append("</span></td>");
					} else {
						sb.append("<td><span class='text-danger'>")
						  .append(translate("local.onnx.validate.failed"))
						  .append(": ")
						  .append(StringHelper.escapeHtml(error))
						  .append("</span></td>");
					}
				} else {
					sb.append("<td></td>");
				}
			}
			sb.append("</tr>");
		}
		sb.append("</tbody></table>");
		return sb.toString();
	}

	private long directorySize(Path dir) {
		if (!Files.isDirectory(dir)) {
			return 0;
		}
		try (Stream<Path> files = Files.walk(dir)) {
			return files.filter(Files::isRegularFile).mapToLong(p -> {
				try {
					return Files.size(p);
				} catch (IOException e) {
					return 0;
				}
			}).sum();
		} catch (IOException e) {
			return 0;
		}
	}

	private boolean hasExternalDataFile(Path modelDir) {
		if (!Files.isDirectory(modelDir)) {
			return false;
		}
		try (Stream<Path> files = Files.list(modelDir)) {
			return files.anyMatch(p -> {
				String name = p.getFileName().toString().toLowerCase();
				return name.endsWith(".onnx_data") || name.endsWith(".onnx.data");
			});
		} catch (IOException e) {
			return false;
		}
	}

	private String formatSize(long bytes) {
		if (bytes <= 0) {
			return "—";
		}
		String[] units = {"B", "KB", "MB", "GB", "TB"};
		int unit = 0;
		double size = bytes;
		while (size >= 1024 && unit < units.length - 1) {
			size /= 1024;
			unit++;
		}
		return String.format("%.1f %s", size, units[unit]);
	}
}
