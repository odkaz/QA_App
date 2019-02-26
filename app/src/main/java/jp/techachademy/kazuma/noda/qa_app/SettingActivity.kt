package jp.techachademy.kazuma.noda.qa_app

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    private lateinit var mDatabaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)



        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sp.edit()
        val name = sp.getString(NameKEY, "")
        nameText.setText(name)

        mDatabaseReference = FirebaseDatabase.getInstance().reference


        changeButton.setOnClickListener { v ->
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            val user = FirebaseAuth.getInstance().currentUser


            if (user == null) {
                Snackbar.make(v, "please login", Snackbar.LENGTH_LONG).show()

            } else {
                val setName = nameText.text.toString()
                val userRef = mDatabaseReference.child(UserPATH).child(user!!.uid)
                val data = HashMap<String, String>()
                data["name"] = name
                userRef.setValue(data)

                editor.putString(NameKEY, setName)
                editor.commit()

                Snackbar.make(v, "applied change", Snackbar.LENGTH_LONG).show()
            }
        }

        logoutButton.setOnClickListener { v ->
           FirebaseAuth.getInstance().signOut()
            nameText.setText("")
            Snackbar.make(v, "logout successful", Snackbar.LENGTH_LONG).show()
        }
    }
}
