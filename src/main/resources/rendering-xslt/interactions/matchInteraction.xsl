<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti qw xs">

  <xsl:template match="qti:matchInteraction">
    <input name="qtiworks_presented_{@responseIdentifier}" type="hidden" value="1"/>
    <xsl:variable name="orderedSet1" select="qw:get-visible-ordered-choices(., qti:simpleMatchSet[1]/qti:simpleAssociableChoice)" as="element(qti:simpleAssociableChoice)*"/>
    <xsl:variable name="orderedSet2" select="qw:get-visible-ordered-choices(., qti:simpleMatchSet[2]/qti:simpleAssociableChoice)" as="element(qti:simpleAssociableChoice)*"/>
    <div class="{local-name()}">
      <xsl:variable name="responseIdentifier" select="@responseIdentifier" as="xs:string"/>
      <xsl:if test="qw:is-invalid-response(@responseIdentifier)">
        <xsl:call-template name="qw:generic-bad-response-message"/>
      </xsl:if>

      <div class="prompt">
        <xsl:apply-templates select="qti:prompt"/>
      </div>
      <table>
        <thead>
          <tr>
            <th/>
            <xsl:for-each select="$orderedSet2">
              <th>
                <xsl:apply-templates/>
              </th>
            </xsl:for-each>
          </tr>
        </thead>
        <tbody>
          <xsl:for-each select="$orderedSet1">
            <xsl:variable name="set1Identifier" select="@identifier" as="xs:string"/>
            <tr>
              <th>
                <xsl:apply-templates/>
              </th>
              <xsl:for-each select="$orderedSet2">
                <xsl:variable name="set2Identifier" select="@identifier" as="xs:string"/>
                <td align="center">
                  <xsl:variable name="responseValue" select="concat($set1Identifier, ' ', $set2Identifier)" as="xs:string"/>
                  <input type="checkbox" name="qtiworks_response_{$responseIdentifier}" value="{$responseValue}">
                    <xsl:if test="$isItemSessionEnded">
                      <xsl:attribute name="disabled">disabled</xsl:attribute>
                    </xsl:if>
                    <xsl:if test="qw:value-contains(qw:get-response-value(/, $responseIdentifier), $responseValue)">
                      <xsl:attribute name="checked">checked</xsl:attribute>
                    </xsl:if>
                  </input>
                </td>
              </xsl:for-each>
            </tr>
          </xsl:for-each>
        </tbody>
      </table>
      <xsl:if test="$isItemSessionOpen">
        <script type='text/javascript'>
          QtiWorksRendering.registerMatchInteraction('<xsl:value-of select="@responseIdentifier"/>',
            <xsl:value-of select="@maxAssociations"/>,
            {<xsl:for-each select="$orderedSet1">
              <xsl:if test="position() > 1">,</xsl:if>
              <xsl:value-of select="@identifier"/>:<xsl:value-of select="@matchMax"/>
            </xsl:for-each>},
            {<xsl:for-each select="$orderedSet2">
              <xsl:if test="position() > 1">,</xsl:if>
              <xsl:value-of select="@identifier"/>:<xsl:value-of select="@matchMax"/>
            </xsl:for-each>});
        </script>
      </xsl:if>
    </div>
  </xsl:template>
</xsl:stylesheet>
