#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <time.h>
#include <unistd.h>

// This is an E, it Echo server, it replies the same UDP to sender but with the TTL set to the value desired by packet info.
// Assumptions
// The port through which we are transferring files is PORT_NUM
// The following attributes define the given problem specifications.
#define MSS 1000
#define SIZE_OF_BYTES 4
#define NAME_OF_FILENAME 20
#define START_SIZE_OF_BYTES 0
#define START_NAME_OF_FILENAME 4
#define MAX_SIZE_OF_FNAME 256
#define BAD_FNAME 777

void echo_UDP(int server_socket)
{
    int len,n,ttl, size;
    char bufin[MSS];
    char input[8];
    char ** inp = NULL;
    struct sockaddr_in remote;

    /* need to know how big address struct is, len must be set before the
       call to recvfrom!!! */

    len = sizeof(remote);
    int a;

    while (1) {

      	// char ttl_pointer[4];
      	// char memory_size_pointer[2];
      	char *ttl_pointer;
      	char *memory_size_pointer;
	
		memset(bufin, 0, sizeof(bufin));
      	/* read a datagram from the socket (put result in bufin) */
      	n=recvfrom(server_socket,bufin,MSS,0,(struct sockaddr *)&remote,&len);

      	/* print out the address of the sender */
      	printf("Server: Got a datagram from %d port %d\n",inet_ntoa(remote.sin_addr), ntohs(remote.sin_port));

      	if (n<0) {
        	perror("Error receiving data");
      	} else {
        	printf("GOT %d BYTES\n",n);

        	/* Got something, just send it back */
			// strcpy(input, bufin);
			// strcpy(ttl_pointer, input);
			// strcpy(memory_size_pointer, (input+3));
			printf("Server: The value of input string is %s \n",bufin);
			const char * split = ",";
			ttl_pointer = strtok(bufin, split);
			printf("Server: The value of TTL string is %s \n", ttl_pointer);
			memory_size_pointer = strtok(NULL, split);
			printf("Server: The value of Volume size string is %s \n", memory_size_pointer);
			ttl = atoi(ttl_pointer);
			size = atoi(memory_size_pointer);

			
			printf("Server: The value of TTL mentioned in the packet is %d\n", ttl);
			printf("Server: The value of size mentioned in the packet is %d\n", size);


			/* Set ttl to the value mentioned */

			if (!(setsockopt(server_socket, IPPROTO_IP, IP_TTL, &ttl, sizeof(ttl)))) {
    			printf("Server: TTL set successfully to %d\n",ttl);
			} else {
    			printf("Server: Error setting TTL: %s\n", strerror(errno));
			}

			/* Send the packet*/
	
			if ( size == 0) {
	        	a = sendto(server_socket, bufin, 10, 0, (struct sockaddr *)&remote, len);
			if ( a > 0) { printf("Server: Send packet to Android\n"); }
      		} else {
				int file_size = 0;
				if ( size > 10) { size = 10; }
				while ( file_size < size*1000)
				{
					sendto(server_socket, bufin, MSS, 0, (struct sockaddr *)&remote, len);
					file_size++;
				}
			}
    	}
	}
}

int main(int argc, char *argv[] )
{

	if (argc >= 1)
		{
		printf(" Server: The Server is listening at Port number :  %s\n",argv[1]);
		}

	int PORT_NUM_RECV = 5555; 	// Port at which the server accepts the UDP packets

	// Variable declarations
	struct sockaddr_in server_address, client_address;
	int new_client,client_length;

	// Socket creation
	printf("Server: Creating socket at server end\n");
	int server_socket;
	server_socket = socket(AF_INET,SOCK_DGRAM,0);
	if (server_socket < 0)
        {
        printf("Server: Socket error with error number: %d\n",errno);
        exit(0);
        }
	printf("Server: Socket succesfully Created\n");

	server_address.sin_family = AF_INET;
	server_address.sin_port = htons(PORT_NUM_RECV);
	server_address.sin_addr.s_addr = htons(INADDR_ANY);

	// Bind the socket
	int bind_id = bind(server_socket, (struct sockaddr *)&server_address, sizeof(server_address));
	if (bind_id < 0)
	{
		printf("Server: Socket Bind Error with error No: %d\n", errno);
		exit(0);
	}
	printf("Server: Binding Server socket successful\n");
	printf("Server: The server is listening for UDP packets at port number %d\n",ntohs(server_address.sin_port));

	// // Android Socket  creation
	// printf("Android: Creating socket to send data to client(Android)\n");

	// int client_socket = socket(AF_INET,SOCK_DGRAM,IPPROTO_UDP);
	// if (client_socket < 0)
	// {
	// 	printf("Android: Client Socket error: %d\n",errno);
	// 	exit(0);
	// }
	// printf("Android: Client Socket succesfully created\n");

	// Echo every datagram we get
	echo_UDP(server_socket);
	printf("Completed . Good bye\n");
}