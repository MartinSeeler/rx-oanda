package rx.oanda.account

import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.prop.PropertyChecks
import cats.data.Xor
import io.circe.DecodingFailure
import io.circe.parser._
import org.scalatest._

class AccountPropertiesDecoderSpec extends FlatSpec with Matchers {

  behavior of "The AccountProperties Decoder"

  it can "parse an AccountProperties from valid json" in {
    val json =
      """
        |{
        |  "id": "001-011-5838423-001",
        |  "tags": []
        |}
      """.stripMargin
    decode[AccountProperties](json) should matchPattern {
      case Xor.Right(AccountProperties("001-011-5838423-001", None, Nil)) => //
    }
  }

  it can "parse an AccountProperties with mt4 id from valid json" in {
    val json =
      """
        |{
        |  "id": "001-011-5838423-001",
        |  "mt4AccountID": 133742,
        |  "tags": ["foo", "bar"]
        |}
      """.stripMargin
    decode[AccountProperties](json) should matchPattern {
      case Xor.Right(AccountProperties("001-011-5838423-001", Some(133742), Seq("foo", "bar"))) => //
    }
  }

}
