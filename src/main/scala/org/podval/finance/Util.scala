package org.podval.finance

object Util:
  def fromRate(rate: Int): Double = rate.toDouble / 100.0

  def toRate(part: Double, whole: Double): Int = toInt(part / whole * 100.0)

  def toInt(amount: Double): Int = amount.round.toInt
  
  def compound(growth: Double, years: Int): Double =
    require(years >= 0)
    Math.pow(1.0 + growth, years)
  
  def compound(growth: Double, years: Int, contribution: Double): Double =
    require(years >= 0)
    1.to(years).map(year => contribution * compound(growth, years - year)).sum

  def round(value: Double): String = BigDecimal.valueOf(value).setScale(2, BigDecimal.RoundingMode.HALF_UP).toString
  