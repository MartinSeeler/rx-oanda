---
layout: page
title: Instruments
---

```tut:invisible
import cats.data.Xor
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, Sink}
import com.typesafe.config.ConfigFactory
import rx.oanda.OandaEnvironment._

implicit val system = ActorSystem("instruments-tut", ConfigFactory.parseString("akka.loglevel = ERROR"))
implicit val mat = ActorMaterializer()
import system.dispatcher
val practiceEnv = TradePracticeEnvironment("your-token")
val accountId = 133742L
```

## Instruments

To get access to tradable instruments  