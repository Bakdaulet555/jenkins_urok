package iot

import akka.actor.{Actor, Props}

object SensorActor {
  final case class Record(temperature: Double)
  final case class Accepted(temperature: Double)
  final case class Rejected(reason: String)
  case object GetLatest
  final case class Latest(value: Option[Double])
  def props: Props = Props(new SensorActor)
}

final class SensorActor extends Actor {
  import SensorActor._
  private var latest: Option[Double] = None
  def receive: Receive = {
    case Record(value) if value.isNaN || value.isInfinity || value < -60 || value > 120 =>
      sender() ! Rejected("Temperature must be finite and between -60 and 120 C")
    case Record(value) =>
      latest = Some(value)
      sender() ! Accepted(value)
    case GetLatest => sender() ! Latest(latest)
  }
}
