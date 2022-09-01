package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.RepositoryApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.RepositoryApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class RepositoryApiServiceFactory {
    private final static RepositoryApiService service = new RepositoryApiServiceImpl();

    public static RepositoryApiService getRepositoryApi() {
        return service;
    }
}
