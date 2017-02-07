package io.quartic.cartan

import com.google.cloud.pubsub.PubSubOptions
import com.google.cloud.pubsub.SubscriptionInfo
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.websockets.WebsocketBundle
import io.quartic.common.application.ApplicationBase
import io.quartic.common.websocket.WebsocketClientSessionFactory
import io.quartic.common.websocket.WebsocketListener
import io.quartic.common.websocket.serverEndpointConfig
import io.quartic.tracker.api.UserId
import rx.Observable
import rx.internal.util.ObserverSubscriber
import rx.subjects.BehaviorSubject
import javax.websocket.server.ServerEndpointConfig

class CartanApplication : ApplicationBase<CartanConfiguration>() {
    private val websocketBundle = WebsocketBundle(*arrayOf<ServerEndpointConfig>())

    public override fun initializeApplication(bootstrap: Bootstrap<CartanConfiguration>) {
        bootstrap.addBundle(websocketBundle)
    }


    /**
     * Make an observable act like a Behavior(Subject) - hot, and emits the current item on subscription.
     */
    fun <T> likeBehavior() = Observable.Transformer<T, T> { observable ->
        val bs = BehaviorSubject.create<T>()
        observable.subscribe(ObserverSubscriber(bs))
        bs
    }

    public override fun runApplication(configuration: CartanConfiguration, environment: Environment) {
        val catalogueWatcher = CatalogueWatcher(WebsocketListener.Factory(
                configuration.catalogueWatchUrl!!,
                WebsocketClientSessionFactory(javaClass)
        ))

        val pubsub = PubSubOptions.getDefaultInstance().service
        if (pubsub.getSubscription("wat") == null) {
            pubsub.create(SubscriptionInfo.of("tracker.devtest.quartic.io", "wat"))
        }

        val messages = MessageProcessor.messages("wat", pubsub)
        val states = MessageProcessor.states(messages).compose(likeBehavior<Map<UserId, MessageProcessor.Entity>>())
        websocketBundle.addEndpoint(serverEndpointConfig("/ws", WebsocketEndpoint(states)))

        //catalogueWatcher.start()
    }

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            CartanApplication().run(*args)
        }
    }
}
