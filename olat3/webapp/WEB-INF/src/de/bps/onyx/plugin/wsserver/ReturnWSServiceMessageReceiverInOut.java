
/**
 * ReturnWSServiceMessageReceiverInOut.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5  Built on : Apr 30, 2009 (06:07:24 EDT)
 */
        package de.bps.onyx.plugin.wsserver;

        /**
        *  ReturnWSServiceMessageReceiverInOut message receiver
        */

        public class ReturnWSServiceMessageReceiverInOut extends org.apache.axis2.receivers.AbstractInOutMessageReceiver{


        public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext, org.apache.axis2.context.MessageContext newMsgContext)
        throws org.apache.axis2.AxisFault{

        try {

        // get the implementation class for the Web Service
        Object obj = getTheImplementationObject(msgContext);

        ReturnWSServiceSkeleton skel = (ReturnWSServiceSkeleton)obj;
        //Out Envelop
        org.apache.axiom.soap.SOAPEnvelope envelope = null;
        //Find the axisOperation that has been set by the Dispatch phase.
        org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext().getAxisOperation();
        if (op == null) {
        throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
        }

        java.lang.String methodName;
        if((op.getName() != null) && ((methodName = org.apache.axis2.util.JavaUtils.xmlNameToJavaIdentifier(op.getName().getLocalPart())) != null)){



            if("saveResultLocal".equals(methodName)){

                de.bps.onyx.plugin.wsserver.SaveResultLocalResponse saveResultLocalResponse1 = null;
	                        de.bps.onyx.plugin.wsserver.SaveResultLocal wrappedParam =
                                                             (de.bps.onyx.plugin.wsserver.SaveResultLocal)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    de.bps.onyx.plugin.wsserver.SaveResultLocal.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));

                                               saveResultLocalResponse1 =


                                                         skel.saveResultLocal(wrappedParam)
                                                    ;

                                        envelope = toEnvelope(getSOAPFactory(msgContext), saveResultLocalResponse1, false);
                                    } else

            if("saveResult".equals(methodName)){

                de.bps.onyx.plugin.wsserver.SaveResultResponse saveResultResponse3 = null;
	                        de.bps.onyx.plugin.wsserver.SaveResult wrappedParam =
                                                             (de.bps.onyx.plugin.wsserver.SaveResult)fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    de.bps.onyx.plugin.wsserver.SaveResult.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));

                                               saveResultResponse3 =


                                                         skel.saveResult(wrappedParam)
                                                    ;

                                        envelope = toEnvelope(getSOAPFactory(msgContext), saveResultResponse3, false);

            } else {
              throw new java.lang.RuntimeException("method not found");
            }


        newMsgContext.setEnvelope(envelope);
        }
        }
        catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
        }

        //
            private  org.apache.axiom.om.OMElement  toOM(de.bps.onyx.plugin.wsserver.SaveResultLocal param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {


                        try{
                             return param.getOMElement(de.bps.onyx.plugin.wsserver.SaveResultLocal.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }


            }

            private  org.apache.axiom.om.OMElement  toOM(de.bps.onyx.plugin.wsserver.SaveResultLocalResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {


                        try{
                             return param.getOMElement(de.bps.onyx.plugin.wsserver.SaveResultLocalResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }


            }

            private  org.apache.axiom.om.OMElement  toOM(de.bps.onyx.plugin.wsserver.SaveResult param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {


                        try{
                             return param.getOMElement(de.bps.onyx.plugin.wsserver.SaveResult.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }


            }

            private  org.apache.axiom.om.OMElement  toOM(de.bps.onyx.plugin.wsserver.SaveResultResponse param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {


                        try{
                             return param.getOMElement(de.bps.onyx.plugin.wsserver.SaveResultResponse.MY_QNAME,
                                          org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                        } catch(org.apache.axis2.databinding.ADBException e){
                            throw org.apache.axis2.AxisFault.makeFault(e);
                        }


            }

                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, de.bps.onyx.plugin.wsserver.SaveResultLocalResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

                                    emptyEnvelope.getBody().addChild(param.getOMElement(de.bps.onyx.plugin.wsserver.SaveResultLocalResponse.MY_QNAME,factory));


                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }

                         private de.bps.onyx.plugin.wsserver.SaveResultLocalResponse wrapsaveResultLocal(){
                                de.bps.onyx.plugin.wsserver.SaveResultLocalResponse wrappedElement = new de.bps.onyx.plugin.wsserver.SaveResultLocalResponse();
                                return wrappedElement;
                         }

                    private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, de.bps.onyx.plugin.wsserver.SaveResultResponse param, boolean optimizeContent)
                        throws org.apache.axis2.AxisFault{
                      try{
                          org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

                                    emptyEnvelope.getBody().addChild(param.getOMElement(de.bps.onyx.plugin.wsserver.SaveResultResponse.MY_QNAME,factory));


                         return emptyEnvelope;
                    } catch(org.apache.axis2.databinding.ADBException e){
                        throw org.apache.axis2.AxisFault.makeFault(e);
                    }
                    }

                         private de.bps.onyx.plugin.wsserver.SaveResultResponse wrapsaveResult(){
                                de.bps.onyx.plugin.wsserver.SaveResultResponse wrappedElement = new de.bps.onyx.plugin.wsserver.SaveResultResponse();
                                return wrappedElement;
                         }



        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
        return factory.getDefaultEnvelope();
        }


        private  java.lang.Object fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{

        try {

                if (de.bps.onyx.plugin.wsserver.SaveResultLocal.class.equals(type)){

                           return de.bps.onyx.plugin.wsserver.SaveResultLocal.Factory.parse(param.getXMLStreamReaderWithoutCaching());


                }

                if (de.bps.onyx.plugin.wsserver.SaveResultLocalResponse.class.equals(type)){

                           return de.bps.onyx.plugin.wsserver.SaveResultLocalResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());


                }

                if (de.bps.onyx.plugin.wsserver.SaveResult.class.equals(type)){

                           return de.bps.onyx.plugin.wsserver.SaveResult.Factory.parse(param.getXMLStreamReaderWithoutCaching());


                }

                if (de.bps.onyx.plugin.wsserver.SaveResultResponse.class.equals(type)){

                           return de.bps.onyx.plugin.wsserver.SaveResultResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());


                }

        } catch (java.lang.Exception e) {
        throw org.apache.axis2.AxisFault.makeFault(e);
        }
           return null;
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

        private org.apache.axis2.AxisFault createAxisFault(java.lang.Exception e) {
        org.apache.axis2.AxisFault f;
        Throwable cause = e.getCause();
        if (cause != null) {
            f = new org.apache.axis2.AxisFault(e.getMessage(), cause);
        } else {
            f = new org.apache.axis2.AxisFault(e.getMessage());
        }

        return f;
    }

        }//end of class
