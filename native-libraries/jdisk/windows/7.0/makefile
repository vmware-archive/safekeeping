#makefile for nmake 
#C:\Users\mdaneri\Documents\Eclipse\vmbk\jdisk>cl /Iinclude  jDiskLib.c jUtils.c  vixDiskLib.lib vixMntApi.lib /link /LIBPATH:..\lib /DLL /out:jDiskLib.dll

CC = cl
LINK = link
LDFLAGS= /DLL /VERSION:7.0
DEBUG= /PDB:jDiskLib.pdb /DEBUG
PARENT = ..
PREFIX = .
INCS=$(PREFIX)\include
INCS2=$(JAVA_HOME)\include
INCS3=$(JAVA_HOME)\include\win32
LIBS=$(PREFIX)\lib

 
		
all: $(TARGET)

OBJ_FILES= \
jDiskLib.obj jUtils.obj

LIB_FILES= vixDiskLib.lib 

#INC_FILES= $(INCS)\jDiskLib.h $(INCS)\jMntApi.h

		
jUtils.obj: jUtils.c 
	$(CC) /c jUtils.c    -I $(INCS) /I$(INCS2) /I$(INCS3)
jDiskLib.obj: jDiskLib.c
	$(CC) /c jDiskLib.c   /I $(INCS) /I$(INCS2) /I$(INCS3)
	
jDiskLib.dll:  $(INC_FILES) $(OBJ_FILES)
	$(LINK)  $(LDFLAGS) $(OBJ_FILES)  /LIBPATH:$(LIBS)  $(LIB_FILES)  /out:release\jDiskLib.dll

deb-jDiskLib.dll:  $(INC_FILES) $(OBJ_FILES)
	$(LINK)  $(LDFLAGS)  $(DEBUG) $(OBJ_FILES)  /LIBPATH:$(LIBS)  $(LIB_FILES)  /out:debug\jDiskLib.dll
		
all:jDiskLib.dll

debug:deb-jDiskLib.dll

clean:
   del /q *.dll *.obj *.lib *.exp *.ilk *.pdb >nul 2>&1 
   del /q release\*.dll release\*.obj release\*.lib release\*.exp release\*.ilk release\*.pdb >nul 2>&1
   
    