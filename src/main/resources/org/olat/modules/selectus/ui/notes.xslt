<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
  <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>
  <xsl:param name="versionParam" select="'1.0'"/> 
  <!-- ========================= -->
  <!-- root element: projectteam -->
  <!-- ========================= -->
  <xsl:template match="export">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="simpleA4"
        	page-height="21cm" page-width="29.7cm"
        	margin-top="2cm" margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
          <fo:region-body margin-top="12mm" margin-bottom="10mm"/>
          <fo:region-before extent="10mm"/>
          <fo:region-after extent="8mm"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="simpleA4">
      	<fo:static-content flow-name="xsl-region-before">
      		<xsl:call-template name="page-header"/>
      	</fo:static-content>
      	<fo:static-content flow-name="xsl-region-after">
      		<xsl:call-template name="page-footer"/>
      	</fo:static-content>
      
		<fo:flow flow-name="xsl-region-body">
			<xsl:if test="count(content/row) &gt; 0">
				<fo:block font-size="10pt" font-family="sans-serif">
					<fo:table table-layout="fixed" width="100%" border-collapse="collapse">
						<fo:table-column column-width="0.78cm"/>
						<fo:table-column column-width="1.78cm"/>
						<fo:table-column column-width="7.51cm"/>
						<fo:table-column column-width="1.38cm"/>
						<fo:table-column column-width="9.65cm"/>
						<fo:table-column column-width="2.31cm"/>
						<fo:table-column column-width="2.31cm"/>
						<fo:table-header>
							<xsl:apply-templates select="headers"/>
						</fo:table-header>
						<fo:table-body>
							<xsl:apply-templates select="content"/>
						</fo:table-body>
					</fo:table>
				</fo:block>
			</xsl:if>
			<fo:block id="last-page"/>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
  
  <!-- Page header and footer -->
  <xsl:template name="page-header">
  	<fo:block-container
		border-color="#000000" border-style="solid"
		border-before-width="0.75pt" border-after-width="0.75pt"
		border-start-width="0.75pt" border-end-width="0.75pt"
		font-family="sans-serif" font-size="10pt" font-weight="bold"
		width="100%" height="0.75cm" space-after="0.62cm">
			<fo:block-container absolute-position="absolute"
				width="3cm" top="0.2cm" left="0.2cm"
				text-align="left">
				<fo:block color="red">Confidential</fo:block></fo:block-container>
			<fo:block-container absolute-position="absolute"
				width="15cm" top="0.2cm" left="5.35cm"
				text-align="center"><fo:block><xsl:value-of select="position/title"/></fo:block></fo:block-container>
			<fo:block-container absolute-position="absolute"
				width="5cm" top="0.2cm" left="20.5cm"
				text-align="right"><fo:block><xsl:value-of select="@output-date"/></fo:block></fo:block-container>
		</fo:block-container>
  </xsl:template>
  
  <xsl:template name="page-footer">
		<fo:block-container
			font-family="sans-serif" font-size="10pt"
			width="100%" height="0.75cm">
				<fo:block-container absolute-position="absolute"
					width="5cm" top="0.2cm" left="10.3cm"
					text-align="center"><fo:block><fo:inline>Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/></fo:inline></fo:block></fo:block-container>
				<fo:block-container absolute-position="absolute"
					width="10cm" top="0.2cm" left="15.7cm"
					text-align="right"><fo:block><fo:inline><xsl:value-of select="downloader/lastname"/>, <xsl:value-of select="downloader/firstname"/></fo:inline></fo:block></fo:block-container>
		</fo:block-container>
  </xsl:template>
  
  <!-- Table headers -->
  <xsl:template match="headers">
  	<fo:table-row>
  		<fo:table-cell
				border-color="#000000" border-style="solid"
				border-before-width="0.75pt" border-after-width="0.75pt"
				border-start-width="0.75pt" border-end-width="0.75pt">
				<fo:block/></fo:table-cell>
  		<xsl:apply-templates select="header[@attribute-name = 'title']"/>
  		<xsl:apply-templates select="header[@attribute-name = 'name']"/>
  		<xsl:apply-templates select="header[@attribute-name = 'yearOfBirthday']"/>
  		<xsl:apply-templates select="header[@attribute-name = 'organization']"/>
  		<xsl:apply-templates select="header[@attribute-name = 'nationality']"/>
  		<xsl:apply-templates select="header[@attribute-name = 'gender']"/>
  	</fo:table-row>
  </xsl:template>
  
	<xsl:template match="header">
		<fo:table-cell
			border-color="#000000" border-style="solid"
      border-before-width="0.75pt" border-after-width="0.75pt"
      border-start-width="0pt" border-end-width="0.75pt">
			<fo:block
				color="#000000" font-size="10pt" font-weight="bold"
				margin-top="1mm" margin-right="1mm" margin-bottom="0.25mm" margin-left="1mm"><xsl:value-of select="."/></fo:block >
		</fo:table-cell>
  </xsl:template>
  
  <!-- ========================= -->
  <!-- child element: member     -->
  <!-- ========================= -->
  <xsl:template match="content">
  	<xsl:apply-templates select="row"/>
  </xsl:template>
  
  <xsl:template match="row">
    <xsl:if test="(cell[@attribute-name='afterDeadline'][text()='true']) and (preceding-sibling::row[1]/cell[@attribute-name='afterDeadline'][text()='false'])">
    	<fo:table-row>
    		<fo:table-cell
    			number-columns-spanned="6">
					<fo:block
						padding-top="0.62cm" font-weight="bold"
						margin-top="1mm" margin-right="1mm" margin-bottom="0.25mm" margin-left="1mm"
						>After deadline</fo:block></fo:table-cell></fo:table-row>
    </xsl:if>
    <fo:table-row>
		<fo:table-cell
			border-color="#000000" border-style="solid"
			border-before-width="0.5pt" border-after-width="0.5pt"
			border-start-width="0.5pt" border-end-width="0.5pt"
			number-rows-spanned="2"><fo:block
				margin-top="1mm" margin-right="1mm" margin-bottom="0.25mm" margin-left="1mm"
				><xsl:value-of select="cell[@attribute-name = 'id']"/></fo:block></fo:table-cell>
		<xsl:apply-templates select="cell[@attribute-name = 'title']"/>
		<xsl:apply-templates select="cell[@attribute-name = 'name']"/>
		<xsl:apply-templates select="cell[@attribute-name = 'yearOfBirthday']"/>
		<xsl:apply-templates select="cell[@attribute-name = 'organization']"/>
		<xsl:apply-templates select="cell[@attribute-name = 'nationality']"/>
		<xsl:apply-templates select="cell[@attribute-name = 'gender']"/>
  	</fo:table-row>
  	<fo:table-row>
  		<fo:table-cell
			border-color="#000000" border-style="solid"
			border-before-width="0.5pt" border-after-width="0.5pt"
			border-start-width="0.5pt" border-end-width="0.5pt"
			number-columns-spanned="6"><fo:block
				margin-top="1mm" margin-right="1mm" margin-bottom="0.25mm" margin-left="1mm"
				><xsl:apply-templates select="cell[@attribute-name = 'notes']"/></fo:block></fo:table-cell>
  	</fo:table-row>
  </xsl:template>
  
  <xsl:template match="cell">
		<fo:table-cell
			border-color="black" border-style="solid"
			border-before-width="0.5pt" border-after-width="0.5pt"
			border-start-width="0pt" border-end-width="0.5pt"><fo:block 
			margin-top="1mm" margin-right="1mm" margin-bottom="0.25mm" margin-left="1mm"
			><xsl:value-of select="."/></fo:block></fo:table-cell>
  </xsl:template>
  
  <xsl:template match="cell[@attribute-name = 'notes']">
  	<xsl:apply-templates select="*|text()"/>
  </xsl:template>
  
  <xsl:template match="br">
  	<fo:block> </fo:block>
  </xsl:template>
  
</xsl:stylesheet>