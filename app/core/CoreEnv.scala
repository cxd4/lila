package lila
package core

import com.mongodb.casbah.MongoConnection
import com.mongodb.{ Mongo, MongoOptions, ServerAddress ⇒ MongoServer }

import akka.actor._

import play.api.libs.concurrent._
import play.api.Application

import ui._

final class CoreEnv private(application: Application, settings: Settings) {

  implicit val app = application
  import settings._

  lazy val i18n = new lila.i18n.I18nEnv(
    app = app,
    settings = settings)

  lazy val user = new lila.user.UserEnv(
    settings = settings,
    mongodb = mongodb.apply _)

  lazy val lobby = new lila.lobby.LobbyEnv(
    app = app,
    settings = settings,
    mongodb = mongodb.apply _,
    userRepo = user.userRepo,
    gameRepo = game.gameRepo,
    gameSocket = game.socket,
    gameMessenger = game.messenger,
    entryRepo = timeline.entryRepo,
    ai = ai.ai)

  lazy val setup = new lila.setup.SetupEnv(
    settings = settings,
    mongodb = mongodb.apply _)

  lazy val timeline = new lila.timeline.TimelineEnv(
    settings = settings,
    mongodb = mongodb.apply _)

  lazy val ai = new lila.ai.AiEnv(
    settings = settings)

  lazy val game = new lila.game.GameEnv(
    app = app,
    settings = settings,
    mongodb = mongodb.apply _,
    userRepo = user.userRepo,
    eloUpdater = user.eloUpdater,
    ai = ai.ai)

  lazy val analyse = new lila.analyse.AnalyseEnv(
    settings = settings,
    gameRepo = game.gameRepo,
    userRepo = user.userRepo)

  lazy val site = new lila.site.SiteEnv(
    app = app,
    settings = settings,
    gameRepo = game.gameRepo)

  lazy val appApi = new AppApi(
    userRepo = user.userRepo,
    gameRepo = game.gameRepo,
    gameSocket = game.socket,
    messenger = game.messenger,
    starter = lobby.starter,
    eloUpdater = user.eloUpdater,
    gameInfo = analyse.gameInfo)

  lazy val mongodb = MongoConnection(
    new MongoServer(MongoHost, MongoPort),
    mongoOptions
  )(MongoDbName)

  // http://stackoverflow.com/questions/6520439/how-to-configure-mongodb-java-driver-mongooptions-for-production-use
  private val mongoOptions = new MongoOptions() ~ { o ⇒
    o.connectionsPerHost = MongoConnectionsPerHost
    o.autoConnectRetry = MongoAutoConnectRetry
    o.connectTimeout = MongoConnectTimeout
    o.threadsAllowedToBlockForConnectionMultiplier = MongoBlockingThreads
  }

  lazy val gameFinishCommand = new command.GameFinish(
    gameRepo = game.gameRepo,
    finisher = game.finisher)

  lazy val gameCleanNextCommand = new command.GameCleanNext(gameRepo = game.gameRepo)
}

object CoreEnv {

  def apply(app: Application) = new CoreEnv(
    app, 
    new Settings(app.configuration.underlying)
  )
}
