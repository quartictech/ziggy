#!/usr/bin/env bash
set -eu

gcloud beta emulators pubsub start --host-port=localhost:10000 &
pid[0]=$!
gcloud beta emulators datastore start --host-port=localhost:11000 &
pid[1]=$!
trap "kill ${pid[0]} ${pid[1]}; exit 1" EXIT

sleep 10

export PUBSUB_EMULATOR_HOST=localhost:10000
export DATASTORE_EMULATOR_HOST=localhost:11000
./scripts/configure-emulators.py

wait
