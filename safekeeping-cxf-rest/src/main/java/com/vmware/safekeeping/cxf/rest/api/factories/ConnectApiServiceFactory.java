package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.ConnectApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.ConnectApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T22:00:47.492Z[GMT]")public class ConnectApiServiceFactory {
    private final static ConnectApiService service = new ConnectApiServiceImpl();

    public static ConnectApiService getConnectApi() {
        return service;
    }
}
