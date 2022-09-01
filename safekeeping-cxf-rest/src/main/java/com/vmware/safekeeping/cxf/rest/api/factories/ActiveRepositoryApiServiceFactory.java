package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.ActiveRepositoryApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.ActiveRepositoryApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class ActiveRepositoryApiServiceFactory {
    private final static ActiveRepositoryApiService service = new ActiveRepositoryApiServiceImpl();

    public static ActiveRepositoryApiService getActiveRepositoryApi() {
        return service;
    }
}
