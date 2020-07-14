package com.example.shoppingcart.impl

import com.lightbend.lagom.scaladsl.persistence.AggregateEventTag
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.slick.SlickReadSide
import slick.dbio.DBIOAction
import akka.Done
import com.example.shoppingcart.impl.ShoppingCart._

import scala.util.Random

import play.api.Mode
import play.api.Environment

class ShoppingCartReportProcessor(readSide: SlickReadSide, repository: ShoppingCartReportRepository, environment: Environment)
    extends ReadSideProcessor[Event] {

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[Event] =
    readSide
      .builder[Event]("shopping-cart-report")
      .setGlobalPrepare(repository.createTable())
      .setEventHandler[ItemAdded] { envelope =>
        repository.createReport(envelope.entityId)
      }
      .setEventHandler[ItemRemoved] { envelope =>
        DBIOAction.successful(Done) // not used in report
      }
      .setEventHandler[ItemQuantityAdjusted] { envelope =>
        DBIOAction.successful(Done) // not used in report
      }
      .setEventHandler[CartCheckedOut] { envelope =>
        // This is not part of a real application, but we are adding it here to show
        // how Lightbend Telemetry handles failures on Lagom's read-side processors.
        if (Random.nextInt(5) == 0 && environment.mode != Mode.Test) throw new RuntimeException("Sometimes event handling a checkout fails.")
        repository.addCheckoutTime(envelope.entityId, envelope.event.eventTime)
      }
      .build()

  override def aggregateTags: Set[AggregateEventTag[Event]] = Event.Tag.allTags
}
