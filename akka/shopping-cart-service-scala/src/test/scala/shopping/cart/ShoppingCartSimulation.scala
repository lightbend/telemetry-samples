package shopping.cart

import java.util.UUID

import akka.grpc.internal.Marshaller
import com.github.phisgr.gatling.grpc.Predef._
import com.github.phisgr.gatling.pb._
import io.gatling.core.Predef._
import io.grpc.{ ManagedChannelBuilder, MethodDescriptor }
import shopping.cart.proto.ShoppingCartService.Serializers.{
  AddItemRequestSerializer,
  CartSerializer,
  CheckoutRequestSerializer,
  GetCartRequestSerializer,
  UpdateItemRequestSerializer
}

import scala.concurrent.duration._
import scala.util.Random

object ShoppingCartServiceMethodDescriptors {
  val AddItem: MethodDescriptor[
    shopping.cart.proto.AddItemRequest,
    shopping.cart.proto.Cart] =
    MethodDescriptor
      .newBuilder()
      .setType(MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(MethodDescriptor
        .generateFullMethodName("shoppingcart.ShoppingCartService", "AddItem"))
      .setRequestMarshaller(new Marshaller(AddItemRequestSerializer))
      .setResponseMarshaller(new Marshaller(CartSerializer))
      .setSampledToLocalTracing(true)
      .build()

  val UpdateItem: MethodDescriptor[
    shopping.cart.proto.UpdateItemRequest,
    shopping.cart.proto.Cart] =
    MethodDescriptor
      .newBuilder()
      .setType(MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(
        MethodDescriptor.generateFullMethodName(
          "shoppingcart.ShoppingCartService",
          "UpdateItem"))
      .setRequestMarshaller(new Marshaller(UpdateItemRequestSerializer))
      .setResponseMarshaller(new Marshaller(CartSerializer))
      .setSampledToLocalTracing(true)
      .build()

  val GetCart: MethodDescriptor[
    shopping.cart.proto.GetCartRequest,
    shopping.cart.proto.Cart] =
    MethodDescriptor
      .newBuilder()
      .setType(MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(MethodDescriptor
        .generateFullMethodName("shoppingcart.ShoppingCartService", "GetCart"))
      .setRequestMarshaller(new Marshaller(GetCartRequestSerializer))
      .setResponseMarshaller(new Marshaller(CartSerializer))
      .setSampledToLocalTracing(true)
      .build()

  val Checkout: MethodDescriptor[
    shopping.cart.proto.CheckoutRequest,
    shopping.cart.proto.Cart] =
    MethodDescriptor
      .newBuilder()
      .setType(MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(MethodDescriptor
        .generateFullMethodName("shoppingcart.ShoppingCartService", "Checkout"))
      .setRequestMarshaller(new Marshaller(CheckoutRequestSerializer))
      .setResponseMarshaller(new Marshaller(CartSerializer))
      .setSampledToLocalTracing(true)
      .build()

}

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
      .rpc(ShoppingCartServiceMethodDescriptors.AddItem)
      .payload(
        proto
          .AddItemRequest()
          .updateExpr(
            _.cartId :~ $("cartId"),
            _.itemId :~ $("itemId"),
            _.quantity :~ $("quantity")))
      .extract(cart => Some(cart.checkedOut))(_.is(false))

    val updateItemQuantity = grpc("Update item quantity")
      .rpc(ShoppingCartServiceMethodDescriptors.UpdateItem)
      .payload(
        proto
          .UpdateItemRequest()
          .updateExpr(
            _.cartId :~ $("cartId"),
            _.itemId :~ $("itemId"),
            _.quantity :~ $("newQuantity")))
      .extract(cart => Some(cart.checkedOut))(_.is(false))

    val getCart = grpc("Get Shopping Cart")
      .rpc(ShoppingCartServiceMethodDescriptors.GetCart)
      .payload(proto.GetCartRequest().updateExpr(_.cartId :~ $("cartId")))
      .extract(cart => Some(cart.checkedOut))(_.is(false))

    val checkoutCart = grpc("Checkout Shopping Cart")
      .rpc(ShoppingCartServiceMethodDescriptors.Checkout)
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
