# Makefile for server

CC = gcc
OBJSRV = EchoAndAttack.c
CFLAGS = 
# setup for system
LIBS =

all: echo 

echo:	$(OBJSRV)
	$(CC) $(CFLAGS) -o $@ $(OBJSRV) $(LIBS)

clean:
	rm echo
