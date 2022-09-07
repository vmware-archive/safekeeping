package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.RepositoryApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.RepositoryApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T22:00:47.492Z[GMT]")public class RepositoryApiServiceFactory {
    private final static RepositoryApiService service = new RepositoryApiServiceImpl();

    public static RepositoryApiService getRepositoryApi() {
        return service;
    }
}
