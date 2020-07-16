package com.example.shoppingcart.impl;

import com.google.common.collect.ImmutableMap;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.jpa.JpaReadSide;
import org.pcollections.PSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.util.Random;

import play.Environment;

public class ShoppingCartReportProcessor extends ReadSideProcessor<ShoppingCartEntity.Event> {

    final private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JpaReadSide jpaReadSide;
    private final Environment environment;

    @Inject
    public ShoppingCartReportProcessor(JpaReadSide jpaReadSide, Environment environment) {
        this.jpaReadSide = jpaReadSide;
        this.environment = environment;
    }

    @Override
    public ReadSideHandler<ShoppingCartEntity.Event> buildHandler() {
        return jpaReadSide.<ShoppingCartEntity.Event>builder("shopping-cart-report").setGlobalPrepare(this::createSchema)
                .setEventHandler(ShoppingCartEntity.ItemAdded.class, this::createReport)
                .setEventHandler(ShoppingCartEntity.CheckedOut.class, this::addCheckoutTime).build();
    }

    private void createSchema(@SuppressWarnings("unused") EntityManager ignored) {
        Persistence.generateSchema("default", ImmutableMap.of("hibernate.hbm2ddl.auto", "update"));
    }

    private void createReport(EntityManager entityManager, ShoppingCartEntity.ItemAdded evt) {

        logger.debug("Received ItemUpdate event: " + evt);
        if (findReport(entityManager, evt.shoppingCartId) == null) {
            logger.debug("Creating report for CartID: " + evt.shoppingCartId);
            ShoppingCartReport report = new ShoppingCartReport();
            report.setId(evt.shoppingCartId);
            report.setCreationDate(evt.eventTime);
            entityManager.persist(report);
        }
    }

    Random random = new Random();

    private void addCheckoutTime(EntityManager entityManager, ShoppingCartEntity.CheckedOut evt) {
        ShoppingCartReport report = findReport(entityManager, evt.shoppingCartId);

        // This is not part of a real application, but we are adding it here to show
        // how Lightbend Telemetry handles failures on Lagom's read-side processors.
        // Not failing when running tests to that we can have a stable build.
        if (random.nextInt(5) == 0 && !environment.isTest())
            throw new RuntimeException("Sometimes event handling a checkout fails.");

        logger.debug("Received CheckedOut event: " + evt);
        if (report != null) {
            logger.debug("Adding checkout time (" + evt.eventTime + ") for CartID: " + evt.shoppingCartId);
            report.setCheckoutDate(evt.eventTime);
            entityManager.persist(report);
        } else {
            throw new RuntimeException("Didn't find cart for checkout. CartID: " + evt.shoppingCartId);
        }
    }

    private ShoppingCartReport findReport(EntityManager entityManager, String cartId) {
        return entityManager.find(ShoppingCartReport.class, cartId);
    }

    @Override
    public PSequence<AggregateEventTag<ShoppingCartEntity.Event>> aggregateTags() {
        return ShoppingCartEntity.Event.TAG.allTags();
    }

}
