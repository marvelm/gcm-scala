package gcm

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.handler.ssl.{SslContext, SslContextBuilder}

class XMPP(config: GCMConfig) {
  val sslCtx = SslContextBuilder.forClient()
    .trustManager(InsecureTrustManagerFactory.INSTANCE)
    .build()

  val group = new NioEventLoopGroup()
  try {
    val b = new Bootstrap()
    b.group(group)
      .channel(classOf[NioSocketChannel])
      .handler(new XMPPInitializer(sslCtx, config))

    val channel = b.connect(config.host, config.port).sync().channel()
    channel.writeAndFlush(
        <stream:stream to="gcm.googleapis.com"
                       version="1.0"
                       from={config.apiKey + "@gcm.googleapis.com"}
                       xmlns="jabber:client"
                       xmlns:stream="http://etherx.jabber.org/streams"
                       xml:lang="en"/>.toString()
    )
  } catch {
    case e: Throwable => e.printStackTrace()
  }
}

import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.string.{StringDecoder, StringEncoder}
import io.netty.handler.codec.xml.XmlFrameDecoder

class XMPPInitializer(sslCtx: SslContext, config: GCMConfig) extends ChannelInitializer[SocketChannel] {
  override def initChannel(ch: SocketChannel) {
    val pipeline = ch.pipeline()
    pipeline.addLast(sslCtx.newHandler(ch.alloc(), config.host, config.port))

    pipeline.addLast("framer", new XmlFrameDecoder(10000))
    pipeline.addLast("decoder", new StringDecoder)
    pipeline.addLast("encoder", new StringEncoder)

    pipeline.addLast("handler", new XMPPHandler(config))
  }
}

import java.util.Base64
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import scala.xml.XML

// TODO Replace with ChannelInboundHandler
class XMPPHandler(config: GCMConfig) extends SimpleChannelInboundHandler[String] {
  var initialized = false
  // set to true when we receive response from GCM
  var auth = false
  var id: String = ""

  override def channelRead0(ctx: ChannelHandlerContext, msg: String) {
    val xml = XML.loadString(msg)
    println(msg)
    if (!initialized) {
      // To initialize the connection, we must echo the reply
      // The reply contains an ID.
      // Not sure what it's used for
      id = xml \@ "id"
      ctx.writeAndFlush(msg)
      config.listener ! xml
      initialized = true
    } else if (!auth) {
      if (xml.label == "success") {
        auth = true
      } else {
        val encoded = Base64.getEncoder.encodeToString(
          s"""\x00${config.senderId}\x00${config.apiKey}""".getBytes("UTF-8")
        )

        ctx.writeAndFlush(
          <auth mechanism="PLAIN" xmlns="urn:ietf:params:xml:ns:xmpp-sasl">
            {encoded}
          </auth>.toString()
        )
        config.listener ! xml
      }
    } else {
      config.listener ! xml
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    cause.printStackTrace()
  }
}
