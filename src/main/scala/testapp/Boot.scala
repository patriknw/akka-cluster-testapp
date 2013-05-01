package testapp

import scala.collection.immutable
import org.slf4j.LoggerFactory
import com.amazonaws.services.opsworks.AWSOpsWorksClient
import com.amazonaws.services.opsworks.model.DescribeInstancesRequest
import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.cluster.Cluster
import akka.contrib.pattern.ClusterSingletonManager
import akka.kernel.Bootable
import com.amazonaws.auth.InstanceProfileCredentialsProvider

class Boot extends Bootable {
  val log = LoggerFactory.getLogger(getClass)
  var system: ActorSystem = _

  def startup(): Unit = {
    val seedNodes = opsworksNodes
    log.info("seed-nodes=[{}]", seedNodes.mkString(", "))
    val conf =
      if (seedNodes.isEmpty)
        ConfigFactory.load
      else {
        val seedNodesStr = seedNodes.map("akka.tcp://TestApp@" + _ + ":2552").mkString("\"", "\",\"", "\"")
        ConfigFactory.parseString(s"akka.cluster.seed-nodes=[${seedNodesStr}]").
          withFallback(ConfigFactory.load)
      }

    system = ActorSystem("TestApp", conf)
    val cluster = Cluster(system)

    system.actorOf(Props[MemberListener], name = "members")
    system.actorOf(Props[MetricsListener], name = "metrics")

    system.actorOf(ClusterSingletonManager.props(
      singletonProps = _ ⇒ Props[StatsService], singletonName = "singleton",
      terminationMessage = PoisonPill, role = Some("backend")),
      name = "stats")

    if (cluster.selfRoles.contains("frontend"))
      cluster.registerOnMemberUp {
        system.actorOf(Props[StatsClient], "statsClient")
      }

  }

  def opsworksNodes(): immutable.IndexedSeq[String] = {
    try {
      import scala.collection.JavaConverters._
      val client = new AWSOpsWorksClient(new InstanceProfileCredentialsProvider)
      val req = (new DescribeInstancesRequest).withAppId("MyApp")
      val result = client.describeInstances(req)
      val instances = result.getInstances.asScala.toVector
      instances.map(_.getPrivateIp)
    } catch {
      case e: Exception ⇒
        log.warn("OpsWorks not available, due to: {}", e.getMessage)
        Vector.empty
    }
  }

  def shutdown: Unit = {
    if (system ne null) {
      system.shutdown()
      system = null
    }
  }

}

object Main {
  def main(args: Array[String]): Unit = {
    (new Boot).startup()
  }
}

