# Makefile for server

CC = gcc
OBJSRV = EchoAndAttack.c
CFLAGS = 
# setup for system
LIBS =

all: EchoAndAttack 

echo:	$(OBJSRV)
	$(CC) $(CFLAGS) -o $@ $(OBJSRV) $(LIBS)

clean:
	rm EchoAndAttack

