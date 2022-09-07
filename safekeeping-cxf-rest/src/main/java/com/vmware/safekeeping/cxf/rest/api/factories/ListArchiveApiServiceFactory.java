package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.ListArchiveApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.ListArchiveApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T22:00:47.492Z[GMT]")public class ListArchiveApiServiceFactory {
    private final static ListArchiveApiService service = new ListArchiveApiServiceImpl();

    public static ListArchiveApiService getListArchiveApi() {
        return service;
    }
}
