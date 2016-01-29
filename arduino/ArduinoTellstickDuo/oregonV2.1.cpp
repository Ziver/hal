#include "oregonV2.1.h"
#include "buffer.h"

/*******************************************************************************
                     --OREGON v2.1 PROTOCOL SPECIFICATION--

    ----SIGNAL----    
A signal consists of a PREAMBLE, DATA and an POSTAMBLE.
Each signal is sent 2 times in a row to increase the delivery success rate.
Between the two times there is a "low" pause of 8192us.

    ----PREAMBLE----
send 16 "1"(ones) over the air

    ----DATA----
16 bits of sensor type
XX bits of data (where XX <= 64)

The length XX depends on the sensor type. I.e. 0x1A2D => XX=56

Over air a "1"(one) is sent as:
send high for 512 microseconds
send low for 1024 microseconds
send high for 512 microseconds

Over air a "0"(zero) is sent as:
send low for 512 microseconds
send high for 1024 microseconds
send low for 512 microseconds

    ----POSTAMBLE----
send 8 "0"(zeros) over the air

*******************************************************************************/

#define SMALL_PULSE(x) (  4<=x && x<=13 )
#define BIG_PULSE(x)   ( 12<=x && x<=22 )
#define MORE_DATA_NEEDED -1
#define INVALID_DATA -2

enum {
	PARSE_PREAMP = 0,
	PARSE_ID,
	PARSE_DATA
} static state = PARSE_PREAMP;

static uint8_t byteCnt = 0;
static uint8_t bitCnt = 0;
static uint8_t totByteCnt = 0;
int8_t byteLength = -1;

void reset() {
	byteCnt = 0;
	bitCnt = 0;
	totByteCnt = 0;
	state = PARSE_PREAMP;
	byteLength = -1;
};  //end reset

void parseOregonStream(bool level, uint8_t count) {
	static uint8_t cnt = 0; //used for counting stuff independent in every state
	static uint16_t sensorType = 0;
	static int8_t byte;
	static uint8_t bytesToParse = 0;    //the number of bytes left in the data part to parse
	static uint8_t buffer[8];

	if (level) {
		count+=3;
	} else {
		count-=3;
	}

	switch(state) {
		case PARSE_PREAMP:  //look for 25 big pulses followed by one short in a row
			if (BIG_PULSE(count)) {
				++cnt;
				break;
			}
			if (SMALL_PULSE(count)) {
				if (cnt > 25) {
					state=PARSE_ID;
					sensorType = 0;
				}
				cnt = 0;
			}
			break;

		case PARSE_ID:  //get the two first Bytes
			byte = getByte(level, count);
			if (byte == INVALID_DATA) {
				reset();
                cnt = 0;
				break;
			} else if (byte == MORE_DATA_NEEDED) {
				break;
			} else {
				if (sensorType == 0) {
					sensorType = byte << 8;
				} else {
					sensorType |= byte;
					switch (sensorType) {
						case 0xEA4C:
							bytesToParse = 5;
							byteLength = 63;
							break;
						case 0x0A4D:
						case 0x1A2D:    //sensor THGR2228N (channel + sensor_id + battery_level + temp + humidity + checksum)
							bytesToParse = 7;
							byteLength = 79;
							break;
						default:
							reset();
                            cnt = 0;
							return;
					}
					state = PARSE_DATA;
					cnt = 0;
				}
			}
			break;

		case PARSE_DATA:    //get the remaining data
			byte = getByte(level, count);
			if (byte == INVALID_DATA) {
				reset();
                cnt = 0;
				break;
			} else if (byte == MORE_DATA_NEEDED) {
				break;
			}
			buffer[cnt] = byte;
            ++cnt;
			if (bytesToParse == 0) {
                Serial.print(F("+Wclass:sensor;protocol:oregon;model:0x"));
                Serial.print(sensorType, HEX);
                Serial.print(F(";data:0x"));
                for (int8_t i = 0; i < cnt; ++i) {
                  Serial.print(buffer[i], HEX);
                }
                Serial.println(F(";"));
                reset();
                cnt = 0;
			}
            --bytesToParse;
            
			break;
	}
    
}; //end parseOregonStream

int8_t getByte(bool level, uint8_t count) {
	int8_t bit = getBit(level, count);
	static uint8_t byte = 0;
	
	if (bit == INVALID_DATA) {
		return INVALID_DATA;
	} else if (bit == MORE_DATA_NEEDED) {
		return MORE_DATA_NEEDED;
	}
	byte >>= 1;
	if (bit) {
		byte |= (1<<7);
	}
	++totByteCnt;
	++byteCnt;
	if (byteCnt < 8) {
		return MORE_DATA_NEEDED;
	}
	byteCnt=0;
	return byte;
}; //end getByte

int8_t getBit(bool level, uint8_t count) {
	static bool bit = 0;

	if (bitCnt == 0) {
		//First pulse must be small
		if (!SMALL_PULSE(count)) {
			return INVALID_DATA;
		}
		bitCnt = 1;

	} else if (bitCnt == 1) {
		//Second pulse must be long
		if (!BIG_PULSE(count) && totByteCnt!=byteLength){ //special check - last byte might have strange values
			bitCnt = 0;
			return INVALID_DATA;
		}

		bit = level;
		bitCnt = 2;
		return bit;

	} else if (bitCnt == 2) {
		//Prepare for next bit
		if (level && SMALL_PULSE(count)) {
			//Clean start
			bitCnt = 0;
		} else if (BIG_PULSE(count)) {
			//Combined bit
			bitCnt = 1;
		} else if (SMALL_PULSE(count)) {
			//Clean start
			bitCnt = 0;
		}
		return MORE_DATA_NEEDED;
	}

	return MORE_DATA_NEEDED;
}; //end getBit
