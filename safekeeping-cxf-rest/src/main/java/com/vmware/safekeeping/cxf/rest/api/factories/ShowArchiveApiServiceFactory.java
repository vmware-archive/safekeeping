package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.ShowArchiveApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.ShowArchiveApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class ShowArchiveApiServiceFactory {
    private final static ShowArchiveApiService service = new ShowArchiveApiServiceImpl();

    public static ShowArchiveApiService getShowArchiveApi() {
        return service;
    }
}
