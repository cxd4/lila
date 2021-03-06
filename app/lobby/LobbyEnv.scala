package lila
package lobby

import com.mongodb.casbah.MongoCollection

import akka.actor._

import play.api.libs.concurrent._
import play.api.Application
import play.api.i18n.Lang
import play.api.i18n.MessagesPlugin

import user.UserRepo
import game.{ GameRepo, Socket ⇒ GameSocket, Messenger ⇒ GameMessenger }
import timeline.EntryRepo
import ai.Ai
import core.Settings

final class LobbyEnv(
    app: Application,
    settings: Settings,
    mongodb: String ⇒ MongoCollection,
    userRepo: UserRepo,
    gameRepo: GameRepo,
    gameSocket: GameSocket,
    gameMessenger: GameMessenger,
    entryRepo: EntryRepo,
    ai: () ⇒ Ai) {

  implicit val ctx = app
  import settings._

  lazy val starter = new Starter(
    gameRepo = gameRepo,
    entryRepo = entryRepo,
    ai = ai,
    socket = socket)

  lazy val history = new History(timeout = LobbyMessageLifetime)

  lazy val messenger = new Messenger(
    messageRepo = messageRepo,
    userRepo = userRepo)

  lazy val hub = Akka.system.actorOf(Props(new Hub(
    messenger = messenger,
    history = history,
    timeout = SiteUidTimeout
  )), name = ActorLobbyHub)

  lazy val socket = new Socket(hub = hub)

  lazy val preloader = new Preload(
    fisherman = fisherman,
    history = history,
    hookRepo = hookRepo,
    gameRepo = gameRepo,
    messageRepo = messageRepo,
    entryRepo = entryRepo)

  lazy val fisherman = new Fisherman(
    hookRepo = hookRepo,
    hookMemo = hookMemo,
    socket = socket)

  lazy val messageRepo = new MessageRepo(
    collection = mongodb(MongoCollectionMessage),
    max = LobbyMessageMax)

  lazy val api = new Api(
    hookRepo = hookRepo,
    fisherman = fisherman,
    gameRepo = gameRepo,
    gameSocket = gameSocket,
    gameMessenger = gameMessenger,
    starter = starter)

  lazy val hookRepo = new HookRepo(mongodb(MongoCollectionHook))

  lazy val hookMemo = new HookMemo(timeout = MemoHookTimeout)
}
