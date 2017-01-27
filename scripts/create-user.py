#!/usr/bin/env python

import requests

API_ROOT = "http://localhost:9000/api"

if __name__ == "__main__":
    # Create
    r = requests.post("{}/users".format(API_ROOT))
    r.raise_for_status()
    user_id = r.json()

    # Get registration code
    r = requests.get("{}/users/{}".format(API_ROOT, user_id))
    r.raise_for_status()
    code = r.json()["registrationCode"]

    print("Created user with ID {} and code {}".format(user_id, code))