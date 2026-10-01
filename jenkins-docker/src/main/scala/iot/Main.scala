package iot

import akka.actor.ActorSystem
import java.nio.charset.StandardCharsets
import org.eclipse.paho.client.mqttv3.{IMqttDeliveryToken, MqttCallback, MqttClient, MqttConnectOptions, MqttMessage}
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

object Main {
  def main(args: Array[String]): Unit = {
    val broker = sys.env.getOrElse("MQTT_BROKER", "tcp://localhost:1883")
    val topic = sys.env.getOrElse("MQTT_TOPIC", "sensors/temperature")
    val system = ActorSystem("iot-ingest")
    val sensor = system.actorOf(SensorActor.props, "sensor")
    val client = new MqttClient(broker, MqttClient.generateClientId(), new MemoryPersistence())
    client.setCallback(new MqttCallback {
      override def connectionLost(cause: Throwable): Unit = system.log.warning("MQTT connection lost: {}", cause.toString)
      override def deliveryComplete(token: IMqttDeliveryToken): Unit = ()
      override def messageArrived(topic: String, message: MqttMessage): Unit = {
        val payload = new String(message.getPayload, StandardCharsets.UTF_8).trim
        payload.toDoubleOption match {
          case Some(value) => sensor ! SensorActor.Record(value)
          case None => system.log.warning("Invalid temperature payload on {}", topic)
        }
      }
    })
    val options = new MqttConnectOptions()
    options.setAutomaticReconnect(true)
    options.setCleanSession(false)
    sys.addShutdownHook {
      if (client.isConnected) client.disconnect()
      client.close()
      system.terminate()
    }
    try {
      client.connect(options)
      client.subscribe(topic, 1)
      system.log.info("Subscribed to {}", topic)
    } catch {
      case ex: Exception =>
        system.log.error(ex, "MQTT startup failed")
        client.close()
        system.terminate()
        throw ex
    }
  }
}
