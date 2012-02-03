package unfiltered.netty.request
import org.specs._

object MixedPlanSpec extends Specification
  with unfiltered.spec.netty.Served {

  import unfiltered.request._
  import unfiltered.request.{Path => UFPath, _}
  import unfiltered.response._
  import unfiltered.netty._

  import dispatch._

  import dispatch.mime.Mime._
  import java.io.{File => JFile,FileInputStream => FIS}
  import org.apache.commons.io.{IOUtils => IOU}

  val html = <html>
        <head><title>unfiltered file netty uploads test</title></head>
        <body>
          <p>hello</p>
        </body>
      </html>

  def setup = {
    _.handler(cycle.MultiPartDecoder{
      case POST(UFPath("/cycle/disk")) & MultiPart(req) =>
        val disk = MultiPartParams.Disk(req)
        (disk.files("f"), disk.params("p")) match {
          case (Seq(f, _*), p) =>
            ResponseString(
              "cycle disk read file f named %s with content type %s and param p %s" format(
                f.name, f.contentType, p))
            case _ =>  ResponseString("what's f?")
          }
      case POST(UFPath("/cycle/stream") & MultiPart(req)) =>
        val stream = MultiPartParams.Streamed(req)
        (stream.files("f"), stream.params("p")) match {
          case (Seq(f, _*), p) => ResponseString(
            "cycle stream read file f is named %s with content type %s and param p %s" format(
              f.name, f.contentType, p))
          case _ =>  ResponseString("what's f?")
        }
      case r@POST(UFPath("/cycle/mem") & MultiPart(req)) =>
        val mem =  MultiPartParams.Memory(req)
        (mem.files("f"), mem.params("p")) match {
          case (Seq(f, _*), p) => ResponseString(
            "cycle memory read file f is named %s with content type %s and param p %s" format(
              f.name, f.contentType, p))
          case _ =>  ResponseString("what's f?")
        }
    }).handler(async.MultiPartDecoder{
      case r @ POST(UFPath("/async/disk")) & MultiPart(req) =>
        val disk = MultiPartParams.Disk(req)
        (disk.files("f"), disk.params("p")) match {
          case (Seq(f, _*), p) =>
            r.respond(ResponseString(
              "async disk read file f named %s with content type %s and param p" format(
                f.name, f.contentType, p)))
            case _ =>  r.respond(ResponseString("what's f?"))
          }
      case r @ POST(UFPath("/async/stream") & MultiPart(req)) =>
        val stream = MultiPartParams.Streamed(req)
        (stream.files("f"), stream.params("p")) match {
          case (Seq(f, _*), p) => r.respond(ResponseString(
            "async stream read file f is named %s with content type %s and param p %s" format(
              f.name, f.contentType, p)))
          case _ =>  r.respond(ResponseString("what's f?"))
        }
      case r @ POST(UFPath("/async/mem") & MultiPart(req)) =>
        val mem = MultiPartParams.Memory(req)
        (mem.files("f"), mem.params("p")) match {
          case (Seq(f, _*), p) => r.respond(ResponseString(
            "async memory read file f is named %s with content type %s and param p %s" format(
              f.name, f.contentType, p)))
          case _ => r.respond(ResponseString("what's f?"))
        }
    }).handler(cycle.Planify{
      case UFPath("/") => Html(html)
    })
  }

  "Netty MultiPartParams" should {
    shareVariables()
    doBefore {
      val out = new JFile("netty-upload-test-out.txt")
      if(out.exists) out.delete
    }
    /*
    "pass GET request downstream" in {
      http(host as_str) must_== html.toString
    }*/
    /*
    "handle cycle file uploads to disk" in {
      val file = new JFile(getClass.getResource("/netty-upload-big-text-test.txt").toURI)
      file.exists must_==true
      http(host / "cycle" / "disk" <<* ("f", file, "text/plain") as_str) must_=="cycle disk read file f named netty-upload-big-text-test.txt with content type text/plain and param p List()"
    }*/
    "handle async file uploads to disk" in {
      val file = new JFile(getClass.getResource("/netty-upload-big-text-test.txt").toURI)
      file.exists must_==true
      http(host / "async" / "disk" <<* ("f", file, "text/plain") as_str) must_=="async disk read file f named netty-upload-big-text-test.txt with content type text/plain and param p"
    }
  }
}
