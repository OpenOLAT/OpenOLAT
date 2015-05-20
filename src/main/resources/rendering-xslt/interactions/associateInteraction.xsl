<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1" 
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti xs qw">

  <xsl:template match="qti:associateInteraction">
    <input name="qtiworks_presented_{@responseIdentifier}" type="hidden" value="1"/>
    <div class="{local-name()}">
      <xsl:if test="qti:prompt">
        <div class="prompt">
          <xsl:apply-templates select="qti:prompt"/>
        </div>
      </xsl:if>
      <xsl:if test="qw:is-invalid-response(@responseIdentifier)">
        <xsl:call-template name="qw:generic-bad-response-message"/>
      </xsl:if>
      <xsl:variable name="appletContainerId" select="concat('qtiworks_id_appletContainer_', @responseIdentifier)" as="xs:string"/>
      <div id="{$appletContainerId}" class="appletContainer">
        <object type="application/x-java-applet" height="360" width="360">
          <param name="code" value="BoundedGraphicalApplet"/>
          <param name="codebase" value="{$appletCodebase}"/>
          <param name="identifier" value="{@responseIdentifier}"/>
          <param name="baseType" value="pair" />
          <param name="operation_mode" value="graphic_associate_interaction" />
          <!-- (BoundedGraphicalApplet uses -1 to represent 'unlimited') -->
          <param name="number_of_responses" value="{if (@maxAssociations &gt; 0) then @maxAssocations else -1}"/>

          <xsl:variable name="choices" as="element(qti:simpleAssociableChoice)*" select="qw:filter-visible(qti:simpleAssociableChoice)"/>
          <param name="hotspot_count" value="{count($choices)}"/>
          <xsl:for-each select="$choices">
            <!-- (Content is flowStatic, but we can only show strings in labels) -->
            <xsl:variable name="content" as="node()*">
              <xsl:apply-templates/>
            </xsl:variable>
            <param name="hotspot{position()-1}" value="{@identifier}::{normalize-space(string-join(for $n in $content return string($n), ''))}"/>
          </xsl:for-each>

          <xsl:variable name="responseValue" select="qw:get-response-value(/, @responseIdentifier)" as="element(qw:responseVariable)?"/>
          <xsl:if test="qw:is-not-null-value($responseValue)">
            <param name="feedback">
              <xsl:attribute name="value">
                <xsl:value-of select="$responseValue/qw:value" separator=","/>
              </xsl:attribute>
            </param>
          </xsl:if>
        </object>
        <script type="text/javascript">
          jQuery(document).ready(function() {
            QtiWorksRendering.registerAppletBasedInteractionContainer('<xsl:value-of
              select="$appletContainerId"/>', ['<xsl:value-of select="@responseIdentifier"/>']);
          });
        </script>
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>
