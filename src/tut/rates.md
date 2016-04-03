---
layout: page
title: Instruments and Prices
---

```tut:invisible
import cats.data.Xor
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, Sink}
import com.typesafe.config.ConfigFactory
import rx.oanda.OandaEnvironment._

implicit val system = ActorSystem("rates-tut", ConfigFactory.parseString("akka.loglevel = ERROR"))
implicit val mat = ActorMaterializer()
import system.dispatcher
val practiceEnv = TradePracticeEnvironment("your-token")
val accountId = 133742L
```
## Instruments and Prices

The `RatesClient` provides access to tradable instruments and prices. To get started, we have to create a new instance with our environment.

```tut:book
import rx.oanda.rates._

val ratesClient = new RatesClient(practiceEnv)
```


### Instruments

An [Instrument](http://www.investopedia.com/terms/i/instrument.asp) is either a currency pair, a CFD or a commodity. To get all instruments which are available with our account, we can call `allInstruments` with our account id to get a `Source[Instrument, NotUsed]`, which emits all instruments one by one. Keep in mind that the request itself is executed lazily when we actually run our source connected as graph.

```tut:book
ratesClient.allInstruments(accountId)
```

If we're only interested in specific instruments, there are some more methods available.

```tut:book
ratesClient.instruments(accountId, List("EUR_USD", "EUR_GBP", "GBP_USD"))
ratesClient.instrument(accountId, "EUR_USD")
```

Most of the other methods (also in other clients) will require an instrument code, which can be found at the instrument's field `instrument`.

### Prices

To get the latest price for one or more instruments from Oanda, we can use the `price` or `prices` methods, which will emit the latest known price per instrument as instance of a `Price` and then finishes.

```tut:book
ratesClient.prices(List("EUR_USD", "EUR_GBP", "GBP_USD"))
ratesClient.price("EUR_USD")
```

Another option to receive prices for instruments is to stream them continuously, which is handy if we want to trade on a per tick base. 

```tut:book
ratesClient.livePrices(accountId, List("EUR_USD", "GBP_USD"))
```

As we can see, the resulting return type is `cats.data.Xor[Price, Heartbeat]`. The `Heartbeat`s are provided by Oanda to keep the connection alive. This is useful to monitor the connection and restart it, when nothing was received for 10 seconds or more (more about it in their documentation [here](http://developer.oanda.com/rest-live/streaming/#ratesStreaming) and [here](http://developer.oanda.com/rest-live/streaming/#connections)).


If you are only interested in the prices and want to ignore the heartbeats, here is what you wanna use


```tut:book
ratesClient.livePrices(accountId, List("EUR_USD", "GBP_USD")).flatMapConcat {
  case cats.data.Xor.Left(price) ⇒ Source.single(price)
  case cats.data.Xor.Right(heartbeat) ⇒ Source.empty
}
```

It is also possible to define a session id, since Oanda will disconnect all other streaming connections if 
there are more than two currently open. By providing a session id, only the other open connection with the same id will be closed. 

```tut:book
ratesClient.livePrices(accountId, List("EUR_USD", "GBP_USD"), sessionId = Some("session-42"))
```

The following is another example, how to stream prices for all instruments.

```tut:book
ratesClient.allInstruments(accountId).
    map(_.instrument).
    fold(Vector.empty[String])(_ :+ _).
    flatMapConcat(ratesClient.livePrices(accountId, _)).
    flatMapConcat {
      case Xor.Left(price) ⇒ Source.single(price)
      case Xor.Right(heartbeat) ⇒ Source.empty
    }
```

```tut:invisible
import akka.http.scaladsl.Http
Http().shutdownAllConnectionPools.onComplete(_ => system.terminate())
```