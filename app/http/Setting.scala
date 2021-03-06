package lila
package http

import ui.Color
import user.UserRepo

import scalaz.effects._

final class Setting(ctx: Context) extends HttpEnvironment {

  def color = Color(get("color"))

  def color(value: String) = set("color", Color(value).toString) _

  def sound = get("sound", "false").parseBoolean | false

  def sound(value: String) = set("sound", value) _

  private def get(name: String, default: String = ""): String =
    ctx.req.session.get(name) orElse {
      ctx.me flatMap (_ setting name) map (_.toString)
    } getOrElse default

  private def set(name: String, value: String)(userRepo: UserRepo): IO[SessionMap] =
    ctx.me.fold(
      m ⇒ userRepo.saveSetting(m, name, value.toString),
      io()) map { _ ⇒ (s => s + (name -> value.toString)) }
}

object Setting {

  def apply(ctx: Context) = new Setting(ctx)
}
