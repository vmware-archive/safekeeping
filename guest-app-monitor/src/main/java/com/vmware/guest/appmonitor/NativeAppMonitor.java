package com.vmware.guest.appmonitor;

class NativeAppMonitor {

	public static native int enable();

	public static native int disable();

	public static native int isEnabled();

	public static native int markActive();

	public static native String getAppStatus();

	public static native int postAppState(String statusStr);

}