
/**
 * Onyx_reporter_media_providerStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */
        package de.bps.webservices.clients.onyxreporter.stubs;



        /*
        *  Onyx_reporter_media_providerStub java implementation
        */


        public class MediaProviderStub extends org.apache.axis2.client.Stub
        {
        protected org.apache.axis2.description.AxisOperation[] _operations;

        //hashmaps to keep the fault mapping
        private java.util.HashMap faultExceptionNameMap = new java.util.HashMap();
        private java.util.HashMap faultExceptionClassNameMap = new java.util.HashMap();
        private java.util.HashMap faultMessageMap = new java.util.HashMap();

        private static int counter = 0;

        private static synchronized String getUniqueSuffix(){
            // reset the counter if it is greater than 99999
            if (counter > 99999){
                counter = 0;
            }
            counter = counter + 1;
            return Long.toString(System.currentTimeMillis()) + "_" + counter;
        }


    private void populateAxisService() throws org.apache.axis2.AxisFault {

     //creating the Service with a unique name
     _service = new org.apache.axis2.description.AxisService("Onyx_reporter_media_provider" + getUniqueSuffix());
     addAnonymousOperations();

        //creating the operations
        org.apache.axis2.description.AxisOperation __operation;

        _operations = new org.apache.axis2.description.AxisOperation[1];

                   __operation = new org.apache.axis2.description.OutInAxisOperation();


            __operation.setName(new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/", "getMediaFiles"));
	    _service.addOperation(__operation);




            _operations[0]=__operation;


        }

    //populates the faults
    private void populateFaults(){



    }

    /**
      *Constructor that takes in a configContext
      */

    public MediaProviderStub(org.apache.axis2.context.ConfigurationContext configurationContext,
       java.lang.String targetEndpoint)
       throws org.apache.axis2.AxisFault {
         this(configurationContext,targetEndpoint,false);
   }


   /**
     * Constructor that takes in a configContext  and useseperate listner
     */
   public MediaProviderStub(org.apache.axis2.context.ConfigurationContext configurationContext,
        java.lang.String targetEndpoint, boolean useSeparateListener)
        throws org.apache.axis2.AxisFault {
         //To populate AxisService
         populateAxisService();
         populateFaults();

        _serviceClient = new org.apache.axis2.client.ServiceClient(configurationContext,_service);


        configurationContext = _serviceClient.getServiceContext().getConfigurationContext();

        _serviceClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(
                targetEndpoint));
        _serviceClient.getOptions().setUseSeparateListener(useSeparateListener);


    }

    /**
     * Default Constructor
     */
    public MediaProviderStub(org.apache.axis2.context.ConfigurationContext configurationContext) throws org.apache.axis2.AxisFault {

                    this(configurationContext,"http://kelvin.hrz.tu-chemnitz.de:8484/axis2_tester/services/onyx_reporter_media_provider/" );

    }

    /**
     * Default Constructor
     */
    public MediaProviderStub() throws org.apache.axis2.AxisFault {

                    this("http://kelvin.hrz.tu-chemnitz.de:8484/axis2_tester/services/onyx_reporter_media_provider/" );

    }

    /**
     * Constructor taking the target endpoint
     */
    public MediaProviderStub(java.lang.String targetEndpoint) throws org.apache.axis2.AxisFault {
        this(null,targetEndpoint);
    }




                    /**
                     * Auto generated method signature
                     *
                     * @see localhost.axis2_tester.services.Onyx_reporter_media_provider#getMediaFiles
                     * @param mediaFileRequest

                     */



                            public  de.bps.webservices.clients.onyxreporter.stubs.MediaProviderStub.MediaFileResponseE getMediaFiles(

                            de.bps.webservices.clients.onyxreporter.stubs.MediaProviderStub.MediaFileRequestE mediaFileRequest)


                    throws java.rmi.RemoteException

                    {
              org.apache.axis2.context.MessageContext _messageContext = null;
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
              _operationClient.getOptions().setAction("http://kelvin.hrz.tu-chemnitz.de:8484/axis2_tester/services/onyx_reporter_media_provider/getMediaFileForStudent");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);



                  addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");


              // create a message context
              _messageContext = new org.apache.axis2.context.MessageContext();



              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;


                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    mediaFileRequest,
                                                    optimizeContent(new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/",
                                                    "getMediaFiles")));

        //adding SOAP soap_headers
         _serviceClient.addHeadersToEnvelope(env);
        // set the message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);


               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();


                                java.lang.Object object = fromOM(
                                             _returnEnv.getBody().getFirstElement() ,
                                             de.bps.webservices.clients.onyxreporter.stubs.MediaProviderStub.MediaFileResponseE.class,
                                              getEnvelopeNamespaces(_returnEnv));


                                        return (de.bps.webservices.clients.onyxreporter.stubs.MediaProviderStub.MediaFileResponseE)object;

         }catch(org.apache.axis2.AxisFault f){

            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExceptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});


                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
            } finally {
                _messageContext.getTransportOut().getSender().cleanup(_messageContext);
            }
        }



       /**
        *  A utility method that copies the namepaces from the SOAPEnvelope
        */
       private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env){
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
            org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
            returnMap.put(ns.getPrefix(),ns.getNamespaceURI());
        }
       return returnMap;
    }



     private boolean optimizeContent(javax.xml.namespace.QName opName) {
         return false;
    }
     //http://kelvin.hrz.tu-chemnitz.de:8484/axis2_tester/services/onyx_reporter_media_provider/
        public static class ContentPackageDownload
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = contentPackageDownload
                Namespace URI = http://localhost:8080/axis2_tester/services/
                Namespace Prefix = ns1
                */


        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://localhost:8080/axis2_tester/services/")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }



                        /**
                        * field for RelativePath
                        * This was an Array!
                        */


                                    protected java.lang.String[] localRelativePath ;


                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getRelativePath(){
                               return localRelativePath;
                           }






                              /**
                               * validate the array for RelativePath
                               */
                              protected void validateRelativePath(java.lang.String[] param){

                              if ((param != null) && (param.length < 1)){
                                throw new java.lang.RuntimeException();
                              }

                              }


                             /**
                              * Auto generated setter method
                              * @param param RelativePath
                              */
                              public void setRelativePath(java.lang.String[] param){

                                   validateRelativePath(param);


                                      this.localRelativePath=param;
                              }



                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addRelativePath(java.lang.String param){
                                   if (localRelativePath == null){
                                   localRelativePath = new java.lang.String[]{};
                                   }



                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localRelativePath);
                               list.add(param);
                               this.localRelativePath =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }


     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;

        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }


        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{



               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

                 public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                       ContentPackageDownload.this.serialize(parentQName,factory,xmlWriter);
                 }
               };
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               parentQName,factory,dataSource);

       }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,factory,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{




                java.lang.String prefix = null;
                java.lang.String namespace = null;


                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();

                    if ((namespace != null) && (namespace.trim().length() > 0)) {
                        java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                        if (writerPrefix != null) {
                            xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                        } else {
                            if (prefix == null) {
                                prefix = generatePrefix(namespace);
                            }

                            xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                            xmlWriter.writeNamespace(prefix, namespace);
                            xmlWriter.setPrefix(prefix, namespace);
                        }
                    } else {
                        xmlWriter.writeStartElement(parentQName.getLocalPart());
                    }

                  if (serializeType){


                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://localhost:8080/axis2_tester/services/");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":contentPackageDownload",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "contentPackageDownload",
                           xmlWriter);
                   }


                   }

                             if (localRelativePath!=null) {
                                   namespace = "";
                                   boolean emptyNamespace = namespace == null || namespace.length() == 0;
                                   prefix =  emptyNamespace ? null : xmlWriter.getPrefix(namespace);
                                   for (int i = 0;i < localRelativePath.length;i++){

                                            if (localRelativePath[i] != null){

                                                if (!emptyNamespace) {
                                                    if (prefix == null) {
                                                        java.lang.String prefix2 = generatePrefix(namespace);

                                                        xmlWriter.writeStartElement(prefix2,"relativePath", namespace);
                                                        xmlWriter.writeNamespace(prefix2, namespace);
                                                        xmlWriter.setPrefix(prefix2, namespace);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace,"relativePath");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("relativePath");
                                                }


                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRelativePath[i]));

                                                xmlWriter.writeEndElement();

                                                } else {

                                                           throw new org.apache.axis2.databinding.ADBException("relativePath cannot be null!!");

                                                }

                                   }
                             } else {

                                         throw new org.apache.axis2.databinding.ADBException("relativePath cannot be null!!");

                             }


                    xmlWriter.writeEndElement();


        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }




         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }



        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{



                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();


                            if (localRelativePath!=null){
                                  for (int i = 0;i < localRelativePath.length;i++){

                                         if (localRelativePath[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("",
                                                                              "relativePath"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRelativePath[i]));
                                          } else {

                                                    throw new org.apache.axis2.databinding.ADBException("relativePath cannot be null!!");

                                          }


                                  }
                            } else {

                                    throw new org.apache.axis2.databinding.ADBException("relativePath cannot be null!!");

                            }



                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());



        }



     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{




        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static ContentPackageDownload parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ContentPackageDownload object =
                new ContentPackageDownload();

            try {

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();


                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);

                            if (!"contentPackageDownload".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (ContentPackageDownload)ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }


                  }


                }




                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.


                    reader.next();

                        java.util.ArrayList list1 = new java.util.ArrayList();


                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","relativePath").equals(reader.getName())){



                                    // Process the array and step past its final element's end.
                                    list1.add(reader.getElementText());

                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone1 = false;
                                            while(!loopDone1){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone1 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("","relativePath").equals(reader.getName())){
                                                         list1.add(reader.getElementText());

                                                    }else{
                                                        loopDone1 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array

                                                    object.setRelativePath((java.lang.String[])
                                                        list1.toArray(new java.lang.String[list1.size()]));

                              }  // End of if for expected property start element

                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }

                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();

                                if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());




            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class



        }


        public static class MediaFileRequestChoice_type0
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = mediaFileRequestChoice_type0
                Namespace URI = http://localhost:8080/axis2_tester/services/
                Namespace Prefix = ns1
                */


        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://localhost:8080/axis2_tester/services/")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }


            /** Whenever a new property is set ensure all others are unset
             *  There can be only one choice and the last one wins
             */
            private void clearAllSettingTrackers() {

                   localResultFileDownloadTracker = false;

                   localContentPackageDownloadTracker = false;

            }


                        /**
                        * field for ResultFileDownload
                        * This was an Array!
                        */


                                    protected ResultFileDownload[] localResultFileDownload ;

                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localResultFileDownloadTracker = false ;


                           /**
                           * Auto generated getter method
                           * @return ResultFileDownload[]
                           */
                           public  ResultFileDownload[] getResultFileDownload(){
                               return localResultFileDownload;
                           }






                              /**
                               * validate the array for ResultFileDownload
                               */
                              protected void validateResultFileDownload(ResultFileDownload[] param){

                              if ((param != null) && (param.length < 1)){
                                throw new java.lang.RuntimeException();
                              }

                              }


                             /**
                              * Auto generated setter method
                              * @param param ResultFileDownload
                              */
                              public void setResultFileDownload(ResultFileDownload[] param){

                                   validateResultFileDownload(param);


                                   clearAllSettingTrackers();

                                          if (param != null){
                                             //update the setting tracker
                                             localResultFileDownloadTracker = true;
                                          } else {
                                             localResultFileDownloadTracker = false;

                                          }

                                      this.localResultFileDownload=param;
                              }



                             /**
                             * Auto generated add method for the array for convenience
                             * @param param ResultFileDownload
                             */
                             public void addResultFileDownload(ResultFileDownload param){
                                   if (localResultFileDownload == null){
                                   localResultFileDownload = new ResultFileDownload[]{};
                                   }


                                   clearAllSettingTrackers();

                                 //update the setting tracker
                                localResultFileDownloadTracker = true;


                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localResultFileDownload);
                               list.add(param);
                               this.localResultFileDownload =
                             (ResultFileDownload[])list.toArray(
                            new ResultFileDownload[list.size()]);

                             }


                        /**
                        * field for ContentPackageDownload
                        */


                                    protected ContentPackageDownload localContentPackageDownload ;

                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localContentPackageDownloadTracker = false ;


                           /**
                           * Auto generated getter method
                           * @return ContentPackageDownload
                           */
                           public  ContentPackageDownload getContentPackageDownload(){
                               return localContentPackageDownload;
                           }



                            /**
                               * Auto generated setter method
                               * @param param ContentPackageDownload
                               */
                               public void setContentPackageDownload(ContentPackageDownload param){

                                clearAllSettingTrackers();

                                       if (param != null){
                                          //update the setting tracker
                                          localContentPackageDownloadTracker = true;
                                       } else {
                                          localContentPackageDownloadTracker = false;

                                       }

                                            this.localContentPackageDownload=param;


                               }


     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;

        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }


        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{



               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

                 public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                       MediaFileRequestChoice_type0.this.serialize(parentQName,factory,xmlWriter);
                 }
               };
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               parentQName,factory,dataSource);

       }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,factory,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{




                java.lang.String prefix = null;
                java.lang.String namespace = null;

                  if (serializeType){


                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://localhost:8080/axis2_tester/services/");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":mediaFileRequestChoice_type0",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "mediaFileRequestChoice_type0",
                           xmlWriter);
                   }


                   }
                if (localResultFileDownloadTracker){
                                       if (localResultFileDownload!=null){
                                            for (int i = 0;i < localResultFileDownload.length;i++){
                                                if (localResultFileDownload[i] != null){
                                                 localResultFileDownload[i].serialize(new javax.xml.namespace.QName("","resultFileDownload"),
                                                           factory,xmlWriter);
                                                } else {

                                                           throw new org.apache.axis2.databinding.ADBException("resultFileDownload cannot be null!!");

                                                }

                                            }
                                     } else {

                                               throw new org.apache.axis2.databinding.ADBException("resultFileDownload cannot be null!!");

                                    }
                                 } if (localContentPackageDownloadTracker){
                                            if (localContentPackageDownload==null){
                                                 throw new org.apache.axis2.databinding.ADBException("contentPackageDownload cannot be null!!");
                                            }
                                           localContentPackageDownload.serialize(new javax.xml.namespace.QName("","contentPackageDownload"),
                                               factory,xmlWriter);
                                        }

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }


         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }



        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{



                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localResultFileDownloadTracker){
                             if (localResultFileDownload!=null) {
                                 for (int i = 0;i < localResultFileDownload.length;i++){

                                    if (localResultFileDownload[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("",
                                                                          "resultFileDownload"));
                                         elementList.add(localResultFileDownload[i]);
                                    } else {

                                               throw new org.apache.axis2.databinding.ADBException("resultFileDownload cannot be null !!");

                                    }

                                 }
                             } else {

                                        throw new org.apache.axis2.databinding.ADBException("resultFileDownload cannot be null!!");

                             }

                        } if (localContentPackageDownloadTracker){
                            elementList.add(new javax.xml.namespace.QName("",
                                                                      "contentPackageDownload"));


                                    if (localContentPackageDownload==null){
                                         throw new org.apache.axis2.databinding.ADBException("contentPackageDownload cannot be null!!");
                                    }
                                    elementList.add(localContentPackageDownload);
                                }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());



        }



     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{




        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static MediaFileRequestChoice_type0 parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            MediaFileRequestChoice_type0 object =
                new MediaFileRequestChoice_type0();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();




                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.


                        java.util.ArrayList list1 = new java.util.ArrayList();

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","resultFileDownload").equals(reader.getName())){



                                    // Process the array and step past its final element's end.
                                    list1.add(ResultFileDownload.Factory.parse(reader));

                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone1 = false;
                                                        while(!loopDone1){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone1 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("","resultFileDownload").equals(reader.getName())){
                                                                    list1.add(ResultFileDownload.Factory.parse(reader));

                                                                }else{
                                                                    loopDone1 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array

                                                        object.setResultFileDownload((ResultFileDownload[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                ResultFileDownload.class,
                                                                list1));

                              }  // End of if for expected property start element

                                        else

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","contentPackageDownload").equals(reader.getName())){

                                                object.setContentPackageDownload(ContentPackageDownload.Factory.parse(reader));

                                        reader.next();

                              }  // End of if for expected property start element




            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class



        }


        public static class MediaFileResponseE
        implements org.apache.axis2.databinding.ADBBean{

                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://localhost:8080/axis2_tester/services/",
                "mediaFileResponse",
                "ns1");



        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://localhost:8080/axis2_tester/services/")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }



                        /**
                        * field for MediaFileResponse
                        */


                                    protected MediaFileResponse localMediaFileResponse ;


                           /**
                           * Auto generated getter method
                           * @return MediaFileResponse
                           */
                           public  MediaFileResponse getMediaFileResponse(){
                               return localMediaFileResponse;
                           }



                            /**
                               * Auto generated setter method
                               * @param param MediaFileResponse
                               */
                               public void setMediaFileResponse(MediaFileResponse param){

                                            this.localMediaFileResponse=param;


                               }


     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;

        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }


        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{



                org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME){

                 public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                       MediaFileResponseE.this.serialize(MY_QNAME,factory,xmlWriter);
                 }
               };
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);

       }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,factory,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{


                //We can safely assume an element has only one type associated with it

                                 if (localMediaFileResponse==null){
                                   throw new org.apache.axis2.databinding.ADBException("Property cannot be null!");
                                 }
                                 localMediaFileResponse.serialize(MY_QNAME,factory,xmlWriter);


        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

        /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
                if (namespace.equals(""))
              {
                  xmlWriter.writeAttribute(attName,attValue);
              }
              else
              {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }


           /**
             * Util method to write an attribute without the ns prefix
             */
            private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

                java.lang.String attributeNamespace = qname.getNamespaceURI();
                java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                if (attributePrefix == null) {
                    attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                }
                java.lang.String attributeValue;
                if (attributePrefix.trim().length() > 0) {
                    attributeValue = attributePrefix + ":" + qname.getLocalPart();
                } else {
                    attributeValue = qname.getLocalPart();
                }

                if (namespace.equals("")) {
                    xmlWriter.writeAttribute(attName, attributeValue);
                } else {
                    registerPrefix(xmlWriter, namespace);
                    xmlWriter.writeAttribute(namespace, attName, attributeValue);
                }
            }
        /**
         *  method to handle Qnames
         */

        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix,namespaceURI);
                }

                if (prefix.trim().length() > 0){
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }

            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix,namespaceURI);
                        }

                        if (prefix.trim().length() > 0){
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }

        }


         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }



        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{




                //We can safely assume an element has only one type associated with it
                return localMediaFileResponse.getPullParser(MY_QNAME);

        }



     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{




        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static MediaFileResponseE parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            MediaFileResponseE object =
                new MediaFileResponseE();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();




                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.

                while(!reader.isEndElement()) {
                    if (reader.isStartElement() ){

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/","mediaFileResponse").equals(reader.getName())){

                                                object.setMediaFileResponse(MediaFileResponse.Factory.parse(reader));

                              }  // End of if for expected property start element

                             else{
                                        // A start element we are not expecting indicates an invalid parameter was passed
                                        throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                             }

                             } else {
                                reader.next();
                             }
                           }  // end of while loop




            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class



        }


        public static class ResultFileUploadSequence_type0
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = resultFileUploadSequence_type0
                Namespace URI = http://localhost:8080/axis2_tester/services/
                Namespace Prefix = ns1
                */

		private static final long serialVersionUID = 1L;

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://localhost:8080/axis2_tester/services/")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }



                        /**
                        * field for RelativePath
                        */


                                    protected java.lang.String localRelativePath ;


                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getRelativePath(){
                               return localRelativePath;
                           }



                            /**
                               * Auto generated setter method
                               * @param param RelativePath
                               */
                               public void setRelativePath(java.lang.String param){

                                            this.localRelativePath=param;


                               }


                        /**
                        * field for MediaFile
                        */


                                    protected transient javax.activation.DataHandler localMediaFile ;


                           /**
                           * Auto generated getter method
                           * @return javax.activation.DataHandler
                           */
                           public  javax.activation.DataHandler getMediaFile(){
                               return localMediaFile;
                           }



                            /**
                               * Auto generated setter method
                               * @param param MediaFile
                               */
                               public void setMediaFile(javax.activation.DataHandler param){

                                            this.localMediaFile=param;


                               }


     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;

        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }


        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{



               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

                 public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                       ResultFileUploadSequence_type0.this.serialize(parentQName,factory,xmlWriter);
                 }
               };
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               parentQName,factory,dataSource);

       }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,factory,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{




                java.lang.String prefix = null;
                java.lang.String namespace = null;

                  if (serializeType){


                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://localhost:8080/axis2_tester/services/");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":resultFileUploadSequence_type0",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "resultFileUploadSequence_type0",
                           xmlWriter);
                   }


                   }

                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"relativePath", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"relativePath");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("relativePath");
                                    }


                                          if (localRelativePath==null){
                                              // write the nil attribute

                                                     throw new org.apache.axis2.databinding.ADBException("relativePath cannot be null!!");

                                          }else{


                                                   xmlWriter.writeCharacters(localRelativePath);

                                          }

                                   xmlWriter.writeEndElement();

                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"mediaFile", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"mediaFile");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("mediaFile");
                                    }


                                    if (localMediaFile!=null)
                                    {
                                       xmlWriter.writeDataHandler(localMediaFile);
                                    }

                                   xmlWriter.writeEndElement();


        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }



        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{



                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();


                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "relativePath"));

                                        if (localRelativePath != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRelativePath));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("relativePath cannot be null!!");
                                        }

                                      elementList.add(new javax.xml.namespace.QName("",
                                        "mediaFile"));

                            elementList.add(localMediaFile);


                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());



        }



     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{




        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static ResultFileUploadSequence_type0 parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ResultFileUploadSequence_type0 object =
                new ResultFileUploadSequence_type0();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();




                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.




                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","relativePath").equals(reader.getName())){

                                    java.lang.String content = reader.getElementText();

                                              object.setRelativePath(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

                                        reader.next();

                              }  // End of if for expected property start element

                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }


                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","mediaFile").equals(reader.getName())){
                                reader.next();
                                    if (isReaderMTOMAware(reader)
                                            &&
                                            java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_BINARY)))
                                    {
                                        //MTOM aware reader - get the datahandler directly and put it in the object
                                        object.setMediaFile(
                                                (javax.activation.DataHandler) reader.getProperty(org.apache.axiom.om.OMConstants.DATA_HANDLER));
                                    } else {
                                        if (reader.getEventType() == javax.xml.stream.XMLStreamConstants.START_ELEMENT && reader.getName().equals(new javax.xml.namespace.QName(org.apache.axiom.om.impl.MTOMConstants.XOP_NAMESPACE_URI, org.apache.axiom.om.impl.MTOMConstants.XOP_INCLUDE)))
                                        {
                                            java.lang.String id = org.apache.axiom.om.util.ElementHelper.getContentID(reader, "UTF-8");
                                            object.setMediaFile(((org.apache.axiom.soap.impl.builder.MTOMStAXSOAPModelBuilder) ((org.apache.axiom.om.impl.llom.OMStAXWrapper) reader).getBuilder()).getDataHandler(id));
                                            reader.next();

                                                reader.next();

                                        } else if(reader.hasText()) {
                                            //Do the usual conversion
                                            java.lang.String content = reader.getText();
                                            object.setMediaFile(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBase64Binary(content));

                                                reader.next();

                                        }
                                    }


                                        reader.next();

                              }  // End of if for expected property start element

                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }

                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();




            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class



        }


        public static class MediaFileResponse
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = mediaFileResponse
                Namespace URI = http://localhost:8080/axis2_tester/services/
                Namespace Prefix = ns1
                */


        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://localhost:8080/axis2_tester/services/")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }


            /** Whenever a new property is set ensure all others are unset
             *  There can be only one choice and the last one wins
             */
            private void clearAllSettingTrackers() {

                   localResultFileUploadTracker = false;

                   localContentPackageUploadTracker = false;

            }


                        /**
                        * field for ResultFileUpload
                        * This was an Array!
                        */


                                    protected ResultFileUpload[] localResultFileUpload ;

                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localResultFileUploadTracker = false ;


                           /**
                           * Auto generated getter method
                           * @return ResultFileUpload[]
                           */
                           public  ResultFileUpload[] getResultFileUpload(){
                               return localResultFileUpload;
                           }






                              /**
                               * validate the array for ResultFileUpload
                               */
                              protected void validateResultFileUpload(ResultFileUpload[] param){

                              if ((param != null) && (param.length < 1)){
                                throw new java.lang.RuntimeException();
                              }

                              }


                             /**
                              * Auto generated setter method
                              * @param param ResultFileUpload
                              */
                              public void setResultFileUpload(ResultFileUpload[] param){

                                   validateResultFileUpload(param);


                                   clearAllSettingTrackers();

                                          if (param != null){
                                             //update the setting tracker
                                             localResultFileUploadTracker = true;
                                          } else {
                                             localResultFileUploadTracker = false;

                                          }

                                      this.localResultFileUpload=param;
                              }



                             /**
                             * Auto generated add method for the array for convenience
                             * @param param ResultFileUpload
                             */
                             public void addResultFileUpload(ResultFileUpload param){
                                   if (localResultFileUpload == null){
                                   localResultFileUpload = new ResultFileUpload[]{};
                                   }


                                   clearAllSettingTrackers();

                                 //update the setting tracker
                                localResultFileUploadTracker = true;


                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localResultFileUpload);
                               list.add(param);
                               this.localResultFileUpload =
                             (ResultFileUpload[])list.toArray(
                            new ResultFileUpload[list.size()]);

                             }


                        /**
                        * field for ContentPackageUpload
                        * This was an Array!
                        */


                                    protected ContentPackageUpload[] localContentPackageUpload ;

                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localContentPackageUploadTracker = false ;


                           /**
                           * Auto generated getter method
                           * @return ContentPackageUpload[]
                           */
                           public  ContentPackageUpload[] getContentPackageUpload(){
                               return localContentPackageUpload;
                           }






                              /**
                               * validate the array for ContentPackageUpload
                               */
                              protected void validateContentPackageUpload(ContentPackageUpload[] param){

                              if ((param != null) && (param.length < 1)){
                                throw new java.lang.RuntimeException();
                              }

                              }


                             /**
                              * Auto generated setter method
                              * @param param ContentPackageUpload
                              */
                              public void setContentPackageUpload(ContentPackageUpload[] param){

                                   validateContentPackageUpload(param);


                                   clearAllSettingTrackers();

                                          if (param != null){
                                             //update the setting tracker
                                             localContentPackageUploadTracker = true;
                                          } else {
                                             localContentPackageUploadTracker = false;

                                          }

                                      this.localContentPackageUpload=param;
                              }



                             /**
                             * Auto generated add method for the array for convenience
                             * @param param ContentPackageUpload
                             */
                             public void addContentPackageUpload(ContentPackageUpload param){
                                   if (localContentPackageUpload == null){
                                   localContentPackageUpload = new ContentPackageUpload[]{};
                                   }


                                   clearAllSettingTrackers();

                                 //update the setting tracker
                                localContentPackageUploadTracker = true;


                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localContentPackageUpload);
                               list.add(param);
                               this.localContentPackageUpload =
                             (ContentPackageUpload[])list.toArray(
                            new ContentPackageUpload[list.size()]);

                             }


     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;

        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }


        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{



               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

                 public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                       MediaFileResponse.this.serialize(parentQName,factory,xmlWriter);
                 }
               };
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               parentQName,factory,dataSource);

       }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,factory,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{




                java.lang.String prefix = null;
                java.lang.String namespace = null;


                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();

                    if ((namespace != null) && (namespace.trim().length() > 0)) {
                        java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                        if (writerPrefix != null) {
                            xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                        } else {
                            if (prefix == null) {
                                prefix = generatePrefix(namespace);
                            }

                            xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                            xmlWriter.writeNamespace(prefix, namespace);
                            xmlWriter.setPrefix(prefix, namespace);
                        }
                    } else {
                        xmlWriter.writeStartElement(parentQName.getLocalPart());
                    }

                  if (serializeType){


                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://localhost:8080/axis2_tester/services/");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":mediaFileResponse",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "mediaFileResponse",
                           xmlWriter);
                   }


                   }
                if (localResultFileUploadTracker){
                                       if (localResultFileUpload!=null){
                                            for (int i = 0;i < localResultFileUpload.length;i++){
                                                if (localResultFileUpload[i] != null){
                                                 localResultFileUpload[i].serialize(new javax.xml.namespace.QName("","resultFileUpload"),
                                                           factory,xmlWriter);
                                                } else {

                                                           throw new org.apache.axis2.databinding.ADBException("resultFileUpload cannot be null!!");

                                                }

                                            }
                                     } else {

                                               throw new org.apache.axis2.databinding.ADBException("resultFileUpload cannot be null!!");

                                    }
                                 } if (localContentPackageUploadTracker){
                                       if (localContentPackageUpload!=null){
                                            for (int i = 0;i < localContentPackageUpload.length;i++){
                                                if (localContentPackageUpload[i] != null){
                                                 localContentPackageUpload[i].serialize(new javax.xml.namespace.QName("","contentPackageUpload"),
                                                           factory,xmlWriter);
                                                } else {

                                                           throw new org.apache.axis2.databinding.ADBException("contentPackageUpload cannot be null!!");

                                                }

                                            }
                                     } else {

                                               throw new org.apache.axis2.databinding.ADBException("contentPackageUpload cannot be null!!");

                                    }
                                 }
                    xmlWriter.writeEndElement();


        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }



         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }



        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{



                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localResultFileUploadTracker){
                             if (localResultFileUpload!=null) {
                                 for (int i = 0;i < localResultFileUpload.length;i++){

                                    if (localResultFileUpload[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("",
                                                                          "resultFileUpload"));
                                         elementList.add(localResultFileUpload[i]);
                                    } else {

                                               throw new org.apache.axis2.databinding.ADBException("resultFileUpload cannot be null !!");

                                    }

                                 }
                             } else {

                                        throw new org.apache.axis2.databinding.ADBException("resultFileUpload cannot be null!!");

                             }

                        } if (localContentPackageUploadTracker){
                             if (localContentPackageUpload!=null) {
                                 for (int i = 0;i < localContentPackageUpload.length;i++){

                                    if (localContentPackageUpload[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("",
                                                                          "contentPackageUpload"));
                                         elementList.add(localContentPackageUpload[i]);
                                    } else {

                                               throw new org.apache.axis2.databinding.ADBException("contentPackageUpload cannot be null !!");

                                    }

                                 }
                             } else {

                                        throw new org.apache.axis2.databinding.ADBException("contentPackageUpload cannot be null!!");

                             }

                        }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());



        }



     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{




        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static MediaFileResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            MediaFileResponse object =
                new MediaFileResponse();

            try {

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();


                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);

                            if (!"mediaFileResponse".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (MediaFileResponse)ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }


                  }


                }




                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.


                    reader.next();

                        java.util.ArrayList list1 = new java.util.ArrayList();

                        java.util.ArrayList list2 = new java.util.ArrayList();

                while(!reader.isEndElement()) {
                    if (reader.isStartElement() ){

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","resultFileUpload").equals(reader.getName())){



                                    // Process the array and step past its final element's end.
                                    list1.add(ResultFileUpload.Factory.parse(reader));

                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone1 = false;
                                                        while(!loopDone1){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone1 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("","resultFileUpload").equals(reader.getName())){
                                                                    list1.add(ResultFileUpload.Factory.parse(reader));

                                                                }else{
                                                                    loopDone1 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array

                                                        object.setResultFileUpload((ResultFileUpload[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                ResultFileUpload.class,
                                                                list1));

                              }  // End of if for expected property start element

                                        else

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","contentPackageUpload").equals(reader.getName())){



                                    // Process the array and step past its final element's end.
                                    list2.add(ContentPackageUpload.Factory.parse(reader));

                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone2 = false;
                                                        while(!loopDone2){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone2 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("","contentPackageUpload").equals(reader.getName())){
                                                                    list2.add(ContentPackageUpload.Factory.parse(reader));

                                                                }else{
                                                                    loopDone2 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array

                                                        object.setContentPackageUpload((ContentPackageUpload[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                ContentPackageUpload.class,
                                                                list2));

                              }  // End of if for expected property start element

                             } else {
                                reader.next();
                             }
                           }  // end of while loop




            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class



        }


        public static class MediaFileRequestE
        implements org.apache.axis2.databinding.ADBBean{

                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://localhost:8080/axis2_tester/services/",
                "mediaFileRequest",
                "ns1");



        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://localhost:8080/axis2_tester/services/")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }



                        /**
                        * field for MediaFileRequest
                        */


                                    protected MediaFileRequest localMediaFileRequest ;


                           /**
                           * Auto generated getter method
                           * @return MediaFileRequest
                           */
                           public  MediaFileRequest getMediaFileRequest(){
                               return localMediaFileRequest;
                           }



                            /**
                               * Auto generated setter method
                               * @param param MediaFileRequest
                               */
                               public void setMediaFileRequest(MediaFileRequest param){

                                            this.localMediaFileRequest=param;


                               }


     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;

        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }


        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{



                org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME){

                 public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                       MediaFileRequestE.this.serialize(MY_QNAME,factory,xmlWriter);
                 }
               };
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);

       }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,factory,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{


                //We can safely assume an element has only one type associated with it

                                 if (localMediaFileRequest==null){
                                   throw new org.apache.axis2.databinding.ADBException("Property cannot be null!");
                                 }
                                 localMediaFileRequest.serialize(MY_QNAME,factory,xmlWriter);


        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

        /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
                if (namespace.equals(""))
              {
                  xmlWriter.writeAttribute(attName,attValue);
              }
              else
              {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }


           /**
             * Util method to write an attribute without the ns prefix
             */
            private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

                java.lang.String attributeNamespace = qname.getNamespaceURI();
                java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                if (attributePrefix == null) {
                    attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                }
                java.lang.String attributeValue;
                if (attributePrefix.trim().length() > 0) {
                    attributeValue = attributePrefix + ":" + qname.getLocalPart();
                } else {
                    attributeValue = qname.getLocalPart();
                }

                if (namespace.equals("")) {
                    xmlWriter.writeAttribute(attName, attributeValue);
                } else {
                    registerPrefix(xmlWriter, namespace);
                    xmlWriter.writeAttribute(namespace, attName, attributeValue);
                }
            }
        /**
         *  method to handle Qnames
         */

        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix,namespaceURI);
                }

                if (prefix.trim().length() > 0){
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }

            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix,namespaceURI);
                        }

                        if (prefix.trim().length() > 0){
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }

        }


         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }



        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{




                //We can safely assume an element has only one type associated with it
                return localMediaFileRequest.getPullParser(MY_QNAME);

        }



     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{




        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static MediaFileRequestE parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            MediaFileRequestE object =
                new MediaFileRequestE();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();




                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.


                while(!reader.isEndElement()) {
                    if (reader.isStartElement() ){

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/","mediaFileRequest").equals(reader.getName())){

                                                object.setMediaFileRequest(MediaFileRequest.Factory.parse(reader));

                              }  // End of if for expected property start element

                             else{
                                        // A start element we are not expecting indicates an invalid parameter was passed
                                        throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                             }

                             } else {
                                reader.next();
                             }
                           }  // end of while loop




            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class



        }


        public static class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{


                  if (
                  "http://localhost:8080/axis2_tester/services/".equals(namespaceURI) &&
                  "contentPackageDownload".equals(typeName)){

                            return  ContentPackageDownload.Factory.parse(reader);


                  }


                  if (
                  "http://localhost:8080/axis2_tester/services/".equals(namespaceURI) &&
                  "mediaFileRequest".equals(typeName)){

                            return  MediaFileRequest.Factory.parse(reader);


                  }


                  if (
                  "http://localhost:8080/axis2_tester/services/".equals(namespaceURI) &&
                  "mediaFileResponse".equals(typeName)){

                            return  MediaFileResponse.Factory.parse(reader);


                  }


                  if (
                  "http://localhost:8080/axis2_tester/services/".equals(namespaceURI) &&
                  "resultFileUpload".equals(typeName)){

                            return  ResultFileUpload.Factory.parse(reader);


                  }


                  if (
                  "http://localhost:8080/axis2_tester/services/".equals(namespaceURI) &&
                  "resultFileDownload".equals(typeName)){

                            return  ResultFileDownload.Factory.parse(reader);


                  }


                  if (
                  "http://localhost:8080/axis2_tester/services/".equals(namespaceURI) &&
                  "contentPackageUpload".equals(typeName)){

                            return  ContentPackageUpload.Factory.parse(reader);


                  }


             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }

        public static class MediaFileRequest
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = mediaFileRequest
                Namespace URI = http://localhost:8080/axis2_tester/services/
                Namespace Prefix = ns1
                */


        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://localhost:8080/axis2_tester/services/")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }



                        /**
                        * field for TestId
                        */


                                    protected java.lang.String localTestId ;


                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getTestId(){
                               return localTestId;
                           }



                            /**
                               * Auto generated setter method
                               * @param param TestId
                               */
                               public void setTestId(java.lang.String param){

                                            this.localTestId=param;


                               }


                        /**
                        * field for UserId
                        */


                                    protected java.lang.String localUserId ;


                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getUserId(){
                               return localUserId;
                           }



                            /**
                               * Auto generated setter method
                               * @param param UserId
                               */
                               public void setUserId(java.lang.String param){

                                            this.localUserId=param;


                               }


                        /**
                        * field for SecretToShare
                        */


                                    protected java.lang.String localSecretToShare ;


                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSecretToShare(){
                               return localSecretToShare;
                           }



                            /**
                               * Auto generated setter method
                               * @param param SecretToShare
                               */
                               public void setSecretToShare(java.lang.String param){

                                            this.localSecretToShare=param;


                               }


                        /**
                        * field for MediaFileRequestChoice_type0
                        */


                                    protected MediaFileRequestChoice_type0 localMediaFileRequestChoice_type0 ;


                           /**
                           * Auto generated getter method
                           * @return MediaFileRequestChoice_type0
                           */
                           public  MediaFileRequestChoice_type0 getMediaFileRequestChoice_type0(){
                               return localMediaFileRequestChoice_type0;
                           }



                            /**
                               * Auto generated setter method
                               * @param param MediaFileRequestChoice_type0
                               */
                               public void setMediaFileRequestChoice_type0(MediaFileRequestChoice_type0 param){

                                            this.localMediaFileRequestChoice_type0=param;


                               }


     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;

        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }


        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{



               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

                 public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                       MediaFileRequest.this.serialize(parentQName,factory,xmlWriter);
                 }
               };
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               parentQName,factory,dataSource);

       }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,factory,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{




                java.lang.String prefix = null;
                java.lang.String namespace = null;


                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();

                    if ((namespace != null) && (namespace.trim().length() > 0)) {
                        java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                        if (writerPrefix != null) {
                            xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                        } else {
                            if (prefix == null) {
                                prefix = generatePrefix(namespace);
                            }

                            xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                            xmlWriter.writeNamespace(prefix, namespace);
                            xmlWriter.setPrefix(prefix, namespace);
                        }
                    } else {
                        xmlWriter.writeStartElement(parentQName.getLocalPart());
                    }

                  if (serializeType){


                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://localhost:8080/axis2_tester/services/");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":mediaFileRequest",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "mediaFileRequest",
                           xmlWriter);
                   }


                   }

                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"testId", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"testId");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("testId");
                                    }


                                          if (localTestId==null){
                                              // write the nil attribute

                                                     throw new org.apache.axis2.databinding.ADBException("testId cannot be null!!");

                                          }else{


                                                   xmlWriter.writeCharacters(localTestId);

                                          }

                                   xmlWriter.writeEndElement();

                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"userId", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"userId");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("userId");
                                    }


                                          if (localUserId==null){
                                              // write the nil attribute

                                                     throw new org.apache.axis2.databinding.ADBException("userId cannot be null!!");

                                          }else{


                                                   xmlWriter.writeCharacters(localUserId);

                                          }

                                   xmlWriter.writeEndElement();

                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"secretToShare", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"secretToShare");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("secretToShare");
                                    }


                                          if (localSecretToShare==null){
                                              // write the nil attribute

                                                     throw new org.apache.axis2.databinding.ADBException("secretToShare cannot be null!!");

                                          }else{


                                                   xmlWriter.writeCharacters(localSecretToShare);

                                          }

                                   xmlWriter.writeEndElement();

                                            if (localMediaFileRequestChoice_type0==null){
                                                 throw new org.apache.axis2.databinding.ADBException("mediaFileRequestChoice_type0 cannot be null!!");
                                            }
                                           localMediaFileRequestChoice_type0.serialize(null,factory,xmlWriter);

                    xmlWriter.writeEndElement();


        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }



        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{



                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();


                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "testId"));

                                        if (localTestId != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTestId));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("testId cannot be null!!");
                                        }

                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "userId"));

                                        if (localUserId != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUserId));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("userId cannot be null!!");
                                        }

                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "secretToShare"));

                                        if (localSecretToShare != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSecretToShare));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("secretToShare cannot be null!!");
                                        }

                            elementList.add(new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/",
                                                                      "mediaFileRequestChoice_type0"));


                                    if (localMediaFileRequestChoice_type0==null){
                                         throw new org.apache.axis2.databinding.ADBException("mediaFileRequestChoice_type0 cannot be null!!");
                                    }
                                    elementList.add(localMediaFileRequestChoice_type0);


                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());



        }



     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{




        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static MediaFileRequest parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            MediaFileRequest object =
                new MediaFileRequest();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();


                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);

                            if (!"mediaFileRequest".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (MediaFileRequest)ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }


                  }


                }




                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.



                    reader.next();


                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","testId").equals(reader.getName())){

                                    java.lang.String content = reader.getElementText();

                                              object.setTestId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

                                        reader.next();

                              }  // End of if for expected property start element

                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }


                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","userId").equals(reader.getName())){

                                    java.lang.String content = reader.getElementText();

                                              object.setUserId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

                                        reader.next();

                              }  // End of if for expected property start element

                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }


                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","secretToShare").equals(reader.getName())){

                                    java.lang.String content = reader.getElementText();

                                              object.setSecretToShare(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

                                        reader.next();

                              }  // End of if for expected property start element

                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }


                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                                    if (reader.isStartElement() ){

                                                object.setMediaFileRequestChoice_type0(MediaFileRequestChoice_type0.Factory.parse(reader));

                              }  // End of if for expected property start element

                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();

                                if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());




            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class



        }


        public static class ResultFileDownload
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = resultFileDownload
                Namespace URI = http://localhost:8080/axis2_tester/services/
                Namespace Prefix = ns1
                */


        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://localhost:8080/axis2_tester/services/")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }



                        /**
                        * field for RelativePath
                        * This was an Array!
                        */


                                    protected java.lang.String[] localRelativePath ;


                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getRelativePath(){
                               return localRelativePath;
                           }






                              /**
                               * validate the array for RelativePath
                               */
                              protected void validateRelativePath(java.lang.String[] param){

                              if ((param != null) && (param.length < 1)){
                                throw new java.lang.RuntimeException();
                              }

                              }


                             /**
                              * Auto generated setter method
                              * @param param RelativePath
                              */
                              public void setRelativePath(java.lang.String[] param){

                                   validateRelativePath(param);


                                      this.localRelativePath=param;
                              }



                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addRelativePath(java.lang.String param){
                                   if (localRelativePath == null){
                                   localRelativePath = new java.lang.String[]{};
                                   }



                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localRelativePath);
                               list.add(param);
                               this.localRelativePath =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }


                        /**
                        * field for StudentId
                        */


                                    protected java.lang.String localStudentId ;


                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getStudentId(){
                               return localStudentId;
                           }



                            /**
                               * Auto generated setter method
                               * @param param StudentId
                               */
                               public void setStudentId(java.lang.String param){

                                            this.localStudentId=param;


                               }


     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;

        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }


        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{



               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

                 public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                       ResultFileDownload.this.serialize(parentQName,factory,xmlWriter);
                 }
               };
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               parentQName,factory,dataSource);

       }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,factory,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{




                java.lang.String prefix = null;
                java.lang.String namespace = null;


                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();

                    if ((namespace != null) && (namespace.trim().length() > 0)) {
                        java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                        if (writerPrefix != null) {
                            xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                        } else {
                            if (prefix == null) {
                                prefix = generatePrefix(namespace);
                            }

                            xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                            xmlWriter.writeNamespace(prefix, namespace);
                            xmlWriter.setPrefix(prefix, namespace);
                        }
                    } else {
                        xmlWriter.writeStartElement(parentQName.getLocalPart());
                    }

                  if (serializeType){


                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://localhost:8080/axis2_tester/services/");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":resultFileDownload",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "resultFileDownload",
                           xmlWriter);
                   }


                   }

                             if (localRelativePath!=null) {
                                   namespace = "";
                                   boolean emptyNamespace = namespace == null || namespace.length() == 0;
                                   prefix =  emptyNamespace ? null : xmlWriter.getPrefix(namespace);
                                   for (int i = 0;i < localRelativePath.length;i++){

                                            if (localRelativePath[i] != null){

                                                if (!emptyNamespace) {
                                                    if (prefix == null) {
                                                        java.lang.String prefix2 = generatePrefix(namespace);

                                                        xmlWriter.writeStartElement(prefix2,"relativePath", namespace);
                                                        xmlWriter.writeNamespace(prefix2, namespace);
                                                        xmlWriter.setPrefix(prefix2, namespace);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace,"relativePath");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("relativePath");
                                                }


                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRelativePath[i]));

                                                xmlWriter.writeEndElement();

                                                } else {

                                                           throw new org.apache.axis2.databinding.ADBException("relativePath cannot be null!!");

                                                }

                                   }
                             } else {

                                         throw new org.apache.axis2.databinding.ADBException("relativePath cannot be null!!");

                             }


                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"studentId", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"studentId");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("studentId");
                                    }


                                          if (localStudentId==null){
                                              // write the nil attribute

                                                     throw new org.apache.axis2.databinding.ADBException("studentId cannot be null!!");

                                          }else{


                                                   xmlWriter.writeCharacters(localStudentId);

                                          }

                                   xmlWriter.writeEndElement();

                    xmlWriter.writeEndElement();


        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }


         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }



        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{



                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();


                            if (localRelativePath!=null){
                                  for (int i = 0;i < localRelativePath.length;i++){

                                         if (localRelativePath[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("",
                                                                              "relativePath"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRelativePath[i]));
                                          } else {

                                                    throw new org.apache.axis2.databinding.ADBException("relativePath cannot be null!!");

                                          }


                                  }
                            } else {

                                    throw new org.apache.axis2.databinding.ADBException("relativePath cannot be null!!");

                            }


                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "studentId"));

                                        if (localStudentId != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStudentId));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("studentId cannot be null!!");
                                        }


                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());



        }



     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{




        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static ResultFileDownload parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ResultFileDownload object =
                new ResultFileDownload();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();


                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);

                            if (!"resultFileDownload".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (ResultFileDownload)ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }


                  }


                }




                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.


                    reader.next();

                        java.util.ArrayList list1 = new java.util.ArrayList();


                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","relativePath").equals(reader.getName())){



                                    // Process the array and step past its final element's end.
                                    list1.add(reader.getElementText());

                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone1 = false;
                                            while(!loopDone1){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone1 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("","relativePath").equals(reader.getName())){
                                                         list1.add(reader.getElementText());

                                                    }else{
                                                        loopDone1 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array

                                                    object.setRelativePath((java.lang.String[])
                                                        list1.toArray(new java.lang.String[list1.size()]));

                              }  // End of if for expected property start element

                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }


                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","studentId").equals(reader.getName())){

                                    java.lang.String content = reader.getElementText();

                                              object.setStudentId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

                                        reader.next();

                              }  // End of if for expected property start element

                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }

                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();

                                if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());




            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class



        }


        public static class ResultFileUpload
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = resultFileUpload
                Namespace URI = http://localhost:8080/axis2_tester/services/
                Namespace Prefix = ns1
                */


        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://localhost:8080/axis2_tester/services/")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }



                        /**
                        * field for StudentId
                        */


                                    protected java.lang.String localStudentId ;


                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getStudentId(){
                               return localStudentId;
                           }



                            /**
                               * Auto generated setter method
                               * @param param StudentId
                               */
                               public void setStudentId(java.lang.String param){

                                            this.localStudentId=param;


                               }


                        /**
                        * field for ResultFileUploadSequence_type0
                        * This was an Array!
                        */


                                    protected ResultFileUploadSequence_type0[] localResultFileUploadSequence_type0 ;


                           /**
                           * Auto generated getter method
                           * @return ResultFileUploadSequence_type0[]
                           */
                           public  ResultFileUploadSequence_type0[] getResultFileUploadSequence_type0(){
                               return localResultFileUploadSequence_type0;
                           }






                              /**
                               * validate the array for ResultFileUploadSequence_type0
                               */
                              protected void validateResultFileUploadSequence_type0(ResultFileUploadSequence_type0[] param){

                              if ((param != null) && (param.length < 1)){
                                throw new java.lang.RuntimeException();
                              }

                              }


                             /**
                              * Auto generated setter method
                              * @param param ResultFileUploadSequence_type0
                              */
                              public void setResultFileUploadSequence_type0(ResultFileUploadSequence_type0[] param){

                                   validateResultFileUploadSequence_type0(param);


                                      this.localResultFileUploadSequence_type0=param;
                              }



                             /**
                             * Auto generated add method for the array for convenience
                             * @param param ResultFileUploadSequence_type0
                             */
                             public void addResultFileUploadSequence_type0(ResultFileUploadSequence_type0 param){
                                   if (localResultFileUploadSequence_type0 == null){
                                   localResultFileUploadSequence_type0 = new ResultFileUploadSequence_type0[]{};
                                   }



                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localResultFileUploadSequence_type0);
                               list.add(param);
                               this.localResultFileUploadSequence_type0 =
                             (ResultFileUploadSequence_type0[])list.toArray(
                            new ResultFileUploadSequence_type0[list.size()]);

                             }


     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;

        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }


        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{



               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

                 public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                       ResultFileUpload.this.serialize(parentQName,factory,xmlWriter);
                 }
               };
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               parentQName,factory,dataSource);

       }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,factory,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{




                java.lang.String prefix = null;
                java.lang.String namespace = null;


                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();

                    if ((namespace != null) && (namespace.trim().length() > 0)) {
                        java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                        if (writerPrefix != null) {
                            xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                        } else {
                            if (prefix == null) {
                                prefix = generatePrefix(namespace);
                            }

                            xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                            xmlWriter.writeNamespace(prefix, namespace);
                            xmlWriter.setPrefix(prefix, namespace);
                        }
                    } else {
                        xmlWriter.writeStartElement(parentQName.getLocalPart());
                    }

                  if (serializeType){


                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://localhost:8080/axis2_tester/services/");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":resultFileUpload",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "resultFileUpload",
                           xmlWriter);
                   }


                   }

                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"studentId", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"studentId");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("studentId");
                                    }


                                          if (localStudentId==null){
                                              // write the nil attribute

                                                     throw new org.apache.axis2.databinding.ADBException("studentId cannot be null!!");

                                          }else{


                                                   xmlWriter.writeCharacters(localStudentId);

                                          }

                                   xmlWriter.writeEndElement();


                                      if (localResultFileUploadSequence_type0!=null){
                                            for (int i = 0;i < localResultFileUploadSequence_type0.length;i++){
                                                if (localResultFileUploadSequence_type0[i] != null){
                                                 localResultFileUploadSequence_type0[i].serialize(null,factory,xmlWriter);
                                                } else {

                                                           throw new org.apache.axis2.databinding.ADBException("resultFileUploadSequence_type0 cannot be null!!");

                                                }

                                            }
                                     } else {
                                        throw new org.apache.axis2.databinding.ADBException("resultFileUploadSequence_type0 cannot be null!!");
                                     }

                    xmlWriter.writeEndElement();


        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

       /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }



        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{



                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();


                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "studentId"));

                                        if (localStudentId != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localStudentId));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("studentId cannot be null!!");
                                        }

                             if (localResultFileUploadSequence_type0!=null) {
                                 for (int i = 0;i < localResultFileUploadSequence_type0.length;i++){

                                    if (localResultFileUploadSequence_type0[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://localhost:8080/axis2_tester/services/",
                                                                          "resultFileUploadSequence_type0"));
                                         elementList.add(localResultFileUploadSequence_type0[i]);
                                    } else {

                                               throw new org.apache.axis2.databinding.ADBException("resultFileUploadSequence_type0 cannot be null !!");

                                    }

                                 }
                             } else {

                                        throw new org.apache.axis2.databinding.ADBException("resultFileUploadSequence_type0 cannot be null!!");

                             }



                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());



        }



     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{




        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static ResultFileUpload parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ResultFileUpload object =
                new ResultFileUpload();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();


                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);

                            if (!"resultFileUpload".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (ResultFileUpload)ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }


                  }


                }




                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.




                    reader.next();

                        java.util.ArrayList list2 = new java.util.ArrayList();


                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","studentId").equals(reader.getName())){

                                    java.lang.String content = reader.getElementText();

                                              object.setStudentId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

                                        reader.next();

                              }  // End of if for expected property start element

                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }


                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                                    if (reader.isStartElement() ){



                                    // Process the array and step past its final element's end.
                                    list2.add(ResultFileUploadSequence_type0.Factory.parse(reader));
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone2 = false;
                                                        while(!loopDone2){

                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone2 = true;
                                                            } else {
                                                                list2.add(ResultFileUploadSequence_type0.Factory.parse(reader));
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        object.setResultFileUploadSequence_type0((ResultFileUploadSequence_type0[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                ResultFileUploadSequence_type0.class,
                                                                list2));


                              }  // End of if for expected property start element

                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();

                                if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());




            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class



        }


        public static class ContentPackageUpload
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = contentPackageUpload
                Namespace URI = http://localhost:8080/axis2_tester/services/
                Namespace Prefix = ns1
                */


        private static final long serialVersionUID = 1L;

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://localhost:8080/axis2_tester/services/")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }



                        /**
                        * field for RelativePath
                        */


                                    protected java.lang.String localRelativePath ;


                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getRelativePath(){
                               return localRelativePath;
                           }



                            /**
                               * Auto generated setter method
                               * @param param RelativePath
                               */
                               public void setRelativePath(java.lang.String param){

                                            this.localRelativePath=param;


                               }


                        /**
                        * field for MediaFile
                        */


                                    protected transient javax.activation.DataHandler localMediaFile ;


                           /**
                           * Auto generated getter method
                           * @return javax.activation.DataHandler
                           */
                           public  javax.activation.DataHandler getMediaFile(){
                               return localMediaFile;
                           }



                            /**
                               * Auto generated setter method
                               * @param param MediaFile
                               */
                               public void setMediaFile(javax.activation.DataHandler param){

                                            this.localMediaFile=param;


                               }


     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;

        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }


        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{



               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

                 public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                       ContentPackageUpload.this.serialize(parentQName,factory,xmlWriter);
                 }
               };
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               parentQName,factory,dataSource);

       }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,factory,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{




                java.lang.String prefix = null;
                java.lang.String namespace = null;


                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();

                    if ((namespace != null) && (namespace.trim().length() > 0)) {
                        java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                        if (writerPrefix != null) {
                            xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                        } else {
                            if (prefix == null) {
                                prefix = generatePrefix(namespace);
                            }

                            xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                            xmlWriter.writeNamespace(prefix, namespace);
                            xmlWriter.setPrefix(prefix, namespace);
                        }
                    } else {
                        xmlWriter.writeStartElement(parentQName.getLocalPart());
                    }

                  if (serializeType){


                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://localhost:8080/axis2_tester/services/");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":contentPackageUpload",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "contentPackageUpload",
                           xmlWriter);
                   }


                   }

                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"relativePath", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"relativePath");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("relativePath");
                                    }


                                          if (localRelativePath==null){
                                              // write the nil attribute

                                                     throw new org.apache.axis2.databinding.ADBException("relativePath cannot be null!!");

                                          }else{


                                                   xmlWriter.writeCharacters(localRelativePath);

                                          }

                                   xmlWriter.writeEndElement();

                                    namespace = "";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"mediaFile", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"mediaFile");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("mediaFile");
                                    }


                                    if (localMediaFile!=null)
                                    {
                                       xmlWriter.writeDataHandler(localMediaFile);
                                    }

                                   xmlWriter.writeEndElement();

                    xmlWriter.writeEndElement();


        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = generatePrefix(namespace);

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }



        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{



                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();


                                      elementList.add(new javax.xml.namespace.QName("",
                                                                      "relativePath"));

                                        if (localRelativePath != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRelativePath));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("relativePath cannot be null!!");
                                        }

                                      elementList.add(new javax.xml.namespace.QName("",
                                        "mediaFile"));

                            elementList.add(localMediaFile);


                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());



        }



     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{




        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static ContentPackageUpload parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            ContentPackageUpload object =
                new ContentPackageUpload();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();


                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);

                            if (!"contentPackageUpload".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (ContentPackageUpload)ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }


                  }


                }




                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.



                    reader.next();


                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","relativePath").equals(reader.getName())){

                                    java.lang.String content = reader.getElementText();

                                              object.setRelativePath(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

                                        reader.next();

                              }  // End of if for expected property start element

                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }


                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                                    if (reader.isStartElement() && new javax.xml.namespace.QName("","mediaFile").equals(reader.getName())){
                                reader.next();
                                    if (isReaderMTOMAware(reader)
                                            &&
                                            java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_BINARY)))
                                    {
                                        //MTOM aware reader - get the datahandler directly and put it in the object
                                        object.setMediaFile(
                                                (javax.activation.DataHandler) reader.getProperty(org.apache.axiom.om.OMConstants.DATA_HANDLER));
                                    } else {
                                        if (reader.getEventType() == javax.xml.stream.XMLStreamConstants.START_ELEMENT && reader.getName().equals(new javax.xml.namespace.QName(org.apache.axiom.om.impl.MTOMConstants.XOP_NAMESPACE_URI, org.apache.axiom.om.impl.MTOMConstants.XOP_INCLUDE)))
                                        {
                                            java.lang.String id = org.apache.axiom.om.util.ElementHelper.getContentID(reader, "UTF-8");
                                            object.setMediaFile(((org.apache.axiom.soap.impl.builder.MTOMStAXSOAPModelBuilder) ((org.apache.axiom.om.impl.llom.OMStAXWrapper) reader).getBuilder()).getDataHandler(id));
                                            reader.next();

                                                reader.next();

                                        } else if(reader.hasText()) {
                                            //Do the usual conversion
                                            java.lang.String content = reader.getText();
                                            object.setMediaFile(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToBase64Binary(content));

                                                reader.next();

                                        }
                                    }


                                        reader.next();

                              }  // End of if for expected property start element

                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                                }

                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();

                                if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());




            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class



        }


            private  org.apache.axiom.om.OMElement  toOM(de.bps.webservices.clients.onyxreporter.stubs.MediaProviderStub.MediaFileRequestE param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {


                        try{
                             return param.getOMElement(de.bps.webservices.clients.onyxreporter.stubs.MediaProviderStub.MediaFileRequestE.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }


            }

            private  org.apache.axiom.om.OMElement  toOM(de.bps.webservices.clients.onyxreporter.stubs.MediaProviderStub.MediaFileResponseE param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {


                        try{
                             return param.getOMElement(de.bps.webservices.clients.onyxreporter.stubs.MediaProviderStub.MediaFileResponseE.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }


            }


                                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, de.bps.webservices.clients.onyxreporter.stubs.MediaProviderStub.MediaFileRequestE param, boolean optimizeContent)
                                        throws org.apache.axis2.AxisFault{


                                                    try{

                                                            org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                            emptyEnvelope.getBody().addChild(param.getOMElement(de.bps.webservices.clients.onyxreporter.stubs.MediaProviderStub.MediaFileRequestE.MY_QNAME,factory));
                                                            return emptyEnvelope;
                                                        } catch(org.apache.axis2.databinding.ADBException e){
                                                            throw org.apache.axis2.AxisFault.makeFault(e);
                                                        }


                                        }


                             /* methods to provide back word compatibility */




        private  java.lang.Object fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{

        try {

                if (de.bps.webservices.clients.onyxreporter.stubs.MediaProviderStub.MediaFileRequestE.class.equals(type)){

                           return de.bps.webservices.clients.onyxreporter.stubs.MediaProviderStub.MediaFileRequestE.Factory.parse(param.getXMLStreamReaderWithoutCaching());


                }

                if (de.bps.webservices.clients.onyxreporter.stubs.MediaProviderStub.MediaFileResponseE.class.equals(type)){

                           return de.bps.webservices.clients.onyxreporter.stubs.MediaProviderStub.MediaFileResponseE.Factory.parse(param.getXMLStreamReaderWithoutCaching());


                }

        } catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
           return null;
        }




   }
