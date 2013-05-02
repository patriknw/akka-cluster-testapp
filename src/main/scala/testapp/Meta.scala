package testapp

import org.slf4j.LoggerFactory
import scala.collection.immutable
import com.amazonaws.services.opsworks.AWSOpsWorksClient
import com.amazonaws.services.opsworks.model.DescribeInstancesRequest
import com.amazonaws.services.opsworks.model.{ Instance ⇒ OpsInstance }
import com.amazonaws.services.ec2.AmazonEC2Client
import scala.collection.JavaConverters._
import com.amazonaws.services.ec2.model.{ Instance ⇒ EC2Instance }

object Meta {

  val log = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {

    val opsMeta = (opsInstances map { i ⇒
      s"""ID: ${i.getInstanceId} (${i.getEc2InstanceId}) => ${i.getStatus} ${i.getPrivateDns()} ${i.getHostname} == ${i.getPrivateIp}"""
    })

    log.info("OpsWorks meta data:{}", opsMeta.mkString("\n", "\n", ""))

    //    val ec2Meta = (ec2Instances map { i ⇒
    //      s"""ID: ${i.getInstanceId} => ip: ${i.getPrivateIpAddress}"""
    //    })
    //
    //    log.info("EC2 meta data:{}", ec2Meta.mkString("\n", "\n", ""))

  }

  def opsInstances(): immutable.IndexedSeq[OpsInstance] = {
    try {
      val client = new AWSOpsWorksClient
      val req = (new DescribeInstancesRequest).withStackId("cd3d679d-7b85-49c8-99d0-72bfe88c2c1c")
      //withAppId("eb5d5894-fc55-4251-a100-d677e984d925")
      val result = client.describeInstances(req)
      val instances = result.getInstances.asScala.toVector
      instances
    } catch {
      case e: Exception ⇒
        log.warn("OpsWorks not available, due to: {}", e.getMessage)
        Vector.empty
    }
  }

  def ec2Instances(): immutable.IndexedSeq[EC2Instance] = {
    try {
      import scala.collection.JavaConverters._
      val ec2 = new AmazonEC2Client
      val reservations = ec2.describeInstances.getReservations.asScala.toVector
      val instances = reservations.flatMap(_.getInstances.asScala.toVector)
      instances
    } catch {
      case e: Exception ⇒
        log.warn("EC2 not available, due to: {}", e.getMessage)
        Vector.empty
    }
  }

}