package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.BackupApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.BackupApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T22:00:47.492Z[GMT]")public class BackupApiServiceFactory {
    private final static BackupApiService service = new BackupApiServiceImpl();

    public static BackupApiService getBackupApi() {
        return service;
    }
}
