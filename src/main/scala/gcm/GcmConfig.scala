package gcm

import akka.actor.{ ActorRef, ActorSystem }

case class GcmConfig(
  apiKey: String,
  senderId: String,
  listener: Option[ActorRef] = None,
  system: Option[ActorSystem] = None,
  testing: Boolean = false
)

