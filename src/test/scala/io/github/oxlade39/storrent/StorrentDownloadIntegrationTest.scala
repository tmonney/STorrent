package io.github.oxlade39.storrent

import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import akka.testkit.{ImplicitSender, TestKit}
import akka.actor._
import org.scalatest.matchers.MustMatchers
import io.github.oxlade39.storrent.test.util.FileOps
import concurrent.duration._
import akka.io.{IO, Tcp}
import java.net.InetSocketAddress
import scala.concurrent.{ExecutionContext, Future}
import io.github.oxlade39.storrent.piece.PieceManager
import io.github.oxlade39.storrent.peer.PeerManager
import io.github.oxlade39.storrent.piece.PieceManager.PeerPieceMappings
import akka.util.Timeout
import io.github.oxlade39.storrent.peer.PeerManager.ConnectedPeers
import java.util.Date
import java.text.SimpleDateFormat

class StorrentDownloadIntegrationTest extends TestKit(ActorSystem("StorrentDownloadIntegrationTest"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender with MustMatchers with FileOps {

  import StorrentDownloadIntegrationTest._

  override def afterAll(): Unit = {
    system.shutdown()
  }

  "StorrentDownloadIntegrationTest" must {
    "start tracking" ignore {

      val fakePeer = system.actorOf(fakeTcpClient(self))

      val download = system.actorOf(Props(new StorrentDownload("examples" / "ubuntu.torrent")), "download")

      Thread.sleep(2.minutes.toMillis)
    }
  }

}

object StorrentDownloadIntegrationTest {

  val fakeClientAddress = new InetSocketAddress(0)

  def fakeTcpClient(recipient: ActorRef)(implicit sys: ActorSystem) = Props(new FakeClient(recipient))

  class FakeClient(recipient: ActorRef)(implicit sys: ActorSystem) extends Actor {
    IO(Tcp) ! Tcp.Bind(self, fakeClientAddress)

    def receive: Receive = {
      case msg => recipient forward msg
    }
  }

}