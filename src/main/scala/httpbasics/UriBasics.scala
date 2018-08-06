package httpbasics

import akka.http.scaladsl.model._

object UriBasics {

  def test1(): Unit = {
    val uri1 = Uri("http://localhost")
    println("\t" + uri1)

    val uri2 = Uri("ftp://ftp.is.co.za/rfc/rfc1808.txt")
    val uri3 = Uri.from(scheme = "ftp", host = "ftp.is.co.za", path = "/rfc/rfc1808.txt")
    println("\turi2==uri3: " + (uri2 == uri3))

    val uri4 = Uri.from(scheme = "http", host = "localhost", path = "/cellosaps/requestSR", queryString = Some("type=CreateSession"))
    println("\tquery-string: " + uri4.query())

    val uri5 = Uri.from(scheme = "mailto", path = "ali@cellossoftware.com")
    println("\t" + uri5)

  }

  def test2(): Unit = {
    println(Uri.Query(("type", "gtpc createSession")))

  }

  def main(args: Array[String]): Unit = {
    println("=============== UriBasics starts ================")

    test2()
  }
}