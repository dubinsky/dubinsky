package org.podval.finance

import org.podval.finance.IncomeTax.BracketWithNext
import org.podval.finance.Util.{toInt, fromRate, toRate}

final class IncomeTax(
  income: Int,
  federalTaxDeductible: Int = 0,
  isSingle: Boolean = false
):
  require(federalTaxDeductible <= income)

  override def toString: String = s"net $$$net tax $$${toInt(amount)} rate $rate%"

  def net: Int = toInt(income - amount)

  def rate: Int = toRate(amount, income)

  def amount: Double = state + federal

  def state: Int = toInt(income * fromRate(IncomeTax.maStateFlatRate))

  private def federalIncome: Int = income - federalTaxDeductible - state

  def federalByBracket: String =
    val details = brackets
      .map(bracket => s"$$${toInt(amountInBracket(bracket))}($$${incomeInBracket(bracket)}@${bracket.bracket.rate}%)")
      .mkString("+")
    s"${toInt(federal)}@$federalRate%: $details"

  def federal: Double = brackets.map(amountInBracket).sum

  def federalRate: Int = toRate(federal, federalIncome)

  private def brackets: List[BracketWithNext] = IncomeTax
    .bracketsWithNext
    .takeWhile(bracket => threshhold(bracket.bracket) < federalIncome)

  private def threshhold(bracket: IncomeTax.Bracket) = if isSingle then bracket.single else bracket.jointly

  private def incomeInBracket(bracket: BracketWithNext) =
    Math.min(federalIncome, threshhold(bracket.next)) - threshhold(bracket.bracket)

  private def amountInBracket(bracket: BracketWithNext): Double =
    incomeInBracket(bracket) * fromRate(bracket.bracket.rate)

object IncomeTax:
  private val maStateFlatRate: Int = 5
  
  private final class Bracket(val rate: Int, val single: Int, val jointly: Int)

  private final class BracketWithNext(val bracket: Bracket, val next: Bracket)

  // https://www.irs.gov/newsroom/irs-releases-tax-inflation-adjustments-for-tax-year-2025
  private val brackets: List[Bracket] = List(
    Bracket(10,      0,      0),
    Bracket(12,  11925,  23850),
    Bracket(22,  48475,  96950),
    Bracket(24, 103350, 206700),
    Bracket(32, 197300, 394600),
    Bracket(35, 250525, 501050),
    Bracket(37, 626350, 751600), // Note: the only one where jointly != 2*single
    Bracket(0, Int.MaxValue, Int.MaxValue)
  )

  private val bracketsWithNext: List[BracketWithNext] = brackets
    .zip(brackets.tail)
    .map { case (bracket, next) => BracketWithNext(bracket, next) }
