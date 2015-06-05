<?xml version="1.0"?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:ma="http://mathassess.qtitools.org/xsd/mathassess"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xs qti ma qw m">

  <xsl:template match="qti:customInteraction[@class='org.qtitools.mathassess.MathEntryInteraction']">
    <input name="qtiworks_presented_{@responseIdentifier}" type="hidden" value="1"/>
    <xsl:variable name="responseInput" select="qw:get-response-input(@responseIdentifier)" as="element(qw:responseInput)?"/>
    <xsl:variable name="responseValue" select="qw:get-response-value(/, @responseIdentifier)" as="element(qw:responseVariable)?"/>
    <xsl:variable name="asciiMathInput" select="qw:extract-single-cardinality-response-input($responseInput)" as="xs:string?"/>
    <xsl:variable name="orientation" select="if (../self::qti:td or count(../*)!=1) then 'vertical' else 'horizontal'" as="xs:string"/>
    <div class="mathEntryInteraction {$orientation}">
      <div class="inputPanel">
        <a href="{$webappContextPath}/rendering/mathEntryInteractionHelp.html" target="_blank" id="qtiworks_id_mathEntryHelp_{@responseIdentifier}"></a>
        <input id="qtiworks_id_mathEntryInput_{@responseIdentifier}" name="qtiworks_response_{@responseIdentifier}" type="text"
            size="{if (exists(@ma:expectedLength)) then @ma:expectedLength else '10'}">
          <xsl:if test="$isItemSessionEnded">
            <xsl:attribute name="disabled">disabled</xsl:attribute>
          </xsl:if>
          <xsl:if test="exists($asciiMathInput)">
            <xsl:attribute name="value">
              <xsl:value-of select="$asciiMathInput"/>
            </xsl:attribute>
          </xsl:if>
        </input>
      </div>

      <div class="previewPanel">
        <div id="qtiworks_id_mathEntryMessages_{@responseIdentifier}"></div>
        <div id="qtiworks_id_mathEntryPreview_{@responseIdentifier}">
          <!-- Keep this in -->
          <math xmlns="http://www.w3.org/1998/Math/MathML"></math>
        </div>
      </div>
      <script type="text/javascript">
      	jQuery(function() {
      		//because we use ajax to render the page
      		var math = document.getElementById("qtiworks_id_mathEntryPreview_{@responseIdentifier}");
			MathJax.Hub.Queue(["Typeset",MathJax.Hub,math]);
      	});
        QtiWorksRendering.registerReadyCallback(function() {
          var inputControlId = 'qtiworks_id_mathEntryInput_<xsl:value-of select="@responseIdentifier"/>';
          var messageContainerId = 'qtiworks_id_mathEntryMessages_<xsl:value-of select="@responseIdentifier"/>';
          var previewContainerId = 'qtiworks_id_mathEntryPreview_<xsl:value-of select="@responseIdentifier"/>';
          var helpContainerId = 'qtiworks_id_mathEntryHelp_<xsl:value-of select="@responseIdentifier"/>';

          var upConversionAjaxControl = UpConversionAjaxController.createUpConversionAjaxControl(messageContainerId, previewContainerId);
          var widget = AsciiMathInputController.bindInputWidget(inputControlId, upConversionAjaxControl);
          widget.setHelpButtonId(helpContainerId);
          widget.init();
          <xsl:choose>
            <xsl:when test="qw:is-bad-response(@responseIdentifier)">
              widget.show('<xsl:value-of select="$asciiMathInput"/>', {
                cmathFailures: {}
              });
            </xsl:when>
            <xsl:when test="qw:is-null-value($responseValue)">
              widget.syncWithInput();
            </xsl:when>
            <xsl:otherwise>
              widget.show('<xsl:value-of select="$asciiMathInput"/>', {
                cmath: '<xsl:value-of select="qw:escape-for-javascript-string($responseValue/qw:value[@fieldIdentifier='CMathML'])"/>',
                pmathBracketed: '<xsl:value-of select="qw:escape-for-javascript-string($responseValue/qw:value[@fieldIdentifier='PMathMLBracketed'])"/>',
              });
            </xsl:otherwise>
          </xsl:choose>
        });
      </script>
    </div>
  </xsl:template>

</xsl:stylesheet>
