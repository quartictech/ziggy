# Tracking Android app + backend services

## Running backend services locally

The backend services rely on Google Cloud components - Datastore and PubSub.  There are emulators for
Datastore and PubSub.

In order to run locally:
 
1. Configure Python virtualenv:

    ```
    virtualenv .env --python=`which python3`
    source .env/bin/activate
    pip install -r scripts/requirements.txt
    ```

2. Start and configure the emulators (you may be prompted to install various components the first time through):

    ```
    ./scripts/configure-local-env.sh
    ```
    
   The emulators can be killed via Ctrl+C.
    
3. Start the services:

    ```
    export PUBSUB_EMULATOR_HOST=localhost:10000
    export DATASTORE_EMULATOR_HOST=localhost:11000
    ./gradlew run --parallel    
    ```
    
### API scripts

**Note:** All of these require the above emulator environment variables to be set.

#### Creating a user

```
./scripts/create-user.py
```

This prints the user's ID and code.

#### Registering a user

```
./scripts/register-user.py -u ${USER_ID}
```

This prints the user's ID.

#### Uploading fake data

```
./scripts/register-user.py -u ${USER_ID}
```

You can also specify the number of messages to send with the `-n` flag, and override the JSON message content with the
`-p` flag.


## APK signing

The release APK is signed with a private key maintained in a keystore that's injected via a CircleCI environment
variable (along with passwords).  This avoids this material being held in the same repo as the code itself.  The
script in the `ziggy-secrets` repo sets the following environment variables in CircleCI:

- `ZIGGY_KEYSTORE`
- `ZIGGY_STORE_PASSWORD`
- `ZIGGY_KEY_PASSWORD`


## TODO before this becomes real

### Application ID
 
Need to set the `applicationId` to something sensible - it needs to be the same for the lifetime of the app.

See https://developer.android.com/studio/build/application-id.html for more info.

### Versioning scheme

Note there's both `versionCode` and `versionName` to consider.  Could use the CircleCI build # for the former, unclear
what to do for the latter.  Maybe based on Git tags?

See https://developer.android.com/studio/publish/versioning.html for more info.