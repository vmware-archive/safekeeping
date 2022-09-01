package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.KeepAliveApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.KeepAliveApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class KeepAliveApiServiceFactory {
    private final static KeepAliveApiService service = new KeepAliveApiServiceImpl();

    public static KeepAliveApiService getKeepAliveApi() {
        return service;
    }
}
