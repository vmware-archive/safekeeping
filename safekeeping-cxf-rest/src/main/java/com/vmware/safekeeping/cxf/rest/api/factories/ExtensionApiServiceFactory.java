package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.ExtensionApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.ExtensionApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T22:00:47.492Z[GMT]")public class ExtensionApiServiceFactory {
    private final static ExtensionApiService service = new ExtensionApiServiceImpl();

    public static ExtensionApiService getExtensionApi() {
        return service;
    }
}
