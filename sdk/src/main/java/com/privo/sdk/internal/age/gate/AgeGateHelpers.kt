package com.privo.sdk.internal.age.gate

import android.content.Context
import com.privo.sdk.extensions.isDeviceOnline
import com.privo.sdk.internal.AgeSettingsInternal
import com.privo.sdk.model.*
import java.text.SimpleDateFormat
import java.util.*

internal class AgeGateHelpers (val context: Context, val serviceSettings: AgeSettingsInternal) {

    private val AGE_FORMAT_YYYYMMDD = "yyyy-MM-dd"
    private val AGE_FORMAT_YYYYMM = "yyyy-MM"
    private val AGE_FORMAT_YYYY = "yyyy"


    internal fun toStatus(action: AgeGateAction?): AgeGateStatus {
        when (action) {
            AgeGateAction.Allow -> {
                return AgeGateStatus.Allowed
            }
            AgeGateAction.Block -> {
                return AgeGateStatus.Blocked
            }
            AgeGateAction.Consent -> {
                return AgeGateStatus.ConsentRequired
            }
            AgeGateAction.IdentityVerify -> {
                return AgeGateStatus.IdentityVerificationRequired
            }
            AgeGateAction.AgeVerify -> {
                return AgeGateStatus.AgeVerificationRequired
            }
            else -> {
                return AgeGateStatus.Undefined
            }
        }
    }


    internal fun getStatusTargetPage(status: AgeGateStatus?, recheckRequired: Boolean): String {
        if(recheckRequired) {
            return "recheck"
        }
        return when (status) {
            AgeGateStatus.Pending -> {
                "verification-pending"
            }
            AgeGateStatus.Blocked -> {
                "access-restricted"
            }
            AgeGateStatus.ConsentRequired -> {
                "request-consent"
            }
            AgeGateStatus.AgeVerificationRequired -> {
                "request-age-verification"
            }
            AgeGateStatus.IdentityVerificationRequired -> {
                "request-verification"
            } else -> {
                "dob"
            }
        }
    };

    internal fun getDateAndFormat(data: CheckAgeData): Pair<String?,String?> {
        return when {
            data.birthDateYYYYMMDD != null -> Pair(data.birthDateYYYYMMDD, AGE_FORMAT_YYYYMMDD)
            data.birthDateYYYYMM != null -> Pair(data.birthDateYYYYMM, AGE_FORMAT_YYYYMM)
            data.birthDateYYYY != null -> Pair(data.birthDateYYYY, AGE_FORMAT_YYYY)
            else -> Pair(null,null)
        }
    }

    internal fun isAgeCorrect(rawDate: String, format: String): Boolean {
        val formatter = SimpleDateFormat(format, Locale.US)
        val calendar = Calendar.getInstance()


        val date = formatter.parse(rawDate)

        if (date != null) {
            val currentYear = calendar.get(Calendar.YEAR);
            calendar.time = date
            val birthYear = calendar.get(Calendar.YEAR)
            val age = currentYear - birthYear;
            return age in 1..120;
        }
        return false
    }

    @Throws(NoInternetConnectionException::class)
    internal fun checkNetwork() {
        if (!context.isDeviceOnline()) {
            throw NoInternetConnectionException()
        }
    }

    @Throws(
        NotAllowedEmptyStringUserIdentifierException::class,
        NotAllowedEmptyStringNicknameException::class,
        NotAllowedEmptyStringAgIdException::class,
        NotAllowedMultiUserUsageException::class
    )
    internal fun checkUserData(userIdentifier: String?, nickname: String?, agId: String? = null) {
        userIdentifier?.let { ui ->
            if (ui.isEmpty()) {
                throw NotAllowedEmptyStringUserIdentifierException()
            }
        }
        nickname?.let { n ->
            if (n.isEmpty()) {
                throw NotAllowedEmptyStringNicknameException()
            }
            serviceSettings.getSettings { settings ->
                if(!settings.isMultiUserOn) {
                    throw NotAllowedMultiUserUsageException()
                }
            }
        }
        agId?.let { ai ->
            if (ai.isEmpty()) {
                throw NotAllowedEmptyStringAgIdException()
            }
        }
    }

    @Throws(
        IncorrectDateOfBirthException::class,
        NoInternetConnectionException::class,
        NotAllowedEmptyStringUserIdentifierException::class,
        NotAllowedEmptyStringNicknameException::class,
        NotAllowedEmptyStringAgIdException::class,
        NotAllowedMultiUserUsageException::class
    )
    internal fun checkRequest(data: CheckAgeData) {
        checkNetwork()
        checkUserData(data.userIdentifier, data.nickname)
        val (date,format) = getDateAndFormat(data)
        if (date != null && format != null) {
            if (!isAgeCorrect(date, format)) {
                throw throw IncorrectDateOfBirthException()
            }
        }
    }

}