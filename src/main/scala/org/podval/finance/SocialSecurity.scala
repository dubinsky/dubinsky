package org.podval.finance

import org.podval.text.{Files, Table}
import java.io.File

final class SocialSecurity(ageClaimed: Int):
  require(SocialSecurity.early <= ageClaimed && ageClaimed <= SocialSecurity.late)

  override def toString: String = s"$ageClaimed: ${Util.toInt(portion*100)}%"

  private def portion: Double = 1.0 + SocialSecurity.adjustment(ageClaimed)

  private def totalPortion(age: Int, growth: Double): Double = Util.compound(growth/100.0, age - ageClaimed, portion)

// https://www.congress.gov/crs-product/IF11747
// Adjustments for Claiming Age
object SocialSecurity:
  // The earliest eligibility age is the age at which a retired worker can first claim benefits (age 62).
  private val early: Int = 62
  // The full retirement age (FRA) is the age at which the worker can receive the full PIA increased by any COLAs.
  // For workers born in 1960 or later, the FRA is age 67.
  private val full: Int = 67
  // The permanent reduction in monthly benefits that applies to people who claim before the FRA
  // is an actuarial reduction.
  // It equals five-ninths of 1% for each month (6⅔% per year) for the first three years of early claim
  private val firstYears: Int = 3
  private val first: Double = -(0.06 + 0.02/3.0)
  // and five-twelfths of 1% for each month (5% per year) beyond 36 months.
  private val after: Double = -0.05

  // The permanent increase in monthly benefits that applies to those who claim after the FRA
  // is called the delayed retirement credit (DRC).
  // For people born after 1942, the DRC is 8% for each year of delayed claim after the FRA up to age 70.
  private val late: Int = 70
  private val dcr: Double = 0.08

  private def adjustment(ageClaimed: Int): Double = if ageClaimed >= full then (ageClaimed - full) * dcr else
    val yearsEarly: Int = full - ageClaimed
    val firstAdjustment: Double = Math.min(yearsEarly, firstYears) * first
    val afterAdjustment = Math.max(yearsEarly - firstYears, 0) * after
    firstAdjustment + afterAdjustment

  def main(args: Array[String]): Unit =
    val file = File("./notes/Social Security.md")
    Files.write(file, Files.spliceMarkdown(Files.read(file), "breakeven table", breakEvenTable.markdown))

  private val old: Int = 95

  private def breakEvenTable: Table[Int] =
    final class GrowthColumn extends Table.Column[Int]:
      override def title: Any = "age claimed:"
      override def header(n: Int): Any = n match
        case 0 => "portion of the benefits when claiming at 67:<br>growth"
      override def value(growth: Int): Any = s"$growth%"

    final class AgeColumn(ageClaimed: Int) extends Table.Column[Int]:
      override def title: Any = ageClaimed
      override def header(n: Int): Any = n match
        case 0 => s"${Util.toRate(SocialSecurity(ageClaimed).portion, 1.0)}%"
      override def value(growth: Int): Any =
        val v: String = breakEven(early, ageClaimed, growth).getOrElse("x").toString
        val z: String = total(old, ageClaimed, growth)
        s"$v<br>$z"

    Table(
      nHeaders = 1,
      GrowthColumn() +: early.to(late).map(AgeColumn(_)),
      0.to(9)
    )

  private def breakEven(ageClaimed1: Int, ageClaimed2: Int, growth: Double): Option[Int] =
    require(ageClaimed1 <= ageClaimed2)
    ageClaimed2.to(120).find(age =>
      def totalPortion1 = SocialSecurity(ageClaimed1).totalPortion(age, growth)
      def totalPortion2 = SocialSecurity(ageClaimed2).totalPortion(age, growth)
      totalPortion1 == totalPortion2 || totalPortion1 / totalPortion2 <= 1.0
    )

  private def total(
    age: Int,
    ageClaimed: Int,
    growth: Double
  ) = s"${Util.toRate(SocialSecurity(ageClaimed).totalPortion(age, growth), SocialSecurity(late).totalPortion(age, growth))}%"
