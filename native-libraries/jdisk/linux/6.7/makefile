.PHONY: all build clean rebuild

THREAD = 1
CC = gcc
CXX = g++
CFLAGS = -fPIC -Wextra -Iinclude -I../../../jdk/include -I../../../jdk/include/linux
LDFLAGS = -Wl,-rpath,./lib/lib64:\$$ORIGIN/./lib/lib64 -Wl,-rpath-link,$$ORIGIN/./lib/lib64 
LDLIBS = -L. -L./lib/lib64 -lvixDiskLib -lvixMntapi
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

LIB_FILES=./lib/lib64/libjDiskLib.so

all: build

build: $(LIB_FILES)	 

clean:
	rm -f *.o *.gch $(LIB_FILES)
	
rebuild: clean build


PFILES= \
jDiskLib.o jUtils.o

.cpp.o:
	$(CXX) -c $< -o $@ $(CFLAGS) 
 

./lib/lib64/libjDiskLib.so:	 $(PFILES)
	$(CXX) -shared -o $@ $(CFLAGS) $(PFILES)  $(LDFLAGS) $(LDLIBS)	 

	
