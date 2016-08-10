# trackxsport
Utility for exporting GPS/HRM sports watch tracks with heartbeat rates to GPX.

The particular watch is cheap GD-003 sports watch from Taiwanese company Chou Chin Electronic Inc
(or [Trek Electronic Inc.](http://www.treklimited.com/)), purchased in June 2016 from 
[Deal eXtreme](http://www.dx.com/p/gd-003-multi-function-outdoor-digital-sport-watch-w-pedometer-gps-compass-backlight-navy-354517#.V6rwr6Isw-o).

## Disclaimer

The watch reports its revision as E3.628 (dated 2014/06/30). This can be seen from watch "Infor" menu. It is very much possible
the protocol described here, and the utility itself work with other versions of the watch. However, obviously as the protocol
have been reverse engineered by monitoring the transferred data I cannot give any guarantees. Further I cannot give any sort of
guarantees, that the software do not make any harm to your watch. 

Use at your own risk!

## Protocol

The protocol being described below has been reverse engineered by using analyzing the serial port traffic while using
the "Tracker" software shipped with the watch. In practice the serial communication is deployed as a Virtual COM Port
(VCP), implemented by the
[CP210x USB to UART Bridge VCP Driver](https://www.silabs.com/products/mcu/Pages/USBtoUARTBridgeVCPDrivers.aspx).
The handshaking parameters were: baudrate=57600, databits=8, parity=no, stop-bits=1.

### Data Types

byte - used to represent integer values of range 0...255 or ASCII-US characters
uint16 - 16-bit (2 bytes), unsigned integer, little-endian
float32 - 32-bit (4 bytes), little-endian single-precision floating point number (IEEE 754-2008)

### Receiving data

While it's possible to retrieve the message's header -portion (see below), and based on
/data length/ continue reading the exact number of bytes, the watch seems to sent several
packages and so far I haven't been able to figure out if the protocol indicates the number
of messages to be sent. Therefore, the port has been listened for till a timeout, and
if nothing gets received it is assumed that everything have been received. 

### Message

Both sent, and received messages apply the following structure:

| header ---------------------------------------------------| payload ------------------------|
| prefix      | command: byte, byte  | data length, uint16  | data        | checksum, 2 bytes |
| 0x48 | 0x59 | <FAMILY> | <COMMAND> | <LENGTH> | <LENGTH>  | <DATA>  ... | <CHK>   | <CHK>   |
0      1      2          3           4          5           6             n        n+1

prefix - categorically two byte prefix 'H', 'Y' (0x48, followed by 0x59). There seems no exception to this rule.
command - two byte patttern, first indicating the command "family" (will be covered soon), and the second indicating the command (within the family). 
data length - unsigned integer representing the length of the data. It is notable, that the length excludes checksum bytes.
checksum - 2 byte checksum. The algorithm is still unclear to me, but luckily it's possible to leave checksum verification out when receiving messages, and use fixed requests based on sniffed packages.

When receiving a reply to a request, the request contains essentially the same header (except the data length)
used in the request.

### Get Track Descriptors

To get an overview of what's been stored in the watch's memory, you can make a "Get Track Descriptors" request.
The exact message to be sent:

0x48, 0x59, 0x03, 0x01, 0x00, 0x00, ?, ?

The response message you'll get:

