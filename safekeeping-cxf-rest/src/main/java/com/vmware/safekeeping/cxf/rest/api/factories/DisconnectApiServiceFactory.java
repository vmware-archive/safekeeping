package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.DisconnectApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.DisconnectApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class DisconnectApiServiceFactory {
    private final static DisconnectApiService service = new DisconnectApiServiceImpl();

    public static DisconnectApiService getDisconnectApi() {
        return service;
    }
}
