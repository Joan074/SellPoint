package org.joan.project.db.seguridad

import org.mindrot.jbcrypt.BCrypt

class Password {
    companion object {
        @JvmStatic
        fun hash(password: String): String {
            return BCrypt.hashpw(password, BCrypt.gensalt())
        }

        @JvmStatic
        fun verify(password: String, hashedPassword: String): Boolean {
            return BCrypt.checkpw(password, hashedPassword)
        }
    }
}
