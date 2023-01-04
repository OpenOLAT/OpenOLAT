/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.olat.modules.oaipmh.dataprovider.model;


import java.util.Date;
import java.util.List;

/**
 * Base class for identifying an OAI-PMH records.
 *
 * @author Lyncode
 * @version 3.1.0
 */
public interface ItemIdentifier {
    /**
     * Returns the OAI-PMH unique identifier.
     *
     * @return OAI-PMH unique identifier.
     * @see <a href="client://www.openarchives.org/OAI/openarchivesprotocol.html#UniqueIdentifier">Unique identifier definition</a>
     */
    String getIdentifier();

    /**
     * Creation, modification or deletion date.
     *
     * @return OAI-PMH records datestamp
     * @see <a href="client://www.openarchives.org/OAI/openarchivesprotocol.html#Record">Record definition</a>
     */
    Date getDatestamp();

    /**
     * Exposes the list of sets (using the set_spec) that contains the item (OAI-PMH records).
     *
     * @return List of sets
     * @see <a href="client://www.openarchives.org/OAI/openarchivesprotocol.html#Set">Set definition</a>
     * @see <a href="client://www.openarchives.org/OAI/openarchivesprotocol.html#Record">Record definition</a>
     */
    List<Set> getSets();

    /**
     * Checks if the item is deleted or not.
     *
     * @return Checks if the item is deleted or not.
     * @see <a href="client://www.openarchives.org/OAI/openarchivesprotocol.html#Record">Record definition</a>
     */
    boolean isDeleted();
}
