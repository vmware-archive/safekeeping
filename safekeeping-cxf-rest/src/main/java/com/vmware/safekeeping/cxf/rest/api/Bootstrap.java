package com.vmware.safekeeping.cxf.rest.api;

import com.vmware.safekeeping.cxf.rest.Cxf;

//
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.servers.ServerVariable;
import io.swagger.v3.oas.annotations.tags.Tag;

@OpenAPIDefinition(info = @Info(title = "Safekeeping Server", version = Cxf.API_VERSION, description = "Safekeeping OpenAPI", termsOfService = "", 
		license = @License(name = "BSD-2", url = "https://github.com/vmware/safekeeping/blob/master/LICENSE")),

		servers = { @Server(description = "Safekeeping endpoint", url = "/sapi/" + Cxf.API_VERSION),
				@Server(description = "External Safekeeping endpoint", url = "{protocol}://{server}/sapi/"
						+ Cxf.API_VERSION, variables = {
								@ServerVariable(name = "protocol", allowableValues = { "http",
										"https" }, defaultValue = "https"),
								@ServerVariable(name = "server", defaultValue = "server") }) }, tags = {
										@Tag(name = "admin", description = "Operations available to admin"),
										@Tag(name = "backup", description = "Backup operations"),
										@Tag(name = "archive", description = "Archive operations"),
										@Tag(name = "connectivity", description = "Connectivity operations") },
		security = { @SecurityRequirement(name = "api_key") })

@SecuritySchemes(value = {
		@SecurityScheme(name = "api_key", description = "In order to make VMware Safekeeping REST API calls, you need to authenticate with the connect call.<p>",

				type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER,paramName = "api_key") })

public class Bootstrap {

}