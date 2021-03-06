#!/usr/bin/env python

import socket, thread, sys, subprocess
import datetime, time

sys.stdin = open('/dev/tty')

def input_thread(L):
	key = raw_input()
	L.append(None)


def bashCall(inp, options):
	cmd = []
	cmd.append("radamsa")
	cmd.extend(options.split())
	p1 = subprocess.Popen(["echo", inp], stdout=subprocess.PIPE)
	p2 = subprocess.Popen(cmd, stdin = p1.stdout,  stdout=subprocess.PIPE)
	return p2.communicate()[0]

# INIT
options = ""
inp = ""

port = 4445
sock = socket.socket()
sock.bind(('', port))
sock.listen(1)

now = datetime.datetime.now()
dt = now.strftime("_%Y-%m-%d_%H:%M")
filename = "log/radamsa" + dt + ".log"
f = open(filename, 'w')

print "\n(TCP) Waiting messages on port:", port

L = []

thread.start_new_thread(input_thread,(L,))
	
while True:
	conn, addr = sock.accept()
	if L: break
	try:
		data = conn.recv(1024)
		if data:
			# Notify that options revieved
			conn.send("ok\r\n")

			if data != "NONE":
				# Recieving radamsa options
				line = "options recieved: " + data + "\n"
				print line
				ts = time.time()
				f.write(datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S') + line)
				options = data
			else:
				# No options
				line = " | no options recieved" + "\n"
				print line
				ts = time.time()
				f.write(datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S') + line)
				options = ""
			# Recieving input
			data = conn.recv(1024)
			if data:
				line = "| message 2 fuzz recieved: " + ':'.join(x.encode('hex') for x in data) + "\n"
				print line
				ts = time.time()
				f.write(datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S') + line) 
				inp = data 
				output = bashCall(inp, options)
			
				# Sending radamsa output to client
				conn.send(output + b"\r\n")
				resLen = len(output)
				if resLen > 20:
					output = output[:20] + '...'
				hexOut = ':'.join(x.encode('hex') for x in output)
				print "fuzzed value: " + hexOut + "\n===============\n"
				ts = time.time()
				f.write(datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S') + "|fuzzed value: " + hexOut + "\n")
				f.write("====\n")
	finally:
		print "finalyzer"
		conn.close()


