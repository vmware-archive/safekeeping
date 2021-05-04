/*******************************************************************************
 * Copyright (C) 2021, VMware Inc
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
package com.vmware.safekeeping.core.profile.vmspec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.vmware.safekeeping.common.Utility;
import com.vmware.vim25.VirtualPCIPassthroughDeviceBackingInfo;

abstract class AbstractVirtualDeviceProperties {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(AbstractVirtualDeviceProperties.class.getName());

    protected Map<String, Object> properties;

    public Map<String, Object> getProperties() {
        return this.properties;
    }

    protected Boolean getPropertyAsBoolean(final String key) {
        Boolean result = false;
        final Object object = this.properties.get(key);
        if (object != null) {
            if (object instanceof Boolean) {
                result = (Boolean) object;
            } else if (object instanceof String) {
                result = Boolean.valueOf((String) object);
            } else if (object instanceof Short) {
                result = ((Short) object) != 0;
            } else if (object instanceof Long) {
                result = ((Long) object) != 0;
            } else if (object instanceof Integer) {
                result = ((Integer) object) != 0;
            } else {
                result = false;
                logger.warning("Unsupported type " + object.getClass().getName());
            }

        }
        return result;
    }

    protected Integer getPropertyAsInteger(final String key) {
        Integer result = 0;
        final Object object = this.properties.get(key);
        if (object != null) {
            try {
                if (object instanceof Integer) {
                    result = (Integer) object;
                } else if (object instanceof Short) {
                    result = ((Short) object).intValue();
                } else if (object instanceof Long) {
                    result = ((Long) object).intValue();
                } else if (object instanceof String) {
                    result = Integer.valueOf((String) object);
                } else {
                    result = 0;
                    logger.warning("Unsupported type " + object.getClass().getName());
                }

            } catch (final NumberFormatException e) {
                Utility.logWarning(logger, e);
            }
        }
        return result;
    }

    protected List<byte[]> getPropertyAsListBytes(final String key) {
        final List<byte[]> byteList = new ArrayList<>();
        @SuppressWarnings("unchecked")
        final List<String> endorsementKeyCertificate = (List<String>) this.properties.get(key);
        if (endorsementKeyCertificate != null) {

            for (final String cer : endorsementKeyCertificate) {
                byteList.add(cer.getBytes());
            }
        }
        return byteList;
    }

    protected Long getPropertyAsLong(final String key) {
        Long result = 0L;
        final Object object = this.properties.get(key);
        if (object != null) {
            try {
                if (object instanceof Long) {
                    result = (Long) object;
                } else if (object instanceof Integer) {
                    result = ((Integer) object).longValue();
                } else if (object instanceof Short) {
                    result = ((Short) object).longValue();
                } else if (object instanceof String) {
                    result = Long.valueOf((String) object);
                } else {
                    result = 0L;
                    logger.warning("Unsupported type " + object.getClass().getName());
                }

            } catch (final NumberFormatException e) {
                Utility.logWarning(logger, e);
            }
        }
        return result;
    }

    protected Short getPropertyAsShort(final String key) {
        Short result = 0;
        final Object object = this.properties.get(key);
        if (object != null) {
            try {
                if (object instanceof Short) {
                    result = (Short) object;
                } else if (object instanceof Integer) {
                    result = ((Integer) object).shortValue();
                } else if (object instanceof Long) {
                    result = ((Long) object).shortValue();
                } else if (object instanceof String) {
                    result = Short.valueOf((String) object);
                } else {
                    result = 0;
                    logger.warning("Unsupported type " + object.getClass().getName());
                }

            } catch (final NumberFormatException e) {
                Utility.logWarning(logger, e);
            }
        }
        return result;
    }

    protected String getPropertyAsString(final String key) {
        String result = null;
        final Object object = this.properties.get(key);
        if (object != null) {
            if (object instanceof Boolean) {
                result = ((Boolean) object).toString();
            } else if (object instanceof Integer) {
                result = ((Integer) object).toString();
            } else if (object instanceof Long) {
                result = ((Long) object).toString();
            } else if (object instanceof String) {
                result = (String) object;
            } else {
                result = null;
                logger.warning("Unsupported type " + object.getClass().getName());
            }
        }
        return result;
    }

    protected VirtualPCIPassthroughDeviceBackingInfo getPropertyAsVirtualPCIPassthroughDeviceBackingInfo(
            final String key) {
        VirtualPCIPassthroughDeviceBackingInfo result = null;
        try {
            result = new VirtualPCIPassthroughDeviceBackingInfo();
            result.setDeviceId(getPropertyAsString(key + ".deviceId"));
            result.setDeviceName(getPropertyAsString(key + ".deviceName"));
            result.setId(getPropertyAsString(key + ".id"));
            result.setSystemId(getPropertyAsString(key + ".systemId"));
            result.setUseAutoDetect(getPropertyAsBoolean(key + ".useAutodetect"));
            result.setVendorId(getPropertyAsShort(key + ".vendorId"));
        } catch (final Exception e) {
        }

        return result;
    }

    public void setProperties(final Map<String, Object> properties) {
        this.properties = properties;
    }

}
