CC = cl
LINK = link
LDFLAGS= /DLL /VERSION:7.0
DEBUG= /PDB:jDiskLib.pdb /DEBUG
PARENT = ..
PREFIX = .
INCS=$(PARENT)\include
INCS2=$(JAVA_HOME)\include
INCS3=$(JAVA_HOME)\include\win32
LIBS=$(PREFIX)\lib

all: $(TARGET)

OBJ_FILES= \
VMGuestAppMonitor.obj

LIB_FILES= vmware-appmonitor.lib

VMGuestAppMonitor.obj: VMGuestAppMonitor.c
	$(CC) /c VMGuestAppMonitor.c   /I $(INCS) /I$(INCS2) /I$(INCS3)

libVMGuestAppMonitorNative.dll: $(INC_FILES) $(OBJ_FILES)
	$(LINK)  $(LDFLAGS) $(OBJ_FILES)  /LIBPATH:$(LIBS)  $(LIB_FILES)  /out:release\VMGuestAppMonitorNative.dll

deb-libVMGuestAppMonitorNative.dll:  $(INC_FILES) $(OBJ_FILES)
	$(LINK)  $(LDFLAGS)  $(DEBUG) $(OBJ_FILES)  /LIBPATH:$(LIBS)  $(LIB_FILES)  /out:debug\libVMGuestAppMonitorNative.dll
	
all:libVMGuestAppMonitorNative.dll

debug:deb-libVMGuestAppMonitorNative.dll
 
clean:
	del /q *.dll *.obj *.lib *.exp *.ilk *.pdb >nul 2>&1 
	del /q release\*.dll release\*.obj release\*.lib release\*.exp release\*.ilk release\*.pdb >nul 2>&1