package iot

import akka.actor.ActorSystem
import akka.cluster.{Cluster, MemberStatus}
import akka.testkit.{ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import scala.concurrent.duration._

object SensorActorSpec {
  val profile: String = sys.props.getOrElse("iot.profile", "Standard")
  require(Set("Standard", "Cluster").contains(profile))
  val config = ConfigFactory.parseString(
    if (profile == "Cluster") """
      akka.actor.provider = cluster
      akka.remote.artery.canonical.hostname = "127.0.0.1"
      akka.remote.artery.canonical.port = 0
      akka.cluster.jmx.multi-mbeans-in-same-jvm = on
    """ else "akka.actor.provider = local"
  ).withFallback(ConfigFactory.load())
}

final class SensorActorSpec
    extends TestKit(ActorSystem("sensor-test", SensorActorSpec.config))
    with ImplicitSender with AnyWordSpecLike with Matchers with BeforeAndAfterAll {
  import SensorActor._
  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)
  "SensorActor" should {
    "start with no reading" in {
      val actor = system.actorOf(SensorActor.props)
      actor ! GetLatest
      expectMsg(Latest(None))
    }
    "acknowledge a valid temperature" in {
      val actor = system.actorOf(SensorActor.props)
      actor ! Record(23.5)
      val expected = if (sys.props.get("demo.fail").contains("true")) 99.0 else 23.5
      expectMsg(Accepted(expected))
    }
    "retain the latest reading and reject invalid readings" in {
      val actor = system.actorOf(SensorActor.props)
      actor ! Record(20.0)
      expectMsg(Accepted(20.0))
      for (invalid <- Seq(Double.NaN, Double.PositiveInfinity, -61.0, 121.0)) {
        actor ! Record(invalid)
        expectMsgType[Rejected]
      }
      actor ! GetLatest
      expectMsg(Latest(Some(20.0)))
    }
    if (SensorActorSpec.profile == "Cluster") {
      "join a real single-node cluster" in {
        val cluster = Cluster(system)
        cluster.join(cluster.selfAddress)
        awaitAssert(cluster.selfMember.status shouldBe MemberStatus.Up, 20.seconds, 200.millis)
      }
    }
  }
}
