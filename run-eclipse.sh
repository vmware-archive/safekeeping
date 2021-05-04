#!/bin/sh
export VMBK_HOME=/opt/safekeeping
export VMBK_LIB=$VMBK_HOME/lib/lib64
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$VMBK_LIB/
'/root/eclipse/java-2020-09/eclipse/eclipse' 
