package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.ListArchiveApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.ListArchiveApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class ListArchiveApiServiceFactory {
    private final static ListArchiveApiService service = new ListArchiveApiServiceImpl();

    public static ListArchiveApiService getListArchiveApi() {
        return service;
    }
}
