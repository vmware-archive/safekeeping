package com.vmware.safekeeping.cxf.rest.api;

import com.vmware.safekeeping.cxf.rest.api.*;
import com.vmware.safekeeping.cxf.rest.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import com.vmware.safekeeping.cxf.rest.model.ArchiveListOptions;
import com.vmware.safekeeping.cxf.rest.model.SapiTask;

import java.util.Map;
import java.util.List;
import com.vmware.safekeeping.cxf.rest.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T22:00:47.492Z[GMT]")public abstract class ListArchiveApiService {
    public abstract Response listArchive(ArchiveListOptions body,SecurityContext securityContext) throws NotFoundException;
}
