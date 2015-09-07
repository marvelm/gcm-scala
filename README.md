# gcm-scala

## A Scala client library for Google Cloud Messaging

`gcm-scala` provides two ways to interact with GCM: `GcmSmack` and `GcmHttp`.
The primary difference between the two is that the `GcmSmack` class communicates with GCM via XMPP which allows
for the bidirectional flow of notifications. If you need to just send push notifications from the server to mobile clients, you should use `GcmHttp`.

### An example of `GcmHttp`
```scala
val config = GcmConfig(
  "ApiKey",
  "SenderId"
)
val gcmClient = gcm.GcmHttp(config)
gcmClient.send(gcm.SendToSync("registration id"))
```
