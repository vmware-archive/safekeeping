package com.vmware.safekeeping.core.type.fco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vmware.safekeeping.core.soap.VimConnection;
import com.vmware.safekeeping.core.type.ManagedEntityInfo;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFaultFaultMsg;

public interface IManagedEntityInfoPath {
    default String getManagedEntityInfoFullPath(final ManagedEntityInfo rpInfo)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        return ManagedEntityInfo.composeEntityInfoName(getManagedEntityInfoPath(rpInfo));
    }

    default List<ManagedEntityInfo> getManagedEntityInfoPath(final ManagedEntityInfo entity)
            throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException {
        final ArrayList<ManagedEntityInfo> listManagedEntityInfo = new ArrayList<>();
        ManagedObjectReference mor = entity.getMoref();
        listManagedEntityInfo.add(entity);
        while (true) {
            mor = (ManagedObjectReference) getVimConnection().getVimHelper().entityProps(mor, "parent");
            if (mor == null) {
                break;
            }
            final String entityName = getVimConnection().getVimHelper().entityName(mor);

            final ManagedEntityInfo parentEntity = new ManagedEntityInfo(entityName, mor,
                    getVimConnection().getServerIntanceUuid());
            listManagedEntityInfo.add(parentEntity);
        }
        Collections.reverse(listManagedEntityInfo);

        return listManagedEntityInfo;
    }

    ManagedObjectReference getMoref();

    ManagedObjectReference getParentFolder() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException;

    ManagedEntityInfo getResourcePoolInfo() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException;

    VimConnection getVimConnection();

    ManagedEntityInfo getVmFolderInfo() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InterruptedException;

}
