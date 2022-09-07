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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * BlockInfo
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class BlockInfo   {
  @JsonProperty("DiskId")
  private Integer diskId = null;

  @JsonProperty("EndTime")
  private Long endTime = null;

  @JsonProperty("Failed")
  private Boolean failed = null;

  @JsonProperty("GenerationId")
  private Integer generationId = null;

  @JsonProperty("Index")
  private Integer index = null;

  @JsonProperty("Key")
  private String key = null;

  @JsonProperty("KeyPath")
  private String keyPath = null;

  @JsonProperty("LastBlock")
  private Long lastBlock = null;

  @JsonProperty("Length")
  private Long length = null;

  @JsonProperty("Md5")
  private String md5 = null;

  @JsonProperty("Offset")
  private Long offset = null;

  @JsonProperty("Reason")
  private String reason = null;

  @JsonProperty("Sha1")
  private String sha1 = null;

  @JsonProperty("Size")
  private Long size = null;

  @JsonProperty("StartTime")
  private Long startTime = null;

  @JsonProperty("StreamSize")
  private Long streamSize = null;

  @JsonProperty("TotalBlocks")
  private Integer totalBlocks = null;

  @JsonProperty("Cipher")
  private Boolean cipher = null;

  @JsonProperty("Compress")
  private Boolean compress = null;

  @JsonProperty("Duplicated")
  private Boolean duplicated = null;

  @JsonProperty("Modified")
  private Boolean modified = null;

  public BlockInfo diskId(Integer diskId) {
    this.diskId = diskId;
    return this;
  }

  /**
   * Get diskId
   * @return diskId
   **/
  @JsonProperty("DiskId")
  @Schema(description = "")
  public Integer getDiskId() {
    return diskId;
  }

  public void setDiskId(Integer diskId) {
    this.diskId = diskId;
  }

  public BlockInfo endTime(Long endTime) {
    this.endTime = endTime;
    return this;
  }

  /**
   * Get endTime
   * @return endTime
   **/
  @JsonProperty("EndTime")
  @Schema(description = "")
  public Long getEndTime() {
    return endTime;
  }

  public void setEndTime(Long endTime) {
    this.endTime = endTime;
  }

  public BlockInfo failed(Boolean failed) {
    this.failed = failed;
    return this;
  }

  /**
   * Get failed
   * @return failed
   **/
  @JsonProperty("Failed")
  @Schema(description = "")
  public Boolean isFailed() {
    return failed;
  }

  public void setFailed(Boolean failed) {
    this.failed = failed;
  }

  public BlockInfo generationId(Integer generationId) {
    this.generationId = generationId;
    return this;
  }

  /**
   * Get generationId
   * @return generationId
   **/
  @JsonProperty("GenerationId")
  @Schema(description = "")
  public Integer getGenerationId() {
    return generationId;
  }

  public void setGenerationId(Integer generationId) {
    this.generationId = generationId;
  }

  public BlockInfo index(Integer index) {
    this.index = index;
    return this;
  }

  /**
   * Get index
   * @return index
   **/
  @JsonProperty("Index")
  @Schema(description = "")
  public Integer getIndex() {
    return index;
  }

  public void setIndex(Integer index) {
    this.index = index;
  }

  public BlockInfo key(String key) {
    this.key = key;
    return this;
  }

  /**
   * Get key
   * @return key
   **/
  @JsonProperty("Key")
  @Schema(description = "")
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public BlockInfo keyPath(String keyPath) {
    this.keyPath = keyPath;
    return this;
  }

  /**
   * Get keyPath
   * @return keyPath
   **/
  @JsonProperty("KeyPath")
  @Schema(description = "")
  public String getKeyPath() {
    return keyPath;
  }

  public void setKeyPath(String keyPath) {
    this.keyPath = keyPath;
  }

  public BlockInfo lastBlock(Long lastBlock) {
    this.lastBlock = lastBlock;
    return this;
  }

  /**
   * Get lastBlock
   * @return lastBlock
   **/
  @JsonProperty("LastBlock")
  @Schema(description = "")
  public Long getLastBlock() {
    return lastBlock;
  }

  public void setLastBlock(Long lastBlock) {
    this.lastBlock = lastBlock;
  }

  public BlockInfo length(Long length) {
    this.length = length;
    return this;
  }

  /**
   * Get length
   * @return length
   **/
  @JsonProperty("Length")
  @Schema(description = "")
  public Long getLength() {
    return length;
  }

  public void setLength(Long length) {
    this.length = length;
  }

  public BlockInfo md5(String md5) {
    this.md5 = md5;
    return this;
  }

  /**
   * Get md5
   * @return md5
   **/
  @JsonProperty("Md5")
  @Schema(description = "")
  public String getMd5() {
    return md5;
  }

  public void setMd5(String md5) {
    this.md5 = md5;
  }

  public BlockInfo offset(Long offset) {
    this.offset = offset;
    return this;
  }

  /**
   * Get offset
   * @return offset
   **/
  @JsonProperty("Offset")
  @Schema(description = "")
  public Long getOffset() {
    return offset;
  }

  public void setOffset(Long offset) {
    this.offset = offset;
  }

  public BlockInfo reason(String reason) {
    this.reason = reason;
    return this;
  }

  /**
   * Get reason
   * @return reason
   **/
  @JsonProperty("Reason")
  @Schema(description = "")
  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public BlockInfo sha1(String sha1) {
    this.sha1 = sha1;
    return this;
  }

  /**
   * Get sha1
   * @return sha1
   **/
  @JsonProperty("Sha1")
  @Schema(description = "")
  public String getSha1() {
    return sha1;
  }

  public void setSha1(String sha1) {
    this.sha1 = sha1;
  }

  public BlockInfo size(Long size) {
    this.size = size;
    return this;
  }

  /**
   * Get size
   * @return size
   **/
  @JsonProperty("Size")
  @Schema(description = "")
  public Long getSize() {
    return size;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public BlockInfo startTime(Long startTime) {
    this.startTime = startTime;
    return this;
  }

  /**
   * Get startTime
   * @return startTime
   **/
  @JsonProperty("StartTime")
  @Schema(description = "")
  public Long getStartTime() {
    return startTime;
  }

  public void setStartTime(Long startTime) {
    this.startTime = startTime;
  }

  public BlockInfo streamSize(Long streamSize) {
    this.streamSize = streamSize;
    return this;
  }

  /**
   * Get streamSize
   * @return streamSize
   **/
  @JsonProperty("StreamSize")
  @Schema(description = "")
  public Long getStreamSize() {
    return streamSize;
  }

  public void setStreamSize(Long streamSize) {
    this.streamSize = streamSize;
  }

  public BlockInfo totalBlocks(Integer totalBlocks) {
    this.totalBlocks = totalBlocks;
    return this;
  }

  /**
   * Get totalBlocks
   * @return totalBlocks
   **/
  @JsonProperty("TotalBlocks")
  @Schema(description = "")
  public Integer getTotalBlocks() {
    return totalBlocks;
  }

  public void setTotalBlocks(Integer totalBlocks) {
    this.totalBlocks = totalBlocks;
  }

  public BlockInfo cipher(Boolean cipher) {
    this.cipher = cipher;
    return this;
  }

  /**
   * Get cipher
   * @return cipher
   **/
  @JsonProperty("Cipher")
  @Schema(description = "")
  public Boolean isCipher() {
    return cipher;
  }

  public void setCipher(Boolean cipher) {
    this.cipher = cipher;
  }

  public BlockInfo compress(Boolean compress) {
    this.compress = compress;
    return this;
  }

  /**
   * Get compress
   * @return compress
   **/
  @JsonProperty("Compress")
  @Schema(description = "")
  public Boolean isCompress() {
    return compress;
  }

  public void setCompress(Boolean compress) {
    this.compress = compress;
  }

  public BlockInfo duplicated(Boolean duplicated) {
    this.duplicated = duplicated;
    return this;
  }

  /**
   * Get duplicated
   * @return duplicated
   **/
  @JsonProperty("Duplicated")
  @Schema(description = "")
  public Boolean isDuplicated() {
    return duplicated;
  }

  public void setDuplicated(Boolean duplicated) {
    this.duplicated = duplicated;
  }

  public BlockInfo modified(Boolean modified) {
    this.modified = modified;
    return this;
  }

  /**
   * Get modified
   * @return modified
   **/
  @JsonProperty("Modified")
  @Schema(description = "")
  public Boolean isModified() {
    return modified;
  }

  public void setModified(Boolean modified) {
    this.modified = modified;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BlockInfo blockInfo = (BlockInfo) o;
    return Objects.equals(this.diskId, blockInfo.diskId) &&
        Objects.equals(this.endTime, blockInfo.endTime) &&
        Objects.equals(this.failed, blockInfo.failed) &&
        Objects.equals(this.generationId, blockInfo.generationId) &&
        Objects.equals(this.index, blockInfo.index) &&
        Objects.equals(this.key, blockInfo.key) &&
        Objects.equals(this.keyPath, blockInfo.keyPath) &&
        Objects.equals(this.lastBlock, blockInfo.lastBlock) &&
        Objects.equals(this.length, blockInfo.length) &&
        Objects.equals(this.md5, blockInfo.md5) &&
        Objects.equals(this.offset, blockInfo.offset) &&
        Objects.equals(this.reason, blockInfo.reason) &&
        Objects.equals(this.sha1, blockInfo.sha1) &&
        Objects.equals(this.size, blockInfo.size) &&
        Objects.equals(this.startTime, blockInfo.startTime) &&
        Objects.equals(this.streamSize, blockInfo.streamSize) &&
        Objects.equals(this.totalBlocks, blockInfo.totalBlocks) &&
        Objects.equals(this.cipher, blockInfo.cipher) &&
        Objects.equals(this.compress, blockInfo.compress) &&
        Objects.equals(this.duplicated, blockInfo.duplicated) &&
        Objects.equals(this.modified, blockInfo.modified);
  }

  @Override
  public int hashCode() {
    return Objects.hash(diskId, endTime, failed, generationId, index, key, keyPath, lastBlock, length, md5, offset, reason, sha1, size, startTime, streamSize, totalBlocks, cipher, compress, duplicated, modified);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BlockInfo {\n");
    
    sb.append("    diskId: ").append(toIndentedString(diskId)).append("\n");
    sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
    sb.append("    failed: ").append(toIndentedString(failed)).append("\n");
    sb.append("    generationId: ").append(toIndentedString(generationId)).append("\n");
    sb.append("    index: ").append(toIndentedString(index)).append("\n");
    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    keyPath: ").append(toIndentedString(keyPath)).append("\n");
    sb.append("    lastBlock: ").append(toIndentedString(lastBlock)).append("\n");
    sb.append("    length: ").append(toIndentedString(length)).append("\n");
    sb.append("    md5: ").append(toIndentedString(md5)).append("\n");
    sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
    sb.append("    sha1: ").append(toIndentedString(sha1)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
    sb.append("    streamSize: ").append(toIndentedString(streamSize)).append("\n");
    sb.append("    totalBlocks: ").append(toIndentedString(totalBlocks)).append("\n");
    sb.append("    cipher: ").append(toIndentedString(cipher)).append("\n");
    sb.append("    compress: ").append(toIndentedString(compress)).append("\n");
    sb.append("    duplicated: ").append(toIndentedString(duplicated)).append("\n");
    sb.append("    modified: ").append(toIndentedString(modified)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
