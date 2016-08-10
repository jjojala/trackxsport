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

To find out more about the protocol, please refer to sources.
