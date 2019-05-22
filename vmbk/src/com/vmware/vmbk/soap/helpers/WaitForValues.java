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
package com.vmware.vmbk.soap.helpers;

import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Element;

import com.vmware.vim25.HttpNfcLeaseState;
import com.vmware.vim25.InvalidCollectorVersionFaultMsg;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.ObjectUpdate;
import com.vmware.vim25.ObjectUpdateKind;
import com.vmware.vim25.PropertyChange;
import com.vmware.vim25.PropertyChangeOp;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertyFilterUpdate;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.UpdateSet;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.WaitOptions;
import com.vmware.vmbk.soap.IVimConnection;

public class WaitForValues extends BaseHelper {
    public WaitForValues(final IVimConnection connection) {
	super(connection);
    }

    private PropertyFilterSpec propertyFilterSpec(final ManagedObjectReference objmor, final String[] filterProps) {
	final PropertyFilterSpec spec = new PropertyFilterSpec();
	final ObjectSpec oSpec = new ObjectSpec();
	oSpec.setObj(objmor);
	oSpec.setSkip(Boolean.FALSE);
	spec.getObjectSet().add(oSpec);

	final PropertySpec pSpec = new PropertySpec();
	pSpec.getPathSet().addAll(Arrays.asList(filterProps));
	pSpec.setType(objmor.getType());
	spec.getPropSet().add(pSpec);
	return spec;
    }

    private void updateValues(final String[] props, final Object[] vals, final PropertyChange propchg) {
	for (int findi = 0; findi < props.length; findi++) {
	    if (propchg.getName().lastIndexOf(props[findi]) >= 0) {
		if (propchg.getOp() == PropertyChangeOp.REMOVE) {
		    vals[findi] = "";
		} else {
		    vals[findi] = propchg.getVal();
		}
	    }
	}
    }

    public Object[] wait(final ManagedObjectReference objmor, final String[] filterProps, final String[] endWaitProps,
	    final Object[][] expectedVals)
	    throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidCollectorVersionFaultMsg {
	VimPortType vimPort;
	ManagedObjectReference filterSpecRef = null;
	ServiceContent serviceContent;

	try {
	    vimPort = this.connection.getVimPort();
	    serviceContent = this.connection.getServiceContent();
	} catch (final Throwable cause) {
	    throw new BaseHelper.HelperException(cause);
	}

	String version = "";
	final Object[] endVals = new Object[endWaitProps.length];
	final Object[] filterVals = new Object[filterProps.length];
	String stateVal = null;

	final PropertyFilterSpec spec = propertyFilterSpec(objmor, filterProps);

	filterSpecRef = vimPort.createFilter(serviceContent.getPropertyCollector(), spec, true);

	boolean reached = false;

	UpdateSet updateset = null;
	List<PropertyFilterUpdate> filtupary = null;
	List<ObjectUpdate> objupary = null;
	List<PropertyChange> propchgary = null;
	while (!reached) {
	    updateset = vimPort.waitForUpdatesEx(serviceContent.getPropertyCollector(), version, new WaitOptions());
	    if ((updateset == null) || (updateset.getFilterSet() == null)) {
		continue;
	    }
	    version = updateset.getVersion();

	    filtupary = updateset.getFilterSet();

	    for (final PropertyFilterUpdate filtup : filtupary) {
		objupary = filtup.getObjectSet();
		for (final ObjectUpdate objup : objupary) {

		    if ((objup.getKind() == ObjectUpdateKind.MODIFY) || (objup.getKind() == ObjectUpdateKind.ENTER)
			    || (objup.getKind() == ObjectUpdateKind.LEAVE)) {
			propchgary = objup.getChangeSet();
			for (final PropertyChange propchg : propchgary) {
			    updateValues(endWaitProps, endVals, propchg);
			    updateValues(filterProps, filterVals, propchg);
			}
		    }
		}
	    }

	    Object expctdval = null;

	    for (int chgi = 0; (chgi < endVals.length) && !reached; chgi++) {
		for (int vali = 0; (vali < expectedVals[chgi].length) && !reached; vali++) {
		    expctdval = expectedVals[chgi][vali];
		    if (endVals[chgi] == null) {

		    } else if (endVals[chgi].toString().contains("val: null")) {

			final Element stateElement = (Element) endVals[chgi];
			if ((stateElement != null) && (stateElement.getFirstChild() != null)) {
			    stateVal = stateElement.getFirstChild().getTextContent();
			    reached = expctdval.toString().equalsIgnoreCase(stateVal) || reached;
			}
		    } else {
			expctdval = expectedVals[chgi][vali];
			reached = expctdval.equals(endVals[chgi]) || reached;
			stateVal = "filtervals";
		    }
		}
	    }
	}
	Object[] retVal = null;

	try {
	    vimPort.destroyPropertyFilter(filterSpecRef);
	} catch (final RuntimeFaultFaultMsg e) {

	    e.printStackTrace();
	}
	if (stateVal != null) {
	    if (stateVal.equalsIgnoreCase("ready")) {
		retVal = new Object[] { HttpNfcLeaseState.READY };
	    }
	    if (stateVal.equalsIgnoreCase("error")) {
		retVal = new Object[] { HttpNfcLeaseState.ERROR };
	    }
	    if (stateVal.equals("filtervals")) {
		retVal = filterVals;
	    }
	} else {
	    retVal = new Object[] { HttpNfcLeaseState.ERROR };
	}
	return retVal;
    }

}
