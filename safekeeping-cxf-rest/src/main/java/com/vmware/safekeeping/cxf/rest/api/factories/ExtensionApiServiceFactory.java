package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.ExtensionApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.ExtensionApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class ExtensionApiServiceFactory {
    private final static ExtensionApiService service = new ExtensionApiServiceImpl();

    public static ExtensionApiService getExtensionApi() {
        return service;
    }
}
