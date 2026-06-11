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
package org.olat.core.commons.services.ai.spi.localonnx;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.ai.AiEmbeddingSPI;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.Tracing;
import org.olat.modules.taxonomy.matching.TaxonomyMatchingModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.OnnxEmbeddingModel;
import dev.langchain4j.model.embedding.onnx.PoolingMode;
import dev.langchain4j.model.chat.ChatModel;

/**
 * AI SPI that runs an ONNX embedding model in-process.
 * The admin uploads the model file — it is NOT bundled in the WAR.
 * Chat methods are not supported; this SPI is embedding-only.
 *
 * Initial date: 2026-06-05<br>
 * @author uhensler, https://www.frentix.com
 */
@Service("localOnnxSPI")
public class LocalOnnxSPI implements AiEmbeddingSPI {

	private static final Logger log = Tracing.createLoggerFor(LocalOnnxSPI.class);
	public static final String SPI_ID = "LocalOnnx";
	public static final String MODEL_FILE = "model.onnx";
	public static final String TOKENIZER_FILE = "tokenizer.json";
	private static final int ONNX_MAGIC_0 = 0x08;

	@Autowired
	private TaxonomyMatchingModule matchingModule;

	private final AtomicReference<EmbeddingModel> loadedModel = new AtomicReference<>();
	private final ReentrantReadWriteLock modelLock = new ReentrantReadWriteLock();
	private volatile String loadedModelId;
	private final ConcurrentHashMap<String, Integer> dimensionsByModelName = new ConcurrentHashMap<>();

	@Override
	public String getId() {
		return SPI_ID;
	}

	@Override
	public boolean isEnabled() {
		return isEmbeddingEnabled();
	}

	@Override
	public boolean isEmbeddingEnabled() {
		return !getAvailableEmbeddingModels().isEmpty();
	}

	@Override
	public EmbeddingModel buildEmbeddingModel(String modelName) {
		modelLock.readLock().lock();
		try {
			if (modelName.equals(loadedModelId) && loadedModel.get() != null) {
				return loadedModel.get();
			}
		} finally {
			modelLock.readLock().unlock();
		}

		modelLock.writeLock().lock();
		try {
			if (modelName.equals(loadedModelId) && loadedModel.get() != null) {
				return loadedModel.get();
			}
			EmbeddingModel model = loadOnnxModel(modelName);
			loadedModel.set(model);
			loadedModelId = model != null ? modelName : null;
			return model;
		} finally {
			modelLock.writeLock().unlock();
		}
	}

	@Override
	public List<String> getAvailableEmbeddingModels() {
		List<String> models = new ArrayList<>();
		String modelDir = resolveModelDir();
		if (modelDir == null || modelDir.isBlank()) {
			return models;
		}
		Path base = Paths.get(modelDir);
		if (!Files.isDirectory(base)) {
			return models;
		}
		try {
			Files.list(base).filter(Files::isDirectory).forEach(dir -> {
				Path modelFile = dir.resolve(MODEL_FILE);
				if (Files.exists(modelFile)) {
					models.add(dir.getFileName().toString());
				}
			});
		} catch (IOException e) {
			log.warn("Could not list local ONNX models from {}: {}", base, e.getMessage());
		}
		return models;
	}

	@Override
	public int getEmbeddingDimension(String modelName) {
		Integer cached = dimensionsByModelName.get(modelName);
		return cached != null ? cached : -1;
	}

	@Override
	public String getName() {
		return "Local ONNX";
	}

	@Override
	public void setEnabled(boolean enabled) {
	}

	@Override
	public ChatModel buildChatModel(String modelName, int maxTokens) {
		throw new UnsupportedOperationException("LocalOnnxSPI does not support chat models");
	}

	@Override
	public List<String> getAvailableModels() {
		return getAvailableEmbeddingModels();
	}

	@Override
	public Controller createAdminController(UserRequest ureq, WindowControl wControl) {
		return new org.olat.core.commons.services.ai.spi.localonnx.ui.LocalOnnxModelAdminController(ureq, wControl);
	}

	public String validateModel(String modelName) {
		String modelDir = resolveModelDir();
		if (modelDir == null) {
			return "Model directory not configured";
		}
		String safeName = sanitize(modelName);
		Path base = Paths.get(modelDir).resolve(safeName);
		Path modelFile = base.resolve(MODEL_FILE);
		Path tokenizerFile = base.resolve(TOKENIZER_FILE);
		if (!isPathSafe(base, Paths.get(modelDir))) {
			return "Path traversal rejected";
		}
		if (!Files.exists(modelFile)) {
			return MODEL_FILE + " not found";
		}
		if (!Files.exists(tokenizerFile)) {
			return TOKENIZER_FILE + " not found";
		}
		if (!validateOnnxMagicBytes(modelFile)) {
			return "Invalid ONNX magic bytes";
		}
		return buildEmbeddingModel(modelName) != null ? null : "Failed to load model - check server logs";
	}

	EmbeddingModel loadOnnxModel(String modelName) {
		String modelDir = resolveModelDir();
		if (modelDir == null) {
			return null;
		}
		String safeName = sanitize(modelName);
		Path base = Paths.get(modelDir).resolve(safeName);
		Path modelFile = base.resolve(MODEL_FILE);
		Path tokenizerFile = base.resolve(TOKENIZER_FILE);

		if (!isPathSafe(base, Paths.get(modelDir))) {
			log.warn("Path traversal attempt blocked for model name: {}", modelName);
			return null;
		}
		if (!Files.exists(modelFile) || !Files.exists(tokenizerFile)) {
			log.warn("ONNX model or tokenizer not found at {}", base);
			return null;
		}
		if (!validateOnnxMagicBytes(modelFile)) {
			log.warn("ONNX file at {} failed magic byte validation", modelFile);
			return null;
		}

		try {
			EmbeddingModel model;
			int dim;
			// Qwen needs position_ids + last-token pooling -- workaround until langchain4j PR #5093 lands. See QwenOnnxEmbeddingModel.
			if (modelName.toLowerCase().contains("qwen")) {
				QwenOnnxEmbeddingModel qwen = new QwenOnnxEmbeddingModel(modelFile, tokenizerFile);
				dim = qwen.getDimension();
				model = qwen;
			} else {
				model = new OnnxEmbeddingModel(modelFile, tokenizerFile, PoolingMode.MEAN);
				dim = model.embed("dim").content().vector().length;
			}
			if (dim > 0) {
				dimensionsByModelName.put(modelName, dim);
				log.info("Loaded ONNX model '{}' with embedding dimension {}", modelName, dim);
			}
			return model;
		} catch (Exception e) {
			log.error("Failed to load ONNX model from {}: {}", base, e.getMessage(), e);
			return null;
		}
	}

	private boolean validateOnnxMagicBytes(Path modelFile) {
		try (var in = Files.newInputStream(modelFile)) {
			return in.read() == ONNX_MAGIC_0;
		} catch (IOException e) {
			log.warn("Could not read ONNX file for validation: {}", e.getMessage());
			return false;
		}
	}

	private boolean isPathSafe(Path target, Path allowedBase) {
		try {
			Path canonical = target.toRealPath();
			Path baseCanonical = allowedBase.toRealPath();
			return canonical.startsWith(baseCanonical);
		} catch (IOException e) {
			Path normalized = target.normalize();
			Path baseNormalized = allowedBase.normalize();
			return normalized.startsWith(baseNormalized);
		}
	}

	public static String sanitize(String name) {
		if (name == null) {
			return "";
		}
		return name.replaceAll("[^a-zA-Z0-9_\\-]", "");
	}

	private String resolveModelDir() {
		if (matchingModule == null) {
			return null;
		}
		String dir = matchingModule.getLocalModelDir();
		if (dir == null || dir.isBlank()) {
			return null;
		}
		return dir;
	}

	public void invalidateLoadedModel() {
		modelLock.writeLock().lock();
		try {
			loadedModel.set(null);
			loadedModelId = null;
			dimensionsByModelName.clear();
		} finally {
			modelLock.writeLock().unlock();
		}
	}
}
