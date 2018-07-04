from socket import *
import thread, datetime, time

def input_thread(L):
    key = raw_input()
    L.append(None)

port = 4444
s = socket(AF_INET, SOCK_DGRAM)
s.bind(("", port))

now = datetime.datetime.now()
dt = now.strftime("_%Y-%m-%d_%H:%M")
filename = "log/fuzzing" + dt + ".log"
f = open(filename, 'w')


print "\n(UDP) Waiting messages on port:", port

L = []
thread.start_new_thread(input_thread, (L,))

try:
	while 1:
		if L: break
		data, addr = s.recvfrom(1024)
		f.write(data)
		ts = time.time()
		print datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S') + " | msg recieved: " + data
except (KeyboardInterrupt, SystemExit):
	s.close()
	f.close()
