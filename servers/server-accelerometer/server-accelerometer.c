#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "contiki-net.h"
#include "net/rpl/rpl.h"
#include "rest-engine.h"
#include "adxl345.h"

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
unsigned long last_battery_request = 0;

void
accelerometer_handler(void* request, void* response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  int length;
  char message[40];

  int16_t x, y, z;

  requests++;

  unsigned long request_time = clock_seconds();
  float aux = request_time;
  aux -= (float)last_battery_request;
  last_battery_request = request_time;

  srand(RTIMER_NOW());
  int r = abs(rand() % 5);
  battery_charge -= aux/TIME_DRAIN + r + 1; 
  
  x = accm_read_axis(X_AXIS);
  y = accm_read_axis(Y_AXIS);
  z = accm_read_axis(Z_AXIS);

  sprintf(message, "X : %d, Y : % d, Z : %d", x, y, z);
  length = strlen(message);
  memcpy(buffer, message, length);  

  REST.set_header_content_type(response, REST.type.TEXT_PLAIN); 
  REST.set_header_etag(response, (uint8_t *) &length, 1);
  REST.set_response_payload(response, buffer, length);
}

void
battery_handler(void *request, void *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  int length;
  char message[40];

  unsigned long request_time = clock_seconds();
  float aux = request_time;
  aux -= (float)last_battery_request;
  last_battery_request = request_time;

  /*
  PRINTF("request time: %lu\n", request_time);
  PRINTF("aux: %lu\n", aux);
  PRINTF("aux3600: %lu\n", aux/180);
  PRINTF("aux36: %lu\n", aux/360);
  PRINTF("last request: %lu\n", last_battery_request);
  PRINTF("leds: %d\n", leds_requests);
  PRINTF("toggle: %d\n", toggle_requests);
  PRINTF("temperature: %d\n", temperature_requests);
  PRINTF("accelerometer: %d\n", accelerometer_requests);
  */

  /*
  PRINTF("random: %d\n", r);
  */

   battery_charge -= aux/TIME_DRAIN + BATTERY_DRAIN;

PRINTF("battery charge: %d\n", (int)battery_charge);

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

RESOURCE(battery, "title=\"Batt\";rt=\"Batt\"", battery_handler, NULL, NULL, NULL);

RESOURCE(accelerometer, "title=\"Acc\";rt=\"Acc\"", accelerometer_handler, NULL, NULL, NULL);

PROCESS(server_accelerometer, "Accelerometer server");
AUTOSTART_PROCESSES(&server_accelerometer);

PROCESS_THREAD(server_accelerometer, ev, data)
{
  PROCESS_BEGIN();

  PRINTF("Starting Erbium Accelerometer Server\n");

  PRINTF("uIP buffer: %u\n", UIP_BUFSIZE);
  PRINTF("LL header: %u\n", UIP_LLH_LEN);
  PRINTF("IP+UDP header: %u\n", UIP_IPUDPH_LEN);
  PRINTF("REST max chunk: %u\n", REST_MAX_CHUNK_SIZE);

  /* Initialize the REST engine. */
  rest_init_engine();
  
  /* Activate the application-specific resources. */
  rest_activate_resource(&accelerometer, "acc");
  rest_activate_resource(&battery, "batt");
  
  clock_init();

  /* Define application-specific events here. */
  while(1) {
    PROCESS_WAIT_EVENT();
  } /* while (1) */

  PROCESS_END();
}
