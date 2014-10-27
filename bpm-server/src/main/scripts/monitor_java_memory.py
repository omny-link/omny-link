#!/usr/bin/python
import os.path
import sys

f = sys.argv[1]
if os.path.isfile(f):
  print("Detected low memory condition in tomcat, restarting")
  from subprocess import call
  ok = call(["service", "tomcat7", "restart"])
  if ok == 0: 
    with open("/var/log/tomcat7/catalina.out", "a") as logFile:
      logFile.write("ERROR: Restart due to low memory")
    os.remove(f)