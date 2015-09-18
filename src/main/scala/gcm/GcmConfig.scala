package gcm

import akka.actor.{ ActorRef, ActorSystem }

/**
 *
 * @param apiKey
 * @param senderId
 * @param listener
 * @param system
 * @param testing Only supported for XMPP connections
 */
case class GcmConfig(
  apiKey: String,
  senderId: String,
  listener: Option[ActorRef] = None,
  system: Option[ActorSystem] = None,
  testing: Boolean = false
)

