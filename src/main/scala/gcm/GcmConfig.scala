package gcm

import akka.actor.{ ActorRef, ActorSystem }

/**
 *
 * @param apiKey
 * @param senderId Google project number
 * @param listener For upstream messages. Should receive [[gcm.xmpp.Status]]. Only for XMPP.
 * @param system Created by HttpGcm if not provided
 * @param testing Only supported for XMPP connections
 */
case class GcmConfig(
  apiKey: String,
  senderId: String,
  listener: Option[ActorRef] = None,
  system: Option[ActorSystem] = None,
  testing: Boolean = false
)

