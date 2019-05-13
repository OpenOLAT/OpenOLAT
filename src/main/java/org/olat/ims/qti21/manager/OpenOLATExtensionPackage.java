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
package org.olat.ims.qti21.manager;

import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti21.manager.extensions.MaximaOperator;

import uk.ac.ed.ph.jacomax.JacomaxRuntimeException;
import uk.ac.ed.ph.jacomax.MaximaConfiguration;
import uk.ac.ed.ph.jqtiplus.ExtensionNamespaceInfo;
import uk.ac.ed.ph.jqtiplus.JqtiExtensionPackage;
import uk.ac.ed.ph.jqtiplus.JqtiLifecycleEventType;
import uk.ac.ed.ph.jqtiplus.exception.QtiLogicException;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.expression.ExpressionParent;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.CustomOperator;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.CustomInteraction;
import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltStylesheetCache;
import uk.ac.ed.ph.qtiworks.mathassess.XsltStylesheetCacheAdapter;
import uk.ac.ed.ph.qtiworks.mathassess.glue.maxima.MaximaLaunchHelper;
import uk.ac.ed.ph.qtiworks.mathassess.glue.maxima.QtiMaximaProcess;
import uk.ac.ed.ph.qtiworks.mathassess.pooling.QtiMaximaProcessPoolManager;
import uk.ac.ed.ph.snuggletex.utilities.StylesheetCache;

/**
 * 
 * Initial date: 3 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenOLATExtensionPackage implements JqtiExtensionPackage<OpenOLATExtensionPackage> {
	
	private static final Logger log = Tracing.createLoggerFor(OpenOLATExtensionPackage.class);
	
    private final ThreadLocal<QtiMaximaProcess> sessionThreadLocal;

    private final StylesheetCache snuggleStylesheetCache;
    private QtiMaximaProcessPoolManager qtiMaximaProcessPoolManager;

	public OpenOLATExtensionPackage(final XsltStylesheetCache xsltStylesheetCache) {
        snuggleStylesheetCache = new XsltStylesheetCacheAdapter(xsltStylesheetCache);
        
        /* Create ThreadLocal for communicating with maxima */
        this.sessionThreadLocal = new ThreadLocal<>();
	}

	@Override
	public String getDisplayName() {
		return "MAXIMA Extension pack";
	}

	@Override
	public Map<String, ExtensionNamespaceInfo> getNamespaceInfoMap() {
		return Collections.emptyMap();
	}

	@Override
	public boolean implementsCustomOperator(String operatorClassName) {
		return MaximaOperator.class.getName().equals(operatorClassName);
	}
	
	@Override
	public CustomOperator<OpenOLATExtensionPackage> createCustomOperator(ExpressionParent expressionParent, String operatorClassName) {
		return new MaximaOperator(expressionParent);
	}

	@Override
	public boolean implementsCustomInteraction(String interactionClassName) {
		return false;
	}

	@Override
	public CustomInteraction<OpenOLATExtensionPackage> createCustomInteraction(QtiNode parentObject, String interactionClassName) {
		return null;
	}
	
    //------------------------------------------------------------------------

    @Override
    public void lifecycleEvent(final Object source, final JqtiLifecycleEventType eventType) {
        log.debug("Received lifecycle event {}", eventType);
        switch (eventType) {
            case MANAGER_INITIALISED:
                startMaximaPool();
                break;

            case MANAGER_DESTROYED:
                closeMaximaPool();
                break;

            case ITEM_TEMPLATE_PROCESSING_STARTING:
            case ITEM_RESPONSE_PROCESSING_STARTING:
            case TEST_OUTCOME_PROCESSING_STARTING:
                /* Rather than creating a Maxima process at this point that may
                 * not be used,
                 * we'll wait until it is first needed. */
                break;

            case ITEM_TEMPLATE_PROCESSING_FINISHED:
            case ITEM_RESPONSE_PROCESSING_FINISHED:
            case TEST_OUTCOME_PROCESSING_FINISHED:
                releaseMaximaSessionForThread();
                break;

            default:
                break;
        }
    }

    private void startMaximaPool() {
        final MaximaConfiguration maximaConfiguration = MaximaLaunchHelper.tryMaximaConfiguration();
        if (maximaConfiguration==null) {
            log.warn("Failed to obtain a MaximaConfiguration. MathAssess extensions will not work and this package should NOT be used.");
            return;
        }
        try {
            qtiMaximaProcessPoolManager = new QtiMaximaProcessPoolManager();
            qtiMaximaProcessPoolManager.setMaximaConfiguration(maximaConfiguration);
            qtiMaximaProcessPoolManager.setStylesheetCache(snuggleStylesheetCache);
            qtiMaximaProcessPoolManager.init();

            log.info("MathAssessExtensionPackage successfully initiated using {} to handle communication with Maxima for MathAssess extensions", QtiMaximaProcessPoolManager.class.getSimpleName());
        }
        catch (final JacomaxRuntimeException e) {
            qtiMaximaProcessPoolManager = null;
            log.warn("Failed to start the {}. MathAssess extensions will not work and this package should NOT be used" +
                    QtiMaximaProcessPoolManager.class.getSimpleName());
        }
    }

    private void closeMaximaPool() {
        if (qtiMaximaProcessPoolManager != null) {
            log.info("Closing {}" + qtiMaximaProcessPoolManager);
            try {
                qtiMaximaProcessPoolManager.shutdown();
            }
            catch (final JacomaxRuntimeException e) {
                /* We'll log this but allow things to continue, as pool closure would normally happen on application exit */
                log.warn("Failed to close the {}." + QtiMaximaProcessPoolManager.class.getSimpleName());
            }
        }
    }

    // ------------------------------------------------------------------------

    public QtiMaximaProcess obtainMaximaSessionForThread() {
        QtiMaximaProcess maximaSession = sessionThreadLocal.get();
        if (maximaSession == null) {
            if (qtiMaximaProcessPoolManager != null) {
                log.debug("Obtaining new maxima process from pool for this request");
                /* Need to get a new process from pool */
                maximaSession = qtiMaximaProcessPoolManager.obtainProcess();
                sessionThreadLocal.set(maximaSession);
            }
            else {
                throw new QtiLogicException("The MathAssess extensions package could not be configured to communicate with Maxima. This package should not have been used in this case");
            }
        }
        return maximaSession;
    }

    private void releaseMaximaSessionForThread() {
        final QtiMaximaProcess maximaSession = sessionThreadLocal.get();
        if (maximaSession != null && qtiMaximaProcessPoolManager != null) {
            log.debug("Finished with maxima process for this request - returning to pool");
            qtiMaximaProcessPoolManager.returnProcess(maximaSession);
            sessionThreadLocal.set(null);
        }
    }
}
