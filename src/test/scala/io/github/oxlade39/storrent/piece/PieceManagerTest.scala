package io.github.oxlade39.storrent.piece

import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.ActorSystem
import org.scalatest.matchers.MustMatchers
import io.github.oxlade39.storrent.peer.{Bitfield, PeerId}

/**
 * @author dan
 */
class PieceManagerTest extends TestKit(ActorSystem("PeerConnectionTest"))
with WordSpecLike with BeforeAndAfterAll with ImplicitSender with MustMatchers {
  import io.github.oxlade39.storrent.test.util.Files._
  import PieceManager._

  "PieceManager" should {
    "By default there are no pieces" in {

      val pieceManager = system.actorOf(PieceManager.props(ubuntuTorrent))
      pieceManager ! GetPeerPieceMappings

      val mappings = expectMsgType[PeerPieceMappings]
      mappings.pieceCounts mustEqual Map.empty[Int, List[PeerId]]
      mappings.global mustEqual Pieces(ubuntuTorrent.pieceCount)
    }

    "keeps track of which peers have which pieces" in {
      val peerOne, peerTwo = PeerId()
      val pieceManager = system.actorOf(PieceManager.props(ubuntuTorrent))

      pieceManager ! PeerHasPieces(peerOne, Bitfield(ubuntuTorrent.pieceHashes.map(_ => false)).set(2))
      pieceManager ! PeerHasPieces(peerTwo, Bitfield(ubuntuTorrent.pieceHashes.map(_ => false)).set(23).set(2))
      pieceManager ! GetPeerPieceMappings

      val mappings = expectMsgType[PeerPieceMappings]

      mappings.pieceCounts mustEqual Map(
        2 -> Set(peerOne, peerTwo),
        23 -> Set(peerTwo)
      )
      mappings.global mustEqual Pieces(ubuntuTorrent.pieceCount, Set(2, 23))
    }
  }
}

object PieceManagerTest {

}

class PeerPieceMappingsTest extends WordSpecLike with MustMatchers {
  import PieceManager._

  "PeerPieceMappings" must {
    "append" in {
      val mappings =
        PeerPieceMappings(Pieces(10)) ++
          (PeerId("has 0"), Pieces(10, Set(0))) ++
          (PeerId("has 5"), Pieces(10, Set(5))) ++
          (PeerId("has 9 and 0"), Pieces(10, Set(9, 0)))

      mappings mustEqual PeerPieceMappings(
        Pieces(10, Set(0,5,9)),
        Map(
          0 -> Set(PeerId("has 0"), PeerId("has 9 and 0")),
          5 -> Set(PeerId("has 5")),
          9 -> Set(PeerId("has 9 and 0"))
        )
      )
    }

    "provide rarest pieces" in {
      val mappings =
        PeerPieceMappings(Pieces(10)) ++
          (PeerId("0"), Pieces(10, Set(0, 1, 2, 3))) ++
          (PeerId("1"), Pieces(10, Set(0, 1, 2))) ++
          (PeerId("2"), Pieces(10, Set(0, 1))) ++
          (PeerId("3"), Pieces(10, Set(0)))

      mappings.rarest mustEqual Seq(
        (3, Set(PeerId("0"))),
        (2, Set(PeerId("0"), PeerId("1"))),
        (1, Set(PeerId("0"), PeerId("1"), PeerId("2"))),
        (0, Set(PeerId("0"), PeerId("1"), PeerId("2"), PeerId("3")))
      )
    }
  }
}