package com.vmware.safekeeping.cxf.rest.api;

import com.vmware.safekeeping.cxf.rest.api.*;
import com.vmware.safekeeping.cxf.rest.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;


import java.util.Map;
import java.util.List;
import com.vmware.safekeeping.cxf.rest.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public abstract class RepositoryApiService {
    public abstract Response repository(SecurityContext securityContext) throws NotFoundException;
}
