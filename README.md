# Tracking Android app + backend services

## Running backend services locally

The backend services rely on several Google Cloud components - Datastore, PubSub, and Storage.  There are emulators for
Datastore and PubSub, but not for Storage.  Thus we write to a real cloud bucket, even when running locally.

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


## TODO before this becomes real

### Application ID
 
Need to set the `applicationId` to something sensible - it needs to be the same for the lifetime of the app.

See https://developer.android.com/studio/build/application-id.html for more info.
  
### Signing
 
Figure out where keystore + passwords should live.  For now the keystore lives in the repo, and the passwords are
hardcoded into `build.gradle`.  Potentially the passwords should be injected as env vars by CI.

Current key is generated with:

```
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -dname "O=Quartic Technologies" -alias release-key
```

See https://developer.android.com/studio/publish/app-signing.html for more info.

### Versioning scheme

Note there's both `versionCode` and `versionName` to consider.  Could use the CircleCI build # for the former, unclear
what to do for the latter.  Maybe based on Git tags?

See https://developer.android.com/studio/publish/versioning.html for more info.