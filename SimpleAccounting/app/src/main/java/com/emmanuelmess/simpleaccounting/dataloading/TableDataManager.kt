package com.emmanuelmess.simpleaccounting.dataloading

import java.math.BigDecimal
import java.util.ArrayList

class TableDataManager {

	private val data = ArrayList<RowDataHandler>()

	val startingTotal: BigDecimal
		get() = data[0].total

	init {
		data.add(FirstRowDataHandler())
	}

	fun addRow() {
		data.add(RowDataHandler(data[data.size - 1].total))
	}

	fun updateCredit(i: Int, credit: BigDecimal) {
		data[i].updateCredit(credit)
		recalculate(i)
	}

	fun updateDebit(i: Int, debit: BigDecimal) {
		data[i].updateDebit(debit)
		recalculate(i)
	}

	fun updateStartingTotal(statingTotal: BigDecimal) {
		(data[0] as FirstRowDataHandler).updateTotal(statingTotal)
	}

	private fun recalculate(i: Int) {
		for (j in i + 1 until data.size) {
			data[j].updateLast(data[j - 1].total)
		}
	}

	fun getTotal(i: Int): BigDecimal {
		if (i == 0) throw IllegalArgumentException("Did you mean getStartingTotal()?")
		return data[i].total
	}

	fun clear() {
		data.clear()
		data.add(FirstRowDataHandler())
	}
}

private open class RowDataHandler constructor(private var last: BigDecimal) {
	private var credit = BigDecimal.ZERO
	private var debit = BigDecimal.ZERO
	open var total: BigDecimal = last

	fun updateLast(last: BigDecimal) {
		this.last = last
		recalculate()
	}

	fun updateCredit(credit: BigDecimal) {
		this.credit = credit
		recalculate()
	}

	fun updateDebit(debit: BigDecimal) {
		this.debit = debit
		recalculate()
	}

	private fun recalculate() {
		total = last.add(credit.subtract(debit))
		if (total.compareTo(BigDecimal.ZERO) == 0) {
			total = total.setScale(1, BigDecimal.ROUND_UNNECESSARY)
		}
	}
}

private class FirstRowDataHandler : RowDataHandler(BigDecimal.ZERO) {
	override var total: BigDecimal = BigDecimal.ZERO
		set(value) {
			super.total = value
		}

	fun updateTotal(total: BigDecimal) {
		this.total = total
	}
}