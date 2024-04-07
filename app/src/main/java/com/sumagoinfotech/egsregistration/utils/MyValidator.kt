package com.sumagoinfotech.egsregistration.utils


import java.util.regex.Pattern

object MyValidator {

    public fun isValidName(name:String):Boolean{
        var result = name!==null && name.isNotEmpty() && name.isNotBlank() && name.length>4
        return result
    }
    public fun isValidEmailX(email:String):Boolean{
        var result = email!==null && email.isNotEmpty() && email.isNotBlank() && email.length==10
        return result
    }
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        val pattern = Pattern.compile(android.util.Patterns.EMAIL_ADDRESS.toString())
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }
    public fun isValidMobileNumber(mobileNumber: String): Boolean {
        val regex = "^[6-9]\\d{9}$"
        return mobileNumber.matches(Regex(regex))
    }

    fun isValidPassword(password: String): Boolean {
        var result = password!==null && password.isNotEmpty() && password.isNotBlank() && password.length>=8
        return result
    }

}