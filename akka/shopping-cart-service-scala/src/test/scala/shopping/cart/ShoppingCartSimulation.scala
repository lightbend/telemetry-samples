package shopping.cart

import java.util.UUID

import com.github.phisgr.gatling.grpc.Predef._
import com.github.phisgr.gatling.pb._
import io.gatling.core.Predef._
import io.grpc._

import scala.concurrent.duration._
import scala.util.Random

class ShoppingCartSimulation extends Simulation {

  // Random data feeder:
  // See https://gatling.io/docs/current/session/feeder/
  val shoppingCardFeeder = Iterator.continually[Map[String, _]](
    Map(
      "cartId" -> UUID.randomUUID().toString,
      "itemId" -> UUID.randomUUID().toString,
      "quantity" -> (Random.nextInt(5) + 1), // + 1 to avoid 0 quantity
      "newQuantity" -> (Random.nextInt(10) + 1)))

  // Use plain text requests since we are running the tests locally
  val grpcProtocol = grpc(
    ManagedChannelBuilder.forAddress("localhost", 8101).usePlaintext())

  val shoppingCartScenario = {

    val openCart = grpc("Add first item to cart")
      .rpc(proto.ShoppingCartServiceGrpc.METHOD_ADD_ITEM)
      .payload(
        proto
          .AddItemRequest()
          .updateExpr(
            _.cartId :~ $("cartId"),
            _.itemId :~ $("itemId"),
            _.quantity :~ $("quantity")))
      .extract(cart => Some(cart.checkedOut))(_.is(false))

    val updateItemQuantity = grpc("Update item quantity")
      .rpc(proto.ShoppingCartServiceGrpc.METHOD_UPDATE_ITEM)
      .payload(
        proto
          .UpdateItemRequest()
          .updateExpr(
            _.cartId :~ $("cartId"),
            _.itemId :~ $("itemId"),
            _.quantity :~ $("newQuantity")))
      .extract(cart => Some(cart.checkedOut))(_.is(false))

    val getCart = grpc("Get Shopping Cart")
      .rpc(proto.ShoppingCartServiceGrpc.METHOD_GET_CART)
      .payload(proto.GetCartRequest().updateExpr(_.cartId :~ $("cartId")))
      .extract(cart => Some(cart.checkedOut))(_.is(false))

    val checkoutCart = grpc("Checkout Shopping Cart")
      .rpc(proto.ShoppingCartServiceGrpc.METHOD_CHECKOUT)
      .payload(proto.CheckoutRequest().updateExpr(_.cartId :~ $("cartId")))
      .extract(cart => Some(cart.checkedOut))(_.is(true))

    scenario("Shopping Cart Scenario")
      .feed(shoppingCardFeeder)
      .exec(openCart)
      .exec(updateItemQuantity)
      .exec(getCart)
      .exec(checkoutCart)
  }

  setUp(shoppingCartScenario.inject(rampUsers(1).during(1.seconds)))
    .protocols(grpcProtocol)
}
