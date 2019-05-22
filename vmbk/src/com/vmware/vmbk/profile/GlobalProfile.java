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
package com.vmware.vmbk.profile;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vmbk.control.IoFunction;
import com.vmware.vmbk.soap.helpers.MorefUtil;
import com.vmware.vmbk.type.EntityType;
import com.vmware.vmbk.type.ImprovedVirtuaDisk;
import com.vmware.vmbk.type.K8s;
import com.vmware.vmbk.type.ManagedFcoEntityInfo;
import com.vmware.vmbk.type.VirtualAppManager;
import com.vmware.vmbk.util.Utility;

public class GlobalProfile extends Profile {

    private static final Logger logger = Logger.getLogger(GlobalProfile.class.getName());

    private String groupIvdName2Uuid;
    private String groupIvdUuid2Name;

    private String groupVmMoref2Uuid;

    private String groupVmName2Uuid;

    private String groupVmUuid2Moref;

    private String groupVmUuid2Name;

    private String groupVAppName2Uuid;
    private String groupVAppUuid2Moref;
    private String groupVAppMoref2Uuid;
    private String groupVAppUuid2Name;

    private String groupK8sName2Uuid;
    private String groupK8sUuid2Name;

    public GlobalProfile() {
	super();
    }

    public GlobalProfile(final byte[] byteArray) throws IOException {
	super(byteArray);
    }

    public void addIvdEntry(final ImprovedVirtuaDisk ivd, final Calendar calendar) {
	logger.entering(getClass().getName(), "addIvdEntry", new Object[] { ivd, calendar });
	this.valuesMap.setStringProperty(this.groupIvdUuid2Name, ivd.getUuid(), ivd.getName());
	this.valuesMap.setStringProperty(this.groupIvdName2Uuid, ivd.getName(), ivd.getUuid());
	final Date timestamp = calendar.getTime();
	final long timestamp_ms = calendar.getTimeInMillis();
	final String groupIvd = ivd.getUuid();

	this.valuesMap.setStringProperty(groupIvd, "name", ivd.getName());
	this.valuesMap.setStringProperty(groupIvd, "uuid", ivd.getUuid());
	this.valuesMap.setStringProperty(groupIvd, "serverUuid", ivd.getServerUuid());
	this.valuesMap.setBooleanProperty(groupIvd, "availability", true);
	this.valuesMap.setDateProperty(groupIvd, "timestamp", timestamp);
	this.valuesMap.setLongProperty(groupIvd, "timestamp_ms", timestamp_ms);
	this.valuesMap.setBooleanProperty(groupIvd, "isTemplate", false);
	this.valuesMap.setEntityTypeProperty(groupIvd, "type", ivd.getEntityType());
	logger.exiting(getClass().getName(), "addIvdEntry");
    }

    /**
     * @param k8s
     * @param cal
     */
    public void addK8sEntry(final K8s k8s, final Calendar calendar) {
	logger.entering(getClass().getName(), "addK8sEntry", new Object[] { k8s, calendar });
	this.valuesMap.setStringProperty(this.groupK8sUuid2Name, k8s.getUuid(), k8s.getName());
	this.valuesMap.setStringProperty(this.groupK8sName2Uuid, k8s.getName(), k8s.getUuid());
	final Date timestamp = calendar.getTime();
	final long timestamp_ms = calendar.getTimeInMillis();
	final String groupIvd = k8s.getUuid();

	this.valuesMap.setStringProperty(groupIvd, "name", k8s.getName());
	this.valuesMap.setStringProperty(groupIvd, "uuid", k8s.getUuid());
	this.valuesMap.setStringProperty(groupIvd, "serverUuid", k8s.getServerUuid());
	this.valuesMap.setBooleanProperty(groupIvd, "availability", true);
	this.valuesMap.setDateProperty(groupIvd, "timestamp", timestamp);
	this.valuesMap.setLongProperty(groupIvd, "timestamp_ms", timestamp_ms);
	this.valuesMap.setBooleanProperty(groupIvd, "isTemplate", false);
	this.valuesMap.setEntityTypeProperty(groupIvd, "type", k8s.getEntityType());
	logger.exiting(getClass().getName(), "addK8sEntry");

    }

    /**
     * @param vApp
     * @param cal
     */
    public void addVAppEntry(final VirtualAppManager vApp, final Calendar calendar) {
	logger.entering(getClass().getName(), "addVAppEntry", new Object[] { vApp, calendar });
	final Date timestamp = calendar.getTime();
	final long timestamp_ms = calendar.getTimeInMillis();

	this.valuesMap.setStringProperty(this.groupVAppName2Uuid, vApp.getName(), vApp.getUuid());
	this.valuesMap.setStringProperty(this.groupVAppUuid2Moref, vApp.getUuid(), vApp.getMorefValue());
	this.valuesMap.setStringProperty(this.groupVAppMoref2Uuid, vApp.getMorefValue(), vApp.getUuid());
	this.valuesMap.setStringProperty(this.groupVAppUuid2Name, vApp.getUuid(), vApp.getName());

	final String groupVApp = vApp.getUuid();
	this.valuesMap.setStringProperty(groupVApp, "moref", vApp.getMorefValue());
	this.valuesMap.setStringProperty(groupVApp, "name", vApp.getName());
	this.valuesMap.setStringProperty(groupVApp, "uuid", vApp.getUuid());
	this.valuesMap.setStringProperty(groupVApp, "serverUuid", vApp.getServerUuid());
	this.valuesMap.setBooleanProperty(groupVApp, "availability", true);
	this.valuesMap.setDateProperty(groupVApp, "timestamp", timestamp);
	this.valuesMap.setLongProperty(groupVApp, "timestamp_ms", timestamp_ms);
	this.valuesMap.setEntityTypeProperty(groupVApp, "type", vApp.getEntityType());
//	this.valuesMap.setBooleanProperty(groupVApp, "isImprovedVirtualDisk", false);
	logger.exiting(getClass().getName(), "addVAppEntry");

    }

    public void addVmEntry(final ManagedFcoEntityInfo vmInfo, final Calendar calendar, final boolean isTemplate) {
	logger.entering(getClass().getName(), "addVmEntry", new Object[] { vmInfo, calendar });
	final Date timestamp = calendar.getTime();
	final long timestamp_ms = calendar.getTimeInMillis();

	this.valuesMap.setStringProperty(this.groupVmName2Uuid, vmInfo.getName(), vmInfo.getUuid());
	this.valuesMap.setStringProperty(this.groupVmUuid2Moref, vmInfo.getUuid(), vmInfo.getMorefValue());
	this.valuesMap.setStringProperty(this.groupVmMoref2Uuid, vmInfo.getMorefValue(), vmInfo.getUuid());
	this.valuesMap.setStringProperty(this.groupVmUuid2Name, vmInfo.getUuid(), vmInfo.getName());

	final String groupVmm = vmInfo.getUuid();
	this.valuesMap.setStringProperty(groupVmm, "moref", vmInfo.getMorefValue());
	this.valuesMap.setStringProperty(groupVmm, "name", vmInfo.getName());
	this.valuesMap.setStringProperty(groupVmm, "uuid", vmInfo.getUuid());
	this.valuesMap.setStringProperty(groupVmm, "serverUuid", vmInfo.getServerUuid());
	this.valuesMap.setBooleanProperty(groupVmm, "availability", true);
	this.valuesMap.setDateProperty(groupVmm, "timestamp", timestamp);
	this.valuesMap.setLongProperty(groupVmm, "timestamp_ms", timestamp_ms);
	this.valuesMap.setBooleanProperty(groupVmm, "isTemplate", isTemplate);
	this.valuesMap.setEntityTypeProperty(groupVmm, "type", vmInfo.getEntityType());
	// this.valuesMap.setBooleanProperty(groupVmm, "isImprovedVirtualDisk", false);
	logger.exiting(getClass().getName(), "addVmEntry");
    }

    public boolean delFcoProfile(final ManagedFcoEntityInfo fcoInfo) {
	logger.entering(getClass().getName(), "delFcoProfile", fcoInfo);
	boolean result = false;
	try {
	    final String vmGrp = fcoInfo.getUuid();

	    switch (fcoInfo.getEntityType()) {
	    case ImprovedVirtualDisk:
		if (this.valuesMap.doesPropertyExist(this.groupIvdUuid2Name, fcoInfo.getUuid())) {
		    this.valuesMap.removeProperty(this.groupIvdUuid2Name, fcoInfo.getUuid());
		}
		if (this.valuesMap.doesPropertyExist(this.groupIvdName2Uuid, fcoInfo.getName())) {
		    this.valuesMap.removeProperty(this.groupIvdName2Uuid, fcoInfo.getName());
		}
		break;
	    case K8sNamespace:
		if (this.valuesMap.doesPropertyExist(this.groupK8sUuid2Name, fcoInfo.getUuid())) {
		    this.valuesMap.removeProperty(this.groupK8sUuid2Name, fcoInfo.getUuid());
		}
		if (this.valuesMap.doesPropertyExist(this.groupK8sName2Uuid, fcoInfo.getName())) {
		    this.valuesMap.removeProperty(this.groupK8sName2Uuid, fcoInfo.getName());
		}
		break;
	    case VirtualMachine:
		if (this.valuesMap.doesPropertyExist(this.groupVmName2Uuid, fcoInfo.getName())) {
		    this.valuesMap.removeProperty(this.groupVmName2Uuid, fcoInfo.getName());
		}
		if (this.valuesMap.doesPropertyExist(this.groupVmUuid2Moref, fcoInfo.getUuid())) {
		    this.valuesMap.removeProperty(this.groupVmUuid2Moref, fcoInfo.getUuid());
		}
		if (this.valuesMap.doesPropertyExist(this.groupVmMoref2Uuid, fcoInfo.getMorefValue())) {
		    this.valuesMap.removeProperty(this.groupVmMoref2Uuid, fcoInfo.getMorefValue());
		}
		if (this.valuesMap.doesPropertyExist(this.groupVmUuid2Name, fcoInfo.getUuid())) {
		    this.valuesMap.removeProperty(this.groupVmUuid2Name, fcoInfo.getUuid());
		}
		break;
	    case VirtualApp:
		if (this.valuesMap.doesPropertyExist(this.groupVAppName2Uuid, fcoInfo.getName())) {
		    this.valuesMap.removeProperty(this.groupVAppName2Uuid, fcoInfo.getName());
		}
		if (this.valuesMap.doesPropertyExist(this.groupVAppUuid2Moref, fcoInfo.getUuid())) {
		    this.valuesMap.removeProperty(this.groupVAppUuid2Moref, fcoInfo.getUuid());
		}
		if (this.valuesMap.doesPropertyExist(this.groupVAppMoref2Uuid, fcoInfo.getMorefValue())) {
		    this.valuesMap.removeProperty(this.groupVAppMoref2Uuid, fcoInfo.getMorefValue());
		}
		if (this.valuesMap.doesPropertyExist(this.groupVAppUuid2Name, fcoInfo.getUuid())) {
		    this.valuesMap.removeProperty(this.groupVAppUuid2Name, fcoInfo.getUuid());
		}
		break;
	    default:
		break;
	    }
	    this.valuesMap.removeSection(vmGrp);
	    result = true;
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = false;
	}
	logger.exiting(getClass().getName(), "delFcoProfile", result);
	return result;
    }

    public boolean delGroupIvdEntry(final String item) {
	logger.entering(getClass().getName(), "delGroupIvdEntry", item);
	String name = null;
	String uuid = null;
	boolean result = true;
	try {
	    if (this.valuesMap.doesPropertyExist(this.groupIvdUuid2Name, item)) {
		name = this.valuesMap.getStringProperty(this.groupIvdUuid2Name, item);
		uuid = item;
	    }
	    if (this.valuesMap.doesPropertyExist(this.groupIvdName2Uuid, item)) {
		uuid = this.valuesMap.getStringProperty(this.groupVmName2Uuid, item);
		name = item;
	    }
	    if (StringUtils.isNotEmpty(uuid)) {
		if (this.valuesMap.doesPropertyExist(this.groupIvdUuid2Name, uuid)) {
		    this.valuesMap.removeProperty(this.groupIvdUuid2Name, uuid);
		}
	    }
	    if (StringUtils.isNotEmpty(name)) {
		if (this.valuesMap.doesPropertyExist(this.groupIvdName2Uuid, name)) {
		    this.valuesMap.removeProperty(this.groupIvdName2Uuid, name);
		}
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = false;
	}
	logger.exiting(getClass().getName(), "delGroupIvdEntry", result);
	return result;
    }

    // TODO Remove unused code found by UCDetector
//     public List<String> filterNonTemplate(final List<String> uuidList) {
// 	logger.entering(getClass().getName(), "filterNonTemplate", uuidList);
//
// 	final LinkedList<String> result = new LinkedList<>();
// 	for (final String uuid : uuidList) {
// 	    if ((uuid != null) && !isTemplateWithUuid(uuid)) {
// 		result.add(uuid);
// 	    }
// 	}
// 	logger.exiting(getClass().getName(), "filterNonTemplate", result);
// 	return result;
//     }
    public boolean delGroupK8sEntry(final String item) {
	logger.entering(getClass().getName(), "delGroupK8sEntry", item);
	String name = null;
	String uuid = null;
	boolean result = true;
	try {
	    if (this.valuesMap.doesPropertyExist(this.groupK8sUuid2Name, item)) {
		name = this.valuesMap.getStringProperty(this.groupK8sUuid2Name, item);
		uuid = item;
	    }
	    if (this.valuesMap.doesPropertyExist(this.groupK8sName2Uuid, item)) {
		uuid = this.valuesMap.getStringProperty(this.groupVmName2Uuid, item);
		name = item;
	    }
	    if (StringUtils.isNotEmpty(uuid)) {
		if (this.valuesMap.doesPropertyExist(this.groupK8sUuid2Name, uuid)) {
		    this.valuesMap.removeProperty(this.groupK8sUuid2Name, uuid);
		}
	    }
	    if (StringUtils.isNotEmpty(name)) {
		if (this.valuesMap.doesPropertyExist(this.groupK8sName2Uuid, name)) {
		    this.valuesMap.removeProperty(this.groupK8sName2Uuid, name);
		}
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = false;
	}
	logger.exiting(getClass().getName(), "delGroupK8sEntry", result);
	return result;
    }

    public boolean delGroupVAppEntry(final String item) {
	logger.entering(getClass().getName(), "delGroupVAppEntry", item);
	String name = null;
	String uuid = null;
	String moref = null;
	boolean result = true;
	try {
	    if (this.valuesMap.doesPropertyExist(this.groupVAppUuid2Name, item)) {
		name = this.valuesMap.getStringProperty(this.groupVAppUuid2Name, item);
		uuid = item;
		if (this.valuesMap.doesPropertyExist(this.groupVAppUuid2Moref, item)) {
		    moref = this.valuesMap.getStringProperty(this.groupVAppUuid2Moref, item);
		}
	    } else if (this.valuesMap.doesPropertyExist(this.groupVAppName2Uuid, item)) {
		uuid = this.valuesMap.getStringProperty(this.groupVAppName2Uuid, item);
		name = item;
		if (this.valuesMap.doesPropertyExist(this.groupVAppUuid2Moref, item)) {
		    moref = this.valuesMap.getStringProperty(this.groupVAppUuid2Moref, item);
		}
	    } else if (this.valuesMap.doesPropertyExist(this.groupVAppUuid2Moref, item)) {
		moref = this.valuesMap.getStringProperty(this.groupVAppUuid2Moref, item);
		uuid = item;
		if (this.valuesMap.doesPropertyExist(this.groupVAppUuid2Name, item)) {
		    name = this.valuesMap.getStringProperty(this.groupVAppUuid2Name, item);
		}
	    }
	    if (this.valuesMap.doesPropertyExist(this.groupVAppMoref2Uuid, item)) {
		uuid = this.valuesMap.getStringProperty(this.groupVAppMoref2Uuid, item);
		moref = item;
		if (this.valuesMap.doesPropertyExist(this.groupVAppUuid2Name, item)) {
		    name = this.valuesMap.getStringProperty(this.groupVAppUuid2Name, item);
		}
	    }
	    if (StringUtils.isNotEmpty(uuid)) {
		if (this.valuesMap.doesPropertyExist(this.groupVAppUuid2Name, uuid)) {
		    this.valuesMap.removeProperty(this.groupVAppUuid2Name, uuid);
		}
		if (this.valuesMap.doesPropertyExist(this.groupVAppUuid2Moref, uuid)) {
		    this.valuesMap.removeProperty(this.groupVAppUuid2Moref, uuid);
		}
	    }
	    if (StringUtils.isNotEmpty(moref)) {
		if (this.valuesMap.doesPropertyExist(this.groupVAppMoref2Uuid, moref)) {
		    this.valuesMap.removeProperty(this.groupVAppMoref2Uuid, moref);
		}
	    }
	    if (StringUtils.isNotEmpty(name)) {
		if (this.valuesMap.doesPropertyExist(this.groupVAppName2Uuid, name)) {
		    this.valuesMap.removeProperty(this.groupVAppName2Uuid, name);
		}
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = false;
	}
	logger.exiting(getClass().getName(), "delGroupVAppEntry", result);
	return result;
    }

    public boolean delGroupVmEntry(final String item) {
	logger.entering(getClass().getName(), "delGroupVmEntry", item);
	String name = null;
	String uuid = null;
	String moref = null;
	boolean result = true;
	try {
	    if (this.valuesMap.doesPropertyExist(this.groupVmUuid2Name, item)) {
		name = this.valuesMap.getStringProperty(this.groupVmUuid2Name, item);
		uuid = item;
		if (this.valuesMap.doesPropertyExist(this.groupVmUuid2Moref, item)) {
		    moref = this.valuesMap.getStringProperty(this.groupVmUuid2Moref, item);
		}
	    } else if (this.valuesMap.doesPropertyExist(this.groupVmName2Uuid, item)) {
		uuid = this.valuesMap.getStringProperty(this.groupVmName2Uuid, item);
		name = item;
		if (this.valuesMap.doesPropertyExist(this.groupVmUuid2Moref, item)) {
		    moref = this.valuesMap.getStringProperty(this.groupVmUuid2Moref, item);
		}
	    } else if (this.valuesMap.doesPropertyExist(this.groupVmUuid2Moref, item)) {
		moref = this.valuesMap.getStringProperty(this.groupVmUuid2Moref, item);
		uuid = item;
		if (this.valuesMap.doesPropertyExist(this.groupVmUuid2Name, item)) {
		    name = this.valuesMap.getStringProperty(this.groupVmUuid2Name, item);
		}
	    }
	    if (this.valuesMap.doesPropertyExist(this.groupVmMoref2Uuid, item)) {
		uuid = this.valuesMap.getStringProperty(this.groupVmMoref2Uuid, item);
		moref = item;
		if (this.valuesMap.doesPropertyExist(this.groupVmUuid2Name, item)) {
		    name = this.valuesMap.getStringProperty(this.groupVmUuid2Name, item);
		}
	    }
	    if (StringUtils.isNotEmpty(uuid)) {
		if (this.valuesMap.doesPropertyExist(this.groupVmUuid2Name, uuid)) {
		    this.valuesMap.removeProperty(this.groupVmUuid2Name, uuid);
		}
		if (this.valuesMap.doesPropertyExist(this.groupVmUuid2Moref, uuid)) {
		    this.valuesMap.removeProperty(this.groupVmUuid2Moref, uuid);
		}
	    }
	    if (StringUtils.isNotEmpty(moref)) {
		if (this.valuesMap.doesPropertyExist(this.groupVmMoref2Uuid, moref)) {
		    this.valuesMap.removeProperty(this.groupVmMoref2Uuid, moref);
		}
	    }
	    if (StringUtils.isNotEmpty(name)) {
		if (this.valuesMap.doesPropertyExist(this.groupVmName2Uuid, name)) {
		    this.valuesMap.removeProperty(this.groupVmName2Uuid, name);
		}
	    }
	} catch (final Exception e) {
	    logger.warning(Utility.toString(e));
	    result = false;
	}
	logger.exiting(getClass().getName(), "delGroupIvdEntry", result);
	return result;
    }

    public List<ManagedFcoEntityInfo> getAllEntities() {
	logger.entering(getClass().getName(), "getAllEntities");
	final List<ManagedFcoEntityInfo> result = new LinkedList<>();

	final List<String> entryList = this.valuesMap.getPropertyNames(this.groupVmUuid2Moref);
	entryList.addAll(this.valuesMap.getPropertyNames(this.groupIvdUuid2Name));
	entryList.addAll(this.valuesMap.getPropertyNames(this.groupVAppUuid2Name));
	entryList.addAll(this.valuesMap.getPropertyNames(this.groupK8sUuid2Name));
	for (final String uuid : entryList) {
	    final ManagedFcoEntityInfo entity = getFirstClassObjectEntityByUuid(uuid);
	    result.add(entity);
	}
	logger.exiting(getClass().getName(), "getAllEntities");
	return result;
    }

    private List<ManagedFcoEntityInfo> getAllEntitiesByGroup(final String groupName) {
	logger.entering(getClass().getName(), "getAllEntitiesByGroup", groupName);
	final List<ManagedFcoEntityInfo> result = new LinkedList<>();

	final List<String> entryList = this.valuesMap.getPropertyNames(groupName);
	for (final String uuid : entryList) {
	    final ManagedFcoEntityInfo entity = getFirstClassObjectEntityByUuid(uuid);
	    if (entity != null) {
		result.add(entity);
	    }
	}
	logger.exiting(getClass().getName(), "getAllEntitiesByGroup");
	return result;
    }

    public List<ManagedFcoEntityInfo> getAllIvdEntities() {
	logger.entering(getClass().getName(), "getAllIvdEntities");
	final List<ManagedFcoEntityInfo> result = getAllEntitiesByGroup(this.groupIvdUuid2Name);
	logger.exiting(getClass().getName(), "getAllIvdEntities");
	return result;
    }

    public List<String> getAllIvdUuid() {
	logger.entering(getClass().getName(), "getAllIvdUuid");
	final List<String> result = this.valuesMap.getPropertyNames(this.groupIvdUuid2Name);
	logger.exiting(getClass().getName(), "getAllIvdUuid", result);
	return result;
    }

    public List<ManagedFcoEntityInfo> getAllK8sEntities() {
	logger.entering(getClass().getName(), "getAllK8sEntities");
	final List<ManagedFcoEntityInfo> result = getAllEntitiesByGroup(this.groupK8sUuid2Name);
	logger.exiting(getClass().getName(), "getAllK8sEntities");
	return result;
    }

    public List<String> getAllK8sUuid() {
	logger.entering(getClass().getName(), "getAllK8sUuid");
	final List<String> result = this.valuesMap.getPropertyNames(this.groupK8sUuid2Name);
	logger.exiting(getClass().getName(), "getAllK8sUuid", result);
	return result;
    }

    public List<ManagedFcoEntityInfo> getAllVAppEntities() {
	logger.entering(getClass().getName(), "getAllVAppEntities");
	final List<ManagedFcoEntityInfo> result = getAllEntitiesByGroup(this.groupVAppUuid2Name);
	logger.exiting(getClass().getName(), "getAllVAppEntities");
	return result;
    }

    public List<String> getAllVAppMorefs() {
	logger.entering(getClass().getName(), "getAllVAppMorefs");
	final List<String> result = this.valuesMap.getPropertyNames(this.groupVAppMoref2Uuid);
	logger.exiting(getClass().getName(), "getAllVAppMorefs", result);
	return result;
    }

    public List<String> getAllVAppName() {
	logger.entering(getClass().getName(), "getAllVAppName");
	final List<String> result = this.valuesMap.getPropertyNames(this.groupVAppName2Uuid);
	logger.exiting(getClass().getName(), "getAllVAppName", result);
	return result;
    }

    public List<String> getAllVAppUuid() {
	logger.entering(getClass().getName(), "getAllVAppUuid");
	final List<String> result = this.valuesMap.getPropertyNames(this.groupVAppUuid2Name);
	logger.exiting(getClass().getName(), "getAllVAppUuid", result);
	return result;
    }

    public List<ManagedFcoEntityInfo> getAllVmEntities() {
	logger.entering(getClass().getName(), "getAllVmEntities");
	final List<ManagedFcoEntityInfo> result = getAllEntitiesByGroup(this.groupVmUuid2Moref);
	logger.exiting(getClass().getName(), "getAllVmEntities");
	return result;
    }

    public List<String> getAllVmMorefs() {
	logger.entering(getClass().getName(), "getAllVmMorefs");
	final List<String> result = this.valuesMap.getPropertyNames(this.groupVmMoref2Uuid);
	logger.exiting(getClass().getName(), "getAllVmMorefs", result);
	return result;
    }

    public List<String> getAllVmUuid() {
	logger.entering(getClass().getName(), "getAllVmUuid");
	final List<String> result = this.valuesMap.getPropertyNames(this.groupVmUuid2Moref);
	logger.exiting(getClass().getName(), "getAllVmUuid", result);
	return result;
    }

    public ManagedFcoEntityInfo getFirstClassObjectEntityByUuid(final String uuid) {
	logger.entering(getClass().getName(), "getFirstClassObjectEntityByUuid", uuid);
	ManagedFcoEntityInfo result = null;
	if (isExistVirtualMachineWithUuid(uuid) || isExistImprovedVirtualDiskWithUuid(uuid)
		|| isExistVirtualAppWithUuid(uuid)) {

	    final String moref = this.valuesMap.getStringProperty(uuid, "moref");
	    final String name = this.valuesMap.getStringProperty(uuid, "name");
	    final String serverUuid = this.valuesMap.getStringProperty(uuid, "serverUuid");
	    if ((name == null) || (serverUuid == null)) {
		IoFunction.showWarning(logger, "Archive entity %s:%s doesn't exist. Remove this entity.",
			isExistVirtualMachineWithUuid(uuid) ? "vm"
				: isExistImprovedVirtualDiskWithUuid(uuid) ? "ivd"
					: isExistVirtualAppWithUuid(uuid) ? "vapp"
						: isExistK8sWithUuid(uuid) ? "k8s" : "unknow",
			uuid);

	    } else {
		final EntityType entityType = this.valuesMap.getEntityTypeProperty(uuid, "type");
		ManagedObjectReference moRef = null;
		switch (entityType) {
		case ImprovedVirtualDisk:
		    moRef = MorefUtil.create(EntityType.ImprovedVirtualDisk, uuid.substring(24));
		    break;
		case VirtualMachine:
		    moRef = MorefUtil.create(EntityType.VirtualMachine, moref);
		    break;
		case VirtualApp:
		    moRef = MorefUtil.create(EntityType.VirtualApp, moref);
		    break;
		case K8sNamespace:
		    moRef = MorefUtil.create(EntityType.K8sNamespace, uuid.substring(24));
		default:
		    break;
		}

		result = new ManagedFcoEntityInfo(name, moRef, uuid, serverUuid);
	    }
	}
	logger.exiting(getClass().getName(), "getFirstClassObjectEntityByUuid", result);
	return result;
    }

// TODO Remove unused code found by UCDetector
//     public String getImprovedVirtualDiskNameByUuid(final String uuid) {
// 	logger.entering(getClass().getName(), "getImprovedVirtualDiskNameByUuid", uuid);
// 	final String result = this.iniConfiguration.getStringProperty(this.groupIvdUuid2Name, uuid);
// 	logger.exiting(getClass().getName(), "getImprovedVirtualDiskNameByUuid", result);
// 	return result;
//     }

    public ManagedFcoEntityInfo getImprovedVirtualDiskEntityByName(final String name) {
	logger.entering(getClass().getName(), "getImprovedVirtualDiskEntityByName", name);
	ManagedFcoEntityInfo result = null;
	if (isExistImprovedVirtualDiskWithName(name)) {
	    final String uuid = getImprovedVirtualDiskUuidByName(name);
	    final String serverUuid = this.valuesMap.getStringProperty(uuid, "serverUuid");
	    final ManagedObjectReference moRef = MorefUtil.create(EntityType.ImprovedVirtualDisk, uuid.substring(24));
	    result = new ManagedFcoEntityInfo(name, moRef, uuid, serverUuid);
	}
	logger.exiting(getClass().getName(), "getFirstClassObjectEntityByUuid", result);
	return result;
    }

    private String getImprovedVirtualDiskUuidByName(final String name) {
	logger.entering(getClass().getName(), "getImprovedVirtualDiskUuidByName", name);
	final String result = this.valuesMap.getStringProperty(this.groupIvdName2Uuid, name);
	logger.exiting(getClass().getName(), "getImprovedVirtualDiskUuidByName", result);
	return result;
    }

    public ManagedFcoEntityInfo getK8sEntityByName(final String name) {
	logger.entering(getClass().getName(), "getK8sEntityByName", name);
	ManagedFcoEntityInfo result = null;
	if (isExistK8sWithName(name)) {
	    final String uuid = getK8sUuidByName(name);
	    final String serverUuid = this.valuesMap.getStringProperty(uuid, "serverUuid");
	    final ManagedObjectReference moRef = MorefUtil.create(EntityType.K8sNamespace, uuid.substring(24));
	    result = new ManagedFcoEntityInfo(name, moRef, uuid, serverUuid);
	}
	logger.exiting(getClass().getName(), "getK8sEntityByName", result);
	return result;
    }

    private String getK8sUuidByName(final String name) {
	logger.entering(getClass().getName(), "getK8sUuidByName", name);
	final String result = this.valuesMap.getStringProperty(this.groupK8sName2Uuid, name);
	logger.exiting(getClass().getName(), "getK8sUuidByName", result);
	return result;
    }

    private ManagedFcoEntityInfo getVirtualAppEntityByMoref(final ManagedObjectReference moRef) {
	logger.entering(getClass().getName(), "getVirtualAppEntityByMoref", moRef);
	ManagedFcoEntityInfo result = null;
	if (isExistVirtualAppWithMoref(moRef)) {
	    final String uuid = getVirtualAppUuidByMoref(moRef);
	    final String name = this.valuesMap.getStringProperty(uuid, "name");
	    final String serverUuid = this.valuesMap.getStringProperty(uuid, "serverUuid");
	    result = new ManagedFcoEntityInfo(name, moRef, uuid, serverUuid);
	}
	logger.exiting(getClass().getName(), "getVirtualAppEntityByMoref", result);
	return result;
    }

    public ManagedFcoEntityInfo getVirtualAppEntityByMoref(final String vmMorefValue) {
	final ManagedObjectReference vmMoref = MorefUtil.create(EntityType.VirtualApp, vmMorefValue);
	return getVirtualAppEntityByMoref(vmMoref);
    }

    public ManagedFcoEntityInfo getVirtualAppEntityByName(final String name) {
	logger.entering(getClass().getName(), "getVirtualAppEntityByName", name);
	ManagedFcoEntityInfo result = null;
	if (isExistVirtualAppWithName(name)) {
	    final String uuid = getVirtualAppUuidByName(name);
	    final String moref = this.valuesMap.getStringProperty(uuid, "moref");
	    final String serverUuid = this.valuesMap.getStringProperty(uuid, "serverUuid");
	    final ManagedObjectReference moRef = MorefUtil.create(EntityType.VirtualApp, moref);
	    result = new ManagedFcoEntityInfo(name, moRef, uuid, serverUuid);
	}
	logger.exiting(getClass().getName(), "getVirtualAppEntityByName", result);
	return result;
    }

    private String getVirtualAppUuidByMoref(final ManagedObjectReference vmMoref) {
	return getVirtualAppUuidByMoref(vmMoref.getValue());
    }

    private String getVirtualAppUuidByMoref(final String moref) {
	logger.entering(getClass().getName(), "getVirtualAppUuidByMoref", moref);
	final String result = this.valuesMap.getStringProperty(this.groupVAppMoref2Uuid, moref);
	logger.exiting(getClass().getName(), "getVirtualAppUuidByMoref", result);
	return result;
    }

// TODO Remove unused code found by UCDetector
//     public String getVirtualMachineMorefByUuid(final String uuid) {
// 	logger.entering(getClass().getName(), "getVirtualMachineMorefByUuid", uuid);
// 	final String result = this.iniConfiguration.getStringProperty(this.groupUuid2Moref, uuid);
// 	logger.exiting(getClass().getName(), "getVirtualMachineMorefByUuid", result);
// 	return result;
//     }

// TODO Remove unused code found by UCDetector
//     public String getVirtualMachineNameByUuid(final String uuid) {
// 	logger.entering(getClass().getName(), "getVirtualMachineNameByUuid", uuid);
// 	final String result = this.iniConfiguration.getStringProperty(this.groupUuid2Name, uuid);
// 	logger.exiting(getClass().getName(), "getVirtualMachineNameByUuid", result);
// 	return result;
//     }

    private String getVirtualAppUuidByName(final String name) {
	logger.entering(getClass().getName(), "getVirtualAppUuidByName", name);
	final String result = this.valuesMap.getStringProperty(this.groupVAppName2Uuid, name);
	logger.exiting(getClass().getName(), "getVirtualAppUuidByName", result);
	return result;
    }

    private ManagedFcoEntityInfo getVirtualMachineEntityByMoref(final ManagedObjectReference moRef) {
	logger.entering(getClass().getName(), "getVirtualMachineEntityByMoref", moRef);
	ManagedFcoEntityInfo result = null;
	if (isExistVirtualMachineWithMoref(moRef)) {
	    final String uuid = getVirtualMachineUuidByMoref(moRef);
	    final String name = this.valuesMap.getStringProperty(uuid, "name");
	    final String serverUuid = this.valuesMap.getStringProperty(uuid, "serverUuid");
	    result = new ManagedFcoEntityInfo(name, moRef, uuid, serverUuid);
	}
	logger.exiting(getClass().getName(), "getVirtualMachineEntityByMoref", result);
	return result;
    }

    public ManagedFcoEntityInfo getVirtualMachineEntityByMoref(final String vmMorefValue) {
	final ManagedObjectReference vmMoref = MorefUtil.create(EntityType.VirtualMachine, vmMorefValue);
	return getVirtualMachineEntityByMoref(vmMoref);
    }

    public ManagedFcoEntityInfo getVirtualMachineEntityByName(final String name) {
	logger.entering(getClass().getName(), "getVirtualMachineEntityByName", name);
	ManagedFcoEntityInfo result = null;
	if (isExistVirtualMachineWithName(name)) {
	    final String uuid = getVirtualMachineUuidByName(name);
	    final String moref = this.valuesMap.getStringProperty(uuid, "moref");
	    final String serverUuid = this.valuesMap.getStringProperty(uuid, "serverUuid");
	    final ManagedObjectReference moRef = MorefUtil.create(EntityType.VirtualMachine, moref);
	    result = new ManagedFcoEntityInfo(name, moRef, uuid, serverUuid);
	}
	logger.exiting(getClass().getName(), "getVirtualMachineEntityByName", result);
	return result;
    }

    private String getVirtualMachineUuidByMoref(final ManagedObjectReference vmMoref) {
	return getVirtualMachineUuidByMoref(vmMoref.getValue());
    }

    private String getVirtualMachineUuidByMoref(final String vmMoref) {
	logger.entering(getClass().getName(), "getVirtualMachineUuidByMoref", vmMoref);
	final String result = this.valuesMap.getStringProperty(this.groupVmMoref2Uuid, vmMoref);
	logger.exiting(getClass().getName(), "getVirtualMachineUuidByMoref", result);
	return result;
    }

    private String getVirtualMachineUuidByName(final String name) {
	logger.entering(getClass().getName(), "getVirtualMachineUuidByName", name);
	final String result = this.valuesMap.getStringProperty(this.groupVmName2Uuid, name);
	logger.exiting(getClass().getName(), "getVirtualMachineUuidByName", result);
	return result;
    }

    @Override
    protected void initializeGroups() {
	this.groupVmName2Uuid = "vm_name_uuid";
	this.groupVmUuid2Moref = "vm_uuid_moref";

	this.groupVmMoref2Uuid = "vm_moref_uuid";
	this.groupVmUuid2Name = "vm_uuid_name";

	this.groupIvdUuid2Name = "ivd_uuid_name";
	this.groupIvdName2Uuid = "ivd_name_uuid";
	this.groupVAppUuid2Name = "vapp_uuid_name";
	this.groupVAppName2Uuid = "vapp_name_uuid";
	this.groupVAppUuid2Moref = "vapp_uuid_moref";
	this.groupVAppMoref2Uuid = "vapp_moref_uuid";
	this.groupK8sName2Uuid = "k8s_name_uuid";
	this.groupK8sUuid2Name = "k8s_uuid_name";
    }

    public boolean isAvailableWithUuid(final String uuid) {
	logger.entering(getClass().getName(), "isAvailableWithUuid", uuid);
	final Boolean result = this.valuesMap.getBooleanProperty(uuid, "availability", false);
	logger.exiting(getClass().getName(), "isAvailableWithUuid", result);
	return result;
    }

    public boolean isExistImprovedVirtualDiskWithName(final String name) {
	logger.entering(getClass().getName(), "isExistImprovedVirtualDiskWithName", name);
	assert name != null;
	final boolean result = this.valuesMap.doesPropertyExist(this.groupIvdName2Uuid, name);
	logger.exiting(getClass().getName(), "isExistImprovedVirtualDiskWithName", result);
	return result;

    }

    public boolean isExistImprovedVirtualDiskWithUuid(final String uuid) {
	logger.entering(getClass().getName(), "isExistImprovedVirtualDiskWithUuid", uuid);
	assert uuid != null;
	final boolean result = this.valuesMap.doesPropertyExist(this.groupIvdUuid2Name, uuid);
	logger.exiting(getClass().getName(), "isExistImprovedVirtualDiskWithUuid", result);
	return result;

    }

    public boolean isExistK8sWithName(final String name) {
	logger.entering(getClass().getName(), "isExistK8sWithName", name);
	assert name != null;
	final boolean result = this.valuesMap.doesPropertyExist(this.groupK8sName2Uuid, name);
	logger.exiting(getClass().getName(), "isExistK8sWithName", result);
	return result;
    }

    public boolean isExistK8sWithUuid(final String uuid) {
	logger.entering(getClass().getName(), "isExistK8sWithUuid", uuid);
	assert uuid != null;
	final boolean result = this.valuesMap.doesPropertyExist(this.groupK8sUuid2Name, uuid);
	logger.exiting(getClass().getName(), "isExistK8sWithUuid", result);
	return result;
    }

    private boolean isExistVirtualAppWithMoref(final ManagedObjectReference vAppMoref) {
	return isExistVirtualAppWithMoref(vAppMoref.getValue());
    }

    public boolean isExistVirtualAppWithMoref(final String vAppMoref) {
	logger.entering(getClass().getName(), "isExistVirtualAppWithMoref", vAppMoref);
	final boolean result = this.valuesMap.doesPropertyExist(this.groupVAppMoref2Uuid, vAppMoref);
	logger.exiting(getClass().getName(), "isExistVirtualAppWithMoref", result);
	return result;
    }

    public boolean isExistVirtualAppWithName(final String name) {
	logger.entering(getClass().getName(), "isExistVirtualAppWithName", name);
	final boolean result = this.valuesMap.doesPropertyExist(this.groupVAppName2Uuid, name);
	logger.exiting(getClass().getName(), "isExistVirtualAppWithName", result);
	return result;
    }
// TODO Remove unused code found by UCDetector
//     public boolean isTemplateWithName(final String name) {
// 	logger.entering(getClass().getName(), "isTemplateWithName", name);
// 	boolean result = false;
// 	final String uuid = getVirtualMachineUuidByName(name);
// 	if (StringUtils.isNotEmpty(uuid)) {
// 	    result = isTemplateWithUuid(uuid);
// 	}
// 	logger.exiting(getClass().getName(), "isTemplateWithName", result);
// 	return result;
//     }

    public boolean isExistVirtualAppWithUuid(final String uuid) {
	logger.entering(getClass().getName(), "isExistVirtualAppWithUuid", uuid);
	assert uuid != null;
	final boolean result = this.valuesMap.doesPropertyExist(this.groupVAppUuid2Name, uuid);
	logger.exiting(getClass().getName(), "isExistVirtualAppWithUuid", result);
	return result;
    }

    private boolean isExistVirtualMachineWithMoref(final ManagedObjectReference vmMoref) {
	return isExistVirtualMachineWithMoref(vmMoref.getValue());
    }

    public boolean isExistVirtualMachineWithMoref(final String vmMoref) {
	logger.entering(getClass().getName(), "isExistVirtualMachineWithMoref", vmMoref);
	final boolean result = this.valuesMap.doesPropertyExist(this.groupVmMoref2Uuid, vmMoref);
	logger.exiting(getClass().getName(), "isExistVirtualMachineWithMoref", result);
	return result;
    }

    public boolean isExistVirtualMachineWithName(final String name) {
	logger.entering(getClass().getName(), "isExistVirtualMachineWithName", name);
	assert name != null;
	final boolean result = this.valuesMap.doesPropertyExist(this.groupVmName2Uuid, name);
	logger.exiting(getClass().getName(), "isExistVirtualMachineWithName", result);
	return result;
    }

    public boolean isExistVirtualMachineWithUuid(final String uuid) {
	logger.entering(getClass().getName(), "isExistVirtualMachineWithUuid", uuid);
	assert uuid != null;
	final boolean result = this.valuesMap.doesPropertyExist(this.groupVmUuid2Name, uuid);
	logger.exiting(getClass().getName(), "isExistVirtualMachineWithUuid", result);
	return result;
    }

}
