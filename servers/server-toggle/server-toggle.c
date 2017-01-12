#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "contiki-net.h"
#include "net/rpl/rpl.h"
#include "rest-engine.h"
#include "dev/leds.h"

#define DEBUG 1
#if DEBUG
#define PRINTF(...) printf(__VA_ARGS__)
#define PRINT6ADDR(addr) PRINTF("[%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x:%02x%02x]", ((uint8_t *)addr)[0], ((uint8_t *)addr)[1], ((uint8_t *)addr)[2], ((uint8_t *)addr)[3], ((uint8_t *)addr)[4], ((uint8_t *)addr)[5], ((uint8_t *)addr)[6], ((uint8_t *)addr)[7], ((uint8_t *)addr)[8], ((uint8_t *)addr)[9], ((uint8_t *)addr)[10], ((uint8_t *)addr)[11], ((uint8_t *)addr)[12], ((uint8_t *)addr)[13], ((uint8_t *)addr)[14], ((uint8_t *)addr)[15])
#define PRINTLLADDR(lladdr) PRINTF("[%02x:%02x:%02x:%02x:%02x:%02x]",(lladdr)->addr[0], (lladdr)->addr[1], (lladdr)->addr[2], (lladdr)->addr[3],(lladdr)->addr[4], (lladdr)->addr[5])
#else
#define PRINTF(...)
#define PRINT6ADDR(addr)
#define PRINTLLADDR(addr)
#endif

#define BATTERY_DRAIN 0.1
#define TIME_DRAIN 500

float battery_charge = 100;
int requests = 0;
unsigned long last_request = 0;

void
toggle_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  requests++;
  
  unsigned long request_time = clock_seconds();
  float aux = request_time;
  aux -= (float)last_request;
  last_request = request_time;

  srand(RTIMER_NOW());
  int r = abs(rand() % 5);
  battery_charge -= aux/TIME_DRAIN + r + 1; 
  
  leds_toggle(LEDS_ALL);
}

void
battery_handler(void *request, void *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  int length;
  char message[40];

  unsigned long request_time = clock_seconds();
  float aux = request_time;
  aux -= (float)last_request;
  last_request = request_time;

  battery_charge -= aux/TIME_DRAIN + BATTERY_DRAIN;

  PRINTF("Battery charge : %d\n", (int)battery_charge);

  if(battery_charge < 0)
	battery_charge = 0;

  requests = 0; //XXX debug purpose
  
  sprintf(message, "%d", (int)battery_charge);
  length = strlen(message);
  memcpy(buffer, message, length);

  REST.set_header_content_type(response, REST.type.TEXT_PLAIN); 
  REST.set_header_etag(response, (uint8_t *) &length, 1);
  REST.set_response_payload(response, buffer, length);
}

RESOURCE(toggle, "title=\"Togg\";rt=\"Togg\"", NULL, toggle_handler, toggle_handler, NULL);

RESOURCE(battery, "title=\"Batt\";rt=\"Batt\"", battery_handler, NULL, NULL, NULL);

PROCESS(server_toggle, "Toggle server");
AUTOSTART_PROCESSES(&server_toggle);

PROCESS_THREAD(server_toggle, ev, data)
{
  PROCESS_BEGIN();

  PRINTF("Starting Erbium Toggle Server\n");

  PRINTF("uIP buffer: %u\n", UIP_BUFSIZE);
  PRINTF("LL header: %u\n", UIP_LLH_LEN);
  PRINTF("IP+UDP header: %u\n", UIP_IPUDPH_LEN);
  PRINTF("REST max chunk: %u\n", REST_MAX_CHUNK_SIZE);

  /* Initialize the REST engine. */
  rest_init_engine();
  
  /* Activate the application-specific resources. */
  rest_activate_resource(&toggle, "togg");
  rest_activate_resource(&battery, "batt");
  
  clock_init();

  /* Define application-specific events here. */
  while(1) {
    PROCESS_WAIT_EVENT();
  } /* while (1) */

  PROCESS_END();
}
