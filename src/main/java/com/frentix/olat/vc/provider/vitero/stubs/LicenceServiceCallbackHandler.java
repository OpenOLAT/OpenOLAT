
/**
 * LicenceServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */

    package com.frentix.olat.vc.provider.vitero.stubs;

    /**
     *  LicenceServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class LicenceServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public LicenceServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public LicenceServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getBookableModulesForGroup method
            * override this method for handling normal response from getBookableModulesForGroup operation
            */
           public void receiveResultgetBookableModulesForGroup(
                    com.frentix.olat.vc.provider.vitero.stubs.LicenceServiceStub.GetBookableModulesForGroupResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getBookableModulesForGroup operation
           */
            public void receiveErrorgetBookableModulesForGroup(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for groupHasBookableModule method
            * override this method for handling normal response from groupHasBookableModule operation
            */
           public void receiveResultgroupHasBookableModule(
                    com.frentix.olat.vc.provider.vitero.stubs.LicenceServiceStub.GroupHasBookableModuleResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from groupHasBookableModule operation
           */
            public void receiveErrorgroupHasBookableModule(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getAllModules method
            * override this method for handling normal response from getAllModules operation
            */
           public void receiveResultgetAllModules(
                    com.frentix.olat.vc.provider.vitero.stubs.LicenceServiceStub.GetAllModulesResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getAllModules operation
           */
            public void receiveErrorgetAllModules(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getModulesForCustomer method
            * override this method for handling normal response from getModulesForCustomer operation
            */
           public void receiveResultgetModulesForCustomer(
                    com.frentix.olat.vc.provider.vitero.stubs.LicenceServiceStub.GetModulesForCustomerResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getModulesForCustomer operation
           */
            public void receiveErrorgetModulesForCustomer(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for isPhoneAvailable method
            * override this method for handling normal response from isPhoneAvailable operation
            */
           public void receiveResultisPhoneAvailable(
                    com.frentix.olat.vc.provider.vitero.stubs.LicenceServiceStub.IsPhoneAvailableResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from isPhoneAvailable operation
           */
            public void receiveErrorisPhoneAvailable(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getBookableRoomsForGroup method
            * override this method for handling normal response from getBookableRoomsForGroup operation
            */
           public void receiveResultgetBookableRoomsForGroup(
                    com.frentix.olat.vc.provider.vitero.stubs.LicenceServiceStub.GetBookableRoomsForGroupResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getBookableRoomsForGroup operation
           */
            public void receiveErrorgetBookableRoomsForGroup(java.lang.Exception e) {
            }
                


    }
    