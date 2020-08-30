import sbt.Keys._
import sbt._

object BootstrapPlugin extends AutoPlugin {
  override def trigger = allRequirements
  val bootstrap = taskKey[Unit]("couriser boostrap standalone")
  override lazy val projectSettings = Seq(bootstrap := {
    publishLocal.value;
    import sys.process._
    val logger = sbt.Keys.streams.value.log
    val res =
      s"coursier bootstrap --standalone ${organization.value}::${name.value}:${version.value} -f -o ${name.value}" !;
    if (res == 0)
      logger.info(s"to run simply enter `./${name.value}`")
    else
      sys.error("boostrap failed!")
  })
}
