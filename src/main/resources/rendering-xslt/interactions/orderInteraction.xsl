<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti qw xs">

  <xsl:template match="qti:orderInteraction">
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

      <div id="qtiworks_response_{@responseIdentifier}">
        <!-- Create holder for hidden form fields that will contain the actual data to pass back -->
        <div class="hiddenInputContainer"></div>

        <!-- Filter out the choice identifiers that are visible and split into those which haven't
        been selected and those which have -->
        <xsl:variable name="thisInteraction" select="." as="element(qti:orderInteraction)"/>
        <xsl:variable name="visibleOrderedChoices" as="element(qti:simpleChoice)*" select="qw:get-visible-ordered-choices(., qti:simpleChoice)"/>
        <xsl:variable name="respondedChoiceIdentifiers" select="qw:extract-iterable-elements(qw:get-response-value(/, @responseIdentifier))" as="xs:string*"/>
        <xsl:variable name="unselectedVisibleChoices" select="$visibleOrderedChoices[not(@identifier = $respondedChoiceIdentifiers)]" as="element(qti:simpleChoice)*"/>
        <xsl:variable name="respondedVisibleChoices" as="element(qti:simpleChoice)*">
          <xsl:for-each select="$respondedChoiceIdentifiers">
            <xsl:sequence select="$thisInteraction/qti:simpleChoice[@identifier=current() and qw:is-visible(.)]"/>
          </xsl:for-each>
        </xsl:variable>

        <!-- Now generate selection widget -->
        <xsl:variable name="orientation" select="if (@orientation) then @orientation else 'horizontal'" as="xs:string"/>
        <div class="source box {$orientation}">
          <xsl:if test="$isItemSessionOpen">
            <span class="info">Drag unused items from here...</span>
          </xsl:if>
          <ul class="{$orientation}">
            <xsl:apply-templates select="$unselectedVisibleChoices"/>
          </ul>
          <br/>
        </div>
        <div class="target box {$orientation}">
          <xsl:if test="$isItemSessionOpen">
            <span class="info">Drop and order your selected items here...</span>
          </xsl:if>
          <ul class="{$orientation}">
            <xsl:apply-templates select="$respondedVisibleChoices"/>
          </ul>
          <br/>
        </div>
        <br/>

        <!-- Register with JavaScript -->
        <script type="text/javascript">
          jQuery(document).ready(function() {
            QtiWorksRendering.registerOrderInteraction('<xsl:value-of
              select="@responseIdentifier"/>', [<xsl:value-of
              select="qw:to-javascript-arguments(for $s in $unselectedVisibleChoices return $s/@identifier)"/>], [<xsl:value-of
              select="qw:to-javascript-arguments(for $s in $respondedVisibleChoices return $s/@identifier)"/>], <xsl:value-of
              select="if (@minChoices) then @minChoices else 'null'"/>, <xsl:value-of
              select="if (@maxChoices) then @maxChoices else 'null'"/>);
          });
        </script>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="qti:orderInteraction/qti:simpleChoice">
    <li id="qtiworks_response_{@identifier}" class="ui-state-default">
      <span class="ui-icon ui-icon-arrowthick-2-n-s"></span>
      <xsl:apply-templates/>
    </li>
  </xsl:template>

</xsl:stylesheet>
