package org.podval.text

import java.io.{BufferedWriter, File, FileWriter}
import scala.io.Source

object Files:
  def toString(strings: Seq[String]): String = strings.mkString("", "\n", "\n")

  def read(file: File): Seq[String] =
    val source = Source.fromFile(file)
    // `toList` materializes the iterator before closing the source
    val result = source.getLines().toList
    source.close
    result

  def write(file: File, content: Seq[String]): Unit =
    val writer: BufferedWriter = BufferedWriter(new FileWriter(file))
    try writer.write(toString(content)) finally writer.close()

  // [//]: # (This may be the most platform independent comment)

  def spliceMarkdown(
    in: Seq[String],
    boundary: String,
    patch: Seq[String]
  ): Seq[String] =
    val markdownBoundary = s"[//]: # ($boundary)"
    splice(
      in,
      markdownBoundary,
      markdownBoundary,
      patch
    )

  private def splice(
    in: Seq[String],
    start: String,
    end: String,
    patch: Seq[String]
  ): Seq[String] =
    in.takeWhile(_ != start) ++
    Seq(start) ++
    Seq("") ++ // empty line after comment
    patch ++
    Seq("") ++ // empty line before comment
    in.dropWhile(_ != start).tail.dropWhile(_ != end)
