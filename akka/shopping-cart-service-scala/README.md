# Akka Shopping Cart Scala Sample

This is based on the sample code from [Akka Platform Guide](https://github.com/akka/akka-platform-guide).

## Requirements

- [sbt](https://www.scala-sbt.org/1.x/docs/Setup.html)
- [Docker Compose](https://docs.docker.com/compose/install/)
- [grpcurl](https://github.com/fullstorydev/grpcurl#installation)
- [Lightbend account and Bintray credentials](https://developer.lightbend.com/docs/telemetry/current/getting-started/start.html#lightbend-account-and-bintray-credentials)

## How to run locally

1. Start a local Cassandra server on default port 9042 and a Kafka broker on port 9092. The included `docker-compose.yml` starts everything required for running locally.

    ```shell
    docker-compose up -d
    ```

2. Create Cassandra keyspace and tables:

    ```shell
    # creates keyspace and all tables needed for Akka Persistence
    # as well as the offset store table for Akka Projection
    docker exec -i shopping-cart-service-scala_cassandra_1 cqlsh -t < ddl-scripts/create_tables.cql
    ```

    ```shell
    # creates the user defined projection table.
    docker exec -i shopping-cart-service-scala_cassandra_1 cqlsh -t < ddl-scripts/create_user_tables.cql
    ```

3. Start a first node:

    ```shell
    sbt -Dconfig.resource=local1.conf run
    ```

4. (Optional) Start another node with different ports:

    ```shell
    sbt -Dconfig.resource=local2.conf run
    ```

5. (Optional) More can be started:

    ```shell
    sbt -Dconfig.resource=local3.conf run
    ```

6. Check for service readiness

    ```shell
    curl http://localhost:9101/ready
    ```

7. Try it with [grpcurl](https://github.com/fullstorydev/grpcurl):

    ```shell
    # add item to cart
    grpcurl -d '{"cartId":"cart1", "itemId":"socks", "quantity":3}' -plaintext 127.0.0.1:8101 shoppingcart.ShoppingCartService.AddItem

    # get cart
    grpcurl -d '{"cartId":"cart1"}' -plaintext 127.0.0.1:8101 shoppingcart.ShoppingCartService.GetCart

    # update quantity of item
    grpcurl -d '{"cartId":"cart1", "itemId":"socks", "quantity":5}' -plaintext 127.0.0.1:8101 shoppingcart.ShoppingCartService.UpdateItem

    # check out cart
    grpcurl -d '{"cartId":"cart1"}' -plaintext 127.0.0.1:8101 shoppingcart.ShoppingCartService.Checkout

    # get item popularity
    grpcurl -d '{"itemId":"socks"}' -plaintext 127.0.0.1:8101 shoppingcart.ShoppingCartService.GetItemPopularity
    ```

    or same `grpcurl` commands to port 8102 to reach node 2.

## How to run the tests

1. Start Cassandra server, create keyspace and tables as explained above.
2. Run the following command:

    ```shell
    sbt test
    ```

## How to run Gatling tests

To run gatling tests, start the service as described above, and then run the following command:

```shell
sbt gatling:test
```
