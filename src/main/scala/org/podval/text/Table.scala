package org.podval.text

final class Table[T](
  nHeaders: Int,
  columns: Seq[Table.Column[T]],
  rows: Seq[T]
):
  def markdown: Seq[String] =
    Seq(
      toRow(_.title),
      toRow(_ => "---")
    ) ++
    0.until(nHeaders).map(nHeader => toRow(_.header(nHeader))) ++
    rows.map(row => toRow(_.value(row)))

  private def toRow(f: Table.Column[T] => Any): String = columns.map(f).mkString("| ", " | ", " |")

object Table:
  abstract class Column[T]:
    def title: Any
    def header(n: Int): Any
    def value(row: T): Any
