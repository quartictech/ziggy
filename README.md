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