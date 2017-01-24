#!/usr/bin/env python

import argparse
import json
import requests
from requests.auth import AuthBase

API_ROOT = "http://localhost:9000/api"

class QuarticAuth(AuthBase):
    def __init__(self, user_id):
        self._user_id = user_id

    def __call__(self, r):
        r.headers["Authorization"] = "QuarticAuth userId=\"{}\", signature=\"1234\"".format(self._user_id)
        return r

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Upload user sensor data.")
    parser.add_argument("-u", "--user-id", required=True, help="ID of registered user")
    parser.add_argument("-n", "--num-messages", default=1, help="Number of duplicate messages")
    parser.add_argument("payload", help="JSON-encoded paylod")
    args = parser.parse_args()

    payload = json.loads(args.payload)

    for i in range(int(args.num_messages)):
        r = requests.post("{}/upload".format(API_ROOT), auth=QuarticAuth(args.user_id), json=payload)
        r.raise_for_status()

    print("Uploaded payload")