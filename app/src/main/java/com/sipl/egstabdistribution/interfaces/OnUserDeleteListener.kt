package com.sipl.egstabdistribution.interfaces

import com.sipl.egstabdistribution.database.model.UsersWithAreaNames

interface OnUserDeleteListener {
    fun onUserDelete(user: UsersWithAreaNames)
}