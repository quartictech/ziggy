## TODO before this comes real

- Set the `applicationId` to something sensible (it needs to be the same for the lifetime of the app - see
  https://developer.android.com/studio/build/application-id.html).
- Figure out where keystore + passwords should live.  For now the keystore lives in the repo, and the passwords are
  hardcoded into `build.gradle`.  Potentially the passwords should be injected as env vars by CI.