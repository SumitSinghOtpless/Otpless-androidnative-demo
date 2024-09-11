package com.flutter.app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.otpless.dto.HeadlessChannelType
import com.otpless.dto.HeadlessRequest
import com.otpless.dto.HeadlessResponse
import com.otpless.dto.Tuple
import com.otpless.main.OtplessManager
import com.otpless.main.OtplessView
import com.otpless.utils.Utility
import java.util.concurrent.Future


class MainActivity : AppCompatActivity() {
    var otplessView: OtplessView? = null

    private var inputEditText: EditText? = null
    private var otpEditText: EditText? = null

    private var channelType: HeadlessChannelType? = null
    private var headlessResponseTv: TextView? = null

    private var otpverify: Button? = null
    private var whatsappButton: Button? = null
    private var gmailButton: Button? = null
    private var twitterButton: Button? = null
    private var slackButton: Button? = null
    private var facebookButton: Button? = null
    private var linkedinButton: Button? = null
    private var microsoftButton: Button? = null
    private var disordButton: Button? = null
    private var githubButton: Button? = null
    private var twitchButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputEditText = findViewById(R.id.input_text_layout)
        otpEditText = findViewById(R.id.otp_et)
        otpverify = findViewById(R.id.otpverify)
        whatsappButton = findViewById(R.id.whatsapp_btn)
        gmailButton = findViewById(R.id.gmail_btn)
        twitterButton = findViewById(R.id.twitter_btn)
        slackButton = findViewById(R.id.slack_btn)
        facebookButton = findViewById(R.id.facebook_btn)
        linkedinButton = findViewById(R.id.linkedin_btn)
        microsoftButton = findViewById(R.id.microsoft_btn)
        disordButton = findViewById(R.id.discord_btn)
        githubButton = findViewById(R.id.github_btn)
        twitchButton = findViewById(R.id.twitch_btn)
        headlessResponseTv = findViewById(R.id.headless_response_tv)

        Utility.debugLogging = true
        // copy this code in onCreate of your Login Activity
        otplessView = OtplessManager.getInstance().getOtplessView(this)
        otplessView?.initHeadless("ALP5OU9SMLB3NSPYGNSG") //replace with your appid provided in documentation
        otplessView?.setHeadlessCallback { response: HeadlessResponse ->
            this.onHeadlessCallback(response)
        }
        otplessView?.enableOneTap(false)
        var appSignature: String? = null
        appSignature = Utility.getAppSignature(this);
        Log.d("onCreate", appSignature);
        otplessView?.onNewIntent(intent)
        isTruecallerInstalledAndStartHeadless()
        initTestingView()
    }

    private val headlessRequest: HeadlessRequest
        get() {
            val input = inputEditText!!.text.toString()
            val request = HeadlessRequest()
            channelType?.let {
                request.setChannelType(it)
            } ?: run {
                if (input.trim().isNotEmpty()) {
                    try {
                        // parse phone number
                        input.toLong()
                        request.setPhoneNumber("+91", input)
                    } catch (ex: Exception) {
                        request.setEmail(input)
                    }
                }
                val otp = otpEditText!!.text.toString()
                if (otp.trim().isNotEmpty()) {
                    request.setOtp(otp)
                }
            }
            return request
        }
    private fun openPhoneHint() {

        otplessView!!.phoneHintManager.showPhoneNumberHint(
            true
        ) { r: Tuple<String?, java.lang.Exception?> ->
            if (r.second != null) {
                var error = "Error in parsing"
                if (r.second!!.message != null) {
                    error = r.second!!.message.toString()
                }
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            } else {
                val phoneNumber = r.first
                val formattedPhoneNumber = phoneNumber?.let { normalizePhoneNumber(it) }
                inputEditText!!.setText(formattedPhoneNumber)
            }

        }
    }
    private fun isTruecallerInstalledAndStartHeadless() {
        val truecallerPackage = "com.truecaller"

        // Check if Truecaller is installed
        val isInstalled = try {
            packageManager.getPackageInfo(truecallerPackage, PackageManager.GET_ACTIVITIES)
            true // Truecaller is installed
        } catch (e: PackageManager.NameNotFoundException) {
            false // Truecaller is not installed
        }

        // If Truecaller is installed, execute the headless request
        if (isInstalled) {
            Log.d("TruecallerCheck", "Truecaller is installed, starting headless.")
            val headlessRequest = HeadlessRequest()
            val channelType = "TRUE_CALLER"
            headlessRequest.setChannelType(channelType)
            otplessView?.startHeadless(headlessRequest) { response: HeadlessResponse ->
                this.onHeadlessCallback(response)
            }
        } else {
            Log.d("TruecallerCheck", "Truecaller is not installed.")
        }
    }

    private fun normalizePhoneNumber(phoneNumber: String): String {
        // Remove non-numeric characters (like spaces, dashes, and parentheses)
        var cleanedNumber = phoneNumber.replace(Regex("\\D"), "") // \D matches any non-digit character

        // If the cleaned number has more than 10 digits, trim it to the last 10 digits
        if (cleanedNumber.length > 10) {
            cleanedNumber = cleanedNumber.takeLast(10)
        }

        // Ensure that only a 10-digit number is returned
        return if (cleanedNumber.length == 10) {
            cleanedNumber
        } else {
            "Invalid phone number"
        }
    }

    private fun initTestingView() {
        inputEditText?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && inputEditText?.text.isNullOrEmpty()) {
                Log.d("initTestingView", "inputEditText is empty and has focus")
                openPhoneHint()
            }
        }
        otpverify?.setOnClickListener {

            otplessView?.startHeadless(headlessRequest) { response: HeadlessResponse ->
                this.onHeadlessCallback(response)
            }
        }
        // This code will be used to detect the WhatsApp installed status on the user's device.
        if (Utility.isWhatsAppInstalled(this)) {
            whatsappButton?.visibility = View.VISIBLE
        } else {
            whatsappButton?.visibility = View.GONE
        }
        //end
        whatsappButton?.setOnClickListener {

            channelType = HeadlessChannelType.WHATSAPP
            otplessView?.startHeadless(headlessRequest) { response: HeadlessResponse ->
                this.onHeadlessCallback(response)
            }
        }

        gmailButton?.setOnClickListener {

            channelType = HeadlessChannelType.GMAIL
            otplessView?.startHeadless(headlessRequest) { response: HeadlessResponse ->
                this.onHeadlessCallback(response)
            }
        }

        twitterButton?.setOnClickListener {

            channelType = HeadlessChannelType.TWITTER
            otplessView?.startHeadless(headlessRequest) { response: HeadlessResponse ->
                this.onHeadlessCallback(response)
            }
        }

        slackButton?.setOnClickListener { _: View? ->

            channelType = HeadlessChannelType.SLACK
            otplessView?.startHeadless(headlessRequest) { response: HeadlessResponse ->
                this.onHeadlessCallback(response)
            }
        }

        facebookButton?.setOnClickListener {

            channelType = HeadlessChannelType.FACEBOOK
            otplessView?.startHeadless(headlessRequest) { response: HeadlessResponse ->
                this.onHeadlessCallback(response)
            }
        }

        linkedinButton?.setOnClickListener {

            channelType = HeadlessChannelType.LINKEDIN
            otplessView?.startHeadless(headlessRequest) { response: HeadlessResponse ->
                this.onHeadlessCallback(response)
            }
        }

        microsoftButton?.setOnClickListener {

            channelType = HeadlessChannelType.MICROSOFT
            otplessView?.startHeadless(headlessRequest) { response: HeadlessResponse ->
                this.onHeadlessCallback(response)
            }
        }

        disordButton?.setOnClickListener {

            channelType = HeadlessChannelType.DISCORD
            otplessView?.startHeadless(headlessRequest) { response: HeadlessResponse ->
                this.onHeadlessCallback(response)
            }
        }

        githubButton?.setOnClickListener {

            channelType = HeadlessChannelType.GITHUB
            otplessView?.startHeadless(headlessRequest) { response: HeadlessResponse ->
                this.onHeadlessCallback(response)
            }
        }

        twitchButton?.setOnClickListener {

            channelType = HeadlessChannelType.TWITCH
            otplessView?.startHeadless(headlessRequest) { response: HeadlessResponse ->
                this.onHeadlessCallback(response)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        otplessView?.onNewIntent(intent)
    }

    private fun onHeadlessCallback(response: HeadlessResponse) {
        if (response.getStatusCode() == 200) {
            when (response.getResponseType()) {
                "INITIATE" -> {
                    headlessResponseTv?.text = response.toString()
                }
                "VERIFY" -> {
                    headlessResponseTv?.text = response.toString()
                }
                "OTP_AUTO_READ" -> {
                    val otp = response.getResponse()?.optString("otp")
                    otpEditText?.setText(otp);
                    otplessView?.startHeadless(headlessRequest) { response: HeadlessResponse ->
                        this.onHeadlessCallback(response)
                    }
                }
                "ONETAP" -> {
                    headlessResponseTv?.text = response.toString()
                }
            }
            val successResponse = response.getResponse()
        } else {
            // handle error
            val error = response.getResponse()?.optString("errorMessage")
        }
    }



    override fun onBackPressed() {
        otplessView?.let {
            if (it.onBackPressed()) return
        }
        super.onBackPressed()
    }

}