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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.TensorInfo;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

/**
 * WORKAROUND for langchain4j: load and run Qwen3-Embedding ONNX models locally.
 *
 * <p>langchain4j's built-in {@code OnnxEmbeddingModel} delegates to
 * {@code OnnxBertBiEncoder}, which only binds {@code input_ids},
 * {@code attention_mask} and (optionally) {@code token_type_ids}. The Qwen3
 * ONNX graph also needs {@code position_ids} (consumed by the rotary
 * embedding), so the BERT encoder fails with
 * {@code Missing Input: position_ids}. Qwen3 also requires last-token
 * pooling + L2 normalization instead of mean pooling.
 *
 * <p><b>Remove this class when one of the following is true:</b>
 * <ul>
 *   <li>langchain4j PR
 *       <a href="https://github.com/langchain4j/langchain4j/pull/5093">#5093</a>
 *       ("add ONNX encoder abstraction, support non-BERT ONNX embedding
 *       encoders") is merged and released — it introduces
 *       {@code OnnxBpeBiEncoder} and {@code PoolingMode.LAST_TOKEN}, which
 *       cover Qwen3.</li>
 *   <li>langchain4j issue
 *       <a href="https://github.com/langchain4j/langchain4j/issues/4408">#4408</a>
 *       ("support Qwen3-Embedding local model file load mode") is closed
 *       as fixed.</li>
 * </ul>
 *
 * <p>When removing: delete this class, drop the Qwen branch in
 * {@link LocalOnnxSPI#loadOnnxModel(String)} and replace it with
 * {@code new OnnxEmbeddingModel(model, tokenizer, PoolingMode.LAST_TOKEN)}
 * (or whatever the upstream API ends up looking like).
 *
 * Initial date: 2026-06-10<br>
 * @author uhensler, https://www.frentix.com
 */
public class QwenOnnxEmbeddingModel implements EmbeddingModel {

	private static final Logger log = Tracing.createLoggerFor(QwenOnnxEmbeddingModel.class);
	private static final int MAX_SEQ_LEN = 512;

	private final OrtEnvironment env;
	private final OrtSession session;
	private final HuggingFaceTokenizer tokenizer;
	private final int dimension;

	public QwenOnnxEmbeddingModel(Path modelFile, Path tokenizerFile) throws Exception {
		env = OrtEnvironment.getEnvironment();
		session = env.createSession(modelFile.toString());
		tokenizer = HuggingFaceTokenizer.newInstance(tokenizerFile);
		dimension = introspectDimension();
	}

	public int getDimension() {
		return dimension;
	}

	private int introspectDimension() {
		try {
			NodeInfo info = session.getOutputInfo().values().iterator().next();
			long[] shape = ((TensorInfo) info.getInfo()).getShape();
			return (int) shape[shape.length - 1];
		} catch (Exception e) {
			log.warn("Could not introspect ONNX output shape; probing via embed: {}", e.getMessage());
			try {
				return embed("dim").content().vector().length;
			} catch (Exception pe) {
				log.warn("ONNX dimension probe failed: {}", pe.getMessage());
				return -1;
			}
		}
	}

	@Override
	public Response<Embedding> embed(String text) {
		try {
			return Response.from(Embedding.from(encodeText(text)));
		} catch (Exception e) {
			log.error("Qwen embedding failed: {}", e.getMessage(), e);
			return Response.from(Embedding.from(new float[0]));
		}
	}

	@Override
	public Response<Embedding> embed(TextSegment textSegment) {
		return embed(textSegment.text());
	}

	@Override
	public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
		List<Embedding> embeddings = new ArrayList<>(textSegments.size());
		for (TextSegment seg : textSegments) {
			embeddings.add(embed(seg).content());
		}
		return Response.from(embeddings);
	}

	private float[] encodeText(String text) throws Exception {
		Encoding enc = tokenizer.encode(text);
		long[] ids = enc.getIds();
		long[] mask = enc.getAttentionMask();

		int seqLen = Math.min(ids.length, MAX_SEQ_LEN);
		ids = truncate(ids, seqLen);
		mask = truncate(mask, seqLen);

		long[][] idsMatrix = new long[1][seqLen];
		long[][] maskMatrix = new long[1][seqLen];
		long[][] posMatrix = new long[1][seqLen];
		System.arraycopy(ids, 0, idsMatrix[0], 0, seqLen);
		System.arraycopy(mask, 0, maskMatrix[0], 0, seqLen);
		for (int i = 0; i < seqLen; i++) {
			posMatrix[0][i] = i;
		}

		Map<String, OnnxTensor> inputs = new HashMap<>();
		try (OnnxTensor tIds = OnnxTensor.createTensor(env, idsMatrix);
			 OnnxTensor tMask = OnnxTensor.createTensor(env, maskMatrix);
			 OnnxTensor tPos = OnnxTensor.createTensor(env, posMatrix)) {
			inputs.put("input_ids", tIds);
			inputs.put("attention_mask", tMask);
			inputs.put("position_ids", tPos);
			try (OrtSession.Result result = session.run(inputs)) {
				float[][][] hidden = (float[][][]) result.get(0).getValue();
				int lastIdx = 0;
				for (int i = 0; i < seqLen; i++) {
					if (mask[i] == 1) {
						lastIdx = i;
					}
				}
				float[] vec = hidden[0][lastIdx].clone();
				return l2normalize(vec);
			}
		}
	}

	private long[] truncate(long[] arr, int len) {
		if (arr.length <= len) {
			return arr;
		}
		long[] result = new long[len];
		System.arraycopy(arr, 0, result, 0, len);
		return result;
	}

	private float[] l2normalize(float[] vec) {
		float norm = 0;
		for (float v : vec) {
			norm += v * v;
		}
		norm = (float) Math.sqrt(norm);
		if (norm > 0) {
			for (int i = 0; i < vec.length; i++) {
				vec[i] /= norm;
			}
		}
		return vec;
	}
}
