package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.ShowArchiveApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.ShowArchiveApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T22:00:47.492Z[GMT]")public class ShowArchiveApiServiceFactory {
    private final static ShowArchiveApiService service = new ShowArchiveApiServiceImpl();

    public static ShowArchiveApiService getShowArchiveApi() {
        return service;
    }
}
