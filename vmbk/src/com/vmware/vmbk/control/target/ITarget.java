/*******************************************************************************
 * Copyright (C) 2019, VMware Inc
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.vmware.vmbk.control.target;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import com.vmware.jvix.jDiskLib.Block;
import com.vmware.vmbk.control.info.DumpFileInfo;
import com.vmware.vmbk.profile.GlobalProfile;
import com.vmware.vmbk.profile.FcoProfile;
import com.vmware.vmbk.profile.GenerationProfile;
import com.vmware.vmbk.type.ByteArrayInOutStream;
import com.vmware.vmbk.type.FirstClassObject;
import com.vmware.vmbk.type.ManagedFcoEntityInfo;

public interface ITarget {

    public void close();

    public boolean closeGetDump(final int index, int bufferIndex);

    public boolean closePostDump(int index, int bufferIndex);

    public boolean createGenerationFolder();

    public boolean createProfileVmFolder(FcoProfile fcoProfile);

    public Map<String, String> defaultConfigurations();

    public void finalize();

    public byte[] getBlockTracks();

    public int getDiskId();

    public boolean getDump(final int index, int bufferIndex, ByteBuffer buffer);

    DumpFileInfo[] getDumpFileInfo();

    public DumpFileInfo getDumpFileInfo(final int index);

    
    public FirstClassObject getFcoTarget();

    public String getFullPath(final String path);

    public byte[] getGenerationProfileToByteArray();

    public byte[] getGenerationProfileToByteArray(final int genId);

    public byte[] getGenerationProfileToByteArray(final FcoProfile fcoProfile, final int genId);

    public String getGroup();

    public byte[] getMd5();

    
    public TreeMap<String, String> getMd5DiskList();

    public byte[] getNvRamToByteArray();

    public byte[] getObjectMd5(final String fullPath);

    public byte[] getPreviousGenerationProfileToByteArray();

    public byte[] getGlobalProfileToByteArray();

    public GenerationProfile getGenerationProfile();

    public byte[] getFcoProfileToByteArray();

    public String getTargetName();

    public String getUri(String path);

    public byte[] getvAppConfigToByteArray();

    public byte[] getVmxToByteArray();

    public boolean initialize();

    public boolean initializeGetBuffersArray(int size);

    public boolean initializemM5List();

    public boolean initializePostBuffersArray(int size);

    
    public GlobalProfile initializeProfileAllFco();

    
    public boolean isActive();

    public boolean isBlockTracksOutExist(int generationId, int diskId);

    public boolean isDumpOutExist(int generationId, int diskId);

    public boolean isMd5FileExist(int generationId);

    public boolean isProfAllVmExist();

    public boolean isProfileVmExist();

    
    public void loadStatus(String key, boolean clean);

    public LinkedHashMap<String, String> manualConfiguration();

    public boolean open(final boolean compress);

    public boolean open(final int diskId, final boolean compress);

    public boolean openGetDump(final int index, final Block block, int bufferIndex, int size);

    public boolean openPostDump(int index, final Block block, int bufferIndex, int size);

    public boolean postBlockTracks(final ByteArrayInOutStream byteArrayStream);

    public boolean postDump(int index, int bufferIndex, final ByteBuffer buffer);

    public boolean postGenerationProfile();

    public boolean postMd5();

    public boolean postNvRam(final ByteArrayInOutStream byteArrayStream);

    public boolean postProfAllFco(ByteArrayInOutStream ByteArrayInOutStream);

    public boolean postProfileFco(ByteArrayInOutStream ByteArrayInOutStream);

    public boolean postReport(int diskId, final ByteArrayInOutStream byteArrayStream);

    public boolean postvAppConfig(ByteArrayInOutStream byteArrayStream);

    public boolean postVmx(final ByteArrayInOutStream byteArrayStream);

    public boolean removeFcoProfile(ManagedFcoEntityInfo fcoInfo);

    public boolean removeGeneration(final int generationId);

    
    public void saveStatus(String key);

    
    public void setFcoTarget(FirstClassObject vmm);

    public void setProfGen(GenerationProfile profGen);
}
