package com.axtstar.slackScalaSample

import java.util

import slack.api.{BlockingSlackApiClient}
import slack.rtm.SlackRtmClient
import akka.actor.ActorSystem
import slack.models.{Message, UserTyping}

import scala.concurrent.ExecutionContextExecutor


object Client extends App {

  case class CommandLineArgs(
                              token: String = "",
                              credentialPath: String ="",
                              spreadsheetId:String="",
                              range:String = ""
  )

  // slackに対してメッセージを送信
  def postMessage(
                   client: BlockingSlackApiClient,
                   channel: String, text: String,
                   username: Option[String])
                 (implicit ec: ExecutionContextExecutor) = {
    client.postChatMessage(
      channel,
      text, //text
      username, //userName
      None, //asUser
      None, //parse
      None, //linkNames
      None, //attachments
      None, //unfurlLinks
      None, //unfurlMedia
      None, //iconUrl
      None //iconEmoji
    )

  }


  override def main(args: Array[String]): Unit = {

    //command line args parser
    val parser = new scopt.OptionParser[CommandLineArgs]("slack api client") {
      opt[String]('t', "token") action { (x, c) =>
        c.copy(token = x)
      } text ("t | token is slack's token")

      opt[String]('c', "credential") action { (x, c) =>
        c.copy(credentialPath = x)
      } text ("c | credential is G suite OAUTH")

      opt[String]('s', "spreadsheetId") action { (x, c) =>
        c.copy(spreadsheetId = x)
      } text ("s | spreadsheetId is spread key")

      opt[String]('r', "range") action { (x, c) =>
        c.copy(range = x)
      } text ("r | range is sheet's range")

    }

    val p = parser.parse(args, CommandLineArgs()).get

    implicit val system = ActorSystem("slack")
    implicit val ec = system.dispatcher

    val client = SlackRtmClient(p.token)
    val selfId = client.state.self.id

    client.onEvent{ event=>
      event match{
        case x:UserTyping =>
        case x:Message =>
          Console.println(event)
        case _ =>
          Console.println(event)

      }
    }

    client.onMessage { message =>

      if (message.text=="bot ping") {
        postMessage(client.apiClient,
          message.channel,
          s"pong",
          Option("G Suite bot") //userName
        )
      }

      else if(message.text=="bot check"){
        val tanto = GoogleUtils.apply.getValueToSheet(
          p.credentialPath,
          p.spreadsheetId,
          p.range) match {
          case x:util.List[util.List[Object]] => x.get(0).get(0)
        }
        postMessage(client.apiClient,
          message.channel,
          s"今日の日直は${tanto}です。",
          Option("G Suite bot"))
      }
    }
  }
}
