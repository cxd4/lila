package lila

import lila.game._
import play.api.data._
import play.api.data.Forms._

import ui.Color

object DataForm {

  val soundForm = Form(single(
    "sound" -> nonEmptyText.verifying(Set("true", "false") contains _)
  ))

  val colorForm = Form(single(
    "color" -> nonEmptyText.verifying(Color contains _)
  ))

  type MoveData = (String, String, Option[String], Option[Int])
  val moveForm = Form(tuple(
    "from" -> nonEmptyText,
    "to" -> nonEmptyText,
    "promotion" -> optional(nonEmptyText),
    "b" -> optional(number)
  ))

  type EntryData = String
  val entryForm = Form(single(
    "entry" -> nonEmptyText
  ))

  type JoinData = (String, String, EntryData)
  val joinForm = Form(tuple(
    "redirect" -> nonEmptyText,
    "messages" -> nonEmptyText,
    "entry" -> nonEmptyText
  ))

  type LobbyJoinData = (MessagesData, EntryData, String, Option[String])
  val lobbyJoinForm = Form(tuple(
    "entry" -> nonEmptyText,
    "messages" -> nonEmptyText,
    "hook" -> nonEmptyText,
    "myHook" -> optional(nonEmptyText)
  ))

  type RematchData = (String, String, EntryData, MessagesData)
  val rematchForm = Form(tuple(
    "whiteRedirect" -> nonEmptyText,
    "blackRedirect" -> nonEmptyText,
    "entry" -> nonEmptyText,
    "messages" -> nonEmptyText
  ))

  private type MessagesData = String
  private val messagesForm = Form(single(
    "messages" -> nonEmptyText
  ))

  type EndData = MessagesData
  val endForm = messagesForm
}
