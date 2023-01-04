/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.oaipmh;


import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.modules.oaipmh.common.exceptions.XmlWriteException;
import org.olat.modules.oaipmh.common.model.Granularity;
import org.olat.modules.oaipmh.common.services.impl.SimpleResumptionTokenFormat;
import org.olat.modules.oaipmh.common.services.impl.UTCDateProvider;
import org.olat.modules.oaipmh.common.xml.XmlWritable;
import org.olat.modules.oaipmh.common.xml.XmlWriter;
import org.olat.modules.oaipmh.dataprovider.builder.OAIRequestParametersBuilder;
import org.olat.modules.oaipmh.dataprovider.exceptions.OAIException;
import org.olat.modules.oaipmh.dataprovider.model.Context;
import org.olat.modules.oaipmh.dataprovider.model.MetadataFormat;
import org.olat.modules.oaipmh.dataprovider.repository.MetadataItemRepository;
import org.olat.modules.oaipmh.dataprovider.repository.MetadataSetRepository;
import org.olat.modules.oaipmh.dataprovider.repository.Repository;
import org.olat.modules.oaipmh.dataprovider.repository.RepositoryConfiguration;

import javax.xml.stream.XMLStreamException;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OAIServiceImpl implements OAIService {

    private static final String METADATA_DEFAULT_PREFIX = "oai_dc";

    private final MetadataSetRepository setRepository = new MetadataSetRepository();
    private final MetadataItemRepository itemRepository = new MetadataItemRepository();
    private final RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration().withDefaults();

    private final Context context = new Context();
    private final Repository repository = new Repository()
            .withSetRepository(setRepository)
            .withItemRepository(itemRepository)
            .withResumptionTokenFormatter(new SimpleResumptionTokenFormat())
            .withConfiguration(repositoryConfiguration);

    @Override
    public MediaResource handleOAIRequest(
            String requestVerbParameter,
            String requestIdentifierParameter,
            String requestMetadataPrefixParameter,
            String requestResumptionTokenParameter,
            String requestFromParameter,
            String requestUntilParameter,
            String requestSetParameter) {
        StringMediaResource mr = new StringMediaResource();
        String result = "";

        if (!StringHelper.containsNonWhitespace(requestMetadataPrefixParameter)
                && requestResumptionTokenParameter == null
                && !requestVerbParameter.equalsIgnoreCase("listmetadataformats")
                && !requestVerbParameter.equalsIgnoreCase("identify")
                && !requestVerbParameter.equalsIgnoreCase("listsets")) {
            requestMetadataPrefixParameter = METADATA_DEFAULT_PREFIX;
        }

        if (StringHelper.containsNonWhitespace(requestVerbParameter)) {
            if (requestMetadataPrefixParameter != null
                    && !Objects.equals(requestMetadataPrefixParameter, METADATA_DEFAULT_PREFIX)) {
                itemRepository.withOORepositoryItems(setRepository);
            } else {
                itemRepository.withRepositoryItems(setRepository);
            }
        }

        if (StringHelper.containsNonWhitespace(requestMetadataPrefixParameter)) {
            context.withMetadataFormat(requestMetadataPrefixParameter, MetadataFormat.identity());
        } else {
            context.withMetadataFormat(METADATA_DEFAULT_PREFIX, MetadataFormat.identity());
        }

        if (requestSetParameter != null) {
            String[] setSpec = requestSetParameter.split(":");
            setRepository.withSet(setSpec[1], requestSetParameter);
        }

        DataProvider dataProvider = new DataProvider(context, repository);

        try {
            Date fromParameter = null;
            Date untilParameter = null;
            if (requestFromParameter != null) {
                fromParameter = new UTCDateProvider().parse(requestFromParameter, Granularity.Day);
            }
            if (requestUntilParameter != null) {
                untilParameter = new UTCDateProvider().parse(requestUntilParameter, Granularity.Day);
            }

            OAIRequestParametersBuilder requestBuilder = new OAIRequestParametersBuilder();
            requestBuilder.withVerb(requestVerbParameter)
                    .withFrom(fromParameter)
                    .withUntil(untilParameter)
                    .withIdentifier(requestIdentifierParameter)
                    .withMetadataPrefix(requestMetadataPrefixParameter)
                    .withResumptionToken(requestResumptionTokenParameter)
                    .withSet(requestSetParameter);

            result = write(dataProvider.handle(requestBuilder));

        } catch (OAIException | XMLStreamException | XmlWriteException e) {
            //
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        mr.setContentType("application/xml");
        mr.setEncoding("UTF-8");
        mr.setData(result);

        return mr;
    }


    private String write(XmlWritable handle) throws XMLStreamException, XmlWriteException {
        return XmlWriter.toString(writer -> writer.write(handle));
    }
}
