---
layout: page
title: Accounts
---

```tut:invisible
import cats.data.Xor
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, Sink}
import com.typesafe.config.ConfigFactory
import rx.oanda.OandaEnvironment._

implicit val system = ActorSystem("accounts-tut", ConfigFactory.parseString("akka.loglevel = ERROR"))
implicit val mat = ActorMaterializer()
import system.dispatcher
val practiceEnv = TradePracticeEnvironment("your-token")
val accountId = 133742L
```

## Account

To retrieve informations about our Oanda accounts, we can use the `AccountClient`.

```tut:book
import rx.oanda.accounts._

val accountClient = new AccountClient(practiceEnv)
```

We can now either request all of our accounts or just a specific one.

```tut:book
accountClient.accountById(accountId)
accountClient.allAccounts()
```

As we can see, `allAccounts` will only retrieve instances of `ShortAccount` per account available. They only contain the 
most basic infos like the account id, the account name and the currency used, as well as the margin rate. When we request 
a specific account by it's id, we get an instance of `Account` instead, which gives us access to much more informations, 
e.g. the number of open trades / orders, the available and used margin and of course the account balance, 
together with the realized and unrealized profit / loss.
  
To retrieve all infos about all of our accounts, you can do something like the following.
  
```tut:book
accountClient.
  allAccounts().
  flatMapConcat(a => accountClient.accountById(a.accountId))
```

```tut:invisible
import akka.http.scaladsl.Http
Http().shutdownAllConnectionPools.onComplete(_ => system.terminate())
```
