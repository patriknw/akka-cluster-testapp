package akka.ops

import com.amazonaws.services.opsworks.AWSOpsWorksClient
import com.amazonaws.services.opsworks.model.DescribeInstancesRequest
import scala.collection.JavaConverters._
import java.io.File
import java.io.PrintWriter
import java.io.IOException

object MultiNodeHosts {

  def main(args: Array[String]): Unit = {
    val client = new AWSOpsWorksClient

    val req = (new DescribeInstancesRequest).withAppId("MyApp")
    val result = client.describeInstances(req)
    val instances = result.getInstances.asScala.toVector

    val file = new File("multi-node-test.hosts")
    val writer: PrintWriter = new PrintWriter(file)
    try {
      instances foreach { instance ⇒
        writer.println(instance.getPrivateIp)
      }
    } finally {
      if (writer ne null) try writer.close() catch {
        case e: IOException ⇒ // ignore
      }
    }

  }

}