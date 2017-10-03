#!/usr/bin/python3
import argparse
import json
import urllib.request
import urllib.parse

# parse args
parser = argparse.ArgumentParser()
parser.add_argument("url", help="api url to call")
parser.add_argument("-u", "--user", required=True,
    help="USER[:PASSWORD]  Server user and password")
parser.add_argument("-X", "--verb", help="specify HTTP verb explicitly (GET and POST are implicit)")
parser.add_argument("-v", "--verbose", help="increase output verbosity",
    action="store_true")
args = parser.parse_args()
usr = args.user[:args.user.index(':')]
pwd = args.user[args.user.index(':')+1:]
 
# login 
loginUrl = args.url[:args.url.index('/', 8)]+'/auth/login'
loginPayload = '{  "username": "'+usr+'", "password": "'+pwd+'" }'
data = loginPayload.encode('utf-8')
headers = {
  "X-Requested-With": "XMLHttpRequest",
  "Content-Type": "application/json",
  "Cache-Control": "no-cache"
}

req = urllib.request.Request(loginUrl, data, headers)
resp = urllib.request.urlopen(req)
respData = resp.read()

token = json.loads(respData.decode("utf-8"))['token']

# make request
headers['X-Authorization'] = 'Bearer '+token

if args.verbose:
  print('connecting to {}'.format(args.url))

req = urllib.request.Request(args.url, headers = headers)
if args.verb:
  req.get_method = lambda: args.verb
resp = urllib.request.urlopen(req)
respData = resp.read()

print(respData.decode('UTF-8'))
