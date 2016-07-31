![banner](banner.jpg)

[![Travis CI](https://img.shields.io/travis/MartinSeeler/rx-oanda/develop.svg?style=flat-square)](https://travis-ci.org/MartinSeeler/rx-oanda)
[![Coveralls branch](https://img.shields.io/coveralls/MartinSeeler/rx-oanda/feature%2Fapi-v20.svg?maxAge=2592000?style=flat-square)](https://coveralls.io/github/MartinSeeler/rx-oanda?branch=feature%2Fapi-v20)
[![Codacy](https://img.shields.io/codacy/c89774a5765442a4922c131cc5c37652.svg?style=flat-square)](https://www.codacy.com/app/MartinSeeler/rx-oanda)
[![Gitter](https://img.shields.io/badge/gitter-Join_Chat-1dce73.svg?style=flat-square)](https://gitter.im/MartinSeeler/rx-oanda)
[![Apache License](https://img.shields.io/badge/license-APACHE_2-green.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0)


## Environments

Oanda provides two different API environments for you, depending on whether you want to use real or fake money.


No matter which one you choose, you need to specifiy the API token for your account, since all endpoints require authentication. 
To obtain your personal token, go to **My Account** -> **My Services** -> **Manage API Access**. 
From there, you can generate a personal access token to use with the Oanda API, as well as revoke a token you may currently have.
                               

Both environments can be found in the following package:

```scala
import rx.oanda.OandaEnvironment._
```


The **fxTrade Practice** environment is used for practicing, as the name suggests. It's a fake money copy of the **fxTrade** environment, 
which means that the market data you receive is real, but the money is not. Use this environment to develop your trading robots / strategies / whatever, 
before you risk your real money. 

```scala
val practiceEnv = TradePracticeEnvironment("your-token")
// practiceEnv: rx.oanda.OandaEnvironment = OandaEnvironment(fxTrade Practice,api-fxpractice.oanda.com,stream-fxpractice.oanda.com,your-token)
```


When you're ready to risk your money and you feel comfortable enough, go ahead and use the **fxTrade** environment. But keep in mind: *you're trading with real money here!*

```scala
val tradeEnv = TradeEnvironment("your-token")
// tradeEnv: rx.oanda.OandaEnvironment = OandaEnvironment(fxTrade,api-fxtrade.oanda.com,stream-fxtrade.oanda.com,your-token)
```


The clients will get the environments as arguments, so they know which endpoints to use. This makes it easy to switch between both environments or to get your strategy ready for real money trading.

## Instruments and Prices

The `RatesClient` provides access to tradable instruments and prices. To get started, we have to create a new instance with our environment.

```scala
import rx.oanda.rates._
// import rx.oanda.rates._

val ratesClient = new RatesClient(practiceEnv)
// ratesClient: rx.oanda.rates.RatesClient = rx.oanda.rates.RatesClient@10b7b517
```


### Instruments

An [Instrument](http://www.investopedia.com/terms/i/instrument.asp) is either a currency pair, a CFD or a commodity. To get all instruments which are available with our account, we can call `allInstruments` with our account id to get a `Source[Instrument, NotUsed]`, which emits all instruments one by one. Keep in mind that the request itself is executed lazily when we actually run our source connected as graph.

```scala
ratesClient.allInstruments(accountId)
// res0: akka.stream.scaladsl.Source[rx.oanda.instruments.Instrument,akka.NotUsed] = akka.stream.scaladsl.Source@332d4686
```

If we're only interested in specific instruments, there are some more methods available.

```scala
ratesClient.instruments(accountId, List("EUR_USD", "EUR_GBP", "GBP_USD"))
// res1: akka.stream.scaladsl.Source[rx.oanda.instruments.Instrument,akka.NotUsed] = akka.stream.scaladsl.Source@49f148be

ratesClient.instrument(accountId, "EUR_USD")
// res2: akka.stream.scaladsl.Source[rx.oanda.instruments.Instrument,akka.NotUsed] = akka.stream.scaladsl.Source@7812324e
```

Most of the other methods (also in other clients) will require an instrument code, which can be found at the instrument's field `instrument`.

### Prices

To get the latest price for one or more instruments from Oanda, we can use the `price` or `prices` methods, which will emit the latest known price per instrument as instance of a `Price` and then finishes.

```scala
ratesClient.prices(List("EUR_USD", "EUR_GBP", "GBP_USD"))
// res3: akka.stream.scaladsl.Source[rx.oanda.rates.Price,akka.NotUsed] = akka.stream.scaladsl.Source@381c048d

ratesClient.price("EUR_USD")
// res4: akka.stream.scaladsl.Source[rx.oanda.rates.Price,akka.NotUsed] = akka.stream.scaladsl.Source@677e8851
```

Another option to receive prices for instruments is to stream them continuously, which is handy if we want to trade on a per tick base. 

```scala
ratesClient.livePrices(accountId, List("EUR_USD", "GBP_USD"))
// res5: akka.stream.scaladsl.Source[cats.data.Xor[rx.oanda.rates.Price,rx.oanda.utils.Heartbeat],akka.NotUsed] = akka.stream.scaladsl.Source@4e8d5396
```

As we can see, the resulting return type is `cats.data.Xor[Price, Heartbeat]`. The `Heartbeat`s are provided by Oanda to keep the connection alive. This is useful to monitor the connection and restart it, when nothing was received for 10 seconds or more (more about it in their documentation [here](http://developer.oanda.com/rest-live/streaming/#ratesStreaming) and [here](http://developer.oanda.com/rest-live/streaming/#connections)).


If you are only interested in the prices and want to ignore the heartbeats, here is what you wanna use


```scala
ratesClient.livePrices(accountId, List("EUR_USD", "GBP_USD")).flatMapConcat {
  case cats.data.Xor.Left(price) ⇒ Source.single(price)
  case cats.data.Xor.Right(heartbeat) ⇒ Source.empty
}
// res6: akka.stream.scaladsl.Source[rx.oanda.rates.Price,akka.NotUsed] = akka.stream.scaladsl.Source@bcc7a93
```

It is also possible to define a session id, since Oanda will disconnect all other streaming connections if 
there are more than two currently open. By providing a session id, only the other open connection with the same id will be closed. 

```scala
ratesClient.livePrices(accountId, List("EUR_USD", "GBP_USD"), sessionId = Some("session-42"))
// res7: akka.stream.scaladsl.Source[cats.data.Xor[rx.oanda.rates.Price,rx.oanda.utils.Heartbeat],akka.NotUsed] = akka.stream.scaladsl.Source@c76bbd1
```

The following is another example, how to stream prices for all instruments.

```scala
ratesClient.allInstruments(accountId).
    map(_.instrument).
    fold(Vector.empty[String])(_ :+ _).
    flatMapConcat(ratesClient.livePrices(accountId, _)).
    flatMapConcat {
      case Xor.Left(price) ⇒ Source.single(price)
      case Xor.Right(heartbeat) ⇒ Source.empty
    }
// res8: akka.stream.scaladsl.Source[rx.oanda.rates.Price,akka.NotUsed] = akka.stream.scaladsl.Source@417a4070
```

