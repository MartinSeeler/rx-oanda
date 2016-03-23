---
layout: page
title: Environments
---

```tut:invisible
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

implicit val system = ActorSystem("environments")
implicit val mat = ActorMaterializer()
import system.dispatcher
```

## Environments

Oanda provides two different API environments for you, depending on whether you want to use real or fake money.


No matter which one you choose, you need to specifiy the API token for your account, since all endpoints require authentication. 
To obtain your personal token, go to **My Account** -> **My Services** -> **Manage API Access**. 
From there, you can generate a personal access token to use with the Oanda API, as well as revoke a token you may currently have.
                               

Both environments can be found in the following package:

```tut:silent
import rx.oanda.OandaEnvironment._
```


The **fxTrade Practice** environment is used for practicing, as the name suggests. It's a fake money copy of the **fxTrade** environment, which means that the market data you receive is real, but the money is not. 


Use this environment to develop your trading robots / strategies / whatever, before you risk your real money. 

```tut
val practiceEnv = TradePracticeEnvironment("your-token")
```


When you're ready to risk your money and you feel comfortable enough, go ahead and use the **fxTrade** environment. But keep in mind: *you're trading with real money here!*

```tut
val tradeEnv = TradeEnvironment("your-token")
```

The clients will get the environments as arguments, so they know which endpoints to use. This makes it easy to switch between both environments or to get your strategy ready for real money trading.


```tut:invisible
import akka.http.scaladsl.Http
Http().shutdownAllConnectionPools.onComplete(_ => system.terminate())
```