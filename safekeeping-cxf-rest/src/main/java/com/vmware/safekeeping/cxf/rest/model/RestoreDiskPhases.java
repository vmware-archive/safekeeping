/*
 * Safekeeping Server
 * Safekeeping OpenAPI
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package com.vmware.safekeeping.cxf.rest.model;

import java.util.Objects;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonValue;
import javax.validation.constraints.*;
import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Gets or Sets RestoreDiskPhases
 */
public enum RestoreDiskPhases {
  NONE("NONE"),
    START("START"),
    START_OPEN_VMDK("START_OPEN_VMDK"),
    END_OPEN_VMDK("END_OPEN_VMDK"),
    START_CALCULATE_NUMBER_OF_GENERATION_DISK_RESTORE("START_CALCULATE_NUMBER_OF_GENERATION_DISK_RESTORE"),
    END_CALCULATE_NUMBER_OF_GENERATION_DISK_RESTORE("END_CALCULATE_NUMBER_OF_GENERATION_DISK_RESTORE"),
    START_DUMP_THREDS("START_DUMP_THREDS"),
    END_DUMP_THREDS("END_DUMP_THREDS"),
    START_DUMPS_TOTAL_CALCULATION("START_DUMPS_TOTAL_CALCULATION"),
    END_DUMPS_TOTAL_CALCULATION("END_DUMPS_TOTAL_CALCULATION"),
    START_CLOSE_VMDK("START_CLOSE_VMDK"),
    END_CLOSE_VMDK("END_CLOSE_VMDK"),
    END("END");

  private String value;

  RestoreDiskPhases(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static RestoreDiskPhases fromValue(String text) {
    for (RestoreDiskPhases b : RestoreDiskPhases.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
