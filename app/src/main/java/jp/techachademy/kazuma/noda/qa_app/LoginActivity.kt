package jp.techachademy.kazuma.noda.qa_app

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCreateAccountListener: OnCompleteListener<AuthResult>
    private lateinit var mLoginListener: OnCompleteListener<AuthResult>
    private lateinit var mDataBaseReference: DatabaseReference

    private var mIsCreateAccount = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mDataBaseReference = FirebaseDatabase.getInstance().reference

        mAuth = FirebaseAuth.getInstance()

        mCreateAccountListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                val email = edit_text_email.text.toString()
                val password = edit_text_password.text.toString()
                login(email, password)
            } else {
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "failed to create an account", Snackbar.LENGTH_LONG).show()

                progressBar.visibility = View.GONE
            }
        }

        mLoginListener = OnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = mAuth.currentUser
                val userRef = mDataBaseReference.child(UserPATH).child(user!!.uid)

                if (mIsCreateAccount) {
                    val name = edit_text_nickname.text.toString()

                    val data = HashMap<String, String>()
                    data["name"] = name
                    userRef.setValue(data)

                    saveName(name)
                } else {
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val data = snapshot.value as Map<*, *>?
                            saveName(data!!["name"] as String)

                        }

                        override fun onCancelled(p0: DatabaseError) {

                        }

                    })

                }

                progressBar.visibility = View.GONE

                finish()
            } else {
                val view = findViewById<View>(android.R.id.content)
                Snackbar.make(view, "failed login", Snackbar.LENGTH_LONG).show()

                progressBar.visibility = View.GONE
            }


        }

        title = "login"

        button_create_account.setOnClickListener { v ->
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = edit_text_email.text.toString()
            val password = edit_text_password.text.toString()
            val name = edit_text_nickname.text.toString()

            if (email.length != 0 && password.length >=6 && name.length != 0) {
                mIsCreateAccount = true

                createAccount(email, password)
            } else {
                Snackbar.make(v, "please enter correctly", Snackbar.LENGTH_LONG).show()
            }
        }


        button_login.setOnClickListener { v ->
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val email = edit_text_email.text.toString()
            val password = edit_text_password.text.toString()

            if (email.length != 0 && password.length >= 6) {
                mIsCreateAccount = false

                login(email, password)
            } else {
                Snackbar.make(v, "please enter correctly", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        progressBar.visibility = View.VISIBLE

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener)
    }

    private fun login(email: String, password: String) {
        progressBar.visibility = View.VISIBLE

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener)
    }

    private fun saveName(name: String) {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        editor.putString(NameKEY, name)
        editor.commit()
    }
}
