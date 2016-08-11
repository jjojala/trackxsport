# trackxsport
Utility for exporting GPS/HRM sports watch tracks with heartbeat rates to GPX.

## The watch

The particular watch used for developing is cheap GD-003 GPS sports watch
from Taiwanese company Chou Chin Electronic Inc (or
[Trek Electronic Inc.](http://www.treklimited.com/)), purchased in June
2016 from 
[Deal eXtreme](http://www.dx.com/p/gd-003-multi-function-outdoor-digital-sport-watch-w-pedometer-gps-compass-backlight-navy-354517#.V6rwr6Isw-o).
The watch reported firmware version E3.628, dated 2014/06/30
(version can be checked from the "Infor" -menu item).

## Disclaimer

The utility worked for my watch and didn't made any harm to my device,
even though I developed the utility with "error-and-try" -method which
obviosly broduces tons of broken requests to the watch.

However, I cannot give any guarantees that the utility works
for you, and do not make any harm to your device. 

*Use at your own risk!*


## Protocol

The protocol between the device have been reverse engineered by analyzing the serial port traffic while using
the "Tracker" software shipped with the watch. In practice the serial communication is deployed as a Virtual COM Port
(VCP), implemented by the
[CP210x USB to UART Bridge VCP Driver](https://www.silabs.com/products/mcu/Pages/USBtoUARTBridgeVCPDrivers.aspx).
The handshaking parameters were: baudrate=57600, databits=8, parity=no, stop-bits=1.

To find out more about the protocol, please refer to sources.
