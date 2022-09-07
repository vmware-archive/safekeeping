package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.TaskInfoApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.TaskInfoApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T22:00:47.492Z[GMT]")public class TaskInfoApiServiceFactory {
    private final static TaskInfoApiService service = new TaskInfoApiServiceImpl();

    public static TaskInfoApiService getTaskInfoApi() {
        return service;
    }
}
