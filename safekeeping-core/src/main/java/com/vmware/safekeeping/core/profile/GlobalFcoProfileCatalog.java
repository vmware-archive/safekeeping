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
package com.vmware.safekeeping.core.profile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.safekeeping.core.control.target.ITarget;
import com.vmware.safekeeping.core.profile.dataclass.FcoProfileItem;
import com.vmware.safekeeping.core.profile.dataclass.FcoProfiles;
import com.vmware.safekeeping.core.type.ByteArrayInOutStream;
import com.vmware.safekeeping.core.type.ManagedFcoEntityInfo;
import com.vmware.safekeeping.core.type.enums.EntityType;

public class GlobalFcoProfileCatalog {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(GlobalFcoProfileCatalog.class.getName());

    private final FcoProfiles profiles;
    private final ITarget target;

    public GlobalFcoProfileCatalog() {
        this.profiles = new FcoProfiles();
        this.target = null;
        // for serialization
    }

    public GlobalFcoProfileCatalog(final ITarget target) throws IOException {
        this.target = target;
        if (target.isProfAllVmExist()) {
            final byte[] bytes = target.getGlobalProfileToByteArray();
            this.profiles = new ObjectMapper().readValue(bytes, FcoProfiles.class);
        } else {
            this.profiles = new FcoProfiles();
        }
    }

    /**
     * @param ivd
     * @param instance
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public boolean createNewProfile(final ManagedFcoEntityInfo fcoEntity, final Calendar instance)
            throws NoSuchAlgorithmException, IOException {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config(
                    "ManagedFcoEntityInfo fcoEntity=" + fcoEntity + ", Calendar instance=" + instance + " - start"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        FcoProfileItem item = getProfile(fcoEntity.getUuid(), fcoEntity.getEntityType());
        if (item != null) {
            final ManagedFcoEntityInfo profileEntity = item.getFcoEntity();
            if (profileEntity.equals(fcoEntity)) {
                if (logger.isLoggable(Level.CONFIG)) {
                    logger.config("ManagedFcoEntityInfo, Calendar - end"); //$NON-NLS-1$
                }
                return false;
            } else {
                removeProfile(fcoEntity.getUuid());
            }
        }
        item = new FcoProfileItem(fcoEntity);
        item.setTimestamp(instance.getTime());
        this.profiles.add(item);
        final boolean returnboolean = updateFcoProfileCatalog();
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("ManagedFcoEntityInfo, Calendar - end"); //$NON-NLS-1$
        }
        return returnboolean;
    }

    public boolean existProfileWithUuid(final String key) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String key=" + key + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        for (final FcoProfileItem profile : this.profiles.getProfiles()) {
            if (profile.getFcoEntity().getUuid().equals(key)) {
                if (logger.isLoggable(Level.CONFIG)) {
                    logger.config("String - end"); //$NON-NLS-1$
                }
                return true;
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String - end"); //$NON-NLS-1$
        }
        return false;
    }

    /**
     *
     * @param key
     * @param type
     * @return
     */
    public boolean existProfileWithUuid(final String key, final EntityType type) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String key=" + key + ", EntityType type=" + type + " - start"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        for (final FcoProfileItem profile : this.profiles.getProfiles()) {
            if ((profile.getFcoEntity().getEntityType() == type) && profile.getFcoEntity().getUuid().equals(key)) {
                if (logger.isLoggable(Level.CONFIG)) {
                    logger.config("String, EntityType - end"); //$NON-NLS-1$
                }
                return true;
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String, EntityType - end"); //$NON-NLS-1$
        }
        return false;
    }

    /**
     * @return
     */
    public Collection<ManagedFcoEntityInfo> getAllEntities(final EntityType type) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("EntityType type=" + type + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        final List<ManagedFcoEntityInfo> result = new ArrayList<>();
        for (final FcoProfileItem profile : this.profiles.getProfiles()) {
            if (profile.getFcoEntity().getEntityType() == type) {
                result.add(profile.getFcoEntity());
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("EntityType - end"); //$NON-NLS-1$
        }
        return result;
    }

    /**
     * @param key
     * @return
     */
    public ManagedFcoEntityInfo getEntityByMoref(final String key, final EntityType type) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String key=" + key + ", EntityType type=" + type + " - start"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        for (final FcoProfileItem profile : this.profiles.getProfiles()) {
            if ((profile.getFcoEntity().getEntityType() == type)
                    && profile.getFcoEntity().getMorefValue().equals(key)) {
                final ManagedFcoEntityInfo returnManagedFcoEntityInfo = profile.getFcoEntity();
                if (logger.isLoggable(Level.CONFIG)) {
                    logger.config("String, EntityType - end"); //$NON-NLS-1$
                }
                return returnManagedFcoEntityInfo;
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String, EntityType - end"); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * @param key
     * @return
     */
    public ManagedFcoEntityInfo getEntityByName(final String key, final EntityType type) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String key=" + key + ", EntityType type=" + type + " - start"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        for (final FcoProfileItem profile : this.profiles.getProfiles()) {
            if ((profile.getFcoEntity().getEntityType() == type) && profile.getFcoEntity().getName().equals(key)) {
                final ManagedFcoEntityInfo returnManagedFcoEntityInfo = profile.getFcoEntity();
                if (logger.isLoggable(Level.CONFIG)) {
                    logger.config("String, EntityType - end"); //$NON-NLS-1$
                }
                return returnManagedFcoEntityInfo;
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String, EntityType - end"); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * @param key
     * @return
     */
    public ManagedFcoEntityInfo getEntityByUuid(final String key) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String key=" + key + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        for (final FcoProfileItem profile : this.profiles.getProfiles()) {
            if (profile.getFcoEntity().getUuid().equals(key)) {
                final ManagedFcoEntityInfo returnManagedFcoEntityInfo = profile.getFcoEntity();
                if (logger.isLoggable(Level.CONFIG)) {
                    logger.config("String - end"); //$NON-NLS-1$
                }
                return returnManagedFcoEntityInfo;
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String - end"); //$NON-NLS-1$
        }
        return null;
    }

    public ManagedFcoEntityInfo getEntityByUuid(final String key, final EntityType type) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String key=" + key + ", EntityType type=" + type + " - start"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        for (final FcoProfileItem profile : this.profiles.getProfiles()) {
            if ((profile.getFcoEntity().getEntityType() == type) && profile.getFcoEntity().getUuid().equals(key)) {
                final ManagedFcoEntityInfo returnManagedFcoEntityInfo = profile.getFcoEntity();
                if (logger.isLoggable(Level.CONFIG)) {
                    logger.config("String, EntityType - end"); //$NON-NLS-1$
                }
                return returnManagedFcoEntityInfo;
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String, EntityType - end"); //$NON-NLS-1$
        }
        return null;
    }

    public FcoProfileItem getProfile(final String key, final EntityType type) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String key=" + key + ", EntityType type=" + type + " - start"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        for (final FcoProfileItem profile : this.profiles.getProfiles()) {
            if ((profile.getFcoEntity().getEntityType() == type) && profile.getFcoEntity().getUuid().equals(key)) {
                if (logger.isLoggable(Level.CONFIG)) {
                    logger.config("String, EntityType - end"); //$NON-NLS-1$
                }
                return profile;
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String, EntityType - end"); //$NON-NLS-1$
        }
        return null;
    }

    public ITarget getTarget() {
        return this.target;
    }

    /**
     * @param fcoInfo
     * @return
     */
    public FcoProfileItem removeProfile(final ManagedFcoEntityInfo fcoInfo) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("ManagedFcoEntityInfo fcoInfo=" + fcoInfo + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        final FcoProfileItem returnFcoProfileItem = removeProfile(fcoInfo.getUuid());
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("ManagedFcoEntityInfo - end"); //$NON-NLS-1$
        }
        return returnFcoProfileItem;
    }

    public FcoProfileItem removeProfile(final String uuid) {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String uuid=" + uuid + " - start"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        final List<FcoProfileItem> pr = this.profiles.getProfiles();
        for (int index = 0; index < pr.size(); index++) {
            if (pr.get(index).getFcoEntity().getUuid().equals(uuid)) {
                final FcoProfileItem returnFcoProfileItem = this.profiles.remove(index);
                if (logger.isLoggable(Level.CONFIG)) {
                    logger.config("String - end"); //$NON-NLS-1$
                }
                return returnFcoProfileItem;
            }
        }

        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("String - end"); //$NON-NLS-1$
        }
        return null;

    }

    /**
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public boolean updateFcoProfileCatalog() throws NoSuchAlgorithmException, IOException {
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - start"); //$NON-NLS-1$
        }

        final String json = new ObjectMapper().writeValueAsString(this.profiles);
        final ByteArrayInOutStream result = new ByteArrayInOutStream(json);
        final boolean returnboolean = this.target.updateFcoProfileCatalog(result);
        if (logger.isLoggable(Level.CONFIG)) {
            logger.config("<no args> - end"); //$NON-NLS-1$
        }
        return returnboolean;
    }

}
