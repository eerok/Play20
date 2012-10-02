package play.api.mvc

import org.specs2.mutable._
import play.api.mvc.Results._

object ResultsSpec extends Specification {

  "SimpleResult" should {

    "have status" in {
      val SimpleResult(ResponseHeader(status, _), _) = Ok("hello")
      status must be_==(200)
    }

    "support Content-Type overriding" in {
      val SimpleResult(ResponseHeader(_, headers), _) = Ok("hello").as("text/html")
      headers must havePair("Content-Type" -> "text/html")
    }

    "support headers manipulaton" in {
      val SimpleResult(ResponseHeader(_, headers), _) =
        Ok("hello").as("text/html").withHeaders("Set-Cookie" -> "yes", "X-YOP" -> "1", "X-YOP" -> "2")

      System.err.println("Headers:")
      System.err.println(headers)
      headers.size must be_==(3)
      headers must havePair("Content-Type" -> "text/html")
      headers must havePair("Set-Cookie" -> "yes")
      headers must havePair("X-YOP" -> "2")
    }

    "support cookies helper" in {
      val setCookieHeader = Cookies.encode(Cookie("session", "items"))

      val decodedCookie = Cookies.decode(setCookieHeader)
      decodedCookie.value must be_==("items")

      val res =
        Ok("hello").as("text/html")
          .withCookies(Cookie("session", "items"), Cookie("preferences", "blue"))
          .withCookies(Cookie("lang", "fr"), Cookie("session", "items2"))
          .discardingCookies("logged")

      val cookieHeaders = res.header.headers.filter(_._1 == "Set-Cookie")
      println(cookieHeaders)
      val setCookies = cookieHeaders.map(x => {
        val decoded = Cookies.decode(x._2)
        (decoded.name -> decoded)
      }).toMap
      setCookies.size must be_==(4)
      setCookies("session").value must be_==("items2")
      setCookies("preferences").value must be_==("blue")
      setCookies("lang").value must be_==("fr")
      setCookies("logged").maxAge must be_==(0)
    }

    "support session helper" in {

      Session.decode("  ").isEmpty must be_==(true)
      import java.io.File
      import play.api._
      import play.core._
      val cl = Thread.currentThread().getContextClassLoader()

      implicit val app: Application = new Application(new File("."), cl, None, Mode.Test) {
        override lazy val configuration = Configuration.from(Map("application.secret" -> "pass"))
      }
      Play.start(app)

      val data = Map("user" -> "kiki", "bad:key" -> "yop", "langs" -> "fr:en:de")
      val encodedSession = Session.encode(data)
      val decodedSession = Session.decode(encodedSession)

      decodedSession.size must be_==(2)
      decodedSession must havePair("user" -> "kiki")
      decodedSession must havePair("langs" -> "fr:en:de")
      val SimpleResult(ResponseHeader(_, headers), _) =
        Ok("hello").as("text/html")
          .withSession("user" -> "kiki", "langs" -> "fr:en:de")
          .withCookies(Cookie("session", "items"), Cookie("preferences", "blue"))
          .discardingCookies("logged")
          .withSession("user" -> "kiki", "langs" -> "fr:en:de")
          .withCookies(Cookie("lang", "fr"), Cookie("session", "items2"))

      val setCookies = headers.filter(_._1 == "Set-Cookie").map(_._2)
        .map(Cookies.decode(_))
        .map(x => x.name -> x).toMap
      setCookies.size must be_==(5)
      setCookies("session").value must be_==("items2")
      setCookies("preferences").value must be_==("blue")
      setCookies("lang").value must be_==("fr")
      setCookies("logged").maxAge must be_==(0)
      val playSession = Session.decodeFromCookie(setCookies.get(Session.COOKIE_NAME))
      playSession.data.size must be_==(2)
      playSession.data must havePair("user" -> "kiki")
      playSession.data must havePair("langs" -> "fr:en:de")
    }

  }
}