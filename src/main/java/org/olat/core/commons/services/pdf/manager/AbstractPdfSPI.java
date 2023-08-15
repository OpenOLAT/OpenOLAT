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
package org.olat.core.commons.services.pdf.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.pdf.PdfOutputOptions;
import org.olat.core.commons.services.pdf.PdfSPI;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.commons.services.pdf.model.PdfDelivery;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.httpclient.HttpClientService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractPdfSPI extends AbstractSpringModule implements PdfSPI {

	private static final Logger log = Tracing.createLoggerFor(AbstractPdfSPI.class);
	
	private final CacheWrapper<String,PdfDelivery> cache;
	
	@Autowired
	protected HttpClientService httpClientService;
	
	public AbstractPdfSPI(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
		cache = coordinatorManager.getCoordinator().getCacher().getCache(PdfService.class.getSimpleName(), "delivery");
	}
	
	@Override
	public void convert(File path, String rootFilename, PdfOutputOptions options, OutputStream out) {
		String key = UUID.randomUUID().toString();
		PdfDelivery delivery = new PdfDelivery(key);
		delivery.setDirectory(path.getAbsolutePath());
		cache.put(key, delivery);
		render(key, rootFilename, options, out);
		cache.remove(key);
	}
	
	@Override
	public void convert(Identity identity, ControllerCreator creator, WindowControl wControl, PdfOutputOptions options, OutputStream out) {
		String key = UUID.randomUUID().toString();
		PdfDelivery delivery = new PdfDelivery(key);
		delivery.setIdentity(identity);
		delivery.setControllerCreator(creator);
		delivery.setWindowControl(wControl);
		cache.put(key, delivery);
		render(key, "index.html", options, out);
		cache.remove(key);
		if(delivery.getBrowserWindow() instanceof Disposable disposableWindow) {
			disposableWindow.dispose();
		}
	}
	
	protected abstract void render(String key, String rootFilename, PdfOutputOptions options, OutputStream out);
	
	protected final void executeRequest(CloseableHttpClient httpclient, HttpRequestBase request, OutputStream out) {
		try(CloseableHttpResponse response = httpclient.execute(request)) {
			if(response.getStatusLine().getStatusCode() == 200) {
				copyResponse(response, out);
			} else {
				log.error("Cannot renderer PDF: {}", response.getStatusLine().getStatusCode());
				EntityUtils.consume(response.getEntity());
			}
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	private void copyResponse(HttpResponse response, OutputStream out) {
		try(InputStream in=response.getEntity().getContent()) {
			FileUtils.cpio(in, out, "pdfd");
		} catch(IOException e) {
			log.error("", e);
		}
	}
}
