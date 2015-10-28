# gcm-scala

[![Build Status](https://travis-ci.org/marvelm/gcm-scala.svg?branch=master)](https://travis-ci.org/marvelm/gcm-scala)

## A Scala client library for Google Cloud Messaging

`gcm-scala` provides two ways to interact with GCM: `SmackGcm` and `HttpGcm`.
The primary difference between the two is that the `SmackGcm` class communicates with GCM via XMPP which allows
for the bidirectional flow of notifications.
If you need to occasionally send a one-off push notification, you should use `HttpGcm`.

If you're constantly sending push notifications, `SmackGcm` might result in better performance because
it doesn't repeatedly open and close sockets like `HttpGcm`.
Don't create too many instances of `SmackGcm`. The underlying library, "Smack", creates a few threads for
each connection to an XMPP server. I'm working on a possible solution for this in the "netty" branch.
Additionally, Google imposes a 1000 connection limit per sender id.

### An example of `HttpGcm`
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
