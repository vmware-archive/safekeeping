.PHONY: all build clean rebuild install

PREFIX = ../../../guest-app-monitor/src/main/resources
THREAD = 1
CC = gcc
CXX = g++
CFLAGS = -fPIC -Wextra -Iinclude -I../include -I../../../jdk/include -I../../../jdk/include/linux
LDFLAGS = -Wl,-rpath,./lib/lib64:\$$ORIGIN/./lib/lib64 -Wl,-rpath-link,$$ORIGIN/./lib/lib64 
LDLIBS = -L. -L./lib/lib64  
ifeq ($(DEBUG),1)
	CFLAGS += -DDEBUG -g
	GPROF = 1
else
	CFLAGS += -DNDEBUG -O2
	GPROF = 0
endif
 
ifeq ($(GPROF),1)
    CFLAGS+=-pg
endif

.PHONY: all build clean rebuild

LIB_FILES=libVMGuestAppMonitorNative.so

all: build

build: $(LIB_FILES)
clean:
	rm -f *.o *.gch $(LIB_FILES)
rebuild: clean build

install: $(LIB_FILES)
	cp  $(LIB_FILES) $(PREFIX)
	cp ./lib/lib64/libappmonitorlib.so $(PREFIX)
 

PFILES= \
VMGuestAppMonitor.o  

.cpp.o:
	$(CXX) -c $< -o $@ $(CFLAGS) 
 

libVMGuestAppMonitorNative.so:  $(PFILES)
	$(CXX) -shared -o $@ $(CFLAGS) $(PFILES)  $(LDFLAGS) $(LDLIBS)   

	
