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
import androidx.lifecycle.lifecycleScope
import com.otpless.dto.Tuple
import com.otpless.main.OtplessView
import com.otpless.utils.Utility
import com.otpless.v2.android.sdk.dto.OtplessChannelType
import com.otpless.v2.android.sdk.dto.OtplessRequest
import com.otpless.v2.android.sdk.dto.OtplessResponse
import com.otpless.v2.android.sdk.dto.ResponseTypes
import com.otpless.v2.android.sdk.main.OtplessSDK
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    var otplessView: OtplessView? = null

    private var inputEditText: EditText? = null
    private var otpEditText: EditText? = null

    private var channelTypes: OtplessChannelType? = null
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
        OtplessSDK.initialize(appId = "YOUR_APPID_UPPERCASE", activity = this) //replace with your appid provided in dashboard
        OtplessSDK.setResponseCallback(this::onOtplessResponse)
        var appSignature: String? = null
        appSignature = Utility.getAppSignature(this);
        Log.d("onCreate", appSignature);
        isTruecallerInstalledAndStartHeadless()
        initTestingView()
    }

    private val otplessRequest: OtplessRequest
        get() {
            val input = inputEditText?.text.toString()
            val request = OtplessRequest()

            channelTypes?.let {
                request.setChannelType(it)
            } ?: run {
                if (input.trim().isNotEmpty()) {
                    try {
                        // parse phone number
                        input.toLong()
                        request.setDeliveryChannel("WHATSAPP")
                        // Note: Order of parameters changed in new SDK
                        request.setPhoneNumber(input, "+91")
                    } catch (ex: Exception) {
                        request.setEmail(input)
                    }
                }

                val otp = otpEditText?.text.toString()
                if (otp.trim().isNotEmpty()) {
                    request.setOtp(otp)
                }
                Log.d("Otpless Request", "Input: $input, OTP: $otp")
            }
            return request
        }

    private fun openPhoneHint() {

        otplessView?.phoneHintManager?.showPhoneNumberHint(
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
            val request = OtplessRequest()
            request.setChannelType(OtplessChannelType.TRUECALLER)
            lifecycleScope.launch {
                OtplessSDK.start(request, ::onOtplessResponse)
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
            lifecycleScope.launch {
                OtplessSDK.start(otplessRequest, ::onOtplessResponse)
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
            val request = OtplessRequest()
            request.setChannelType(OtplessChannelType.WHATSAPP)
            lifecycleScope.launch {
                OtplessSDK.start(request, ::onOtplessResponse)
            }
        }

        gmailButton?.setOnClickListener {
            val request = OtplessRequest()
            request.setChannelType(OtplessChannelType.GMAIL)
            lifecycleScope.launch {
                OtplessSDK.start(request, ::onOtplessResponse)
            }
        }

        twitterButton?.setOnClickListener {
            val request = OtplessRequest()
            request.setChannelType(OtplessChannelType.TWITTER)
            lifecycleScope.launch {
                OtplessSDK.start(request, ::onOtplessResponse)
            }
        }

        slackButton?.setOnClickListener { _: View? ->
            val request = OtplessRequest()
            request.setChannelType(OtplessChannelType.SLACK)
            lifecycleScope.launch {
                OtplessSDK.start(request, ::onOtplessResponse)
            }
        }

        facebookButton?.setOnClickListener {
            val request = OtplessRequest()
            request.setChannelType(OtplessChannelType.FACEBOOK)
            lifecycleScope.launch {
                OtplessSDK.start(request, ::onOtplessResponse)
            }
        }

        linkedinButton?.setOnClickListener {
            val request = OtplessRequest()
            request.setChannelType(OtplessChannelType.LINKEDIN)
            lifecycleScope.launch {
                OtplessSDK.start(request, ::onOtplessResponse)
            }
        }

        microsoftButton?.setOnClickListener {
            val request = OtplessRequest()
            request.setChannelType(OtplessChannelType.MICROSOFT)
            lifecycleScope.launch {
                OtplessSDK.start(request, ::onOtplessResponse)
            }
        }

        disordButton?.setOnClickListener {
            val request = OtplessRequest()
            request.setChannelType(OtplessChannelType.DISCORD)
            lifecycleScope.launch {
                OtplessSDK.start(request, ::onOtplessResponse)
            }
        }

        githubButton?.setOnClickListener {
            val request = OtplessRequest()
            request.setChannelType(OtplessChannelType.GITHUB)
            lifecycleScope.launch {
                OtplessSDK.start(request, ::onOtplessResponse)
            }
        }

        twitchButton?.setOnClickListener {
            val request = OtplessRequest()
            request.setChannelType(OtplessChannelType.TWITCH)
            lifecycleScope.launch {
                OtplessSDK.start(request, ::onOtplessResponse)
            }
        }
    }


    private fun onOtplessResponse(response: OtplessResponse) {
        OtplessSDK.commit(response)
        when (response.responseType) {
            ResponseTypes.INITIATE -> {
                // notify that authentication has been initiated
                headlessResponseTv?.text = response.toString()
            }
            ResponseTypes.VERIFY -> {
                // notify that verification has failed.
                headlessResponseTv?.text = response.toString()
            }
            ResponseTypes.INTERNET_ERR -> {
                // notify that the request could not be processed because of poor/no internet connection
                headlessResponseTv?.text = response.toString()
            }
            ResponseTypes.ONETAP -> {
                // final response with token
                val token = response.response?.optJSONObject("data")?.optString("token")
                if (!token.isNullOrBlank()) {
                    // Process token and proceed.
                    headlessResponseTv?.text = response.toString()
                }
            }
            ResponseTypes.OTP_AUTO_READ -> {
                val otp = response.response?.optString("otp")
                if (!otp.isNullOrBlank()) {
                    // Autofill the OTP in your TextField/EditText
                    headlessResponseTv?.text = response.toString()
                }
            }
            ResponseTypes.FALLBACK_TRIGGERED -> {
                // In case of Smart Auth when channel fallback triggered
                headlessResponseTv?.text = response.toString()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        lifecycleScope.launch {
            OtplessSDK.onNewIntent(intent)
        }
    }
}