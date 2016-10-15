from socket import *
import thread, time

def input_thread(L):
    key = raw_input()
    L.append(None)

port = 4444
s = socket(AF_INET, SOCK_DGRAM)
s.bind(("", port))

f = open('fuzzing.log', 'w')

print "Waiting messages on port:", port

L = []
thread.start_new_thread(input_thread, (L,))

while 1:
	if L: break
	data, addr = s.recvfrom(1024)
	f.write(data)
	print "package recieved: " + data

s.close()
f.close()
