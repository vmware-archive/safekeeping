package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.ConnectRepositoryApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.ConnectRepositoryApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T22:00:47.492Z[GMT]")public class ConnectRepositoryApiServiceFactory {
    private final static ConnectRepositoryApiService service = new ConnectRepositoryApiServiceImpl();

    public static ConnectRepositoryApiService getConnectRepositoryApi() {
        return service;
    }
}
