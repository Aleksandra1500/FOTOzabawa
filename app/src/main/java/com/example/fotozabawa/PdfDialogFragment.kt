package com.example.fotozabawa

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment


class PdfDialogFragment() : DialogFragment() {

    var url: String = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.dialog_pdf_message)
                .setTitle(R.string.dialog_pdf_title)
                .setPositiveButton(R.string.yes,
                    DialogInterface.OnClickListener { dialog, id ->

                        if (!url.startsWith("http://") && !url.startsWith("https://"))
                            url = "http://" + url;

                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(browserIntent)
                    })
                .setNeutralButton(R.string.no,
                    DialogInterface.OnClickListener { dialog, id ->

                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}