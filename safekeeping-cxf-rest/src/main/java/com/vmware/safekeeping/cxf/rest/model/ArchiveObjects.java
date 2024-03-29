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
 * Gets or Sets ArchiveObjects
 */
public enum ArchiveObjects {
  GLOBALPROFILE("GLOBALPROFILE"),
    FCOPROFILE("FCOPROFILE"),
    GENERATIONPROFILE("GENERATIONPROFILE"),
    VMXFILE("VMXFILE"),
    REPORTFILE("REPORTFILE"),
    MD5FILE("MD5FILE"),
    VAPPCONFIG("VAPPCONFIG"),
    NONE("NONE");

  private String value;

  ArchiveObjects(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ArchiveObjects fromValue(String text) {
    for (ArchiveObjects b : ArchiveObjects.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
