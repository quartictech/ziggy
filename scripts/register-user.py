#!/usr/bin/env python

import argparse
import requests

API_ROOT = "http://localhost:9000/api"

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Register user.")
    parser.add_argument("-u", "--user-id", required=True, help="ID of registered user")
    args = parser.parse_args()

    # Get registration code
    r = requests.get("{}/users/{}".format(API_ROOT, args.user_id))
    r.raise_for_status()
    code = r.json()["registrationCode"]

    # Register
    r = requests.post("{}/users/register".format(API_ROOT), json={
        "code": code,
        "publicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+i+pFlvPhzmNyqJJTcyXiQW8mpOe9jaMmCbwFk8DVVhLO5zRN6DZKReznLj3IGV8c22e87ghDp72LGQspBvItA=="
    })
    r.raise_for_status()

    print("Registered user with ID {}".format(args.user_id))