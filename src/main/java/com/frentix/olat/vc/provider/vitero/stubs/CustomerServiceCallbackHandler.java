
/**
 * CustomerServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */

    package com.frentix.olat.vc.provider.vitero.stubs;

    /**
     *  CustomerServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class CustomerServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public CustomerServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public CustomerServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for getCustomerByName method
            * override this method for handling normal response from getCustomerByName operation
            */
           public void receiveResultgetCustomerByName(
                    com.frentix.olat.vc.provider.vitero.stubs.CustomerServiceStub.GetCustomerByNameResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getCustomerByName operation
           */
            public void receiveErrorgetCustomerByName(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getCustomerList method
            * override this method for handling normal response from getCustomerList operation
            */
           public void receiveResultgetCustomerList(
                    com.frentix.olat.vc.provider.vitero.stubs.CustomerServiceStub.GetCustomerListResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getCustomerList operation
           */
            public void receiveErrorgetCustomerList(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for createCustomer method
            * override this method for handling normal response from createCustomer operation
            */
           public void receiveResultcreateCustomer(
                    com.frentix.olat.vc.provider.vitero.stubs.CustomerServiceStub.CreateCustomerResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from createCustomer operation
           */
            public void receiveErrorcreateCustomer(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getCustomer method
            * override this method for handling normal response from getCustomer operation
            */
           public void receiveResultgetCustomer(
                    com.frentix.olat.vc.provider.vitero.stubs.CustomerServiceStub.GetCustomerResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getCustomer operation
           */
            public void receiveErrorgetCustomer(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                


    }
    