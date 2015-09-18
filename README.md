# gcm-scala

[![Build Status](https://travis-ci.org/marvelm/gcm-scala.svg?branch=master)](https://travis-ci.org/marvelm/gcm-scala)

## A Scala client library for Google Cloud Messaging

`gcm-scala` provides two ways to interact with GCM: `GcmSmack` and `GcmHttp`.
The primary difference between the two is that the `GcmSmack` class communicates with GCM via XMPP which allows
for the bidirectional flow of notifications. If you need to just send push notifications from the server to mobile clients, you should use `GcmHttp`.

### An example of `GcmHttp`
```scala
import gcm.http.{Messages, HttpGcm}
val config = GcmConfig(
  "ApiKey",
  "SenderId" // AKA project number
)
val client = HttpGcm(config)

// A simple message with no data
client.sendMessage(Messages.sendToSync(to = "APA91bHun4MxP5egoKMwt2KZFBaFUH-1RYqx..."))

// Notification
client.sendMessage(Messages.notification(
  to = "APA91bHun4MxP5egoKMwt2KZFBaFUH-1RYqx...",
  notification = Notification(
    title = "New messages",
    body = Some("You have 9001 new messages"),
  )
))

// A message containing arbitrary data
import org.json4s.jackson.JsonMethods._
val data = parse("""{"someData": 1, "someMoreData": 2}""")
client.sendMessage(Messages.data(
  to = "APA91bHun4MxP5egoKMwt2KZFBaFUH-1RYqx...",
  data = data
))
```
