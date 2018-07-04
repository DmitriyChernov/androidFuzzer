#!/bin/sh
cd /home/voidgib/diploma/AndroidStudioProjects/Fuzzer/py/
xfce4-terminal -T log -e "bash -c 'python logger.py';bash" \
	--tab -T fuzzIn -e "bash -c 'python tcpRadamsa.py';bash"
