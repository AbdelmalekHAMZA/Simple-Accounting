package com.emmanuelmess.simpleaccounting.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.SimpleAdapter

import com.emmanuelmess.simpleaccounting.R
import com.emmanuelmess.simpleaccounting.patreon.PatreonController
import kotlinx.android.synthetic.main.activity_donate.*

import java.util.ArrayList
import java.util.HashMap

class DonateActivity : AppCompatActivity() {

	companion object {
		private const val BITCOIN_DIRECTION = "1HFhPxH9bqMKvs44nHqXjEEPC2m7z1V8tW"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_donate)

		supportActionBar?.setDisplayHomeAsUpEnabled(true)
	}

	fun onClickDonate(view: View) {
		PatreonController.openPage(this)
	}

	fun onClickBitcoin(view: View) {
		try {
			with(Intent(Intent.ACTION_VIEW)) {
				data = Uri.parse("bitcoin:$BITCOIN_DIRECTION?amount=0.0005")
				startActivity(this@with)
			}
		} catch (e: ActivityNotFoundException) {
			Snackbar.make(view, R.string.no_bitcoin_app, Snackbar.LENGTH_LONG).show()
		}
	}

	fun onClickPaypal(view: View) {
		Intent(Intent.ACTION_VIEW).let {
			it.data = Uri.parse("https://www.paypal.com/invoice/p/#NNT9XYPXLB69BG3A")
			startActivity(it)
		}
	}

}